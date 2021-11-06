package com.java.ccs.secondkill.controller;

import com.java.ccs.secondkill.service.IUserService;
import com.java.ccs.secondkill.vo.LoginVo;
import com.java.ccs.secondkill.vo.ResponseBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @author caocs
 * @date 2021/10/21
 */
@Controller // 页面跳转不能使用RestController
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    IUserService userService;

    /**
     * 跳转到登录页面
     */
    @RequestMapping("/toLogin")
    public String toLogin() {
        return "login";
    }

    /**
     * 测试：手机号：15261138052，明文密码：123456，二次加密密码：b7797cce01b4b131b433b6acf4add449，salt：1a2b3c4d
     * 使用 @Valid 进行参数校验，在每个属性上设置校验类型。
     */
    @RequestMapping("/doLogin")
    @ResponseBody
    public ResponseBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        return userService.doLogin(loginVo, request, response);
    }

}
