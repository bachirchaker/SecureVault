package com.securevault.security;

import com.securevault.config.VaultProperties;
import com.securevault.exception.CryptoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EncryptionService Unit Tests - AES-256-GCM & AEAD Integrity")
class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        VaultProperties properties = new VaultProperties();
        // 64 hex characters = 32 bytes (256-bit AES master key)
        properties.getEncryption().setMasterKey("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        encryptionService = new EncryptionService(properties);
    }

    @Test
    @DisplayName("Should successfully encrypt and decrypt plaintext secrets")
    void testEncryptAndDecrypt() {
        String plaintext = "SuperSecretP@ssw0rd!2026";
        EncryptionService.EncryptedData encryptedData = encryptionService.encrypt(plaintext);

        assertNotNull(encryptedData.ciphertext());
        assertNotNull(encryptedData.nonce());
        assertNotEquals(plaintext, encryptedData.ciphertext());

        String decrypted = encryptionService.decrypt(encryptedData.ciphertext(), encryptedData.nonce());
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("Should generate unique nonces for every encryption operation (Never reuse IV)")
    void testNonceUniqueness() {
        String plaintext = "ConstantSecretText";
        Set<String> nonces = new HashSet<>();
        Set<String> ciphertexts = new HashSet<>();

        int iterations = 100;
        for (int i = 0; i < iterations; i++) {
            EncryptionService.EncryptedData encrypted = encryptionService.encrypt(plaintext);
            nonces.add(encrypted.nonce());
            ciphertexts.add(encrypted.ciphertext());
        }

        // Every nonce and ciphertext must be strictly unique
        assertEquals(iterations, nonces.size(), "All 100 nonces must be cryptographically unique");
        assertEquals(iterations, ciphertexts.size(), "Ciphertexts must differ due to unique nonces");
    }

    @Test
    @DisplayName("Should detect tampered ciphertext and reject decryption via AEAD tag validation")
    void testCiphertextTamperingRejection() {
        String plaintext = "ConfidentialDatabasePassword#99";
        EncryptionService.EncryptedData encrypted = encryptionService.encrypt(plaintext);

        byte[] rawCiphertext = Base64.getDecoder().decode(encrypted.ciphertext());
        // Flip one single bit in the ciphertext
        rawCiphertext[rawCiphertext.length / 2] ^= 0x01;
        String tamperedCiphertext = Base64.getEncoder().encodeToString(rawCiphertext);

        // AEADBadTagException wrapped in CryptoException must be thrown
        CryptoException ex = assertThrows(CryptoException.class, () -> 
            encryptionService.decrypt(tamperedCiphertext, encrypted.nonce())
        );
        assertTrue(ex.getMessage().contains("authentication failed") || ex.getMessage().contains("tampering"),
                "Tampered ciphertext must fail authentication tag check");
    }

    @Test
    @DisplayName("Should reject decryption if nonce has been modified")
    void testModifiedNonceRejection() {
        String plaintext = "ConfidentialToken$42";
        EncryptionService.EncryptedData encrypted = encryptionService.encrypt(plaintext);

        byte[] rawNonce = Base64.getDecoder().decode(encrypted.nonce());
        rawNonce[0] ^= 0xFF; // tamper first byte of nonce
        String tamperedNonce = Base64.getEncoder().encodeToString(rawNonce);

        assertThrows(CryptoException.class, () ->
            encryptionService.decrypt(encrypted.ciphertext(), tamperedNonce)
        );
    }

    @Test
    @DisplayName("Should derive deterministic 256-bit key using Argon2id given same password and salt")
    void testArgon2idKeyDerivation() {
        char[] password = "MasterVaultPassword123!".toCharArray();
        byte[] salt = encryptionService.generateSalt(16);

        byte[] key1 = encryptionService.deriveKeyWithArgon2id(password, salt);
        byte[] key2 = encryptionService.deriveKeyWithArgon2id(password, salt);

        assertEquals(32, key1.length, "Derived key must be exactly 256 bits (32 bytes)");
        assertArrayEquals(key1, key2, "Argon2id key derivation must be deterministic for identical salt");

        byte[] differentSalt = encryptionService.generateSalt(16);
        byte[] key3 = encryptionService.deriveKeyWithArgon2id(password, differentSalt);
        assertFalse(java.util.Arrays.equals(key1, key3), "Different salt must yield completely different key");
    }
}
