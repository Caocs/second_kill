package com.java.ccs.secondkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.vo.LoginVo;
import com.java.ccs.secondkill.vo.ResponseBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ccs
 * @since 2021-10-21
 */
public interface IUserService extends IService<User> {

    ResponseBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    User getUserByCookie(String userTicket, HttpServletResponse response);

}
