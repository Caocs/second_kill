package com.java.ccs.secondkill.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.service.IUserService;
import com.java.ccs.secondkill.util.CookieUtil;
import com.java.ccs.secondkill.vo.ResponseBean;
import com.java.ccs.secondkill.vo.ResponseBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * @author caocs
 * @date 2021/11/6
 * 使用拦截器，实现对AccessLimit注解的解析执行
 */
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler instanceof HandlerMethod) {
            // 把User放到ThreadLocal中
            User user = this.getUser(request, response);
            UserContext.setUser(user);

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            if(accessLimit==null){
                return true;
            }
            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            // 1.校验用户是否存在
            if (needLogin && user == null) {
                this.renderErrorResponse(response, ResponseBeanEnum.LOGIN_ERROR);
                return false;
            }

            // 2.校验每个人的访问频率
            String url = request.getRequestURI();
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Integer count = (Integer) valueOperations.get(url + ":" + user.getId());
            if (count == null) {
                // 第一次请求
                valueOperations.set(url + ":" + user.getId(), 1, second, TimeUnit.SECONDS);
            } else if (count < maxCount) {
                // 后续每次访问+1
                valueOperations.increment(url + ":" + user.getId());
            } else {
                // 5秒内超过5次，返回“过于频繁”
                this.renderErrorResponse(response, ResponseBeanEnum.ACCESS_LIMIT_ERROR);
                return false;
            }
        }
        return true;
    }

    private User getUser(HttpServletRequest request, HttpServletResponse response) {
        String ticket = CookieUtil.getCookieValue(request, "userTicket");
        if (StringUtils.isEmpty(ticket)) {
            return null;
        }
        return userService.getUserByCookie(ticket, response);
    }

    /**
     * 构建校验错误时的返回值
     */
    private void renderErrorResponse(HttpServletResponse response, ResponseBeanEnum responseBeanEnum) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        ResponseBean responseBean = ResponseBean.error(responseBeanEnum);
        out.write(new ObjectMapper().writeValueAsString(responseBean));
    }

}
