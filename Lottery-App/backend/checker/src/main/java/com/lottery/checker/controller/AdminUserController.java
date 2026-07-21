package com.lottery.checker.controller;

import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.UserResponse;
import com.lottery.checker.entity.Role;
import com.lottery.checker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(keyword, pageable)));
    }

    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(@RequestBody Map<String, Object> payload) {
        List<Integer> idInts = (List<Integer>) payload.get("ids");
        List<Long> ids = idInts.stream().map(Integer::longValue).toList();
        boolean isActive = (boolean) payload.get("isActive");
        
        userService.updateStatus(ids, isActive);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        
        String fullName = (String) payload.get("fullName");
        String phone = (String) payload.get("phone");
        String roleStr = (String) payload.get("role");
        Role role = roleStr != null ? Role.valueOf(roleStr) : null;
        Boolean isActive = (Boolean) payload.get("isActive");

        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, fullName, phone, role, isActive)));
    }

    @PostMapping("/send-email")
    public ResponseEntity<ApiResponse<String>> sendEmail(@RequestBody Map<String, Object> payload) {
        List<Integer> idInts = (List<Integer>) payload.get("ids");
        List<Long> ids = idInts.stream().map(Integer::longValue).toList();
        String subject = (String) payload.get("subject");
        String content = (String) payload.get("content");
        userService.sendBulkEmail(ids, subject, content);
        return ResponseEntity.ok(ApiResponse.success("Emails sent successfully"));
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) List<Long> ids) {
        List<UserResponse> users;
        if (ids != null && !ids.isEmpty()) {
            users = userService.getUsersByIds(ids);
        } else {
            users = userService.getAllUsers(keyword, Pageable.unpaged()).getContent();
        }

        return switch (format.toLowerCase()) {
            case "excel", "xlsx" -> {
                byte[] bytes = generateExcel(users);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                headers.setContentDispositionFormData("attachment", "users_export.xlsx");
                yield ResponseEntity.ok().headers(headers).body(bytes);
            }
            case "json" -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users_export.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.success(users));
            default -> {
                byte[] bytes = generateCsv(users);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDispositionFormData("attachment", "users_export.csv");
                yield ResponseEntity.ok().headers(headers).body(bytes);
            }
        };
    }

    private byte[] generateCsv(List<UserResponse> users) {
        StringBuilder csv = new StringBuilder();
        csv.append("User Code,Full Name,Email,Phone,Role,Status,Last Login,Created At%n");
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
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] generateExcel(List<UserResponse> users) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Users");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"User Code", "Full Name", "Email", "Phone", "Role", "Status", "Last Login", "Created At"};
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
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
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}