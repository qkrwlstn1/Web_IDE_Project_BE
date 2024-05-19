package com.als.webIde.service;

import com.als.webIde.DTO.request.ChattingMessageRequestDTO;
import com.als.webIde.DTO.response.ChattingMessageResponseDTO;
import com.als.webIde.domain.entity.ChattingRoom;
import com.als.webIde.domain.entity.Member;
import com.als.webIde.domain.entity.MemberSetting;

public interface ChatService {
    ChattingMessageResponseDTO sendMessage(ChattingMessageRequestDTO cmr);

    ChattingRoom makeRoomTest(Long roomMaser, String roomName);

    Member makeMemberTest(Member member, MemberSetting memberSetting);

    ChattingRoom findRoom(Long id);

    boolean enterUser(Long userId, Long roomId);
    boolean exitUser(Long userId, Long roomId);

    Member findMember(Long id);
}
