package com.meetory.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 안습니다"),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다"),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다"),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다"),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
	ALEADY_LOGGED_OUT(HttpStatus.UNAUTHORIZED, "이미 로그아웃된 토큰입니다"),

	TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 모임입니다"),
	SELF_APPLY_NOT_ALLOWED(HttpStatus.FORBIDDEN, "자신이 개설한 모임에는 신청할 수 없습니다"),
	TEAM_NOT_RECRUITING(HttpStatus.CONFLICT, "모집이 마감된 모임입니다"),
	ALREADY_APPLIED(HttpStatus.CONFLICT, "이미 신청한 모임입니다"),
	TEAM_FULL(HttpStatus.CONFLICT, "모집 정원이 가득 찼습니다"),
	NOT_TEAM_LEADER(HttpStatus.FORBIDDEN, "리더만 처리할 수 있습니다"),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 신청 내역입니다"),
	APPLICATION_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 신청입니다"),

	LEADER_CANNOT_LEAVE(HttpStatus.FORBIDDEN, "리더는 모임을 탈퇴할 수 없습니다"),
	NOT_TEAM_MEMBER(HttpStatus.FORBIDDEN, "모임 멤버만 조회할 수 있습니다"),

	// ---- 쪽지(문의하기) ----
	THREAD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 쪽지함입니다"),
	NOT_THREAD_PARTICIPANT(HttpStatus.FORBIDDEN, "쪽지함에 접근할 권한이 없습니다"),
	SELF_INQUIRY_NOT_ALLOWED(HttpStatus.FORBIDDEN, "자신이 개설한 모임에는 문의할 수 없습니다"),
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
