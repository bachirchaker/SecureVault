# Contributing to SecureVault

Thank you for your interest in contributing to **SecureVault**, an enterprise-grade password management application developed to demonstrate advanced software engineering and cybersecurity best practices.

## Code of Conduct

We are committed to providing a welcoming, inclusive, and harassment-free environment for all contributors. Please adhere to respectful communication in issues, pull requests, and discussions.

## Development Setup

### Prerequisites
- Java 21 JDK (Amazon Corretto, Eclipse Temurin, Oracle JDK, or JetBrains JBR)
- Apache Maven 3.9+ (or use the included `./mvnw` / `mvnw.cmd` wrapper)
- MySQL 8.0+

### Setting Up Locally
1. Fork and clone the repository:
   ```bash
   git clone https://github.com/your-username/SecureVault.git
   cd SecureVault
   ```
2. Copy environment template and configure secrets:
   ```bash
   cp .env.example .env
   ```
3. Run automated tests:
   ```bash
   ./mvnw clean test
   ```
4. Start the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Security Best Practices for Contributions

When modifying or adding code to SecureVault, you must adhere to the following principles:

1. **Zero Secret Leakage:** Never log passwords, encryption keys, nonces, session identifiers, or user secrets.
2. **Strict Cryptography:** Use authenticated encryption (AES-256-GCM) with unique nonces. Never use ECB mode, DES, MD5, or weak SHA-1.
3. **IDOR Defense:** Every query that accesses or mutates a vault resource must explicitly scope by `userId` (e.g., `findByIdAndUserId`).
4. **Input Validation:** Use Bean Validation (`@Valid`, custom validators) on all request transfer objects.
5. **No Secret Commits:** Ensure `.env`, certificates, or private keys are never staged or committed.

## Pull Request Guidelines

1. Create a feature branch: `git checkout -b feature/your-feature-name`.
2. Write unit and integration tests covering both happy paths and security boundary conditions (e.g., unauthorized IDOR attempts, tampering).
3. Ensure all tests pass: `./mvnw test`.
4. Ensure clean code formatting following Standard Java Conventions.
5. Submit a pull request with a clear description of changes and rationale.
