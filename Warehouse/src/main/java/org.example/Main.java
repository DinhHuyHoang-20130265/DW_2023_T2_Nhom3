package org.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<DataFilesConfigs> configs;
        try (Connection connection = DBConnect.getConnection()) {
            configs = DBConnect.getConfigurationsWithFlagOne(connection);
            for (DataFilesConfigs config : configs) {
                if (config.getIsRun() == 0) {
                    String status = DBConnect.getStatus(connection, config.getId());
                    DBConnect.setIsRun(connection, config.getId(), 1);
                    switch (status) {
                        case "FINISHED":
                        case "CRAWLING":
                            if (!Modules.startCrawl(config.getSource_path(), config.getLocation(), config.getId(), connection, DBProperties.getRun()))
                                break;
                            if (!Modules.startExtractToStaging(config.getId(), connection, config.getLocation(), DBProperties.getRun()))
                                break;
                            if (!Modules.Transform(config.getId(), connection))
                                break;
                            if (!Modules.LoadToWarehouse(config.getId(), connection))
                                break;
                            if (!Modules.Aggregate(config.getId(), connection))
                                break;
                            if (!Modules.LoadToDataMart(config.getId(), connection))
                                break;
                            break;
                        case "EXTRACTING":
                            if (!Modules.startExtractToStaging(config.getId(), connection, config.getLocation(), DBProperties.getRun()))
                                break;
                            if (!Modules.Transform(config.getId(), connection))
                                break;
                            if (!Modules.LoadToWarehouse(config.getId(), connection))
                                break;
                            if (!Modules.Aggregate(config.getId(), connection))
                                break;
                            if (!Modules.LoadToDataMart(config.getId(), connection))
                                break;
                            break;
                        case "TRANSFORMING":
                            if (!Modules.Transform(config.getId(), connection))
                                break;
                            if (!Modules.LoadToWarehouse(config.getId(), connection))
                                break;
                            if (!Modules.Aggregate(config.getId(), connection))
                                break;
                            if (!Modules.LoadToDataMart(config.getId(), connection))
                                break;
                            break;
                        case "LOADINGWH":
                            if (!Modules.LoadToWarehouse(config.getId(), connection))
                                break;
                            if (!Modules.Aggregate(config.getId(), connection))
                                break;
                            if (!Modules.LoadToDataMart(config.getId(), connection))
                                break;
                            break;
                        case "AGGREGATING":
                            if (!Modules.Aggregate(config.getId(), connection))
                                break;
                            if (!Modules.LoadToDataMart(config.getId(), connection))
                                break;
                            break;
                        case "MLOADING":
                            if (!Modules.Aggregate(config.getId(), connection))
                                break;
                            break;
                        default:
                            break;
                    }
                }
                DBConnect.setIsRun(connection, config.getId(), 0);
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR CRAWLER: Cant connect to Databases", "<h3 style=\"color: red\">" + e + "</h3>", MailConfig.MAIL_HTML);
        }
    }
}