package com.securevault.security;

import com.securevault.config.VaultProperties;
import com.securevault.entity.Role;
import com.securevault.entity.User;
import com.securevault.repository.UserRepository;
import com.securevault.service.SecurityAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginAttemptService Unit Tests - Brute-Force & Lockout Defense")
class LoginAttemptServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityAuditService auditService;

    private VaultProperties vaultProperties;
    private LoginAttemptService loginAttemptService;

    private User testUser;

    @BeforeEach
    void setUp() {
        vaultProperties = new VaultProperties();
        vaultProperties.getSecurity().setMaxFailedAttempts(5);
        vaultProperties.getSecurity().setLockoutDurationMinutes(15);

        loginAttemptService = new LoginAttemptService(userRepository, vaultProperties, auditService);

        testUser = new User("alice", "alice@example.com", "$2a$12$hashedPassword");
        testUser.setId(1L);
        testUser.setRole(Role.ROLE_USER);
        testUser.setFailedAttempts(0);
        testUser.setAccountNonLocked(true);
    }

    @Test
    @DisplayName("Should increment failed attempts on bad credentials")
    void testFailedLoginIncrement() {
        when(userRepository.findByUsernameOrEmail("alice")).thenReturn(Optional.of(testUser));

        loginAttemptService.loginFailed("alice");

        assertEquals(1, testUser.getFailedAttempts());
        assertTrue(testUser.isAccountNonLocked());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should lock account when failed attempts reach threshold (5 attempts)")
    void testAccountLockoutOnMaxAttempts() {
        testUser.setFailedAttempts(4);
        when(userRepository.findByUsernameOrEmail("alice")).thenReturn(Optional.of(testUser));

        loginAttemptService.loginFailed("alice");

        assertEquals(5, testUser.getFailedAttempts());
        assertFalse(testUser.isAccountNonLocked());
        assertNotNull(testUser.getLockTime());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should reset failed attempts and unlock on successful login")
    void testSuccessfulLoginResetsAttempts() {
        testUser.setFailedAttempts(3);
        when(userRepository.findByUsernameOrEmail("alice")).thenReturn(Optional.of(testUser));

        loginAttemptService.loginSucceeded("alice");

        assertEquals(0, testUser.getFailedAttempts());
        assertTrue(testUser.isAccountNonLocked());
        assertNull(testUser.getLockTime());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should accurately report locked status during lockout duration")
    void testIsAccountLockedStatus() {
        testUser.setAccountNonLocked(false);
        testUser.setLockTime(LocalDateTime.now().minusMinutes(5)); // locked 5 mins ago (limit is 15)
        when(userRepository.findByUsernameOrEmail("alice")).thenReturn(Optional.of(testUser));

        assertTrue(loginAttemptService.isAccountLocked("alice"));

        // After 16 minutes (beyond 15 min limit)
        testUser.setLockTime(LocalDateTime.now().minusMinutes(16));
        assertFalse(loginAttemptService.isAccountLocked("alice"));
    }
}
