package com.lottery.checker.service;

import com.lottery.checker.dto.response.UserResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class UserExportService {

    private static final String[] COLUMNS = {
            "User Code", "Full Name", "Email", "Phone", "Role", "Status", "Last Login", "Created At"
    };

    public byte[] generateCsv(List<UserResponse> users) {
        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",", COLUMNS)).append('\n');

        for (UserResponse u : users) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s%n",
                    escapeCsv(u.userCode()),
                    escapeCsv(u.fullName()),
                    escapeCsv(u.email()),
                    escapeCsv(u.phone() != null ? u.phone() : ""),
                    u.role().name(),
                    u.isActive() ? "Active" : "Blocked",
                    u.lastLogin() != null ? u.lastLogin().toString() : "Never",
                    u.createdAt() != null ? u.createdAt().toString() : ""
            ));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateExcel(List<UserResponse> users) {
        // SXSSF streams rows to disk to avoid OOM on large exports (keeps 100 rows in memory)
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Users");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < COLUMNS.length; i++) {
                headerRow.createCell(i).setCellValue(COLUMNS[i]);
            }
            int rowIdx = 1;
            for (UserResponse u : users) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(u.userCode());
                row.createCell(1).setCellValue(u.fullName());
                row.createCell(2).setCellValue(u.email());
                row.createCell(3).setCellValue(u.phone() != null ? u.phone() : "");
                row.createCell(4).setCellValue(u.role().name());
                row.createCell(5).setCellValue(u.isActive() ? "Active" : "Blocked");
                row.createCell(6).setCellValue(u.lastLogin() != null ? u.lastLogin().toString() : "Never");
                row.createCell(7).setCellValue(u.createdAt() != null ? u.createdAt().toString() : "");
            }
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate Excel file", e);
        }
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}