package com.java.ccs.secondkill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author caocs
 * @date 2021/10/22
 * 公共返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBean {
    private long code;
    private String message;
    private Object obj;

    public static ResponseBean success(Object obj) {
        return new ResponseBean(ResponseBeanEnum.SUCCESS.getCode(), ResponseBeanEnum.SUCCESS.getMessage(), obj);
    }

    public static ResponseBean success() {
        return new ResponseBean(ResponseBeanEnum.SUCCESS.getCode(), ResponseBeanEnum.SUCCESS.getMessage(), null);
    }

    public static ResponseBean error(ResponseBeanEnum errorEnum, Object obj) {
        return new ResponseBean(errorEnum.getCode(), errorEnum.getMessage(), obj);
    }

    public static ResponseBean error(ResponseBeanEnum errorEnum) {
        return new ResponseBean(errorEnum.getCode(), errorEnum.getMessage(), null);
    }
}
