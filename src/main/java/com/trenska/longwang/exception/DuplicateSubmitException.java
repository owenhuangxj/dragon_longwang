package com.trenska.longwang.exception;

/**
 * 2019/7/24
 * 创建人:Owen
 */
public class DuplicateSubmitException extends RuntimeException {
	public DuplicateSubmitException(int httpStatus, String reason, String message) {
		super(message);
	}
}
