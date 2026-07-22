package com.meetory.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.meetory.auth.jwt.JwtTokenProvider;
import com.meetory.auth.repository.TokenBlacklistRepository;
import com.meetory.common.exception.CustomException;
import com.meetory.user.UserService;
import com.meetory.user.dto.AccountDeleteRequest;
import com.meetory.user.dto.PasswordUpdateRequest;
import com.meetory.user.dto.ProfileResponse;
import com.meetory.user.dto.ProfileUpdateRequest;
import com.meetory.user.entity.Role;
import com.meetory.user.entity.User;
import com.meetory.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private TokenBlacklistRepository tokenBlacklistRepository;

    @InjectMocks
    private UserService userService;

    private User createUser() {
        return User.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .nickname("기존닉네임")
                .role(Role.USER)
                .build();
    }

    @Test
    void 프로필_조회_성공() {
        User user = createUser();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        ProfileResponse response = userService.getProfile(1L);

        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.nickname()).isEqualTo("기존닉네임");
    }

    @Test
    void 프로필_조회_실패_사용자없음() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(999L))
                .isInstanceOf(CustomException.class)
                .hasMessage("존재하지 않는 사용자입니다");
    }

    @Test
    void 닉네임_변경_성공() {
        User user = createUser();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        ProfileResponse response = userService.updateNickname(1L, new ProfileUpdateRequest("새닉네임"));

        assertThat(response.nickname()).isEqualTo("새닉네임");
    }

    @Test
    void 비밀번호_변경_성공() {
        User user = createUser();
        PasswordUpdateRequest request = new PasswordUpdateRequest("oldPassword", "newPassword123");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("oldPassword", user.getPassword())).willReturn(true);
        given(passwordEncoder.matches("newPassword123", user.getPassword())).willReturn(false);
        given(passwordEncoder.encode("newPassword123")).willReturn("encodedNewPassword");

        userService.updatePassword(1L, request);

        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    void 비밀번호_변경_실패_현재비밀번호불일치() {
        User user = createUser();
        PasswordUpdateRequest request = new PasswordUpdateRequest("wrongPassword", "newPassword123");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPassword", user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> userService.updatePassword(1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("비밀번호가 일치하지 않습니다");
    }

    @Test
    void 계정삭제_성공() {
        User user = createUser();
        AccountDeleteRequest request = new AccountDeleteRequest("password123");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", user.getPassword())).willReturn(true);
        given(jwtTokenProvider.validateToken("validToken")).willReturn(true);
        given(tokenBlacklistRepository.existsByToken("validToken")).willReturn(false);
        given(jwtTokenProvider.getExpiration("validToken"))
                .willReturn(java.time.LocalDateTime.now().plusHours(1));

        userService.deleteAccount(1L, request, "validToken");

        verify(userRepository).delete(user);
    }

    @Test
    void 계정삭제_실패_비밀번호불일치() {
        User user = createUser();
        AccountDeleteRequest request = new AccountDeleteRequest("wrongPassword");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPassword", user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> userService.deleteAccount(1L, request, "token"))
                .isInstanceOf(CustomException.class)
                .hasMessage("비밀번호가 일치하지 않습니다");
    }
}