package com.java.ccs.secondkill.config;

import com.java.ccs.secondkill.pojo.User;

/**
 * @author caocs
 * @date 2021/11/6
 */
public class UserContext {

    private static ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static void setUser(User user) {
        userHolder.set(user);
    }

    public static User getUser() {
        return userHolder.get();
    }
}
