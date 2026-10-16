package com.securevault.service;

import com.securevault.dto.PasswordStrengthResultDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PasswordStrengthService {

    private static final List<String> COMMON_PATTERNS = List.of(
            "password", "123456", "qwerty", "admin", "welcome", "iloveyou", "secret", "master", "pass123", "default"
    );

    public PasswordStrengthResultDto evaluateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrengthResultDto(0, "Empty", "secondary", 0, "0 seconds");
        }

        int score = 0;
        int poolSize = 0;
        List<String> suggestions = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        boolean hasLower = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasUpper = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("\\d").matcher(password).find();
        boolean hasSymbol = Pattern.compile("[^a-zA-Z0-9]").matcher(password).find();

        if (hasLower) poolSize += 26;
        if (hasUpper) poolSize += 26;
        if (hasDigit) poolSize += 10;
        if (hasSymbol) poolSize += 32;

        int len = password.length();

        // Check common patterns
        String lowerPwd = password.toLowerCase();
        for (String common : COMMON_PATTERNS) {
            if (lowerPwd.contains(common)) {
                warnings.add("Contains common dictionary word or pattern: '" + common + "'");
            }
        }

        // Sequential check (e.g. abc, 123)
        if (hasSequentialChars(password)) {
            warnings.add("Contains consecutive sequential characters");
        }

        // Repetitive check (e.g. aaaa)
        if (hasRepetitiveChars(password)) {
            warnings.add("Contains repetitive characters");
        }

        // Entropy calculation: E = L * log2(R)
        double entropy = poolSize > 0 ? len * (Math.log(poolSize) / Math.log(2)) : 0;
        int entropyBits = (int) Math.round(entropy);

        // Scoring based on length, charset diversity and entropy
        if (len >= 8) score++;
        if (len >= 12) score++;
        if ((hasLower && hasUpper && hasDigit) || (len >= 20 && (hasDigit || hasSymbol))) score++;
        if ((hasSymbol && len >= 14) || len >= 24) score++;
        if (entropyBits >= 80 && len >= 16 && warnings.isEmpty()) {
            score = 4;
        }

        // Penalties
        if (!warnings.isEmpty()) {
            score = Math.max(0, score - 1);
        }
        if (len < 8) {
            score = 0;
            warnings.add("Password is too short (minimum 8 characters required)");
        }

        score = Math.min(4, Math.max(0, score));

        // Suggestions
        if (!hasUpper) suggestions.add("Include at least one uppercase letter (A-Z)");
        if (!hasLower) suggestions.add("Include at least one lowercase letter (a-z)");
        if (!hasDigit) suggestions.add("Include at least one number (0-9)");
        if (!hasSymbol) suggestions.add("Include special symbols (!@#$%^&*)");
        if (len < 16) suggestions.add("Increase length to 16+ characters for quantum resistance");

        String label;
        String colorClass;
        String crackTime;

        switch (score) {
            case 0 -> {
                label = "Very Weak";
                colorClass = "danger";
                crackTime = "< 1 second";
            }
            case 1 -> {
                label = "Weak";
                colorClass = "warning";
                crackTime = "A few minutes";
            }
            case 2 -> {
                label = "Fair";
                colorClass = "info";
                crackTime = "Several days";
            }
            case 3 -> {
                label = "Strong";
                colorClass = "primary";
                crackTime = "Multiple centuries";
            }
            case 4 -> {
                label = "Very Strong";
                colorClass = "success";
                crackTime = "Millions of years";
            }
            default -> {
                label = "Unknown";
                colorClass = "secondary";
                crackTime = "Unknown";
            }
        }

        PasswordStrengthResultDto result = new PasswordStrengthResultDto(score, label, colorClass, entropyBits, crackTime);
        result.setSuggestions(suggestions);
        result.setWarnings(warnings);
        return result;
    }

    private boolean hasSequentialChars(String s) {
        if (s.length() < 3) return false;
        for (int i = 0; i < s.length() - 2; i++) {
            char c1 = s.charAt(i);
            char c2 = s.charAt(i + 1);
            char c3 = s.charAt(i + 2);
            if (c2 == c1 + 1 && c3 == c2 + 1) return true;
            if (c2 == c1 - 1 && c3 == c2 - 1) return true;
        }
        return false;
    }

    private boolean hasRepetitiveChars(String s) {
        if (s.length() < 3) return false;
        for (int i = 0; i < s.length() - 2; i++) {
            if (s.charAt(i) == s.charAt(i + 1) && s.charAt(i + 1) == s.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }
}
