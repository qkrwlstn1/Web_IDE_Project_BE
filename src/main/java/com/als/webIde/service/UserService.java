package com.als.webIde.service;

import com.als.webIde.DTO.etc.CustomException;
import com.als.webIde.DTO.etc.CustomUserDetails;
import com.als.webIde.DTO.etc.TokenDto;
import com.als.webIde.DTO.request.UserId;
import com.als.webIde.DTO.request.UserInfo;
import com.als.webIde.DTO.request.UserNickName;
import com.als.webIde.DTO.response.Message;
import com.als.webIde.DTO.response.ResponseUserInfo;
import com.als.webIde.domain.entity.Member;
import com.als.webIde.domain.entity.MemberSetting;
import com.als.webIde.domain.entity.MemberSettingId;
import com.als.webIde.domain.repository.MemberRepository;
import com.als.webIde.domain.repository.MemberSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.als.webIde.DTO.etc.CustomErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final MemberSettingRepository memberSettingRepository;
    private final MemberRepository memberRepository;

    public TokenDto login(Long userId,String password) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userId,password);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        //token 생성
        String accessToken = tokenProvider.generateAccessToken(authenticationToken);
        String refreshToken = tokenProvider.generateRefreshToken(authenticationToken);

        return TokenDto.builder()
                .grantType("Barer")
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .build();
    }

    public String reissueAccessToken(String refreshToken) {
        refreshToken = refreshToken.substring(7);
        if(tokenProvider.validateToken(refreshToken)){
            Long userId = tokenProvider.getUserIdFromToken(refreshToken);
            return tokenProvider.reissueAccessToken(userId);
        }
        return null;
    }

    public Message checkedUserId(UserId userId){
        //멤버의 아이디를 찾는 서비스
        Optional<Member> memberByUserId = memberRepository.findMemberByUserId(userId.getUserId());

        if (!memberByUserId.isEmpty()){
            throw  new CustomException(INVALID_USERID);
        }

        // 아이디가 없으면 사용할 수 없는 아이디라고 명시
        return Message.builder().message("사용할 수 있는 아이디입니다.").build();
    }

    public Message checkedUserNickName(UserNickName nickName){
        // 멤버의 닉네임이 있는지 확인
        Optional<MemberSetting> findNickName = memberSettingRepository.findByNickname(nickName.getUserNickName());


        if (findNickName.isPresent()){
            // 현재 존재하고 있는 닉네임이면 사용할 수 없은 이름이라고 알림
            throw new CustomException(INVALID_NICKNAME);
        }

        // 닉네임이 없으면 사용할 수 있는 이름이라고 알림
        return Message.builder().message("사용할 수 있는 닉네임입니다.").build();
    }

    public Message signUp(UserInfo userInfo){

        Member saveUser = Member.builder()
                .userId(userInfo.getUserId())
                .password(passwordEncoder.encode(userInfo.getPassword())).build();
        Member member = memberRepository.save(saveUser);

        MemberSettingId memberSettingId = new MemberSettingId(member.getUserPk());
        MemberSetting saveUserSetting = MemberSetting.builder()
                .MemberId(memberSettingId)
                .member(member)
                .nickname(userInfo.getNickname()).build();

        memberSettingRepository.save(saveUserSetting);
        return Message.builder().message("회원가입 성공").build();
    }

    public ResponseUserInfo userInfo(){
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
        return userInfo;
    }
}
