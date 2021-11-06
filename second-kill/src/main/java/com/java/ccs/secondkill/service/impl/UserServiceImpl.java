package com.java.ccs.secondkill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.ccs.secondkill.exception.GlobalException;
import com.java.ccs.secondkill.mapper.UserMapper;
import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.service.IUserService;
import com.java.ccs.secondkill.util.CookieUtil;
import com.java.ccs.secondkill.util.MD5Util;
import com.java.ccs.secondkill.util.UuidUtil;
import com.java.ccs.secondkill.vo.LoginVo;
import com.java.ccs.secondkill.vo.ResponseBean;
import com.java.ccs.secondkill.vo.ResponseBeanEnum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ccs
 * @since 2021-10-21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private UserMapper userMapper;
    private RedisTemplate<String, Object> redisTemplate;

    public UserServiceImpl(UserMapper userMapper, RedisTemplate<String, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ResponseBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        // 通过注解的方式进行校验并通过统一异常处理。
//        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)) {
//            return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
//        }
//        if (!ValidatorUtil.isMobile(mobile)) {
//            return ResponseBean.error(ResponseBeanEnum.MOBILE_ERROR);
//        }

        User user = userMapper.selectById(mobile);
        if (null == user) {
            // 把异常统一抛给GlobalExceptionHandler处理。
            // return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
            throw new GlobalException(ResponseBeanEnum.LOGIN_ERROR);
        }
        // 使用salt再次加密。
        String passwordEncrypted = MD5Util.formPassToDBPass(password, user.getSalt());
        if (!user.getPassword().equals(passwordEncrypted)) {
            // return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
            throw new GlobalException(ResponseBeanEnum.LOGIN_ERROR);
        }
        // 生成cookie(在Cookie中存放ticket，然后把用户信息根据ticket存放到redis中)
        String ticket = UuidUtil.uuid();
        redisTemplate.opsForValue().set("user" + ticket, user);
        CookieUtil.setCookie(response, "userTicket", ticket);
        return ResponseBean.success(ticket);
    }

    @Override
    public User getUserByCookie(String userTicket, HttpServletResponse response) {
        if (StringUtils.isEmpty(userTicket)) {
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user" + userTicket);
        if (user != null) {
            CookieUtil.setCookie(response, "userTicket", userTicket);
        }
        return user;
    }


}
