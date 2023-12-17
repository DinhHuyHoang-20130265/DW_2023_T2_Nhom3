package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnect {
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // 2. Đọc file config.properties
                connection = DriverManager.getConnection(DBProperties.getJdbcUrl(), DBProperties.getUsername(), DBProperties.getPassword());
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Không thể thiết lập kết nối đến cơ sở dữ liệu.");
            }
        }
        return connection;
    }
    // 3. Lấy ra dòng có flag = 1
    public static List<DataFilesConfigs> getConfigurationsWithFlagOne(Connection connection) {
        List<DataFilesConfigs> configurations = new ArrayList<>();

        String query = "SELECT id,name,description,source_path,location, flag, isRun FROM db_controls.data_files_configs WHERE flag = 1";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("id");
                String description = resultSet.getString("description");
                String source_path = resultSet.getString("source_path");
                String location = resultSet.getString("location");
                int flag = resultSet.getInt("flag");
                int isRun = resultSet.getInt("isRun");
                configurations.add(new DataFilesConfigs(id, name, description, source_path, location, flag, isRun));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return configurations;
    }


    public static String getStatus(Connection connection, int idConfig) {
        String status = "";
        // 4. Lấy 1 dòng join với db_controls.data_files
        String query = "SELECT `status` FROM db_controls.data_files WHERE df_config_id=? AND status not like '%ERROR%' ORDER BY file_timestamp DESC , data_files.id DESC LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, idConfig);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    status = resultSet.getString("status");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return status;
    }

    public static void switchDatabase(Connection connection, String databaseName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String sql = "USE " + databaseName;
            statement.execute(sql);
        }
    }

    public static void insertStatusAndName(Connection connection, int id, String name, String status) {
        try (CallableStatement callableStatement = connection.prepareCall("{CALL InsertStatusdAndName(?,?,?)}")) {
            callableStatement.setInt(1, id);
            callableStatement.setString(2, name);
            callableStatement.setString(3, status);
            callableStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertStatus(Connection connection, int id, String status) {
        try (CallableStatement callableStatement = connection.prepareCall("{CALL InsertStatus(?,?)}")) {
            callableStatement.setInt(1, id);
            callableStatement.setString(2, status);
            callableStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    // 6. Cập nhật isRun = 1
    public static void setIsRun(Connection connection, int id, int i) {
        try (CallableStatement callableStatement = connection.prepareCall("{CALL updateIsRun(?,?)}")) {
            callableStatement.setInt(1, id);
            callableStatement.setInt(2, i);
            callableStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        setIsRun(getConnection(), 1, 1);
    }
}
