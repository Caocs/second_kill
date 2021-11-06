package com.java.ccs.secondkill.util;

import ch.qos.logback.core.db.dialect.DBUtil;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * @author caocs
 * @date 2021/10/27
 */
public class DbConnUtil {

    private static Properties props = new Properties();

    static {
        try {
            InputStream in = DBUtil.class.getClassLoader()
                    .getResourceAsStream("application.properties");
            props.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过JDBC获取数据库连接
     */
    public static Connection getConn() throws Exception {
        String url = props.getProperty("spring.datasource.url");
        String username = props.getProperty("spring.datasource.username");
        String password = props.getProperty("spring.datasource.password");
        String driver = props.getProperty("spring.datasource.driver-class-name");
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }

}
