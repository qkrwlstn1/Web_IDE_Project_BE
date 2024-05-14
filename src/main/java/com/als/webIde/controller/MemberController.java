package com.als.webIde.controller;

import com.als.webIde.DTO.etc.CustomUserDetails;
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
import com.als.webIde.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberSettingRepository memberSettingRepository;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/idcheck")
    public ResponseEntity<Message> checkedUserId(@RequestBody UserId userId){
        List<Member> memberByUserId = memberRepository.findMemberByUserId(userId.getUserId());
        if (memberByUserId.isEmpty()){
            return ResponseEntity.ok(Message.builder().message("사용할 수 있는 아이디입니다.").build());
        }
        return ResponseEntity.status(400).body(Message.builder().message("사용할 수 없는 아이디입니다.").build());
    }

    @PostMapping("/nicknamecheck")
    public ResponseEntity<Message> checkedUserNickName(@RequestBody UserNickName nickName){
        List<MemberSetting> findNickName = memberSettingRepository.findMemberSettingByNickname(nickName.getUserNickName());

        if (findNickName.isEmpty()){
            return ResponseEntity.ok(Message.builder().message("사용할 수 있는 닉네임입니다.").build());
        }

        return ResponseEntity.status(400).body(Message.builder().message("사용할 수 없는 닉네임입니다.").build());
    }

    @Transactional
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserInfo userInfo){
        if(!userInfo.getPassword().equals(userInfo.getPasswordConfirm())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        log.info("회원가입 시작!");
        Member saveUser = Member.builder()
                .userId(userInfo.getUserId())
                .password(passwordEncoder.encode(userInfo.getPassword())).build();
        Member member = memberRepository.save(saveUser);
        log.info("유저은 : {}",saveUser.getUserId());
        MemberSettingId memberSettingId = new MemberSettingId(member.getUserPk());
        MemberSetting saveUserSetting = MemberSetting.builder()
                .MemberId(memberSettingId)
                .member(member)
                .nickname(userInfo.getNickname()).build();
        log.info("유저의 세팅은 : {}",saveUserSetting.getMemberId());

        memberSettingRepository.save(saveUserSetting);
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/accessToken")
    public ResponseEntity<String> reissueAccessToken(HttpRequest request){
        HttpHeaders headers = request.getHeaders();
        String refreshToken = headers.get("X-Refresh-Token").get(0);
        String reissueAccessToken = userService.reissueAccessToken(refreshToken);
        return ResponseEntity.ok(reissueAccessToken);

    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLogin userLogin) {

        TokenDto token = new TokenDto("helo","jhele","df");
        List<Member> memberByUserId = memberRepository.findMemberByUserId(userLogin.getUserId());
        try{
            if(memberByUserId.isEmpty()){
                // 에러 핸들러 설정하기
                return ResponseEntity.ok("등록되지 않은 아이디입니다.");
            }
            Member member = memberByUserId.get(0);

            log.info("member : {}",member.toString());

            if (!passwordEncoder.matches(userLogin.getPassword(),member.getPassword())){
                return ResponseEntity.ok("비밀번호가 틀렸습니다");
            }else {
                log.info("여기까지 왔다요");
            }

            token = userService.login(member.getUserPk(),userLogin.getPassword());

            log.info("여기까지 왔다요");
        }catch (Exception e){
            log.info("error : {}",e.getMessage());
        }


        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization",token.getAccessToken());
        httpHeaders.add("X-Refresh-Token",token.getRefreshToken());

        return ResponseEntity.ok().headers(httpHeaders).body("로그인 성공");
    }


    /**
     * 사용자의 마이페이지의 정보를 출력하는 페이지
     * @return
     */
    @GetMapping
    public ResponseEntity<ResponseUserInfo> userInfo(){
        // 반환 해야 되는 내용
        // 닉네임, 비밀번호, 
        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("details의 닉네임 : {}",details.getUserNickName());
        log.info("details의 테마 : {}",details.getUserThema());
        log.info("details의 패스워드 : {}",details.getPassword());
        log.info("details의 아아디 : {}",details.getUsername());
        log.info("details의 유저아이디 : {}",details.getId());

        ResponseUserInfo userInfo = ResponseUserInfo.builder()
                .userId(details.getUsername())
                .nickName(details.getUserNickName()).build();

        return ResponseEntity.ok().body(userInfo);
    }

    @PostMapping("/password")
    public ResponseEntity<String> checkPassword(String password){
        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String checkedPassword = details.getPassword();
        if (checkedPassword.equals(password)){
            return ResponseEntity.ok("ok");
        }
        return ResponseEntity.status(400).body("error");
    }

    @PostMapping("/nickname")
    public ResponseEntity<String> changeNickname(String nickname){
        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        MemberSetting referenceById = memberSettingRepository.getReferenceById(new MemberSettingId(details.getId()));

        MemberSetting member = MemberSetting.builder().MemberId(new MemberSettingId(details.getId()))
                .nickname(nickname).thema(details.getUserThema()).member(referenceById.getMember()).build();

        memberSettingRepository.save(member);

        return ResponseEntity.ok("ok");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(String password){
        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Member member = memberRepository.findById(details.getId()).get();

        Member createMember = Member.builder().userPk(member.getUserPk())
                .userId(member.getUserId())
                .password(password).build();

        memberRepository.save(member);
        return ResponseEntity.ok("ok");
    }
}
