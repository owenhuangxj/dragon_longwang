package com.trenska.longwang.controller;

import com.trenska.longwang.exception.AccountDuplicatedException;
import com.trenska.longwang.model.sys.CommonResponse;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.security.sasl.AuthenticationException;
import javax.servlet.ServletException;
import javax.validation.UnexpectedTypeException;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class MyAdviceController {
//public class MyAdviceController extends ResponseEntityExceptionHandler {

	@ExceptionHandler(AuthorizationException.class)
	public CommonResponse unAuthorized(AuthorizationException ex) {
		return CommonResponse.getInstance().succ(false).msg(ex.getMessage()).reason("unauthorized");
	}

	@ExceptionHandler(AuthenticationException.class)
	public CommonResponse unAuthenticated(AuthenticationException ex) {
		return CommonResponse.getInstance().succ(false).msg(ex.getMessage()).data("{\"reason\":\"unauthenticated\"}");
	}

	@ExceptionHandler({UnknownAccountException.class, IncorrectCredentialsException.class})
	public CommonResponse unknownAccountException() {
		return CommonResponse.getInstance().succ(false).msg("用户名或者密码错误").data("{\"reason\":\"unauthenticated\"}");
	}

	@ExceptionHandler(SQLIntegrityConstraintViolationException.class)
	public CommonResponse duplicated(SQLIntegrityConstraintViolationException ex) {
		ex.printStackTrace();
		return CommonResponse.getInstance().succ(false).msg(ex.getMessage()).data("{\"reason\":\"sql\"}");
	}

	@ExceptionHandler(ServletException.class)
	public CommonResponse accessTimeout(ServletException ex) {
		return CommonResponse.getInstance().succ(false).msg(ex.getMessage().concat("，请重新登陆"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public CommonResponse methodArgumentNotValidException(MethodArgumentNotValidException ex) {
		return CommonResponse.getInstance().succ(false).msg(ex.getMessage());
	}

	@ExceptionHandler(UnexpectedTypeException.class)
	public CommonResponse unexpectedTypeException(UnexpectedTypeException ex) {
		return CommonResponse.getInstance().succ(false).msg(ex.getMessage());
	}

	@ExceptionHandler(AccountDuplicatedException.class)
	public CommonResponse accountDuplicatedException(AccountDuplicatedException ex) {
		return CommonResponse.getInstance().succ(false).msg(ex.getMessage());
	}
}