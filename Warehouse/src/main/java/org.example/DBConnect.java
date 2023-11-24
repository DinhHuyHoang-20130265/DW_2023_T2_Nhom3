package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnect {
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DBProperties.getJdbcUrl(), DBProperties.getUsername(), DBProperties.getPassword());
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Không thể thiết lập kết nối đến cơ sở dữ liệu.");
            }
        }
        return connection;
    }

    public static void main(String[] args) {
        getConnection();
    }
}