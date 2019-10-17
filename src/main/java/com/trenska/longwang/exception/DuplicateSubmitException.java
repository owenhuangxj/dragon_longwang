package com.trenska.longwang.exception;

/**
 * 2019/7/24
 * 创建人:Owen
 */
public class DuplicateSubmitException extends RuntimeException {
	public DuplicateSubmitException(String message) {
		super(message);
	}
}
