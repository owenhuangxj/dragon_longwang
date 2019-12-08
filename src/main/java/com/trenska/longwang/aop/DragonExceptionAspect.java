package com.trenska.longwang.aop;

import com.trenska.longwang.exception.ServiceException;
import com.trenska.longwang.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * 2019/12/8
 * 创建人:Owen
 */
@Slf4j
@Aspect
@Component
public class DragonExceptionAspect {
	@AfterThrowing(value = "execution(* com.trenska.longwang.*.*(..))",throwing = "ex")
	public void afterThrowing(ServiceException ex) throws IOException {
		ResponseUtil.accessDenied(ex.getHttpCode(),ex.getMessage(),ex.getReason());
	}
}
