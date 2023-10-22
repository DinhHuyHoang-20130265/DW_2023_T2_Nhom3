package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    String jdbcUrl = "jdbc:mysql://localhost:3306/xosomn";
    String username = "root";
    String password = "Haomqst01";
    Connection connection;

public Connection Connect(){
    try {
        connection = DriverManager.getConnection(jdbcUrl, username, password);
        System.out.println("Connect success....");
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return connection;
}

//    public static void main(String[] args) {
//        new ConnectDB().Connect();
//    }
}
