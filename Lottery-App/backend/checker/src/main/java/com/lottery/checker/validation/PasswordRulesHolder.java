package com.lottery.checker.validation;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:password-rules.properties")
public class PasswordRulesHolder {

    @Value("${password.min-length}")
    private int minLength;

    @Value("${password.max-length}")
    private int maxLength;

    @Value("${password.blocklist}")
    private String blocklistCsv;

    @Value("${password.dictionary-words}")
    private String dictionaryWordsCsv;

    private static int MIN_LENGTH;
    private static int MAX_LENGTH;
    private static Set<String> BLOCKLIST = new HashSet<>();
    private static Set<String> DICTIONARY_WORDS = new HashSet<>();

    @PostConstruct
    public void init() {
        MIN_LENGTH = this.minLength;
        MAX_LENGTH = this.maxLength;
        BLOCKLIST = parseSet(this.blocklistCsv);
        DICTIONARY_WORDS = parseSet(this.dictionaryWordsCsv);
    }

    private Set<String> parseSet(String csv) {
        if (csv == null || csv.isBlank()) return new HashSet<>();
        return new HashSet<>(Arrays.asList(csv.toLowerCase().split(",")));
    }

    public static int getMinLength() { return MIN_LENGTH; }
    public static int getMaxLength() { return MAX_LENGTH; }
    public static Set<String> getBlocklist() { return BLOCKLIST; }
    public static Set<String> getDictionaryWords() { return DICTIONARY_WORDS; }
}