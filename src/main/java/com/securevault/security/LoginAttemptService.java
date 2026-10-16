package com.securevault.security;

import com.securevault.config.VaultProperties;
import com.securevault.entity.SecurityEventType;
import com.securevault.entity.Severity;
import com.securevault.entity.User;
import com.securevault.repository.UserRepository;
import com.securevault.service.SecurityAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final UserRepository userRepository;
    private final VaultProperties vaultProperties;
    private final SecurityAuditService auditService;

    public LoginAttemptService(UserRepository userRepository,
                               VaultProperties vaultProperties,
                               SecurityAuditService auditService) {
        this.userRepository = userRepository;
        this.vaultProperties = vaultProperties;
        this.auditService = auditService;
    }

    @Transactional
    public void loginSucceeded(String identifier) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(identifier);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFailedAttempts(0);
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            userRepository.save(user);

            auditService.recordEvent(user, SecurityEventType.LOGIN_SUCCESS, "Successful user authentication", Severity.INFO);
        }
    }

    @Transactional
    public void loginFailed(String identifier) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(identifier);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int maxAttempts = vaultProperties.getSecurity().getMaxFailedAttempts();
            int newAttempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(newAttempts);

            if (newAttempts >= maxAttempts) {
                user.setAccountNonLocked(false);
                user.setLockTime(LocalDateTime.now());
                log.warn("Account [{}] locked after {} consecutive failed login attempts.", user.getUsername(), newAttempts);

                auditService.recordEvent(user, SecurityEventType.ACCOUNT_LOCKED,
                        "Account locked for " + vaultProperties.getSecurity().getLockoutDurationMinutes() +
                        " minutes after " + newAttempts + " failed attempts", Severity.CRITICAL);
            } else {
                auditService.recordEvent(user, SecurityEventType.LOGIN_FAILED,
                        "Failed login attempt (" + newAttempts + "/" + maxAttempts + ")", Severity.WARNING);
            }
            userRepository.save(user);
        } else {
            // Record generic failed attempt without leaking account non-existence
            auditService.recordEvent((User) null, SecurityEventType.LOGIN_FAILED,
                    "Failed authentication attempt for unknown identifier: " + identifier, Severity.WARNING);
        }
    }

    @Transactional(readOnly = true)
    public boolean isAccountLocked(String identifier) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(identifier);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        if (user.isAccountNonLocked()) {
            return false;
        }
        if (user.getLockTime() == null) {
            return false;
        }

        int lockoutMinutes = vaultProperties.getSecurity().getLockoutDurationMinutes();
        return user.getLockTime().plusMinutes(lockoutMinutes).isAfter(LocalDateTime.now());
    }
}
