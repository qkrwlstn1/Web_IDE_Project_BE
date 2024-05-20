package com.als.webIde.controller;

import com.als.webIde.DTO.etc.CustomUserDetails;
import com.als.webIde.DTO.request.ChattingMessageRequestDTO;
import com.als.webIde.DTO.response.ChattingMessageResponseDTO;
import com.als.webIde.service.ChatService;
import com.als.webIde.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChattingController {
    private final ChatService chatService;
    private final CustomUserDetailsService customUserDetailsService;
    private static Map<String,CustomUserDetails> sessions= new ConcurrentHashMap<>();//hashMap은 동시성 문제가 발생할 수 있다
    private String getUserId(SessionConnectEvent event){
        return event.getMessage().getHeaders().get("nativeHeaders").toString().split("userInfo=\\[")[1].split("]")[0];
    }

    private String getSession(SessionConnectEvent event){
        return event.getMessage().getHeaders().get("simpSessionId").toString();
    }
    private CustomUserDetails getUserDetails(String userId){
        return (CustomUserDetails) customUserDetailsService.loadUserByUsername(userId);
    }
    private CustomUserDetails getUserDetails(SimpMessageHeaderAccessor accessor){
        return sessions.get(accessor.getSessionId());
    }
    @EventListener(SessionConnectEvent.class)
    public void onConnect(SessionConnectEvent event){
        String sessionId = getSession(event);
        log.info("sessionId = {}", event.getMessage().getHeaders().get("simpSessionId").toString());
        log.info("header : {}",event.getMessage().getHeaders().get("nativeHeaders").toString());
        String userId =  getUserId(event);
        log.info("userId: {}",userId);
        CustomUserDetails userDetails = getUserDetails(userId);
        log.info("userNickName = {}", userDetails.getUserNickName());
        sessions.put(sessionId, userDetails);
        System.out.println(sessions.get(sessionId).getUserNickName());
    }

    @EventListener(SessionDisconnectEvent.class)
    public void onDisconnect(SessionDisconnectEvent event) {
        log.info("user session = {}",event.getMessage().getHeaders().get("simpSessionId").toString());
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
    public String enter(SimpMessageHeaderAccessor accessor){
        log.info("enter start");
        CustomUserDetails userDetails = getUserDetails(accessor);
        log.info("{}님 입장!", userDetails.getUserNickName());
        chatService.enterUser(userDetails.getId(), Long.parseLong(accessor.getFirstNativeHeader("roomId")));// 방에 집어넣고
        return userDetails.getUserNickName();
    }
//    @MessageMapping("/exit")
//    @SendTo("/chat/server/exit")
//    public String exit(Long roomId, SimpMessageHeaderAccessor accessor)){
//        MemberSetting exitUser = chatService.findMemberSetting(cui.getUserId());//닉네임 꺼내와야지
//        log.info("{}님 퇴장 입장!", exitUser.getNickname());
//        chatService.exitUser(1L, roomId);// 방에서 나가라~
//        return exitUser.getNickname();
//    }

    @MessageMapping("/test")
    @SendTo("/chat/server/test")
    public CustomUserDetails test(SimpMessageHeaderAccessor accessor){
        log.info("1");
        String sessionId = accessor.getSessionId();
        log.info("2");
        CustomUserDetails details = sessions.get(sessionId);
        log.info("{}",details.getUserNickName());

        if (details == null) {
            log.warn("Session ID {}에 해당하는 사용자 정보를 찾을 수 없습니다.", sessionId);
            return null;
        }

        log.info("사용자 닉네임: {}", details.getUserNickName());
        log.info("사용자 아이디: {}", details.getId());
        log.info("사용자 이름: {}", details.getUsername());
        log.info("사용자 테마: {}", details.getUserThema());
        log.info("사용자 비밀번호: {}", details.getPassword());

        return details;
    }
}