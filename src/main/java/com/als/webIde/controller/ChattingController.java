package com.als.webIde.controller;

import com.als.webIde.DTO.etc.CustomUserDetails;
import com.als.webIde.DTO.request.ChattingMessageRequestDTO;
import com.als.webIde.DTO.request.ChattingUserInfoRequestDTO;
import com.als.webIde.DTO.response.ChattingMessageResponseDTO;
import com.als.webIde.domain.entity.MemberSetting;
import com.als.webIde.service.ChatService;
import com.als.webIde.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChattingController {
    private final ChatService chatService;
    private final CustomUserDetailsService customUserDetailsService;
    private static Map<String,CustomUserDetails> sessions= new HashMap<>();

    @EventListener(SessionConnectEvent.class)
    public void onConnect(SessionConnectEvent event){
        String sessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        log.info("header : {}",event.getMessage().getHeaders().get("nativeHeaders").toString());
        String userId = event.getMessage().getHeaders().get("nativeHeaders").toString().split("userInfo=\\[")[1].split("]")[0];
        log.info("userId: {}",userId);
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(userId);
        sessions.put(sessionId, userDetails);
    }

    @EventListener(SessionDisconnectEvent.class)
    public void onDisconnect(SessionDisconnectEvent event) {
        sessions.remove(event.getSessionId());
    }

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

    @MessageMapping("/test")
    @SendTo("/chat/server/test")
    public CustomUserDetails test(SimpMessageHeaderAccessor accessor){
        CustomUserDetails details = sessions.get(accessor.getSessionId());
        log.info("이거보면 50프로성공 = {}", details.getUserNickName());
        return details;
    }
}