package com.als.webIde.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration//설정
@EnableWebSocketMessageBroker// server와 client 간에 양방향 통신
public class ChattingConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {

    /**
     * 웹소켓 엔드포인트를 등록하는 메서드입니다.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // "/chat" 경로에 웹소켓 엔드포인트를 추가합니다. 이 경로로 웹소켓 연결이 이루어집니다.
        // withSockJS() 메서드를 호출하여 웹소켓이 지원되지 않는 브라우저에서도
        // 연결을 지원하도록 SockJS 프로토콜을 활성화합니다.
        registry.addEndpoint("/chat").withSockJS();
    }

    /**
     * 메시지 브로커의 구성을 설정하는 메서드입니다.
     * 이 메서드를 통해 메시지를 라우팅하는 방식과 메시지를 처리할 대상(prefix)를 설정할 수 있습니다.
     *
     * @param registry 메시지 브로커를 구성할 때 사용하는 레지스트리 객체
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // "/app"으로 시작하는 경로를 애플리케이션의 메시지 매핑 경로로 설정합니다.
        // 이 경로로 보내진 메시지는 @MessageMapping 어노테이션이 적용된 메서드가 처리합니다.
        registry.setApplicationDestinationPrefixes("/chat/client");

        // 간단한 메모리 기반의 메시지 브로커를 활성화하고, "/topic"을 통해 메시지를 브로드캐스트할 수 있습니다.
        // 예를 들어, 클라이언트가 "/topic/messages"를 구독하고 있다면 해당 주소로 메시지가 전송될 때 모든 구독자에게 메시지가 도달합니다.
        registry.enableSimpleBroker("/chat/server");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

    }
}
