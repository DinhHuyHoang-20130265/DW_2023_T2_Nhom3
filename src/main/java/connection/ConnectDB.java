package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    static String jdbcUrl = "jdbc:mysql://34.126.154.231:3306/warehouse";
    static String username = "root";
    static String password = "1";
    public static Connection connection;

    public static Connection Connect() {
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void main(String[] args) {
        Connect();
    }
}
