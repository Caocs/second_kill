package com.java.ccs.secondkill.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author caocs
 * @date 2021/10/21
 */
public class MD5Util {

    private static final String SALT = "1a2b3c4d";

    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    /**
     * @param inputPass 用户输入的明文密码
     * @return 第一次加密
     */
    public static String inputPassToFormPass(String inputPass) {
        String str = "" + SALT.charAt(0) + SALT.charAt(2) + inputPass + SALT.charAt(5) + SALT.charAt(4);
        return md5(str);
    }

    /**
     * @param formPass 第一次加密后的密码
     * @param salt     随机的salt
     * @return 第二次加密
     */
    public static String formPassToDBPass(String formPass, String salt) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    /**
     * 真正给程序流程调用的方法（二次加密）
     *
     * @param inputPass 用户输入的明文密码
     * @param salt      随机的salt
     * @return 二次加密后的结果
     */
    public static String inputPassToDBPass(String inputPass, String salt) {
        String formPass = inputPassToFormPass(inputPass);
        return formPassToDBPass(formPass, salt);
    }

    public static void main(String[] args) {
        System.out.println(inputPassToDBPass("123456","1a2b3c4d"));
    }

}
