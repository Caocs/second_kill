package com.java.ccs.secondkill.util;

import java.util.UUID;

/**
 * @author caocs
 * @date 2021/10/23
 */
public class UuidUtil {

    public static String uuid(){
        return UUID.randomUUID().toString().replace("-","");
    }

}
