package com.java.ccs.secondkill.vo;

import com.java.ccs.secondkill.validator.IsMobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author caocs
 * @date 2021/10/22
 * 登录参数
 */
@Data
public class LoginVo {

    @NotNull
    @IsMobile // 自定义校验注解
    private String mobile;

    @NotNull
    @Length(min = 32)
    private String password;

}
