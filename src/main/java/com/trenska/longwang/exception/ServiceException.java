package com.trenska.longwang.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 2019/12/8
 * 创建人:Owen
 */
@Getter
@AllArgsConstructor
public class ServiceException extends RuntimeException {
	private int httpCode;
	private String reason;
	private String message;
	private String errorCode;
}
