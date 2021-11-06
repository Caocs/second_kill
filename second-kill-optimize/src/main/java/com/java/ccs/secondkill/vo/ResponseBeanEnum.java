package com.java.ccs.secondkill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author caocs
 * @date 2021/10/22
 * 公共返回对象枚举
 */
@Getter
@ToString
@AllArgsConstructor
public enum ResponseBeanEnum {

    SUCCESS(200, "Success"),
    ERROR(500, "服务异常"),
    BIND_ERROR(500200, "绑定异常"),
    LOGIN_ERROR(500210, "用户名或密码不正确"),
    MOBILE_ERROR(500211, "手机号不正确"),
    STOCK_EMPTY_ERROR(500500, "库存不足"),
    ORDER_REPEAT_ERROR(500501, "每人限购一件"),
    ACCESS_LIMIT_ERROR(500503, "访问过于频繁"),
    CAPTCHA_ERROR(500502, "验证码错误");
    private final Integer code;
    private final String message;

}
