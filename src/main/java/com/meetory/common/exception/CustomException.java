package com.meetory.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

	private ErrorCode errorCode;
	
	public CustomException(ErrorCode errorCode) {
		super(errorCode.getmessage());
		this.errorCode = errorCode;
	}

}
