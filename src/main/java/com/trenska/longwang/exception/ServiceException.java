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
	/**
	 * 错误码
	 */
	private int httpCode;
	/**
	 * 原因
	 */
	private String reason;
	/**
	 * 错误消息
	 */
	private String message;
	/**
	 * 错误码，用于定位异常位置，格式为 服务.功能模块.接口
	 */
	private String errorCode;


}
