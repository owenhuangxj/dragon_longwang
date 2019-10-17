package com.trenska.longwang.annotation;

import java.lang.annotation.*;

/**
 * 2019/8/13
 * 创建人:Owen
 * 权限验证注解->标记方法需要做数据权限验证
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataAuthVerification {
}
