package com.meetory.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.meetory.auth.dto.LoginRequest;
import com.meetory.auth.dto.LoginResponse;
import com.meetory.auth.dto.SignupRequest;
import com.meetory.auth.entity.TokenBlacklist;
import com.meetory.auth.jwt.JwtTokenProvider;
import com.meetory.auth.repository.TokenBlacklistRepository;
import com.meetory.common.exception.CustomException;
import com.meetory.user.entity.Role;
import com.meetory.user.entity.User;
import com.meetory.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void 회원가입_성공() {
        SignupRequest request = new SignupRequest("test@test.com", "password123", "닉네임");
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");

        authService.signup(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void 회원가입_실패_이메일중복() {
        SignupRequest request = new SignupRequest("test@test.com", "password123", "닉네임");
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 가입된 이메일입니다");
    }

    @Test
    void 로그인_성공() {
        LoginRequest request = new LoginRequest("test@test.com", "password123");
        User user = User.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .nickname("닉네임")
                .role(Role.USER)
                .build();

        given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(true);
        given(jwtTokenProvider.createToken(any(), any())).willReturn("token");

        LoginResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("token");
    }

    @Test
    void 로그인_실패_비밀번호불일치() {
        LoginRequest request = new LoginRequest("test@test.com", "wrongPassword");
        User user = User.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .nickname("닉네임")
                .role(Role.USER)
                .build();

        given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 로그아웃_성공() {
        String token = "validToken";
        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(tokenBlacklistRepository.existsByToken(token)).willReturn(false);
        given(jwtTokenProvider.getExpiration(token)).willReturn(LocalDateTime.now().plusHours(1));

        authService.logout(token);

        verify(tokenBlacklistRepository).save(any(TokenBlacklist.class));
    }

    @Test
    void 로그아웃_실패_유효하지않은토큰() {
        String token = "invalidToken";
        given(jwtTokenProvider.validateToken(token)).willReturn(false);

        assertThatThrownBy(() -> authService.logout(token))
                .isInstanceOf(CustomException.class)
                .hasMessage("유효하지 않은 토큰입니다");
    }

    @Test
    void 로그아웃_실패_이미로그아웃됨() {
        String token = "alreadyLoggedOut";
        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(tokenBlacklistRepository.existsByToken(token)).willReturn(true);

        assertThatThrownBy(() -> authService.logout(token))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 로그아웃된 토큰입니다");
    }
}