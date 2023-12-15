package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    static String jdbcUrl = "jdbc:mysql://localhost:3306/data_mart";
    static String username = "root";
    static String password = "";
    public static Connection connection;

    public static Connection Connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public static void main(String[] args) {
        Connect();
    }
}
