package com.meetory.common;

// 모든 API 응답을 이 형식으로 통일 (팀 전체가 같이 쓰는 공통 규격)
// { success: true/false, message: "...", data: {...} }
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {

    // 성공 응답 (메시지 기본값 "성공")
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "성공", data);
    }

    // 성공 응답 (커스텀 메시지)
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // 실패 응답
    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}