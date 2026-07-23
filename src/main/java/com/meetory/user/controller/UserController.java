package com.meetory.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.meetory.common.ApiResponse;
import com.meetory.user.UserService;
import com.meetory.user.dto.AccountDeleteRequest;
import com.meetory.user.dto.PasswordUpdateRequest;
import com.meetory.user.dto.ProfileResponse;
import com.meetory.user.dto.ProfileUpdateRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	
	// GET /api/users/me - 내프로필 조회
	@GetMapping
	public ApiResponse<ProfileResponse> getProfile(@AuthenticationPrincipal Long userId) {
		ProfileResponse response = userService.getProfile(userId);
		return ApiResponse.success(response);
	}
	
	// PATCH /api/users/me -닉네임 변경
	@PatchMapping
	public ApiResponse<ProfileResponse> updateNickname(
			@AuthenticationPrincipal Long userId,
			@Valid @RequestBody ProfileUpdateRequest request) {
		
		ProfileResponse response = userService.updateNickname(userId, request);
		return ApiResponse.success("닉네임이 변경되었습니다", response);
	}
	
	
	// PUT /api/user/me/password - 비밀번호 변경
	@PutMapping("/password")
	public ApiResponse<Void> updatePassword(
			@AuthenticationPrincipal Long userId,
			@Valid @RequestBody PasswordUpdateRequest request) {
		
		userService.updatePassword(userId, request);
		return ApiResponse.success("비밀번호가 변경되었습니다", null);
	}
	
	// DELETE /api/users/me - 계정 삭제
	@DeleteMapping
	public ApiResponse<Void> deleteAccount(
			@AuthenticationPrincipal Long userId,
			@Valid @RequestBody AccountDeleteRequest request,
			HttpServletRequest httpRequest) {
		
		String token = resolveToken(httpRequest);
		userService.deleteAccount(userId, request, token);
		return ApiResponse.success("계정이 삭제되었습니다", null);
	}
	
	private String resolveToken(HttpServletRequest request) {
		String bearer = request.getHeader("Authorization");
		if(bearer != null && bearer.startsWith("Bearer ")) {
    		   return bearer.substring(7);
	}
     return null;
	}
}
