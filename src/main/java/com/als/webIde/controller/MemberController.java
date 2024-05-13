package com.als.webIde.controller;

import com.als.webIde.DTO.etc.TokenDto;
import com.als.webIde.DTO.request.UserId;
import com.als.webIde.DTO.request.UserInfo;
import com.als.webIde.DTO.request.UserLogin;
import com.als.webIde.DTO.request.UserNickName;
import com.als.webIde.domain.entity.Member;
import com.als.webIde.domain.entity.MemberSetting;
import com.als.webIde.domain.repository.MemberRepositpory;
import com.als.webIde.domain.repository.MemberSettingRepository;
import com.als.webIde.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class MemberController {

    private final MemberRepositpory memberRepositpory;
    private final MemberSettingRepository memberSettingRepository;
    private final UserService userService;

    @PostMapping("/idcheck")
    public ResponseEntity<String> checkedUserId(@RequestBody UserId userId){
        List<Member> memberByUserId = memberRepositpory.findMemberByUserId(userId.getUserId());
        if (memberByUserId.isEmpty()){
            return ResponseEntity.ok("사용할 수 있는 아이디입니다.");
        }
        return ResponseEntity.status(400).body("사용할 수 없는 아이디입니다.");
    }

    @PostMapping("/nicknamecheck")
    public ResponseEntity<String> checkedUserNickName(@RequestBody UserNickName nickName){
        List<MemberSetting> findNickName = memberSettingRepository.findMemberSettingByNickname(nickName.getUserNickName());

        if (findNickName.isEmpty()){
            return ResponseEntity.ok("사용할 수 있는 닉네임입니다.");
        }

        return ResponseEntity.status(400).body("사용할 수 없는 닉네임입니다.");
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
                .password(userInfo.getPassword()).build();
        Member member = memberRepositpory.save(saveUser);
        log.info("유저은 : {}",saveUser.getUserId());
        //MemberSettingId memberSettingId = new MemberSettingId(member.getUserPk());
        MemberSetting saveUserSetting = new MemberSetting().builder()
                .member(member)
                .nickname(userInfo.getNickname()).build();
        log.info("유저의 세팅은 : {}",saveUserSetting.getMemberId());

        memberSettingRepository.save(saveUserSetting);
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLogin userLogin) {
        List<Member> memberByUserId = memberRepositpory.findMemberByUserId(userLogin.getUserId());
        if(memberByUserId.isEmpty()){
            // 에러 핸들러 설정하기
            return ResponseEntity.ok("등록되지 않은 아이디입니다.");
        }
        Member member = memberByUserId.get(0);
        if (!member.getPassword().equals(userLogin.getPassword())){
            return ResponseEntity.ok("비밀번호가 틀렸습니다");
        }

        TokenDto token = userService.login(member.getUserPk());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization",token.getAccessToken());
        httpHeaders.add("X-Refrush-Token",token.getRefreshToken());

        return ResponseEntity.ok().headers(httpHeaders).body("dfs");
    }
}
