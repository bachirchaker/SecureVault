package com.securevault.service;

import com.securevault.dto.PasswordGeneratorOptionsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordGeneratorService Unit Tests")
class PasswordGeneratorServiceTest {

    private PasswordGeneratorService generatorService;

    @BeforeEach
    void setUp() {
        generatorService = new PasswordGeneratorService();
    }

    @Test
    @DisplayName("Should generate password with requested length")
    void testPasswordLength() {
        PasswordGeneratorOptionsDto options = new PasswordGeneratorOptionsDto();
        options.setLength(24);

        String pwd = generatorService.generatePassword(options);
        assertNotNull(pwd);
        assertEquals(24, pwd.length());
    }

    @Test
    @DisplayName("Should enforce inclusion of selected character sets")
    void testCharacterSetInclusion() {
        PasswordGeneratorOptionsDto options = new PasswordGeneratorOptionsDto();
        options.setLength(20);
        options.setUppercase(true);
        options.setLowercase(true);
        options.setNumbers(true);
        options.setSymbols(true);

        for (int i = 0; i < 50; i++) {
            String pwd = generatorService.generatePassword(options);
            assertTrue(pwd.matches(".*[a-z].*"), "Must contain lowercase");
            assertTrue(pwd.matches(".*[A-Z].*"), "Must contain uppercase");
            assertTrue(pwd.matches(".*\\d.*"), "Must contain digit");
            assertTrue(pwd.matches(".*[^a-zA-Z0-9].*"), "Must contain symbol");
        }
    }

    @Test
    @DisplayName("Should avoid ambiguous characters when requested")
    void testAvoidAmbiguousCharacters() {
        PasswordGeneratorOptionsDto options = new PasswordGeneratorOptionsDto();
        options.setLength(32);
        options.setAvoidAmbiguous(true);

        String ambiguous = "1lI0Oo";
        for (int i = 0; i < 50; i++) {
            String pwd = generatorService.generatePassword(options);
            for (char c : ambiguous.toCharArray()) {
                assertFalse(pwd.contains(String.valueOf(c)), "Password must not contain ambiguous character: " + c);
            }
        }
    }
}
