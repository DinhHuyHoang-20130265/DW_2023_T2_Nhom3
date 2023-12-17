package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DBProperties {
    private static final Properties prop = new Properties();

    // 1. Đọc file config.proerties
    static {
        try {
            FileInputStream in = new FileInputStream("D:\\config.properties");
            prop.load(in);
            in.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
            Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR : Cant read the config file", "<h3 style=\"color: red\">" + ioException + "</h3>", MailConfig.MAIL_HTML);

        }
    }

    public static String getDbHost() {
        return prop.get("db.host").toString();
    }

    public static String getDbPort() {
        return prop.get("db.port").toString();
    }

    public static String getUsername() {
        return prop.get("db.username").toString();
    }

    public static String getPassword() {
        return prop.get("db.password").toString();
    }

    public static String getJdbcUrl() {
        return prop.get("db.jdbcUrl").toString();
    }

    public static String getRun() {
        return prop.get("run").toString();
    }

    public static void main(String[] args) {
        System.out.println(getRun());
    }
}
