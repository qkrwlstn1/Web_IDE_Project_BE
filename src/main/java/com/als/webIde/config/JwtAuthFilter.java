package com.als.webIde.config;

import com.als.webIde.DTO.etc.ErrorCode;
import com.als.webIde.service.CustomUserDetailsService;
import com.als.webIde.service.TokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;
    private final TokenProvider jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer")){
            String token = authorization.substring(7);

            try {
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);

                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId.toString());

                    if (userDetails !=null) {
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }

                }
            }catch (SecurityException | MalformedJwtException e) {
                request.setAttribute("exception", ErrorCode.WRONG_TYPE_TOKEN.getCode());
            } catch (ExpiredJwtException e) {
                request.setAttribute("exception", ErrorCode.EXPIRED_TOKEN.getCode());
            } catch (UnsupportedJwtException e) {
                request.setAttribute("exception", ErrorCode.UNSUPPORTED_TOKEN.getCode());
            } catch (IllegalArgumentException e) {
                request.setAttribute("exception", ErrorCode.WRONG_TYPE_TOKEN.getCode());
            } catch (Exception e) {
                request.setAttribute("exception", ErrorCode.UNKNOWN_ERROR.getCode());
            }

        }
        filterChain.doFilter(request, response);
    }
}
