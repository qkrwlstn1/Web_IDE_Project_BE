package com.als.webIde.config;

import com.als.webIde.DTO.etc.ErrorCode;
import com.als.webIde.service.CustomUserDetailsService;
import com.als.webIde.service.TokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {
    private final TokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.info("StompHandlerStart");
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if(accessor.getCommand() == StompCommand.CONNECT) {

            String authorization = accessor.getNativeHeader("Authorization").get(0).substring(7);

            if (authorization != null && authorization.startsWith("Bearer")){
                String token = authorization.substring(7);

                try {
                    if (tokenProvider.validateToken(token)) {
                        Long userId = tokenProvider.getUserIdFromToken(token);

                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId.toString());

                        if (userDetails !=null) {
                            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                        }

                    }
                }catch (SecurityException | MalformedJwtException e) {
                    log.info("JWT가 올바르게 구성되지 않았습니다.");
                    throw new MessageDeliveryException("jwt body error");
                } catch (ExpiredJwtException e) {
                    log.info("JWT가 만료됨");
                    throw new MessageDeliveryException("만료됨");
                } catch (UnsupportedJwtException e) {
                    log.info("지원되지 않는 JWT");
                    throw new MessageDeliveryException("지원하지 않는 형식");
                } catch (IllegalArgumentException e) {
                    log.info("JWT의 클래엠이 null 또는 비어 있음");
                    throw new MessageDeliveryException("비어있음");
                } catch (Exception e) {
                    throw new MessageDeliveryException("에러");
                }

            }

        }
        return message;
    }
}
