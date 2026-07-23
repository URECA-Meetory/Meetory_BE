package com.meetory.user.dto;

import jakarta.validation.constraints.NotBlank;

public record AccountDeleteRequest(
        @NotBlank(message = "비밀번호는 필수입니다")
        String password
) {
}
