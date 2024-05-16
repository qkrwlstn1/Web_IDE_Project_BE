package com.als.webIde.service;

import com.als.webIde.DTO.request.ChattingMessageRequestDTO;
import com.als.webIde.DTO.response.ChattingMessageResponseDTO;
import com.als.webIde.domain.entity.*;
import com.als.webIde.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService{
    private final MemberRepository memberRepository;
    private final MemberSettingRepository memberSettingRepository;
    private final ChattingRoomRepository chattingRoomRepository;
    private final ChattingMemberRepository chattingMemberRepository;
    private final ChattingMessageRepository chattingMessageRepository;
    @Override
    public ChattingRoom findRoom(Long id) {
        return chattingRoomRepository.findById(id).get();
    }

    @Override
    public MemberSetting findMemberSetting(Long id) {
        return memberSettingRepository.findById(new MemberSettingId(id)).get();
    }

    @Override
    public boolean enterUser(Long userId, Long roomId) {
        log.info("enterUser Start");
        Member member = this.findMember(userId);
        ChattingRoom cr = this.findRoom(roomId);
        ChattingMember cm = new ChattingMember().builder()
                .chattingRoom(cr)
                .member(member)
                .build();
        chattingMemberRepository.save(cm);
        log.info("enterUser end");
        return true;
    }

    @Override
    public boolean exitUser(Long userId, Long roomId) {
        log.info("exitUser Start");
        Member member = this.findMember(userId);
        ChattingRoom cr = this.findRoom(roomId);
        chattingMemberRepository.deleteByChattingRoomAndMember(cr, member);
        log.info("exitUser end");
        return true;
    }

    @Override
    public Member findMember(Long id) {
        log.info("findMember start");
        Optional<Member> member = memberRepository.findById(id);
        if(member.isPresent()) return member.get();
        log.info("findMember end");
        return null;
    }

    @Override
    public ChattingMessageResponseDTO sendMessage(ChattingMessageRequestDTO cmr) {
        log.info("sendMessage start");
        Optional<Member> optionalSender = memberRepository.findById(cmr.getMemberId());
        Member sender;
        //회원가입 완성 시 변경 될 코드
        if(optionalSender.isPresent()){
            sender = optionalSender.get();
        }else{
            Member tmp = new Member().builder()
                    .userId("asd")
                    .password("asd")
                    .build();
            sender = this.makeMemberTest(tmp, new MemberSetting());
        }
        //-----------
        Optional<ChattingRoom> optionalChattingRoom = chattingRoomRepository.findById(cmr.getChattingRoomId());
        ChattingRoom senderChattingRoom;

        //방 만들기 기능 확장 시 변경 될 코드
        if(optionalChattingRoom.isPresent()){
            senderChattingRoom = optionalChattingRoom.get();
        }else{
            senderChattingRoom = this.makeRoomTest(1L, "방이없네어..");
        }
        //-------------------------------

        ChattingMessage sendMessage = new ChattingMessage().builder()
                .memberId(sender)
                .roomId(senderChattingRoom)
                .sendTime(LocalDateTime.now())
                .messageContent(cmr.getMessageContent())
                .build();
        MemberSetting memberSetting = memberSettingRepository.findById(new MemberSettingId(sender.getUserPk())).get();
        sendMessage = chattingMessageRepository.save(sendMessage);
        ChattingMessageResponseDTO result = new ChattingMessageResponseDTO(sendMessage);
        result.setNickname(memberSetting.getNickname());
        log.info("sendMessage end");
        return result;
    }


    @Override //방 만들기 기능 확장 시 변경 될 메서드
    public ChattingRoom makeRoomTest(Long roomMaster, String roomName){
        log.info("makeRoomTest start");
        ChattingRoom chattingRoom = chattingRoomRepository.save(new ChattingRoom().builder()
                .roomMaster(roomMaster)
                .roomDate(LocalDateTime.now())
                .roomName(roomName)
                .build());
        log.info("makeRoomTest end");
        return  chattingRoom;
    }

    @Override//회원가입 완성 시 없어질 메서드
    public Member makeMemberTest(Member member, MemberSetting memberSetting){
        log.info("makeMemberTest start");
        Member member1 = memberRepository.save(member);
        memberSetting = memberSetting.builder()
                .MemberId(new MemberSettingId(member1.getUserPk()))
                .nickname("testNick")
                .member(member1)
                .build();
        memberSettingRepository.save(memberSetting);
        log.info("makeMemberTest end");
        return member1;
    }


}
