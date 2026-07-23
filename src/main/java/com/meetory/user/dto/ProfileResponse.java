package com.meetory.user.dto;

import java.time.LocalDateTime;

import com.meetory.user.entity.User;

public record ProfileResponse(
        Long id,
        String email,
        String nickname,
        Integer age,
        String gender,
        String hobbies,
        boolean onboardingCompleted,
        String role,
        LocalDateTime createdAt
) {
    public static ProfileResponse from(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAge(),
                user.getGender(),
                user.getHobbies(),
                user.isOnboardingCompleted(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
