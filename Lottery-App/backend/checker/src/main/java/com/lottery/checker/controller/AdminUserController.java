package com.lottery.checker.controller;

import com.lottery.checker.dto.request.SendEmailRequest;
import com.lottery.checker.dto.request.UpdateStatusRequest;
import com.lottery.checker.dto.request.UpdateUserRequest;
import com.lottery.checker.dto.response.ApiResponse;
import com.lottery.checker.dto.response.PagedResponse;
import com.lottery.checker.dto.response.UserResponse;
import com.lottery.checker.service.UserExportService;
import com.lottery.checker.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final UserExportService userExportService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String loginFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(keyword, loginFilter, pageable)));
    }

    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @Valid @RequestBody UpdateStatusRequest request) {
        userService.updateStatus(request.ids(), request.isActive());
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, request)));
    }

    @PostMapping("/send-email")
    public ResponseEntity<ApiResponse<String>> sendEmail(
            @Valid @RequestBody SendEmailRequest request) {
        userService.sendBulkEmail(request.ids(), request.subject(), request.content());
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
            users = userService.getAllUsers(keyword, null, Pageable.unpaged()).getContent();
        }

        return switch (format.toLowerCase()) {
            case "excel", "xlsx" -> {
                byte[] bytes = userExportService.generateExcel(users);
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
                byte[] bytes = userExportService.generateCsv(users);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDispositionFormData("attachment", "users_export.csv");
                yield ResponseEntity.ok().headers(headers).body(bytes);
            }
        };
    }

}