package com.lottery.checker.service;

import com.lottery.checker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class UserCodeGenerator {
    private final UserRepository userRepository;

    public String generate() {
        LocalDateTime now = LocalDateTime.now();
        String monthYear = now.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        long count = userRepository.countUsersByMonth(monthYear);
        String code;
        do {
            count++;
            code = String.format("USR-%s-%08d", monthYear, count);
        } while (userRepository.findByUserCode(code).isPresent());
        return code;
    }
}