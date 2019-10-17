package com.trenska.longwang.exception;

import javax.servlet.UnavailableException;

/**
 * 2019/5/22
 * 创建人:Owen
 */
public class LoginTimeoutException extends UnavailableException {
	public LoginTimeoutException(String message) {
		super(message);
	}
}
