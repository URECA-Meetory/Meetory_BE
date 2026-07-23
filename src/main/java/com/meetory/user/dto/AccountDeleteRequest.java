package com.meetory.user.dto;

import jakarta.validation.constraints.NotBlank;

// 계정 삭제 시 본인 확인용 비밀번호 입력
public record AccountDeleteRequest(

		@NotBlank(message = "비밀번호를 입력해주세요")
		String password
) {
}
