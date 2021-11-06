package com.java.ccs.secondkill.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能：做访问限制
 * 1.判断在second秒内最大访问次数maxCount次。
 * 2.是否登录校验
 * <p>
 * 实现：
 * 在AccessLimitInterceptor类中，对该注解进行解析执行
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {

    int second();

    int maxCount();

    boolean needLogin() default true;

}
