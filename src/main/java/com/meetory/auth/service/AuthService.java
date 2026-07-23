package com.meetory.auth.service;

import com.meetory.auth.dto.LoginRequest;
import com.meetory.auth.dto.LoginResponse;
import com.meetory.auth.dto.SignupRequest;
import com.meetory.auth.entity.TokenBlacklist;
import com.meetory.auth.jwt.JwtTokenProvider;
import com.meetory.auth.repository.TokenBlacklistRepository;
import com.meetory.common.exception.CustomException;
import com.meetory.common.exception.ErrorCode;
import com.meetory.user.entity.Role;
import com.meetory.user.entity.User;
import com.meetory.user.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email.trim());
    }

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = jwtTokenProvider.createToken(user.getId(), user.getRole().name());

        return new LoginResponse(
                token,
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.isOnboardingCompleted()
        );
    }

    @Transactional
    public void logout(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        if (tokenBlacklistRepository.existsByToken(token)) {
            throw new CustomException(ErrorCode.ALEADY_LOGGED_OUT);
        }

        LocalDateTime expiredAt = jwtTokenProvider.getExpiration(token);

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(token)
                .expiredAt(expiredAt)
                .build();

        tokenBlacklistRepository.save(blacklist);
    }
}