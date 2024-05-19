package com.als.webIde.controller;

import com.als.webIde.DTO.etc.CustomException;
import com.als.webIde.DTO.etc.CustomUserDetails;
import com.als.webIde.DTO.etc.ErrorCode;
import com.als.webIde.DTO.etc.TokenDto;
import com.als.webIde.DTO.request.UserId;
import com.als.webIde.DTO.request.UserInfo;
import com.als.webIde.DTO.request.UserLogin;
import com.als.webIde.DTO.request.UserNickName;
import com.als.webIde.DTO.response.Message;
import com.als.webIde.DTO.response.ResponseUserInfo;
import com.als.webIde.domain.entity.Member;
import com.als.webIde.domain.entity.MemberSetting;
import com.als.webIde.domain.entity.MemberSettingId;
import com.als.webIde.domain.repository.MemberRepository;
import com.als.webIde.domain.repository.MemberSettingRepository;
import com.als.webIde.service.DockerService;
import com.als.webIde.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.als.webIde.DTO.etc.CustomErrorCode.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberSettingRepository memberSettingRepository;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final DockerService dockerService;

    // 아이디 중복 확인
    @PostMapping("/idcheck")
    public ResponseEntity<Message> checkedUserId(@RequestBody UserId userId){
        Message message = userService.checkedUserId(userId);
        return ResponseEntity.ok(message);
    }

    // 닉네임 중복 확인
    @PostMapping("/nicknamecheck")
    public ResponseEntity<Message> checkedUserNickName(@RequestBody UserNickName nickName){
        Message message = userService.checkedUserNickName(nickName);
        return ResponseEntity.ok(message);
    }

    // 회원가입
    @Transactional
    @PostMapping("/signup")
    public ResponseEntity<Message> signUp(@RequestBody UserInfo userInfo){
        Message message = userService.signUp(userInfo);
        return ResponseEntity.ok(message);
    }

    // 토큰 재발급
    @PostMapping("/accessToken")
    public ResponseEntity<Message> reissueAccessToken(HttpServletRequest request){

        String refreshToken = request.getHeader("X-Refresh-Token");
        String reissueAccessToken = userService.reissueAccessToken(refreshToken);

        HttpHeaders responseHeader = new HttpHeaders();
        responseHeader.add("Authorization",reissueAccessToken);
        return ResponseEntity.ok().headers(responseHeader).body(Message.builder().message("accessToken이 발급이 되었습니다.").build());

    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Message> login(@RequestBody UserLogin userLogin) {

        TokenDto token = new TokenDto("helo","jhele","df");
        Member member = memberRepository.findMemberByUserId(userLogin.getUserId())
                .orElseThrow(()-> new CustomException(ERROR_USER));


        if (!passwordEncoder.matches(userLogin.getPassword(),member.getPassword())){
            return ResponseEntity.ok(Message.builder().message("비밀번호가 틀렸습니다").build());
        }else {
            log.info("여기까지 왔다요");
        }

        token = userService.login(member.getUserPk(),userLogin.getPassword());

        log.info("여기까지 왔다요");
        try {
            dockerService.findContainerByUserPk(String.valueOf(member.getUserPk()));
        } catch (Exception e) {
            log.info("도커 컨테이너 DB 혹은 실제 구동중인 뭔가 없었음");
            dockerService.createAndStartContainer(String.valueOf(member.getUserPk()));
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization","Bearer "+token.getAccessToken());
        httpHeaders.add("X-Refresh-Token","Bearer "+token.getRefreshToken());

        return ResponseEntity.ok().headers(httpHeaders).body(Message.builder().message("로그인 성공").build());
    }


    /**
     * 사용자의 마이페이지의 정보를 출력하는 페이지
     * @return
     */
    @GetMapping("/mypage")
    public ResponseEntity<ResponseUserInfo> userInfo(){
        ResponseUserInfo userInfo = userService.userInfo();
        return ResponseEntity.ok().body(userInfo);
    }

    // 닉네임 중복확인 -> 닉네임의 중복이 아니면 바로 저장할 수 있는 기능 그래서 PUT Method 사용
    @PutMapping("/nicknamecheck")
    public ResponseEntity<String> changeNickname(@RequestBody UserNickName nickName){
        // 먼저 이이디를 찾아야 함

        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        memberSettingRepository.findMemberSettingByNickname(nickName.getUserNickName()).orElseThrow(() -> new CustomException(INVALID_NICKNAME));

        MemberSetting memberSetting = memberSettingRepository.findById(MemberSettingId.builder().userPk(details.getId()).build()).orElseThrow(() -> new CustomException(ERROR_USER));

        MemberSetting member = MemberSetting.builder().MemberId(new MemberSettingId(details.getId()))
                .nickname(nickName.getUserNickName()).theme(details.getUserThema()).member(memberSetting.getMember()).build();

        memberSettingRepository.save(member);

        return ResponseEntity.ok("ok");
    }

    // 패스워드 체크하는 부분
    @PostMapping("/passwordcheck")
    public ResponseEntity<String> checkPassword(String password){
        // 기존 비밀번호랑 입력 받은 비밀번호가 동일한 지 검사를 해야 한다.
        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String checkedPassword = details.getPassword(); // details 에서 비밀번호 받아옴

        if (!passwordEncoder.matches(password,checkedPassword)){
            throw new CustomException(ERROR_PASSWORD);
        }
        Member member = memberRepository.findById(details.getId()).orElseThrow(() -> new CustomException(ERROR_USER));

        Member createMember = Member.builder().userPk(member.getUserPk())
                .userId(member.getUserId())
                .password(password).build();

        memberRepository.save(createMember);
        return ResponseEntity.ok("ok");
    }

    // 변경된 비밀번호 입력
    @PutMapping("/passwordcheck")
    public ResponseEntity<String> changePassword(String password){
        // 기본 비밀번호와 동일한지 확인후 동일하지 않으면, 비밀번호 저장
        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String checkedPassword = details.getPassword();
        if (passwordEncoder.matches(password,checkedPassword)){
            throw new CustomException(NOT_SAME_PASSWORD);
        }
        Member member = memberRepository.findById(details.getId()).orElseThrow(() -> new CustomException(ERROR_USER));

        Member createMember = Member.builder().userPk(member.getUserPk())
                .userId(member.getUserId())
                .password(password).build();

        memberRepository.save(createMember);
        return ResponseEntity.ok("ok");
    }
}
