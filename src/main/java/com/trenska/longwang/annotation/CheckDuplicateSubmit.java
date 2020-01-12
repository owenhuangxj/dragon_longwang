package com.trenska.longwang.annotation;

import java.lang.annotation.*;

/**
 * @description 防止表单重复提交注解
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckDuplicateSubmit {
	/**
	 * 一次请求完成之前防止重复提交
	 */
	int REQUEST = 1;
	/**
	 * 一次会话中防止重复提交
	 */
	int SESSION = 2;

	/**
	 * 保存重复提交标记 默认为需要保存
	 */
	boolean save() default true;

	/**
	 * 防止重复提交类型，默认：一次请求完成之前防止重复提交
	 */
	int type() default REQUEST;
}