package com.als.webIde.controller;

import com.als.webIde.DTO.request.ChattingMessageRequestDTO;
import com.als.webIde.DTO.request.ChattingUserInfoRequestDTO;
import com.als.webIde.DTO.response.ChattingMessageResponseDTO;
import com.als.webIde.domain.entity.MemberSetting;
import com.als.webIde.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChattingController {
    private final ChatService chatService;

    @MessageMapping("/chatting")
    @SendTo("/chat/server/messages")
    @ResponseBody
    public ChattingMessageResponseDTO sendChatting(ChattingMessageRequestDTO cmr) {
        log.info("start... ChattingMessageRequestDTO = {}",cmr);
        return chatService.sendMessage(cmr);
    }
    @MessageMapping("/enter")
    @SendTo("/chat/server/enter")
    public String enter(ChattingUserInfoRequestDTO cui){
        MemberSetting enterUser = chatService.findMemberSetting(cui.getUserId());//닉네임 꺼내와야지
        log.info("{}님 입장!", enterUser.getNickname());
        chatService.enterUser(cui.getUserId(), cui.getRoomId());// 방에 집어넣고
        return enterUser.getNickname();
    }
    @MessageMapping("/exit")
    @SendTo("/chat/server/exit")
    public String exit(ChattingUserInfoRequestDTO cui){
        MemberSetting exitUser = chatService.findMemberSetting(cui.getUserId());//닉네임 꺼내와야지
        log.info("{}님 퇴장 입장!", exitUser.getNickname());
        chatService.exitUser(cui.getUserId(), cui.getRoomId());// 방에서 나가라~
        return exitUser.getNickname();
    }
}