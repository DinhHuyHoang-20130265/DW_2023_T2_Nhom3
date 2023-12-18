package Modules;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

public class Extract {

    public static void extractToStaging(String pathFile, Connection connection) {
        try (FileInputStream excelFile = new FileInputStream(pathFile); Workbook workbook = new XSSFWorkbook(excelFile)) {

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
        if (!Files.exists(folder) || !Files.isDirectory(folder)) return Optional.empty();

        try (Stream<Path> walk = Files.walk(folder)) {
            return walk.filter(path -> path.toString().toLowerCase().endsWith(".xlsx") || path.toString().toLowerCase().endsWith(".xls")).map(Path::toFile).max(Comparator.comparingLong(File::lastModified));
        }
    }

    public static boolean startExtractToStaging(int id, Connection connection, String location, String run) {
        DBConnect.insertStatus(connection, id, "EXTRACTING");
        try (CallableStatement callableStatement = connection.prepareCall("TRUNCATE staging.ketquaxs_staging")) {
            callableStatement.execute();
            if (run.equals("auto")) {
                Optional<File> latestExcelFile = findLatestExcelFile(location);
                if (latestExcelFile.isPresent()) {
                    File excelFile = latestExcelFile.get();
                    extractToStaging(excelFile.getAbsolutePath(), connection);
                    DBConnect.insertStatus(connection, id, "EXTRACTED");
                } else {
                    DBConnect.insertStatusAndName(connection, id, "Cannot find the file to start Extract", "ERROR");
                    Mail.getInstance().sendMail("PNTSHOP", "dinh37823@gmail.com", "ERROR Extract", "<h3 style=\"color: red\">" + "Cannot find the file to start Extract" + "</h3>", MailConfig.MAIL_HTML);
                    return false;
                }
            } else {
                String[] splited = run.split("-");
                String date = splited[2] + "-" + splited[1] + "-" + splited[0];
                File excelFile = new File(location + "\\" + date + " XSKT.xlsx");
                if (excelFile.exists()) {
                    extractToStaging(excelFile.getAbsolutePath(), connection);
                    DBConnect.insertStatus(connection, id, "EXTRACTED");
                } else {
                    DBConnect.insertStatusAndName(connection, id, "Cannot find the file to start Extract", "ERROR in recrawl date: " + run);
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


    public static void main(String[] args) {
        try (Connection connection = DBConnect.getConnection()) {
            String status = DBConnect.getLatesStatusWithNoIdConfig(connection, 0);
            DataFilesConfigs config = DBConnect.getConfigHaveLatesStatus(connection);
            if (config.getIsRun() == 0) {
                DBConnect.setIsRun(connection, config.getId(), 1);
                if (status.equals("CRAWLED"))
                    startExtractToStaging(config.getId(), connection, config.getLocation(), DBProperties.getRun());
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
