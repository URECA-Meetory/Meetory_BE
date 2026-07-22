package com.meetory.config;

import com.meetory.auth.controller.AuthController;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.meetory.auth.jwt.JwtAuthenticationFilter;
import com.meetory.auth.jwt.JwtTokenProvider;
import com.meetory.auth.repository.TokenBlacklistRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;



@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	
	private final JwtTokenProvider jwtTokenProvider;
	private final TokenBlacklistRepository tokenBlacklistRepository;

	
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 브라우저 테스트용 정적 페이지 (src/main/resources/static/team-test.html)
                        .requestMatchers(HttpMethod.GET, "/", "/team-test.html", "/favicon.ico").permitAll()
                        // 대기중 신청 목록(GET applications)은 리더 본인만 볼 수 있어야 하므로
                        // 아래의 넓은 permitAll 규칙보다 반드시 먼저 매칭되도록 위에 둔다 (Spring Security 는 먼저 매칭되는 규칙을 사용)
                        .requestMatchers(HttpMethod.GET, "/api/teams/*/applications").authenticated()
                        // 팀 매칭 목록/상세/멤버목록은 잡코리아 공고처럼 비로그인 상태에서도 열람 가능해야 함
                        // (모임 개설 POST /api/teams, 신청 POST /api/teams/{id}/apply, 승인/거절 POST 는 로그인 필요 -> anyRequest().authenticated() 로 처리)
                        .requestMatchers(HttpMethod.GET, "/api/teams/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, tokenBlacklistRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "권한이 없습니다"))
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
    	    return new BCryptPasswordEncoder();
    }
}
