package com.trenska.longwang.annotation;

import java.lang.annotation.*;

/**
 * 操作日志标记接口
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ActionLog {
    String type() default "";
    String content() default "";
}
