package com.meetory.user;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.meetory.auth.entity.TokenBlacklist;
import com.meetory.auth.jwt.JwtTokenProvider;
import com.meetory.auth.repository.TokenBlacklistRepository;
import com.meetory.common.exception.CustomException;
import com.meetory.common.exception.ErrorCode;
import com.meetory.user.dto.AccountDeleteRequest;
import com.meetory.user.dto.OnboardingRequest;
import com.meetory.user.dto.PasswordUpdateRequest;
import com.meetory.user.dto.ProfileResponse;
import com.meetory.user.dto.ProfileUpdateRequest;
import com.meetory.user.entity.User;
import com.meetory.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public ProfileResponse getProfile(Long userId) {
        return ProfileResponse.from(findUserById(userId));
    }

    @Transactional
    public ProfileResponse updateNickname(Long userId, ProfileUpdateRequest request) {
        User user = findUserById(userId);
        user.updateNickname(request.nickname());
        return ProfileResponse.from(user);
    }

    @Transactional
    public ProfileResponse completeOnboarding(Long userId, OnboardingRequest request) {
        User user = findUserById(userId);
        user.completeOnboarding(request.age(), request.gender(), request.hobbies());
        return ProfileResponse.from(user);
    }

    @Transactional
    public ProfileResponse skipOnboarding(Long userId) {
        User user = findUserById(userId);
        user.skipOnboarding();
        return ProfileResponse.from(user);
    }

    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void deleteAccount(Long userId, AccountDeleteRequest request, String token) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        if (token != null && jwtTokenProvider.validateToken(token)
                && !tokenBlacklistRepository.existsByToken(token)) {
            LocalDateTime expiredAt = jwtTokenProvider.getExpiration(token);
            tokenBlacklistRepository.save(
                    TokenBlacklist.builder()
                            .token(token)
                            .expiredAt(expiredAt)
                            .build()
            );
        }

        userRepository.delete(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
