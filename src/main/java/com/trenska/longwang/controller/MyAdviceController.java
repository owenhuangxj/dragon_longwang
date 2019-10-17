package com.trenska.longwang.controller;

import com.trenska.longwang.exception.AccountDuplicatedException;
import com.trenska.longwang.model.sys.ResponseModel;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.sasl.AuthenticationException;
import javax.servlet.ServletException;
import javax.validation.UnexpectedTypeException;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class MyAdviceController {

    @ExceptionHandler(AuthorizationException.class)
    public ResponseModel unAuthorized(AuthorizationException ex ){
        return ResponseModel.getInstance().succ(false).msg(ex.getMessage()).reason("unauthorized");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseModel unAuthenticated(AuthenticationException ex ){
        return ResponseModel.getInstance().succ(false).msg(ex.getMessage()).data("{\"reason\":\"unauthenticated\"}");
    }
    @ExceptionHandler({UnknownAccountException.class,IncorrectCredentialsException.class})
    public ResponseModel unknownAccountException(){
        return ResponseModel.getInstance().succ(false).msg("用户名或者密码错误").data("{\"reason\":\"unauthenticated\"}");
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseModel duplicated(SQLIntegrityConstraintViolationException ex){
        ex.printStackTrace();
        return ResponseModel.getInstance().succ(false).msg(ex.getMessage()).data("{\"reason\":\"sql\"}");
    }

    @ExceptionHandler(ServletException.class)
    public ResponseModel accessTimeout(ServletException ex){
        return ResponseModel.getInstance().succ(false).msg(ex.getMessage().concat("，请重新登陆"));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseModel methodArgumentNotValidException(MethodArgumentNotValidException ex){
        return ResponseModel.getInstance().succ(false).msg(ex.getMessage());
    }

    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseModel unexpectedTypeException(UnexpectedTypeException ex){
        return ResponseModel.getInstance().succ(false).msg(ex.getMessage());
    }

    @ExceptionHandler(AccountDuplicatedException.class)
    public ResponseModel accountDuplicatedException(AccountDuplicatedException ex){
        return ResponseModel.getInstance().succ(false).msg(ex.getMessage());
    }
}