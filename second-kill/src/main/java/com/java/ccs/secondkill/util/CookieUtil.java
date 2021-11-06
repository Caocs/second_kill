package com.java.ccs.secondkill.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @author caocs
 * @date 2021/10/23
 */
public class CookieUtil {

    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookieList = request.getCookies();
        if (cookieList == null || cookieName == null) {
            return null;
        }
        return Arrays.stream(cookieList)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
    }

    public static void setCookie(HttpServletResponse response, String cookieName, String cookieValue) {
        // 创建一个 cookie对象
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7天过期
        cookie.setPath("/");
        //将cookie对象加入response响应
        response.addCookie(cookie);
    }

}
