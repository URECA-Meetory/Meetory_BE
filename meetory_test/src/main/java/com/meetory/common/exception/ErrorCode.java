package com.meetory.common.exception;

import org.springframework.http.HttpStatus;



public enum ErrorCode {
	
	// auth 항목
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 안습니다"),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다"),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다"),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
	ALEADY_LOGGED_OUT(HttpStatus.UNAUTHORIZED, "이미 로그아웃된 토큰입니다"),
	
	// user 공통
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다"),
	
	// team / member 관련
	TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 모임입니다"),
	TEAM_NOT_RECRUITING(HttpStatus.CONFLICT, "모집이 마감된 모임입니다"),
	TEAM_FULL(HttpStatus.CONFLICT, "모임 정원이 초과되었습니다"),
	TEAM_NO_PERMISSION(HttpStatus.FORBIDDEN, "모임에 대한 권한이 없습니다"),
	SELP_APPLY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인이 개설한 모임에는 신청할 수 없습니다"),
	MEMBER_ALREADY_APPLIED(HttpStatus.CONFLICT, "이미 신청한 모임입니다"),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "신청 내역을 찾을 수 없습니다"),
	MEMBER_ALEADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 신청입니다");

	private final HttpStatus status;
	private final String message;
	
	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	
	public String getmessage() {
		return message;
	}
}
