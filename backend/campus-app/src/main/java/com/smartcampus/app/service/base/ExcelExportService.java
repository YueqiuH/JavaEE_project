package com.smartcampus.app.service.base;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 通用 Excel 导出（Apache POI OOXML .xlsx）。
 */
@Service
public class ExcelExportService {

    private static final String SHEET_NAME = "Sheet1";

    /** 列定义: header=中文表头, key=数据 Map 中的键 */
    public record ColumnDef(String header, String key) {}

    public void export(HttpServletResponse response, String fileName,
                        List<ColumnDef> columns, List<Map<String, Object>> rows) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);

            // 表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(22);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i).header());
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r + 1);
                Map<String, Object> map = rows.get(r);
                for (int c = 0; c < columns.size(); c++) {
                    Object value = map.get(columns.get(c).key());
                    Cell cell = row.createCell(c);
                    if (value == null) {
                        cell.setCellValue("");
                    } else if (value instanceof Number n) {
                        cell.setCellValue(n.doubleValue());
                    } else {
                        cell.setCellValue(String.valueOf(value));
                    }
                }
            }

            // 列宽：自适配 + 中文加宽 + 最小/最大限制
            for (int c = 0; c < columns.size(); c++) {
                sheet.autoSizeColumn(c, true);
                int w = sheet.getColumnWidth(c) + 3072; // +~12个中文字符
                if (w < 4800) w = 4800;   // 最小 ~18字符
                if (w > 18000) w = 18000;
                sheet.setColumnWidth(c, w);
            }

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        }
    }
}
