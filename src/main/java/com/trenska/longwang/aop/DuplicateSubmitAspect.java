package com.trenska.longwang.aop;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.trenska.longwang.annotation.DuplicateSubmitToken;
import com.trenska.longwang.exception.ServiceException;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 防止重复提交的切面
 */
@Slf4j
@Aspect
@Component
public class DuplicateSubmitAspect {
	public static final String DUPLICATE_TOKEN_KEY = "duplicate_token_key";
	private static final Cache<String, Object> CACHES = CacheBuilder.newBuilder()
			.maximumSize(100)// 最大缓存 100 个
			.expireAfterWrite(10, TimeUnit.SECONDS)// 设置缓存过期时间,单位:秒
			.build();

	@Before("@annotation(token)")
	public void interceptor(JoinPoint pjp, DuplicateSubmitToken token) throws IOException {
		String key = this.getDuplicateTokenKey(pjp);
		if (CACHES.getIfPresent(key) != null) {
			ResponseUtil.accessDenied(HttpServletResponse.SC_BAD_REQUEST, "Please do not submit too often,if the server is blocked,try again 10 seconds later", "");
		}else{
			// 如果是第一次请求,就将key存入缓存中
			String value = UUID.randomUUID().toString();
			CACHES.put(key, value);
		}
	}

	/**
	 * 获取重复提交key
	 *
	 * @param joinPoint
	 * @return
	 */
	private String getDuplicateTokenKey(JoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().getName();
		StringBuilder key = new StringBuilder(DUPLICATE_TOKEN_KEY);
		key.append("::").append(methodName);
		return key.toString();
	}

}