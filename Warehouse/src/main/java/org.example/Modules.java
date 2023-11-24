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

    public static void Transform(int id, Connection connection) throws SQLException {
        DBConnect.insertStatus(connection, id, "TRANSFORMING");
        String[] sqls = {"CALL transformStage_mien()", "CALL transformStage_dai()", "CALL transformStage_date()", "CALL transformStage_giai()"};
        for (String sql : sqls) {
            CallableStatement statement = connection.prepareCall(sql);
            statement.execute();
        }
        DBConnect.insertStatus(connection, id, "TRANSFORMED");
    }

}