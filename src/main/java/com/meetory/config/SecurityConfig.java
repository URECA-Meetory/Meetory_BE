package com.meetory.config;

import com.meetory.auth.controller.AuthController;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) //CORS 추가
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/favicon.ico").permitAll() //정적 페이지 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
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
    
    // 브라우저 fetch 요청을 허용하기 위한 CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
    	    CorsConfiguration configuration = new CorsConfiguration();
    	    configuration.setAllowedOriginPatterns(List.of("http://localhost:8080"));
    	    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    	    configuration.setAllowedMethods(List.of("*"));
    	    configuration.setAllowCredentials(true);
    	    
    	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    	    source.registerCorsConfiguration("/**", configuration);
    	    return source;
    	
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
    	    return new BCryptPasswordEncoder();
    }
}
