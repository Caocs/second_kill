package com.java.ccs.secondkill.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.vo.ResponseBean;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author caocs
 * @date 2021/10/27
 */
public class UserCreateUtil {

    public static void main(String[] args) throws Exception {
        // 创建Users
        List<User> users = getUsers(1000);
        //插入数据库
        insertUsersIntoDb(users);
        //登录，生成token
        writeIntoFile(users);
    }

    private static void writeIntoFile(List<User> users) throws IOException {
        String urlString = "http://localhost:8080/login/doLogin";
        File file = new File("D:/tokens.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        file.createNewFile();
        randomAccessFile.seek(0);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            OutputStream out = httpURLConnection.getOutputStream();
            String params = "mobile=" + user.getId() + "&password=" + MD5Util.inputPassToFormPass("123456");
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = httpURLConnection.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte buff[] = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0, len);
            }
            inputStream.close();
            bout.close();
            String response = new String(bout.toByteArray());
            ObjectMapper mapper = new ObjectMapper();
            ResponseBean responseBean = mapper.readValue(response, ResponseBean.class);
            String userTicket = (String) responseBean.getObj();
            // 写入文件
            String row = user.getId() + "," + userTicket;
            randomAccessFile.seek(randomAccessFile.length());
            randomAccessFile.write(row.getBytes());
            randomAccessFile.write("\r\n".getBytes());
            System.out.println("write to file : " + row);
        }
        randomAccessFile.close();

        System.out.println("over");
    }

    private static List<User> getUsers(int count) {
        List<User> users = new ArrayList<User>(count);
        //生成用户
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId(13000000000L + i);
            user.setNickname("user" + i);
            user.setSalt("1a2b3c4d");
            user.setPassword(MD5Util.inputPassToDBPass("123456", user.getSalt()));
            user.setRegisterDate(new Date());
            user.setLoginCount(1);
            users.add(user);
        }
        System.out.println("create user");
        return users;
    }

    private static void insertUsersIntoDb(List<User> users) throws Exception {
        Connection connection = DbConnUtil.getConn();
        String sql = "insert into t_user(login_count, nickname, register_date, salt, password, id)values(?,?,?,?,?,?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            pstmt.setInt(1, user.getLoginCount());
            pstmt.setString(2, user.getNickname());
            pstmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
            pstmt.setString(4, user.getSalt());
            pstmt.setString(5, user.getPassword());
            pstmt.setLong(6, user.getId());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        pstmt.close();
        connection.close();
        System.out.println("insert to db");
    }


}
