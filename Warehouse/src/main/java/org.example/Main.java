package org.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<DataFilesConfigs> configs;
        // 1. Kết nối db_control và 2. Đọc file config.properties
        try (Connection connection = DBConnect.getConnection()) {
            // 3. Lấy ra dòng có flag = 1
            configs = DBConnect.getConfigurationsWithFlagOne(connection);
            for (DataFilesConfigs config : configs) {
                // 3.1 kiểm tra isRun=0
                if (config.getIsRun() == 0) {
                    // 4. Lấy 1 dòng join với db_controls.data_files và 5. Lấy ra dòng dữ liệu data_files có file_timestamp mới nhất
                    String status = DBConnect.getStatus(connection, config.getId());
                    // 6. Cập nhật isRun = 1
                    DBConnect.setIsRun(connection, config.getId(), 1);
                    switch (status) {
                        // 7. status=Fisnished, 15. status=Crawling
                        case "FINISHED", "CRAWLING":
                            // 8. Crawl data
                            if (!Modules.startCrawl(config.getSource_path(), config.getLocation(), config.getId(), connection, DBProperties.getRun()))
                                break;
                            // 9. Extract file -> staging
                            if (!Modules.startExtractToStaging(config.getId(), connection, config.getLocation(), DBProperties.getRun()))
                                break;
                            // 10. Transform data sang các surrogate keys
                            if (!Modules.Transform(config.getId(), connection))
                                break;
                            // 11. Load data sang warehouse
                            if (!Modules.LoadToWarehouse(config.getId(), connection))
                                break;
                            // 12. Aggregate data
                            if (!Modules.Aggregate(config.getId(), connection))
                                break;
                            // 13. Load data aggregates sang data_mart.ketquaxs_results_mb, data_mart.ketquaxs_results_mt và data_mart.ketquaxs_results_mn
                            if (!Modules.LoadToDataMart(config.getId(), connection))
                                break;
                            break;
                        // 16. status = EXTRACTING ?
                        case "EXTRACTING":
                            // 9. Extract file -> staging
                            if (!Modules.startExtractToStaging(config.getId(), connection, config.getLocation(), DBProperties.getRun()))
                                break;
                            // 10. Transform data sang các surrogate keys
                            if (!Modules.Transform(config.getId(), connection))
                                break;
                            // 11. Load data sang warehouse
                            if (!Modules.LoadToWarehouse(config.getId(), connection))
                                break;
                            // 12. Aggregate data
                            if (!Modules.Aggregate(config.getId(), connection))
                                break;
                            // 13. Load data aggregates sang data_mart.ketquaxs_results_mb, data_mart.ketquaxs_results_mt và data_mart.ketquaxs_results_mn
                            if (!Modules.LoadToDataMart(config.getId(), connection))
                                break;
                            break;
                        // 17. status = TRANSFORMING ?
                        case "TRANSFORMING":
                            // 10. Transform data sang các surrogate keys
                            if (!Modules.Transform(config.getId(), connection))
                                break;
                            // 11. Load data sang warehouse
                            if (!Modules.LoadToWarehouse(config.getId(), connection))
                                break;
                            // 12. Aggregate data
                            if (!Modules.Aggregate(config.getId(), connection))
                                break;
                            // 13. Load data aggregates sang data_mart.ketquaxs_results_mb, data_mart.ketquaxs_results_mt và data_mart.ketquaxs_results_mn
                            if (!Modules.LoadToDataMart(config.getId(), connection))
                                break;
                            break;
                        //    18. status = LOADINGWH ?
                        case "LOADINGWH":
                            // 11. Load data sang warehouse
                            if (!Modules.LoadToWarehouse(config.getId(), connection))
                                break;
                            // 12. Aggregate data
                            if (!Modules.Aggregate(config.getId(), connection))
                                break;
                            // 13. Load data aggregates sang data_mart.ketquaxs_results_mb, data_mart.ketquaxs_results_mt và data_mart.ketquaxs_results_mn
                            if (!Modules.LoadToDataMart(config.getId(), connection))
                                break;
                            break;
                        // 19. status = AGGREGATING ?
                        case "AGGREGATING":
                            // 12. Aggregate data
                            if (!Modules.Aggregate(config.getId(), connection))
                                break;
                            // 13. Load data aggregates sang data_mart.ketquaxs_results_mb, data_mart.ketquaxs_results_mt và data_mart.ketquaxs_results_mn
                            if (!Modules.LoadToDataMart(config.getId(), connection))
                                break;
                            break;
                        // 20. status = LOADINGM ?
                        case "MLOADING":
                            // 13. Load data aggregates sang data_mart.ketquaxs_results_mb, data_mart.ketquaxs_results_mt và data_mart.ketquaxs_results_mn
                            if (!Modules.LoadToDataMart(config.getId(), connection))
                                break;
                            break;
                        default:
                            break;
                    }
                }
                // 14. Cập nhật isRun = 0
                DBConnect.setIsRun(connection, config.getId(), 0);
            }
            // 21. Đóng kết nối db_controls
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
           // 22. Gửi mail báo lỗi
            Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR CRAWLER: Cant connect to Databases", "<h3 style=\"color: red\">" + e + "</h3>", MailConfig.MAIL_HTML);
        }
    }
}
