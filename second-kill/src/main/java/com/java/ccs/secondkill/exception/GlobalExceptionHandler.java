package com.java.ccs.secondkill.exception;

import com.java.ccs.secondkill.vo.ResponseBean;
import com.java.ccs.secondkill.vo.ResponseBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author caocs
 * @date 2021/10/22
 * 全局异常处理类
 * <p>
 * Springboot统一异常处理有两种：
 * 1、@ControllerAdvice+@ExceptionHandler注解 -> 只能处理控制器抛出的异常(此时请求已经进入控制器中)
 * 2、使用ErrorController类 -> 可以处理所有的异常(包括未进入控制器的错误，比如404,401等错误)
 * 注意：
 * 如果应用中两者共同存在，则@ControllerAdvice方式处理控制器抛出的异常，类ErrorController方式未进入控制器的异常。
 * <p>
 * 使用@RestControllerAdvice，相当于@ControllerAdvice+@ResponseBody了，这样就不用了在方法上加@ResponseBody注解了。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseBean ExceptionHandler(Exception e) {
        if (e instanceof GlobalException) {
            // 捕获程序中定义的GlobalException（例如UserServiceImpl）
            GlobalException ex = (GlobalException) e;
            return ResponseBean.error(ex.getErrorEnum());
        } else if (e instanceof BindException) {
            // 使用springboot-validation校验参数绑定，如果异常则会抛出BindException，对该异常捕获并处理。
            BindException ex = (BindException) e;
            ResponseBean responseBean = ResponseBean.error(ResponseBeanEnum.BIND_ERROR);
            responseBean.setMessage("参数校验异常:" + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return responseBean;
        }
        return ResponseBean.error(ResponseBeanEnum.ERROR);
    }


}
