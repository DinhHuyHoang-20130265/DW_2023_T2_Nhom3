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

    public static void startExtractToStaging(int id, Connection connection, String location) {
        DBConnect.insertStatus(connection, id, "EXTRACTING");
        try (CallableStatement callableStatement = connection.prepareCall("TRUNCATE staging.ketquaxs_staging")) {
            callableStatement.execute();
            Optional<File> latestExcelFile = findLatestExcelFile(location);
            if (latestExcelFile.isPresent()) {
                File excelFile = latestExcelFile.get();
                extractToStaging(excelFile.getAbsolutePath(), connection);
                DBConnect.insertStatus(connection, id, "EXTRACTED");
            } else
                DBConnect.insertStatus(connection, id, "ERROR");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    public static void crawl(String source_path, String location, String group, int id, Connection connection) {
        try {
            Document document;
            document = Jsoup.connect(source_path + group).userAgent("Mozilla/5.0").get();
            LocalDate date = LocalDate.now();
            String dateNow = date.getYear() + "-" + (date.getMonthValue() < 10 ? "0" + date.getMonthValue() : date.getMonthValue()) + "-" + (date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth() : date.getDayOfMonth());
            String substring = group.substring(group.indexOf("/xs") + 3, group.indexOf("/xs") + 5);
            String currentResultDate = (!substring.equals("mb") ? substring + "_kqngay_" : "kqngay_") + (date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth() : date.getDayOfMonth()) + (date.getMonthValue() < 10 ? "0" + date.getMonthValue() : date.getMonthValue()) + date.getYear() + "_kq";
            Element table = Objects.requireNonNull(document.getElementById(currentResultDate)).select("#" + currentResultDate + " table:first-child").get(0);
            // Element table = document.getElementsByClass("section").get(2).select("table:first-child").get(0);
            int i = 2;
            if (substring.equals("mb")) {
                String provinceTemp = document.select(".section-header .site-link").get(0).text();
                String province = provinceTemp.substring(provinceTemp.indexOf("(") + 1, provinceTemp.indexOf(")"));
                for (int j = 2; j < 10; j++) {
                    assert table != null;
                    Elements numbers = table.select("tbody tr:nth-child(" + j + ") td:nth-child(" + i + ") span");
                    String prize = "giai" + table.select("tbody tr:nth-child(" + j + ") td:first-child").get(0).text();
                    for (Element number : numbers) {
                        LotteryResult result = new LotteryResult(substring, province, dateNow, prize, number.text());
                        saveToFile(result, dateNow, location);
                    }
                }
            } else {
                assert table != null;
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
        } catch (IOException e) {
            e.printStackTrace();
            DBConnect.insertStatusAndName(connection, id, "Failed to Crawling: " + e, "ERROR");
            Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR CRAWLER", "<h3 style=\"color: red\">" + e + "</h3>", MailConfig.MAIL_HTML);
        }
    }

    public static void startCrawl(String source_path, String location, int id, Connection connection) {
        DBConnect.insertStatus(connection, id, "CRAWLING");
        for (String s : groups)
            crawl(source_path, location, s, id, connection);
        DBConnect.insertStatus(connection, id, "CRAWLED");
    }
    public static void Transform(int id, Connection connection) throws SQLException {
        DBConnect.insertStatus(connection, id, "TRANSFORMING");
        String[] sqls = {"CALL transformStage_mien()", "CALL transformStage_dai()", "CALL transformStage_date()", "CALL transformStage_giai()"};
        for (String sql : sqls) {
            CallableStatement statement = connection.prepareCall(sql);
            statement.execute();
        }
        DBConnect.insertStatus(connection, id, "TRANSFORMED");
    }
    public static void LoadToWarehouse(int id, Connection connection) throws SQLException {
        DBConnect.insertStatus(connection, id, "LOADINGWH");
        String sql = "CALL insert_facts()";
        CallableStatement statement = connection.prepareCall(sql);
        statement.execute();
        DBConnect.insertStatus(connection, id, "WLOADED");
    }

    public static void Aggregate(int id, Connection connection) throws SQLException {
        DBConnect.insertStatus(connection, id, "AGGREGATING");
        String sql = "CALL Aggregate()";
        CallableStatement statement = connection.prepareCall(sql);
        statement.execute();
        DBConnect.insertStatus(connection, id, "AGGREGATED");
    }

}
