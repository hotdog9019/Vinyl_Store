package ru.mirea.project.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mirea.project.model.Order;
import ru.mirea.project.model.User;

import java.io.OutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExportService {
    public Path exportAllTablesToXlsx(List<User> users, List<Order> orders, Path file) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }

        try (Workbook workbook = new XSSFWorkbook();
             OutputStream outputStream = Files.newOutputStream(file)) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);
            CellStyle dateTimeStyle = createDateTimeStyle(workbook);

            createOrdersSheet(workbook, orders, headerStyle, moneyStyle, dateTimeStyle);
            createPlatesSheet(workbook, orders, headerStyle, moneyStyle);
            createUsersSheet(workbook, users, headerStyle);

            workbook.write(outputStream);
        }

        return file;
    }

    public Path exportOrdersToXlsx(List<Order> orders, Path file) throws IOException {
        return exportAllTablesToXlsx(List.of(), orders, file);
    }

    private void createOrdersSheet(
            Workbook workbook,
            List<Order> orders,
            CellStyle headerStyle,
            CellStyle moneyStyle,
            CellStyle dateTimeStyle
    ) {
        Sheet sheet = workbook.createSheet("Orders");
        createHeader(sheet, headerStyle, "id", "user_id", "artist", "album_title", "quantity", "total_price", "status", "created_at");

        int rowNumber = 1;
        for (Order order : orders) {
            Row row = sheet.createRow(rowNumber++);
            setCell(row, 0, order.getId());
            setCell(row, 1, order.getUserId());
            setCell(row, 2, order.getArtist());
            setCell(row, 3, order.getAlbumTitle());
            setCell(row, 4, order.getQuantity());
            setMoneyCell(row, 5, order.getTotalPrice(), moneyStyle);
            setCell(row, 6, order.getStatus().name());
            Cell createdAt = row.createCell(7);
            createdAt.setCellValue(order.getCreatedAt());
            createdAt.setCellStyle(dateTimeStyle);
        }

        autosize(sheet, 8);
    }

    private void createPlatesSheet(Workbook workbook, List<Order> orders, CellStyle headerStyle, CellStyle moneyStyle) {
        Sheet sheet = workbook.createSheet("Plates");
        createHeader(sheet, headerStyle, "artist", "album_title", "total_quantity", "total_price");

        int rowNumber = 1;
        for (PlateRow plate : collectPlates(orders).values()) {
            Row row = sheet.createRow(rowNumber++);
            setCell(row, 0, plate.artist());
            setCell(row, 1, plate.albumTitle());
            setCell(row, 2, plate.quantity());
            setMoneyCell(row, 3, plate.totalPrice(), moneyStyle);
        }

        autosize(sheet, 4);
    }

    private void createUsersSheet(Workbook workbook, List<User> users, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Users");
        createHeader(sheet, headerStyle, "id", "full_name", "email", "phone");

        int rowNumber = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowNumber++);
            setCell(row, 0, user.getId());
            setCell(row, 1, user.getFullName());
            setCell(row, 2, user.getEmail());
            setCell(row, 3, user.getPhone());
        }

        autosize(sheet, 4);
    }

    private Map<String, PlateRow> collectPlates(List<Order> orders) {
        Map<String, PlateRow> plates = new LinkedHashMap<>();

        for (Order order : orders) {
            String key = order.getArtist() + "\u0000" + order.getAlbumTitle();
            PlateRow current = plates.get(key);
            if (current == null) {
                plates.put(key, new PlateRow(
                        order.getArtist(),
                        order.getAlbumTitle(),
                        order.getQuantity(),
                        money(order.getTotalPrice())
                ));
            } else {
                plates.put(key, new PlateRow(
                        current.artist(),
                        current.albumTitle(),
                        current.quantity() + order.getQuantity(),
                        current.totalPrice().add(money(order.getTotalPrice()))
                ));
            }
        }

        return plates;
    }

    private void createHeader(Sheet sheet, CellStyle headerStyle, String... titles) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < titles.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createDateTimeStyle(Workbook workbook) {
        CreationHelper helper = workbook.getCreationHelper();
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(helper.createDataFormat().getFormat("dd.mm.yyyy hh:mm"));
        return style;
    }

    private void setCell(Row row, int column, String value) {
        row.createCell(column).setCellValue(text(value));
    }

    private void setCell(Row row, int column, long value) {
        row.createCell(column).setCellValue(value);
    }

    private void setCell(Row row, int column, int value) {
        row.createCell(column).setCellValue(value);
    }

    private void setMoneyCell(Row row, int column, BigDecimal value, CellStyle moneyStyle) {
        Cell cell = row.createCell(column);
        cell.setCellValue(money(value).doubleValue());
        cell.setCellStyle(moneyStyle);
    }

    private void autosize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String text(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    private record PlateRow(String artist, String albumTitle, int quantity, BigDecimal totalPrice) {
    }
}
