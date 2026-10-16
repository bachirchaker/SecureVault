package com.securevault.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vault")
public class VaultProperties {

    private final Encryption encryption = new Encryption();
    private final Security security = new Security();

    public Encryption getEncryption() {
        return encryption;
    }

    public Security getSecurity() {
        return security;
    }

    public static class Encryption {
        private String masterKey = "4f9a7b2c6d8e0f1a3b5c7d9e1f3a5b7c9d1e3f5a7b9c1d3e5f7a9b1c3d5e7f9a";
        private String algorithm = "AES";
        private String cipher = "AES/GCM/NoPadding";
        private int tagLengthBits = 128;
        private int nonceLengthBytes = 12;

        public String getMasterKey() {
            return masterKey;
        }

        public void setMasterKey(String masterKey) {
            this.masterKey = masterKey;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public String getCipher() {
            return cipher;
        }

        public void setCipher(String cipher) {
            this.cipher = cipher;
        }

        public int getTagLengthBits() {
            return tagLengthBits;
        }

        public void setTagLengthBits(int tagLengthBits) {
            this.tagLengthBits = tagLengthBits;
        }

        public int getNonceLengthBytes() {
            return nonceLengthBytes;
        }

        public void setNonceLengthBytes(int nonceLengthBytes) {
            this.nonceLengthBytes = nonceLengthBytes;
        }
    }

    public static class Security {
        private int maxFailedAttempts = 5;
        private int lockoutDurationMinutes = 15;
        private Argon2 argon2 = new Argon2();

        public int getMaxFailedAttempts() {
            return maxFailedAttempts;
        }

        public void setMaxFailedAttempts(int maxFailedAttempts) {
            this.maxFailedAttempts = maxFailedAttempts;
        }

        public int getLockoutDurationMinutes() {
            return lockoutDurationMinutes;
        }

        public void setLockoutDurationMinutes(int lockoutDurationMinutes) {
            this.lockoutDurationMinutes = lockoutDurationMinutes;
        }

        public Argon2 getArgon2() {
            return argon2;
        }

        public void setArgon2(Argon2 argon2) {
            this.argon2 = argon2;
        }

        public static class Argon2 {
            private int iterations = 3;
            private int memoryKb = 65536;
            private int parallelism = 1;
            private int saltLength = 16;
            private int hashLength = 32;

            public int getIterations() {
                return iterations;
            }

            public void setIterations(int iterations) {
                this.iterations = iterations;
            }

            public int getMemoryKb() {
                return memoryKb;
            }

            public void setMemoryKb(int memoryKb) {
                this.memoryKb = memoryKb;
            }

            public int getParallelism() {
                return parallelism;
            }

            public void setParallelism(int parallelism) {
                this.parallelism = parallelism;
            }

            public int getSaltLength() {
                return saltLength;
            }

            public void setSaltLength(int saltLength) {
                this.saltLength = saltLength;
            }

            public int getHashLength() {
                return hashLength;
            }

            public void setHashLength(int hashLength) {
                this.hashLength = hashLength;
            }
        }
    }
}
