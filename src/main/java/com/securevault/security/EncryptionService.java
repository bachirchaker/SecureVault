package com.securevault.security;

import com.securevault.config.VaultProperties;
import com.securevault.exception.CryptoException;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Enterprise-grade Cryptographic Service providing AES-256-GCM authenticated encryption (AEAD)
 * for vault secrets.
 *
 * Security Properties:
 * - Confidentiality & Authenticity via AES-256 in Galois/Counter Mode (GCM).
 * - 96-bit (12-byte) unique nonce per encryption, cryptographically random, never reused.
 * - 128-bit authentication tag validating ciphertext integrity and authenticity.
 * - Key derivation capabilities utilizing memory-hard Argon2id.
 * - Zeroizing sensitive byte buffers where feasible.
 */
@Service
public class EncryptionService {

    private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_NONCE_LENGTH_BYTES = 12; // 96 bits recommended for GCM
    private static final int KEY_LENGTH_BYTES = 32;       // 256 bits

    private final SecretKey masterSecretKey;
    private final SecureRandom secureRandom;
    private final VaultProperties vaultProperties;

    public record EncryptedData(String ciphertext, String nonce) {}

    public EncryptionService(VaultProperties vaultProperties) {
        this.vaultProperties = vaultProperties;
        this.secureRandom = new SecureRandom();
        this.masterSecretKey = initializeMasterKey(vaultProperties.getEncryption().getMasterKey());
        log.info("SecureVault EncryptionService initialized with AES-256-GCM authenticated cipher.");
    }

    private SecretKey initializeMasterKey(String masterKeyConfig) {
        try {
            byte[] keyBytes;
            if (masterKeyConfig != null && masterKeyConfig.matches("^[0-9a-fA-F]{64}$")) {
                keyBytes = HexFormat.of().parseHex(masterKeyConfig);
            } else if (masterKeyConfig != null && masterKeyConfig.length() >= 32) {
                keyBytes = Arrays.copyOf(masterKeyConfig.getBytes(StandardCharsets.UTF_8), KEY_LENGTH_BYTES);
            } else {
                log.warn("VAULT_MASTER_KEY not properly set. Generating secure ephemeral 256-bit key for this session.");
                keyBytes = new byte[KEY_LENGTH_BYTES];
                secureRandom.nextBytes(keyBytes);
            }

            if (keyBytes.length != KEY_LENGTH_BYTES) {
                throw new CryptoException("Master key must be exactly 256 bits (32 bytes).");
            }

            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new CryptoException("Failed to initialize AES-256 master key", e);
        }
    }

    /**
     * Encrypts plaintext using AES-256-GCM with a newly generated 96-bit cryptographically secure nonce.
     *
     * @param plaintext Sensitive data string to encrypt.
     * @return EncryptedData containing Base64 encoded ciphertext (including 128-bit GCM tag) and Base64 nonce.
     */
    public EncryptedData encrypt(String plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("Plaintext to encrypt cannot be null");
        }

        byte[] nonce = new byte[GCM_NONCE_LENGTH_BYTES];
        secureRandom.nextBytes(nonce);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, masterSecretKey, parameterSpec);

            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertextBytes = cipher.doFinal(plaintextBytes);

            String ciphertextBase64 = Base64.getEncoder().encodeToString(ciphertextBytes);
            String nonceBase64 = Base64.getEncoder().encodeToString(nonce);

            return new EncryptedData(ciphertextBase64, nonceBase64);
        } catch (Exception e) {
            log.error("Encryption failure occurred");
            throw new CryptoException("Failed to encrypt vault secret", e);
        }
    }

    /**
     * Decrypts AES-256-GCM ciphertext and validates the 128-bit authentication tag.
     * Any alteration of ciphertext or nonce will throw an AEADBadTagException / CryptoException.
     *
     * @param ciphertextBase64 Base64 encoded ciphertext + tag
     * @param nonceBase64 Base64 encoded 96-bit nonce
     * @return Decrypted plaintext string
     */
    public String decrypt(String ciphertextBase64, String nonceBase64) {
        if (ciphertextBase64 == null || nonceBase64 == null) {
            throw new IllegalArgumentException("Ciphertext and nonce cannot be null");
        }

        try {
            byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertextBase64);
            byte[] nonceBytes = Base64.getDecoder().decode(nonceBase64);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonceBytes);
            cipher.init(Cipher.DECRYPT_MODE, masterSecretKey, parameterSpec);

            byte[] decryptedBytes = cipher.doFinal(ciphertextBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (AEADBadTagException e) {
            log.error("Cryptographic tag mismatch: Data tampering detected or incorrect key.");
            throw new CryptoException("Ciphertext authentication failed: data tampering detected.", e);
        } catch (Exception e) {
            log.error("Decryption failure occurred");
            throw new CryptoException("Failed to decrypt vault secret: " + e.getMessage(), e);
        }
    }

    /**
     * Derives a cryptographic key from a master password using Argon2id.
     *
     * @param password User master password
     * @param salt Unique per-user cryptographic salt
     * @return 256-bit derived key bytes
     */
    public byte[] deriveKeyWithArgon2id(char[] password, byte[] salt) {
        VaultProperties.Security.Argon2 argon2Props = vaultProperties.getSecurity().getArgon2();
        
        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(argon2Props.getIterations())
                .withMemoryAsKB(argon2Props.getMemoryKb())
                .withParallelism(argon2Props.getParallelism())
                .withSalt(salt);

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());

        byte[] result = new byte[KEY_LENGTH_BYTES];
        byte[] passwordBytes = new String(password).getBytes(StandardCharsets.UTF_8);
        try {
            generator.generateBytes(passwordBytes, result, 0, result.length);
            return result;
        } finally {
            Arrays.fill(passwordBytes, (byte) 0);
        }
    }

    /**
     * Generates a cryptographically secure random salt.
     */
    public byte[] generateSalt(int lengthBytes) {
        byte[] salt = new byte[lengthBytes];
        secureRandom.nextBytes(salt);
        return salt;
    }
}
