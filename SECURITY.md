# Security Policy

## Supported Versions

Only the latest release of SecureVault receives security updates.

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

The SecureVault engineering team takes security vulnerabilities seriously. If you discover a potential vulnerability, please follow responsible disclosure guidelines:

1. **Do not create a public GitHub issue.**
2. Send a detailed vulnerability report via email to `security@securevault.io` or through GitHub's Private Vulnerability Reporting tab.
3. Include the following details:
   - Type of vulnerability (e.g., IDOR, Cryptographic Misconfiguration, Injection, XSS, CSRF).
   - Step-by-step reproduction instructions or a minimal Proof of Concept (PoC).
   - Impact assessment and potential attack vectors.
   - Any suggested mitigations.

We will acknowledge receipt within 48 hours and provide regular updates on remediation progress.

## Core Security Controls in SecureVault

- **Data at Rest Encryption:** AES-256 in Galois/Counter Mode (GCM) with 96-bit unique nonces per secret and 128-bit authentication tags.
- **Authentication & Credential Hashing:** BCrypt with work factor 12 (or Argon2id) preventing credential exposure even in the event of database compromise.
- **Brute-Force & Credential Stuffing Defense:** Progressive rate limiting and temporary account lockout (5 failed attempts locks account for 15 minutes).
- **IDOR / Access Control:** Scoped queries (`findByIdAndUserId`) ensuring authenticated users cannot query or mutate records belonging to other tenants.
- **CSRF & Session Security:** Spring Security CSRF tokens, strict cookie flags (`HttpOnly`, `SameSite=Lax`), and session fixation migration.
- **Defense in Depth:** Content Security Policy (CSP), frame options (anti-clickjacking), and strict output encoding.
