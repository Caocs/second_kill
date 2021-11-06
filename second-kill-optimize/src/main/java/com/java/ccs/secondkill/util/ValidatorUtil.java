package com.java.ccs.secondkill.util;


import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author caocs
 * @date 2021/10/22
 * 数据校验工具类
 */
public class ValidatorUtil {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("[1]([3-9])[0-9]{9}$");

    public static boolean isMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return false;
        }
        Matcher matcher = MOBILE_PATTERN.matcher(mobile);
        return matcher.matches();
    }

}
