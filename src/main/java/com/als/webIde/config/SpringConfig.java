package com.als.webIde.config;

import com.als.webIde.service.CustomUserDetailsService;
import com.als.webIde.service.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jdk.jfr.Frequency;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@EnableWebSecurity
@Configuration
public class SpringConfig {
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenProvider tokenProvider;

    public SpringConfig(CustomUserDetailsService customUserDetailsService, TokenProvider tokenProvider){
        this.customUserDetailsService = customUserDetailsService;
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(AbstractHttpConfigurer::disable);

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.addFilterBefore(new JwtAuthFilter(customUserDetailsService, tokenProvider), UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling(exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(new CustomAuthenticationEntryPoint()));
        http.authorizeHttpRequests(request ->
                request.requestMatchers("/api/user/idcheck","/api/user/nicknamecheck","/api/user/signup","/api/user/login",
                                "/chat/**","/","/index.html")
                        .permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                        .permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public static BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


