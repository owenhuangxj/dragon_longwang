package com.trenska.longwang.aop;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.trenska.longwang.annotation.DuplicateSubmitToken;
import com.trenska.longwang.model.sys.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 防止重复提交的切面
 */
@Slf4j
@Aspect
@Component
public class SubmitAspect {
    public static final String DUPLICATE_TOKEN_KEY = "duplicate_token_key";
    private static final Cache<String, Object> CACHES = CacheBuilder.newBuilder()
            .maximumSize(100)// 最大缓存 100 个
            .expireAfterWrite(10, TimeUnit.SECONDS)// 设置缓存过期时间,单位:秒
            .build();

    @Pointcut("execution(public * com.trenska.longwang.controller..*(..))")
    public void duplicateAspect() {
    }

    @Around("duplicateAspect() && @annotation(token)")
    public Object interceptor(ProceedingJoinPoint pjp, DuplicateSubmitToken token) {

        //MethodSignature signature = (MethodSignature) pjp.getSignature();
        //Method method = signature.getMethod();
        String key = this.getDuplicateTokenKey(pjp);
        if (StringUtils.isNotEmpty(key)) {
            if (CACHES.getIfPresent(key) != null) {
                return ResponseModel.getInstance().succ(false).msg("请勿重复请求").code(405).data(null);
            }
            // 如果是第一次请求,就将key存入缓存中
            String value = UUID.randomUUID().toString();
            CACHES.put(key, value);
        }
        try {
            return pjp.proceed();
        } catch (Throwable throwable) {
        	throwable.printStackTrace();
            throw new RuntimeException("异常 : ".concat(throwable.getClass().getName()));
        } finally {
            CACHES.invalidate(key); // 方法执行完成(return 之后)清除缓存
        }
    }

	/**
	 * 获取重复提交key
	 * @param joinPoint
	 * @return
	 */
	public String getDuplicateTokenKey(JoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().getName();
		StringBuilder key = new StringBuilder(DUPLICATE_TOKEN_KEY);
		key.append("::").append(methodName);
		return key.toString();
	}

}