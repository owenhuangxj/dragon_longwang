package com.trenska.longwang.exception;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 2019/6/27
 * 创建人:Owen
 */
public class AccountDuplicatedException extends SQLIntegrityConstraintViolationException {
	public AccountDuplicatedException(String reason) {
		super(reason);
	}
}
