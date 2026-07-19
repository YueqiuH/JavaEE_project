package com.smartcampus.app.service.base;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 批量导入：解析 Excel(.xlsx) 首行表头→数据行 Map
 */
@Service
public class ExcelImportService {

    /**
     * 解析上传的 Excel 文件，每行一个 Map<表头, 单元格值>
     */
    public List<Map<String, Object>> parse(MultipartFile file) throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet.getLastRowNum() < 1) return rows;

            // 首行作表头
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return rows;
            List<String> headers = new ArrayList<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                headers.add(getCellString(headerRow.getCell(c)));
            }

            // 数据行
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Map<String, Object> map = new LinkedHashMap<>();
                boolean allEmpty = true;
                for (int c = 0; c < headers.size(); c++) {
                    String val = getCellString(row.getCell(c));
                    map.put(headers.get(c), val);
                    if (!val.isEmpty()) allEmpty = false;
                }
                if (!allEmpty) rows.add(map);
            }
        }
        return rows;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) && !Double.isInfinite(v)
                        ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue().trim(); }
                catch (Exception e) { yield String.valueOf((long) cell.getNumericCellValue()); }
            }
            default -> "";
        };
    }

    /** 下载导入模板 */
    public void writeTemplate(HttpServletResponse response, String fileName, String[] headers, Object[]... exampleRows) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"));

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            CellStyle headerStyle = wb.createCellStyle();
            Font hf = wb.createFont(); hf.setBold(true); headerStyle.setFont(hf);

            Row hr = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = hr.createCell(i); c.setCellValue(headers[i]); c.setCellStyle(headerStyle);
            }
            for (int r = 0; r < exampleRows.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < exampleRows[r].length; c++) {
                    Object v = exampleRows[r][c];
                    if (v instanceof Number n) row.createCell(c).setCellValue(n.doubleValue());
                    else row.createCell(c).setCellValue(String.valueOf(v));
                }
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(response.getOutputStream());
        }
    }
}
