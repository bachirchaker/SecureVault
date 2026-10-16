package com.securevault.service;

import com.securevault.dto.PasswordGeneratorOptionsDto;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PasswordGeneratorService {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    // Ambiguous characters: 1, l, I, 0, O, o
    private static final String AMBIGUOUS_CHARS = "1lI0Oo";

    private final SecureRandom secureRandom = new SecureRandom();

    public String generatePassword(PasswordGeneratorOptionsDto options) {
        int length = Math.max(8, Math.min(options.getLength(), 128));

        StringBuilder pool = new StringBuilder();
        List<Character> mandatoryChars = new ArrayList<>();

        String lowerPool = filterAmbiguous(LOWER, options.isAvoidAmbiguous());
        String upperPool = filterAmbiguous(UPPER, options.isAvoidAmbiguous());
        String numberPool = filterAmbiguous(NUMBERS, options.isAvoidAmbiguous());
        String symbolPool = filterAmbiguous(SYMBOLS, options.isAvoidAmbiguous());

        if (options.isLowercase() && !lowerPool.isEmpty()) {
            pool.append(lowerPool);
            mandatoryChars.add(getRandomChar(lowerPool));
        }
        if (options.isUppercase() && !upperPool.isEmpty()) {
            pool.append(upperPool);
            mandatoryChars.add(getRandomChar(upperPool));
        }
        if (options.isNumbers() && !numberPool.isEmpty()) {
            pool.append(numberPool);
            mandatoryChars.add(getRandomChar(numberPool));
        }
        if (options.isSymbols() && !symbolPool.isEmpty()) {
            pool.append(symbolPool);
            mandatoryChars.add(getRandomChar(symbolPool));
        }

        // Fallback to lowercase + numbers if nothing selected
        if (pool.isEmpty()) {
            pool.append(lowerPool).append(numberPool);
            mandatoryChars.add(getRandomChar(lowerPool));
            mandatoryChars.add(getRandomChar(numberPool));
        }

        String characterPool = pool.toString();
        List<Character> passwordChars = new ArrayList<>(mandatoryChars);

        while (passwordChars.size() < length) {
            passwordChars.add(getRandomChar(characterPool));
        }

        // Secure Fisher-Yates shuffle
        for (int i = passwordChars.size() - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            Collections.swap(passwordChars, i, j);
        }

        StringBuilder sb = new StringBuilder(passwordChars.size());
        for (char c : passwordChars) {
            sb.append(c);
        }
        return sb.toString();
    }

    private char getRandomChar(String source) {
        int index = secureRandom.nextInt(source.length());
        return source.charAt(index);
    }

    private String filterAmbiguous(String source, boolean avoidAmbiguous) {
        if (!avoidAmbiguous) {
            return source;
        }
        StringBuilder sb = new StringBuilder();
        for (char c : source.toCharArray()) {
            if (AMBIGUOUS_CHARS.indexOf(c) == -1) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
