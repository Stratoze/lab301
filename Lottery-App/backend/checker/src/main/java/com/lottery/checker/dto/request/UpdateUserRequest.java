package com.lottery.checker.dto.request;

public record UpdateUserRequest(String fullName, String phone, String role, Boolean isActive) {}
