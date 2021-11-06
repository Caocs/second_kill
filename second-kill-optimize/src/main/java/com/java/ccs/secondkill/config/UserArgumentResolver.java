package com.java.ccs.secondkill.config;

import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.service.IUserService;
import com.java.ccs.secondkill.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author caocs
 * @date 2021/10/23
 */
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    IUserService userService;

    /**
     * 条件判断，如果满足条件，才会执行下面的resolveArgument()。
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz = parameter.getParameterType();
        return clazz == User.class;
    }

    /**
     * 拦截请求，判断User是否存在。
     * 对所有包含User参数的请求，进行数据校验拦截。
     * 这样就不用在每个request请求方法内部实现数据校验等重复工作。
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
//        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
//        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
//        String ticket = CookieUtil.getCookieValue(request, "userTicket");
//        if (StringUtils.isEmpty(ticket)) {
//            return null;
//        }
//        return userService.getUserByCookie(ticket, response);

        return UserContext.getUser();
    }
}
