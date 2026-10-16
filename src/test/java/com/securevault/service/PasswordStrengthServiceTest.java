package com.securevault.service;

import com.securevault.dto.PasswordStrengthResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordStrengthService Unit Tests - NIST & Entropy Evaluation")
class PasswordStrengthServiceTest {

    private PasswordStrengthService strengthService;

    @BeforeEach
    void setUp() {
        strengthService = new PasswordStrengthService();
    }

    @Test
    @DisplayName("Should rate short or common passwords as Very Weak or Weak")
    void testWeakPasswords() {
        PasswordStrengthResultDto r1 = strengthService.evaluateStrength("123456");
        assertEquals(0, r1.getScore());
        assertEquals("Very Weak", r1.getLabel());

        PasswordStrengthResultDto r2 = strengthService.evaluateStrength("password123");
        assertTrue(r2.getScore() <= 1);
        assertFalse(r2.getWarnings().isEmpty());
    }

    @Test
    @DisplayName("Should rate long high-entropy complex passwords as Strong or Very Strong")
    void testStrongPasswords() {
        PasswordStrengthResultDto r1 = strengthService.evaluateStrength("X9#mK$2vL!pQ8@zR");
        assertTrue(r1.getScore() >= 3);
        assertTrue(r1.getEntropyBits() > 60);

        PasswordStrengthResultDto r2 = strengthService.evaluateStrength("c0rr3ct-h0rs3-b@tt3ry-st@pl3-2026!!");
        assertEquals(4, r2.getScore());
        assertEquals("Very Strong", r2.getLabel());
        assertEquals("Millions of years", r2.getEstimatedCrackTime());
    }
}
