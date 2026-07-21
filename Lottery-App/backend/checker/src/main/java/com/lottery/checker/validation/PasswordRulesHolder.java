package com.lottery.checker.validation;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:password-rules.properties")
public class PasswordRulesHolder {

    @Value("${password.min-length:10}")
    private int minLength;

    @Value("${password.max-length:64}")
    private int maxLength;

    @Value("${password.blocklist:}")
    private String blocklistCsv;

    @Value("${password.dictionary-words:}")
    private String dictionaryWordsCsv;

    private Set<String> blocklist = Collections.emptySet();
    private Set<String> dictionaryWords = Collections.emptySet();

    @PostConstruct
    public void init() {
        this.blocklist = parseSet(this.blocklistCsv);
        this.dictionaryWords = parseSet(this.dictionaryWordsCsv);
    }

    private Set<String> parseSet(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptySet();
        return Collections.unmodifiableSet(
            Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet())
        );
    }

    public int getMinLength() { return minLength; }
    public int getMaxLength() { return maxLength; }
    public Set<String> getBlocklist() { return Set.copyOf(blocklist); }
    public Set<String> getDictionaryWords() { return Set.copyOf(dictionaryWords); }
}