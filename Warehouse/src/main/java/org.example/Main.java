package org.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = DBConnect.getConnection()) {
            List<DataFilesConfigs> configs = DBConnect.getConfigurationsWithFlagOne(connection);
            for (DataFilesConfigs config : configs) {
                String status = DBConnect.getStatus(connection, config.getId());
                switch (status) {
                    case "FINISHED":
                    case "CRAWLING":
                        Modules.startCrawl(config.getSource_path(), config.getLocation(), config.getId(), connection);
                        Modules.startExtractToStaging(config.getId(), connection, config.getLocation());
                        Modules.Transform(config.getId(), connection);
                        Modules.LoadToWarehouse(config.getId(), connection);
                        Modules.Aggregate(config.getId(), connection);
                        Modules.LoadToDataMart(config.getId(), connection);
                        break;
                    case "EXTRACTING":
                        Modules.startExtractToStaging(config.getId(), connection, config.getLocation());
                        Modules.Transform(config.getId(), connection);
                        Modules.LoadToWarehouse(config.getId(), connection);
                        Modules.Aggregate(config.getId(), connection);
                        Modules.LoadToDataMart(config.getId(), connection);
                        break;
                    case "TRANSFORMING":
                        Modules.Transform(config.getId(), connection);
                        Modules.LoadToWarehouse(config.getId(), connection);
                        Modules.Aggregate(config.getId(), connection);
                        Modules.LoadToDataMart(config.getId(), connection);
                        break;
                    case "LOADINGWH":
                        Modules.LoadToWarehouse(config.getId(), connection);
                        Modules.Aggregate(config.getId(), connection);
                        Modules.LoadToDataMart(config.getId(), connection);
                        break;
                    case "AGGREGATING":
                        Modules.Aggregate(config.getId(), connection);
                        Modules.LoadToDataMart(config.getId(), connection);
                        break;
                    case "MLOADING":
                        Modules.LoadToDataMart(config.getId(), connection);
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR CRAWLER", "<h3 style=\"color: red\">" + e + "</h3>", MailConfig.MAIL_HTML);
        }
    }
}
