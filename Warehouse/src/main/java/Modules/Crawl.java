package Modules;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

public class Crawl {
    static final String[] groups = {"xo-so-mien-bac/xsmb-p1.html", "xo-so-mien-trung/xsmt-p1.html", "xo-so-mien-nam/xsmn-p1.html"};
    static final String[] groups_manual = {"xsmn", "xsmt", "xsmb"};

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
        String dateNow;
        LocalDate date = LocalDate.now();
        try {
            DBConnect.insertStatus(connection, id, "CRAWLING");
            if (run.equals("auto")) {
                dateNow = date.getYear() + "-" + (date.getMonthValue() < 10 ? "0" + date.getMonthValue() : date.getMonthValue()) + "-" + (date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth() : date.getDayOfMonth());
            } else {
                String[] splited = run.split("-");
                dateNow = splited[2] + "-" + splited[1] + "-" + splited[0];
            }
            File excelFile = new File(location + "\\" + dateNow + " XSKT.xlsx");
            if (excelFile.exists()) {
                excelFile.delete();
                System.out.println("deleted");
            }

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

    public static void main(String[] args) {
        try (Connection connection = DBConnect.getConnection()) {
            String status = DBConnect.getLatesStatusWithNoIdConfig(connection, 1);
            DataFilesConfigs config = DBConnect.getConfigHaveLatesStatus(connection);
            if (config.getIsRun() == 0) {
                DBConnect.setIsRun(connection, config.getId(), 1);
                if (status.equals("CRAWLING") || status.equals("FINISHED"))
                    startCrawl(config.getSource_path(), config.getLocation(), config.getId(), connection, DBProperties.getRun());
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
