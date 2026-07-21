package com.lottery.checker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private final PasswordRulesHolder rules;

    public PasswordValidator(PasswordRulesHolder rules) {
        this.rules = rules;
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        int minLength = rules.getMinLength();
        int maxLength = rules.getMaxLength();
        var blocklist = rules.getBlocklist();
        var dictionaryWords = rules.getDictionaryWords();

        if (password == null || password.isBlank()) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Password is required.")
                        .addConstraintViolation();
            }
            return false;
        }

        if (password.length() < minLength) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Password must be at least " + minLength + " characters long.")
                        .addConstraintViolation();
            }
            return false;
        }

        if (password.length() > maxLength) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Password must be no more than " + maxLength + " characters.")
                        .addConstraintViolation();
            }
            return false;
        }

        if (blocklist.contains(password.toLowerCase())) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "This password is too common. Please choose a stronger one.")
                        .addConstraintViolation();
            }
            return false;
        }

        String lowerPassword = password.toLowerCase();
        for (String word : dictionaryWords) {
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