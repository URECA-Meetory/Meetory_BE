package com.meetory.auth.dto;

public record LoginResponse(
        String accessToken,
        Long userId,
        String nickname,
        String email,
        boolean onboardingCompleted
) {
}
