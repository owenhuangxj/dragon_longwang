package com.trenska.longwang.aop;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.util.HttpUtil;
import com.trenska.longwang.util.ResponseUtil;
import com.trenska.longwang.util.SysUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
	public static final List<String> UPDATE_METHODS = Arrays.asList(RequestMethod.POST.name(),
			RequestMethod.DELETE.name(), RequestMethod.PUT.name());
	private static final Cache<String, Object> CACHES = CacheBuilder.newBuilder()
			.maximumSize(1000)// 最大缓存 1000 个
			.expireAfterWrite(2, TimeUnit.SECONDS)// 设置缓存过期时间,单位:秒
			.build();

	@Before("@annotation(checkDuplicateSubmit)")
	public void interceptor(JoinPoint pjp, CheckDuplicateSubmit checkDuplicateSubmit) throws IOException {
		String key = this.makeDuplicateTokenKey(pjp);
		if (UPDATE_METHODS.contains(HttpUtil.getHttpMethod())) {
			if (CACHES.getIfPresent(key) != null) {
				log.info("submit too often,the method is {}", HttpUtil.getHttpMethod());
				ResponseUtil.accessDenied(HttpServletResponse.SC_BAD_REQUEST, "Please do not submit too " +
						"often,if the server is blocked,try again several seconds later", "submit too often");
			} else {
				// 如果是第一次请求,就将key存入缓存中
				String value = UUID.randomUUID().toString();
				CACHES.put(key, value);
			}
		}
	}

	/**
	 * 获取重复提交key
	 *
	 * @param joinPoint
	 * @return
	 */
	private String makeDuplicateTokenKey(JoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().getName();
		StringBuilder key = new StringBuilder(DUPLICATE_TOKEN_KEY);
		key.append(DragonConstant.SPLITTER).append(methodName).append(SysUtil.getEmpIdInToken());
		return key.toString();
	}
}