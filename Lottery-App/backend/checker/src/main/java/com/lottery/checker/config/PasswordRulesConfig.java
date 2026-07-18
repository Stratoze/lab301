package com.lottery.checker.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "password")
@PropertySource("classpath:password-rules.properties")
public class PasswordRulesConfig {

    private int minLength;
    private int maxLength;
    private String blocklist;
    private String dictionaryWords;

    public int getMinLength() { return minLength; }
    public void setMinLength(int minLength) { this.minLength = minLength; }

    public int getMaxLength() { return maxLength; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }

    public Set<String> getBlocklist() {
        return parseSet(blocklist);
    }
    public void setBlocklist(String blocklist) { this.blocklist = blocklist; }

    public Set<String> getDictionaryWords() {
        return parseSet(dictionaryWords);
    }
    public void setDictionaryWords(String dictionaryWords) { this.dictionaryWords = dictionaryWords; }

    private Set<String> parseSet(String csv) {
        if (csv == null || csv.isBlank()) return new HashSet<>();
        return new HashSet<>(Arrays.asList(csv.split(",")));
    }
}