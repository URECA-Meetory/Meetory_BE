package com.meetory.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record OnboardingRequest(
        @Min(value = 14, message = "나이는 14세 이상이어야 합니다")
        @Max(value = 100, message = "나이는 100세 이하여야 합니다")
        Integer age,

        @Size(max = 20, message = "성별은 20자 이하여야 합니다")
        String gender,

        @Size(max = 255, message = "취미는 255자 이하여야 합니다")
        String hobbies
) {
}
