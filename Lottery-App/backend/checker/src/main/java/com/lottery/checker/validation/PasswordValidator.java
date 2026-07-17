package com.lottery.checker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    // Blocklist: commonly used passwords and dictionary words
    public static final Set<String> BLOCKLIST = new HashSet<>(Arrays.asList(
        // Common passwords
        "password", "password1", "password123", "123456", "12345678", "123456789",
        "1234567890", "qwerty", "qwerty123", "abc123", "letmein", "monkey",
        "dragon", "master", "admin", "login", "welcome", "iloveyou", "trustno1",
        "sunshine", "princess", "football", "baseball", "starwars", "access",
        "shadow", "michael", "superman", "batman", "hello", "charlie", "donald",
        "mustang", "hunter", "ranger", "thomas", "george", "robert", "jennifer",
        "jessica", "amanda", "joshua", "andrew", "william", "matthew", "daniel",
        "biteme", "fuckyou", "fuckoff", "asshole", "shithead",
        // Vietnamese common
        "123456a", "123456a@", "matkhau", "matkhau123", "matkhaumoi",
        "abc12345", "a123456", "a1234567", "a12345678", "a123456789",
        "vietnam", "vietnam123", "hanoi", "saigon", "veso123",
        "lottery", "lottery123", "doveso", "doveso123",
        // Sequences & patterns
        "aaaaaa", "aaaaaaa", "aaaaaa1", "aaaaaaa1",
        "111111", "222222", "333333", "444444", "555555",
        "666666", "777777", "888888", "999999", "000000"
    ));

    // Minimum and maximum password lengths
    public static final int MIN_LENGTH = 10;
    public static final int MAX_LENGTH = 64;

    // Dictionary words (case-insensitive)
    public static final Set<String> DICTIONARY_WORDS = new HashSet<>(Arrays.asList(
        "password", "123456", "12345678", "qwerty", "abc123", "letmein",
        "monkey", "dragon", "master", "admin", "login", "welcome",
        "iloveyou", "trustno1", "sunshine", "princess", "football",
        "baseball", "starwars", "access", "shadow", "michael",
        "superman", "batman", "hello", "charlie", "donald", "mustang",
        "hunter", "ranger", "thomas", "george", "robert", "jennifer",
        "jessica", "amanda", "joshua", "andrew", "william", "matthew",
        "daniel", "vietnam", "hanoi", "saigon", "lottery", "doveso",
        "veso", "matkhau"
    ));

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Password is required.")
                        .addConstraintViolation();
            }
            return false;
        }

        // Check minimum length
        if (password.length() < MIN_LENGTH) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Password must be at least " + MIN_LENGTH + " characters long.")
                        .addConstraintViolation();
            }
            return false;
        }

        // Check maximum length
        if (password.length() > MAX_LENGTH) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Password must be no more than " + MAX_LENGTH + " characters.")
                        .addConstraintViolation();
            }
            return false;
        }

        // Check against blocklist (exact match, case-insensitive)
        if (BLOCKLIST.contains(password.toLowerCase())) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "This password is too common. Please choose a stronger one.")
                        .addConstraintViolation();
            }
            return false;
        }

        // Check against dictionary words (contains)
        String lowerPassword = password.toLowerCase();
        for (String word : DICTIONARY_WORDS) {
            if (lowerPassword.contains(word)) {
                if (context != null) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                            "Password must not contain common words like '" + word + "'.")
                            .addConstraintViolation();
                }
                return false;
            }
        }

        return true;
    }
}