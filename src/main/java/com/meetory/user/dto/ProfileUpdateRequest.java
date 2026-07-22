package com.meetory.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 닉네임 변경 요청
public record ProfileUpdateRequest(
	
		@NotBlank(message = "닉네임은 필수입니다")
		@Size(max = 30, message = "닉네임은 30자 이하여야 합니다")
		String nickname
) {
}
