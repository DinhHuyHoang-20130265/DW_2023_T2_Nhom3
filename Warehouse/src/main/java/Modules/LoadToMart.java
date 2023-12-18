package Modules;

import org.example.DBConnect;
import org.example.DataFilesConfigs;
import org.example.Mail;
import org.example.MailConfig;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class LoadToMart {
    public static boolean LoadToDataMart(int id, Connection connection) throws SQLException {
        try {
            DBConnect.insertStatus(connection, id, "MLOADING");
            String sql = "CALL LoadToMart()";
            CallableStatement statement = connection.prepareCall(sql);
            statement.execute();
            DBConnect.insertStatus(connection, id, "MLOADED");
            DBConnect.insertStatus(connection, id, "FINISHED");
        } catch (SQLException e) {
            DBConnect.insertStatusAndName(connection, id, "Failed to LoadToWarehouse: " + e, "ERROR");
            Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR LoadToWarehouse", "<h3 style=\"color: red\">" + e + "</h3>", MailConfig.MAIL_HTML);
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        try (Connection connection = DBConnect.getConnection()) {
            String status = DBConnect.getLatesStatusWithNoIdConfig(connection, 0);
            DataFilesConfigs config = DBConnect.getConfigHaveLatesStatus(connection);
            if (config.getIsRun() == 0) {
                DBConnect.setIsRun(connection, config.getId(), 1);
                if (status.equals("WLOADED"))
                    LoadToDataMart(config.getId(), connection);
                DBConnect.insertStatusAndName(connection, config.getId(), "FINISHED SINGLE PROCESS", "FINISHED");
                DBConnect.setIsRun(connection, config.getId(), 0);
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR CRAWLER: Cant connect to Databases", "<h3 style=\"color: red\">" + e + "</h3>", MailConfig.MAIL_HTML);
        }
    }
}
