package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Modules {
    static final String[] groups = {"xo-so-mien-bac/xsmb-p1.html", "xo-so-mien-trung/xsmt-p1.html", "xo-so-mien-nam/xsmn-p1.html"};
    static final String[] groups_manual = {"xsmn", "xsmt", "xsmb"};

    public static void extractToStaging(String pathFile, Connection connection) {
        try (FileInputStream excelFile = new FileInputStream(pathFile); Workbook workbook = new XSSFWorkbook(excelFile)
        ) {

            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> iterator = sheet.iterator();
            iterator.next();
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                String Mien = currentRow.getCell(0).getStringCellValue();
                String Dai = currentRow.getCell(1).getStringCellValue();
                String ngay = currentRow.getCell(2).getStringCellValue();
                String tenGiai = currentRow.getCell(3).getStringCellValue();
                String soTrungThuong = currentRow.getCell(4).getStringCellValue();

                CallableStatement callableStatement = connection.prepareCall("{call extractDataToStaging(?, ?, ?, ?, ?)}");
                callableStatement.setString(1, Mien);
                callableStatement.setString(2, Dai);
                callableStatement.setString(3, ngay);
                callableStatement.setString(4, tenGiai);
                callableStatement.setString(5, soTrungThuong);

                callableStatement.execute();
            }
            System.out.println("success!");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static Optional<File> findLatestExcelFile(String folderPath) throws IOException {
        Path folder = Paths.get(folderPath);
        if (!Files.exists(folder) || !Files.isDirectory(folder))
            return Optional.empty();

        try (Stream<Path> walk = Files.walk(folder)) {
            return walk
                    .filter(path -> path.toString().toLowerCase().endsWith(".xlsx") || path.toString().toLowerCase().endsWith(".xls"))
                    .map(Path::toFile)
                    .max(Comparator.comparingLong(File::lastModified));
        }
    }

    // 9. Extract file -> staging
    public static boolean startExtractToStaging(int id, Connection connection, String location, String run) {
        // 9.1. Insert vào bảng db_controls.data_files dòng dữ liệu với status = EXTRACTING
        DBConnect.insertStatus(connection, id, "EXTRACTING");
        // 9.2. Thực hiện truncate bảng staging.ketquaxs_staging
        try (CallableStatement callableStatement = connection.prepareCall("TRUNCATE staging.ketquaxs_staging")) {
            callableStatement.execute();
            // 9.3. Lấy tham số run kiểm tra
            if (run.equals("auto")) {
                // 9.4. Tìm đọc file excel có ngày mới nhất với đường dẫn là tham số location/destination
                Optional<File> latestExcelFile = findLatestExcelFile(location);
                if (latestExcelFile.isPresent()) {
                    // 9.5. Insert dữ liệu vào bảng staging.ketquaxs_staging
                    File excelFile = latestExcelFile.get();
                    extractToStaging(excelFile.getAbsolutePath(), connection);
                    // 9.6. Insert vào bảng db_controls.data_files dòng dữ liệu với status = EXTRACTED
                    DBConnect.insertStatus(connection, id, "EXTRACTED");
                } else {
                    // 9.8. Insert vào bảng db_controls.data_files dòng dữ liệu với status = ERROR
                    DBConnect.insertStatusAndName(connection, id, "Cannot find the file to start Extract", "ERROR");
                    // 9.9. Gửi mail thông báo lỗi
                    Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR Extract", "<h3 style=\"color: red\">" + "Cannot find the file to start Extract" + "</h3>", MailConfig.MAIL_HTML);
                    return false;
                }
            } else {
                // 9.4. Tìm đọc file excel có ngày là run với đường dẫn là tham số location/destination
                String[] splited = run.split("-");
                String date = splited[2] + "-" + splited[1] + "-" + splited[0];
                File excelFile = new File(location + "\\" + date + " XSKT.xlsx");
                if (excelFile.exists()) {
                    // 9.5. Insert dữ liệu vào bảng staging.ketquaxs_staging
                    extractToStaging(excelFile.getAbsolutePath(), connection);
                    // 9.6. Insert vào bảng db_controls.data_files dòng dữ liệu với status = EXTRACTED
                    DBConnect.insertStatus(connection, id, "EXTRACTED");
                } else {
                    // 9.8. Insert vào bảng db_controls.data_files dòng dữ liệu với status = ERROR
                    DBConnect.insertStatusAndName(connection, id, "Cannot find the file to start Extract", "ERROR in recrawl date: " + run);
                    // 9.9. Gửi mail thông báo lỗi
                    Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR Extract", "<h3 style=\"color: red\">" + "Cannot find the file to start Extract" + "</h3>", MailConfig.MAIL_HTML);
                    return false;
                }
            }
        } catch (IOException | SQLException e) {
            DBConnect.insertStatusAndName(connection, id, "Failed to Extract: " + e, "ERROR");
            Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR Extract", "<h3 style=\"color: red\">" + e + "</h3>", MailConfig.MAIL_HTML);
            return false;
        }
        return true;
    }

    public static void saveToFile(LotteryResult lotteryResult, String dateNow, String location) throws IOException {
        try {
            String excelFilePath = location + "\\" + dateNow + " XSKT.xlsx";

            Workbook workbook = getWorkbook(excelFilePath);
            Sheet sheet = workbook.getSheetAt(0);

            int lastRowIndex = sheet.getLastRowNum();

            Row rowToInsert = sheet.getRow(lastRowIndex + 1);
            if (rowToInsert == null) rowToInsert = sheet.createRow(lastRowIndex + 1);
            Field[] fields = lotteryResult.getClass().getDeclaredFields();
            for (int i = 0; i < 5; i++) {
                Cell cell1 = rowToInsert.createCell(i);
                Field field = fields[i];
                field.setAccessible(true);
                cell1.setCellValue(field.get(lotteryResult).toString());
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            FileOutputStream outputStream = new FileOutputStream(excelFilePath);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

            System.out.println("Success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Workbook getWorkbook(String excelFilePath) throws IOException {
        File file = new File(excelFilePath);
        boolean fileExists = file.exists();

        if (!fileExists) {
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Data sheet");

            Row headerRow = sheet.createRow(0);
            String[] fieldNames = {"Mien", "Dai", "Ngay", "tenGiai", "soTrungThuong"};
            for (int i = 0; i < 5; i++)
                headerRow.createCell(i).setCellValue(fieldNames[i]);
            FileOutputStream outputStream = new FileOutputStream(excelFilePath);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        }

        FileInputStream inputStream = new FileInputStream(excelFilePath);
        return new XSSFWorkbook(inputStream);
    }

    public static boolean crawl(String source_path, String location, String group, int id, Connection connection, String run) throws SQLException {
        try {
            Document document;
            document = Jsoup.connect(source_path + (run.equals("auto") ? group : group + "-" + run + ".html")).userAgent("Mozilla/5.0").get();
            String dateNow;
            String currentResultDate;
            String substring = group.substring(group.indexOf("/xs") + 3, group.indexOf("/xs") + 5);
            LocalDate date = LocalDate.now();
            String mien = !substring.equals("mb") ? substring + "_kqngay_" : "kqngay_";
            if (run.equals("auto")) {
                dateNow = date.getYear() + "-" + (date.getMonthValue() < 10 ? "0" + date.getMonthValue() : date.getMonthValue()) + "-" + (date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth() : date.getDayOfMonth());
                currentResultDate = mien + (date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth() : date.getDayOfMonth()) + (date.getMonthValue() < 10 ? "0" + date.getMonthValue() : date.getMonthValue()) + date.getYear() + "_kq";
            } else {
                String[] splited = run.split("-");
                dateNow = splited[2] + "-" + splited[1] + "-" + splited[0];
                currentResultDate = mien + run.replace("-", "") + "_kq";
            }
            Element table;
            try {
                table = Objects.requireNonNull(document.getElementById(currentResultDate)).select("#" + currentResultDate + " table:first-child").get(0);
            } catch (Exception e) {
                e.printStackTrace();
                DBConnect.insertStatusAndName(connection, id, "Failed to Crawling: no table to crawl", "ERROR");
                Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR CRAWLER", "<h3 style=\"color: red\">" + e + "</h3>", MailConfig.MAIL_HTML);
                return false;
            }
            int i = 2;
            if (substring.equals("mb") && table != null) {
                String provinceTemp = document.select(".section-header .site-link").get(0).text();
                System.out.println(provinceTemp);
                String province;
                if (run.equals("auto"))
                    province = provinceTemp.substring(provinceTemp.indexOf("(") + 1, provinceTemp.indexOf(")"));
                else {
                    int lastDigitIndex = -1;
                    for (int k = provinceTemp.length() - 1; k >= 0; k--) {
                        char c = provinceTemp.charAt(k);
                        if (Character.isDigit(c)) {
                            lastDigitIndex = k;
                            break;
                        }
                    }
                    province = provinceTemp.substring(lastDigitIndex + 2);
                }
                for (int j = 2; j < 10; j++) {
                    Elements numbers = table.select("tbody tr:nth-child(" + j + ") td:nth-child(" + i + ") span");
                    String prize = "giai" + (run.equals("auto") ? table.select("tbody tr:nth-child(" + j + ") td:first-child").get(0).text() : table.select("tbody tr:nth-child(" + j + ") th:first-child").get(0).text());
                    for (Element number : numbers) {
                        LotteryResult result = new LotteryResult(substring, province, dateNow, prize, number.text());
                        saveToFile(result, dateNow, location);
                    }
                }
            } else {
                if (table != null) {
                    for (Element e : table.select("thead tr th:not(:first-child)")) {
                        String province = e.text();
                        for (int j = 1; j < 10; j++) {
                            String prize = "giai" + table.select("tbody tr:nth-child(" + j + ") th").get(0).text();
                            Elements numbers = table.select("tbody tr:nth-child(" + j + ") td:nth-child(" + i + ") span");
                            for (Element number : numbers) {
                                LotteryResult result = new LotteryResult(substring, province, dateNow, prize, number.text());
                                saveToFile(result, dateNow, location);
                            }
                        }
                        i++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            DBConnect.insertStatusAndName(connection, id, "Failed to Crawling: " + e, "ERROR");
            Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR CRAWLER", "<h3 style=\"color: red\">" + e + "</h3>", MailConfig.MAIL_HTML);
            return false;
        }
        return true;
    }

    public static boolean startCrawl(String source_path, String location, int id, Connection connection, String run) throws SQLException {
        try {
            DBConnect.insertStatus(connection, id, "CRAWLING");
            for (String s : run.equals("auto") ? groups : groups_manual) {
                boolean check = crawl(source_path, location, s, id, connection, run);
                if (!check)
                    return false;
            }
            DBConnect.insertStatus(connection, id, "CRAWLED");
        } catch (SQLException e) {
            DBConnect.insertStatusAndName(connection, id, "Failed to Crawling: " + e, "ERROR");
            Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR CRAWLER", "<h3 style=\"color: red\">" + e + "</h3>", MailConfig.MAIL_HTML);
            return false;
        }
        return true;
    }
    // 10. Transform data sang các surrogate keys
    public static boolean Transform(int id, Connection connection) throws SQLException {
        try {
            // 10.1. Insert vào bảng db_controls.data_files dòng dữ liệu với status = TRANSFORMING
            DBConnect.insertStatus(connection, id, "TRANSFORMING");
            // 10.2. Gọi procedure Transform
            String[] sqls = {"CALL transformStage_date()", "CALL transformStage_mien()", "CALL transformStage_dai()", "CALL transformStage_giai()"};
            for (String sql : sqls) {
                CallableStatement statement = connection.prepareCall(sql);
                statement.execute();
            }
            // 10.3. Insert vào bảng db_controls.data_files dòng dữ liệu với status = TRANSFORMED
            DBConnect.insertStatus(connection, id, "TRANSFORMED");
        } catch (SQLException e) {
            // 10.4. Insert vào bảng db_controls.data_files dòng dữ liệu với status = ERROR
            DBConnect.insertStatusAndName(connection, id, "Failed to Transform: " + e, "ERROR");
            // 10.5. Gửi mail thông báo lỗi
            Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR Transform", "<h3 style=\"color: red\">" + e + "</h3>", MailConfig.MAIL_HTML);
            return false;
        }
        return true;
    }
    // 11. Load data sang warehouse
    public static boolean LoadToWarehouse(int id, Connection connection) throws SQLException {
        // 11.1. insert db_controls.data_files với status = LOADINGWH
        DBConnect.insertStatus(connection, id, "LOADINGWH");
        try {
            // 11.2. Gọi procedure sql
            String sql = "CALL insert_facts()";
            CallableStatement statement = connection.prepareCall(sql);
            statement.execute();
            // 11.3. insert db_controls.data_files với status = WLOADED
            DBConnect.insertStatus(connection, id, "WLOADED");
        } catch (SQLException e) {
            // 11.4. insert db_controls.data_files vớistatus = ERROR
            DBConnect.insertStatusAndName(connection, id, "Failed to LoadToWarehouse: " + e, "ERROR");
            // 11.5. Gửi mail thông báo lỗi
            Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR LoadToWarehouse", "<h3 style=\"color: red\">" + e + "</h3>", MailConfig.MAIL_HTML);
            return false;
        }
        return true;
    }
    // 12. Aggregate data
    public static boolean Aggregate(int id, Connection connection) throws SQLException {
        // 12.1. insert db_controls.data_files với status = AGGREGATING
        DBConnect.insertStatus(connection, id, "AGGREGATING");
        try {
            // 12.2. Gọi procedure aggregate
            String sql = "CALL Aggregate()";
            CallableStatement statement = connection.prepareCall(sql);
            statement.execute();
            // 12.3. insert db_controls.data_files với status = AGGREGATED
            DBConnect.insertStatus(connection, id, "AGGREGATED");
        } catch (SQLException e) {
            // 12.4. insert db_controls.data_files vớistatus = ERROR
            DBConnect.insertStatusAndName(connection, id, "Failed to LoadToWarehouse: " + e, "ERROR");
            // 12.5. Gửi mail thông báo lỗi
            Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR LoadToWarehouse", "<h3 style=\"color: red\">" + e + "</h3>", MailConfig.MAIL_HTML);
            return false;
        }
        return true;
    }

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
}
