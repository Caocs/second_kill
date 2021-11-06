package com.java.ccs.secondkill.exception;

import com.java.ccs.secondkill.vo.ResponseBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author caocs
 * @date 2021/10/22
 * 全局异常
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalException extends RuntimeException{


    private ResponseBeanEnum errorEnum;

}
