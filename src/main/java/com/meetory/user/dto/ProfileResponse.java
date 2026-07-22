package com.meetory.user.dto;

import java.time.LocalDateTime;

import com.meetory.user.entity.User;

// 내 프로필 조회 응답
public record ProfileResponse(
	    Long id,
	    String email,
	    String nickname,
	    String role,
	    LocalDateTime createdAt
) { 
	public static ProfileResponse from(User user) {
		return new ProfileResponse(
				user.getId(),
				user.getEmail(),
				user.getNickname(),
				user.getRole().name(),
				user.getCreatedAt()
		);
		
	}
}
