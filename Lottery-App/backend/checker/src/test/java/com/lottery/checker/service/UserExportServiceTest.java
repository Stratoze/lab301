package com.lottery.checker.service;

import com.lottery.checker.dto.response.UserResponse;
import com.lottery.checker.entity.Role;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserExportServiceTest {

    private UserExportService userExportService;
    private UserResponse activeUser;
    private UserResponse blockedUserWithoutOptionalFields;

    @BeforeEach
    void setUp() {
        userExportService = new UserExportService();

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 1, 10, 30);

        activeUser = new UserResponse(
                3L, "USR-10-2023-00000003", "khach1@gmail.com",
                "0910000001", "Le Van Tam", Role.ROLE_USER,
                true, createdAt, createdAt
        );

        blockedUserWithoutOptionalFields = new UserResponse(
                9L, "USR-11-2023-00000001", "locked@gmail.com",
                null, "Nguoi Bi Khoa", Role.ROLE_USER,
                false, null, null
        );
    }

    @Test
    void generateCsv_MixedUsers_ProducesHeaderAndFormattedRows() {
        byte[] csv = userExportService.generateCsv(
                List.of(activeUser, blockedUserWithoutOptionalFields));

        String content = new String(csv, StandardCharsets.UTF_8);
        String[] lines = content.split("\n");

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo(
                "User Code,Full Name,Email,Phone,Role,Status,Last Login,Created At");
        assertThat(lines[1]).contains("Le Van Tam").contains("Active");
        assertThat(lines[2]).contains("Blocked").contains("Never").contains("Nguoi Bi Khoa");
    }

    @Test
    void generateCsv_NameWithCommaAndQuotes_EscapesCsvSafely() {
        UserResponse trickyUser = new UserResponse(
                5L, "USR-10-2023-00000005", "tricky@gmail.com",
                null, "Nguyen, Van \"A\"", Role.ROLE_USER,
                true, null, null
        );

        String content = new String(
                userExportService.generateCsv(List.of(trickyUser)), StandardCharsets.UTF_8);

        assertThat(content).contains("\"Nguyen, Van \"\"A\"\"\"");
    }

    @Test
    void generateExcel_MixedUsers_ProducesReadableWorkbook() throws Exception {
        byte[] xlsx = userExportService.generateExcel(
                List.of(activeUser, blockedUserWithoutOptionalFields));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(xlsx))) {
            Sheet sheet = workbook.getSheet("Users");

            assertThat(sheet).isNotNull();
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("User Code");
            assertThat(sheet.getRow(1).getCell(2).getStringCellValue()).isEqualTo("khach1@gmail.com");
            assertThat(sheet.getRow(1).getCell(5).getStringCellValue()).isEqualTo("Active");
            assertThat(sheet.getRow(2).getCell(5).getStringCellValue()).isEqualTo("Blocked");
            assertThat(sheet.getRow(2).getCell(6).getStringCellValue()).isEqualTo("Never");
        }
    }
}