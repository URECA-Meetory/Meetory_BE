package com.meetory.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meetory.common.ApiResponse;
import com.meetory.user.UserService;
import com.meetory.user.dto.AccountDeleteRequest;
import com.meetory.user.dto.OnboardingRequest;
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

    @GetMapping
    public ApiResponse<ProfileResponse> getProfile(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(userService.getProfile(userId));
    }

    @PatchMapping
    public ApiResponse<ProfileResponse> updateNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        ProfileResponse response = userService.updateNickname(userId, request);
        return ApiResponse.success("닉네임이 변경되었습니다", response);
    }

    @PutMapping("/onboarding")
    public ApiResponse<ProfileResponse> completeOnboarding(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody OnboardingRequest request) {
        ProfileResponse response = userService.completeOnboarding(userId, request);
        return ApiResponse.success("프로필이 저장되었습니다", response);
    }

    @PostMapping("/onboarding/skip")
    public ApiResponse<ProfileResponse> skipOnboarding(@AuthenticationPrincipal Long userId) {
        ProfileResponse response = userService.skipOnboarding(userId);
        return ApiResponse.success("온보딩을 건너뛰었습니다", response);
    }

    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(userId, request);
        return ApiResponse.success("비밀번호가 변경되었습니다", null);
    }

    @DeleteMapping
    public ApiResponse<Void> deleteAccount(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AccountDeleteRequest request,
            HttpServletRequest httpRequest) {
        userService.deleteAccount(userId, request, resolveToken(httpRequest));
        return ApiResponse.success("계정이 삭제되었습니다", null);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
