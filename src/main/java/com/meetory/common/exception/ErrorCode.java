package com.meetory.common.exception;

import org.springframework.http.HttpStatus;



public enum ErrorCode {
	
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 안습니다"),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다"),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다"),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다"),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
	ALEADY_LOGGED_OUT(HttpStatus.UNAUTHORIZED, "이미 로그아웃된 토큰입니다"),
	BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다"),
	FORBIDDEN_ACTION(HttpStatus.FORBIDDEN, "해당 작업에 대한 권한이 없습니다"),
	//프로필 관리 관련 추가
	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다"),
	SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "기존 비밀번호와 동일합니다");

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
