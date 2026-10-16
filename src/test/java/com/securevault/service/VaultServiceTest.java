package com.securevault.service;

import com.securevault.dto.PasswordStrengthResultDto;
import com.securevault.dto.VaultEntryRequestDto;
import com.securevault.dto.VaultEntryResponseDto;
import com.securevault.entity.Category;
import com.securevault.entity.Role;
import com.securevault.entity.User;
import com.securevault.entity.VaultEntry;
import com.securevault.exception.UnauthorizedAccessException;
import com.securevault.mapper.VaultEntryMapper;
import com.securevault.repository.VaultEntryRepository;
import com.securevault.security.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VaultService Unit Tests - CRUD & IDOR Access Control Enforcement")
class VaultServiceTest {

    @Mock
    private VaultEntryRepository vaultEntryRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private PasswordStrengthService strengthService;

    @Mock
    private SecurityAuditService auditService;

    @Mock
    private UserService userService;

    private VaultEntryMapper mapper;
    private VaultService vaultService;

    private User alice;
    private User bob;
    private VaultEntry aliceEntry;

    @BeforeEach
    void setUp() {
        mapper = new VaultEntryMapper();
        vaultService = new VaultService(vaultEntryRepository, encryptionService, strengthService, auditService, mapper, userService);

        alice = new User("alice", "alice@example.com", "hash1");
        alice.setId(101L);
        alice.setRole(Role.ROLE_USER);

        bob = new User("bob", "bob@example.com", "hash2");
        bob.setId(202L);
        bob.setRole(Role.ROLE_USER);

        aliceEntry = new VaultEntry(alice, "Alice Banking", "alice_bank", "encryptedPass123", "nonceBase64", "https://bank.com", Category.LOGIN, "Notes", false, 3);
        aliceEntry.setId(1L);
    }

    @Test
    @DisplayName("Should successfully encrypt and create vault entry for owner")
    void testCreateVaultEntry() {
        VaultEntryRequestDto request = new VaultEntryRequestDto();
        request.setTitle("GitHub");
        request.setUsername("alice_dev");
        request.setPassword("P@ssword123!");
        request.setCategory(Category.LOGIN);

        when(userService.getById(alice.getId())).thenReturn(alice);
        when(encryptionService.encrypt(request.getPassword()))
                .thenReturn(new EncryptionService.EncryptedData("cipherTextXYZ", "nonceABC"));
        when(strengthService.evaluateStrength(request.getPassword()))
                .thenReturn(new PasswordStrengthResultDto(3, "Strong", "primary", 65, "Centuries"));
        when(vaultEntryRepository.save(any(VaultEntry.class))).thenAnswer(invocation -> {
            VaultEntry ve = invocation.getArgument(0);
            ve.setId(50L);
            return ve;
        });

        VaultEntryResponseDto created = vaultService.createEntry(alice.getId(), request);

        assertNotNull(created);
        assertEquals("GitHub", created.getTitle());
        verify(encryptionService, times(1)).encrypt(request.getPassword());
        verify(vaultEntryRepository, times(1)).save(any(VaultEntry.class));
    }

    @Test
    @DisplayName("IDOR Protection: Bob attempting to view Alice's vault entry MUST be rejected with UnauthorizedAccessException")
    void testIdorViewProtection() {
        Long aliceEntryId = aliceEntry.getId();
        Long bobUserId = bob.getId();

        // When repository looks up by entryId and Bob's userId, it returns empty
        when(vaultEntryRepository.findByIdAndUserId(aliceEntryId, bobUserId)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedAccessException.class, () ->
            vaultService.getEntryById(bobUserId, aliceEntryId)
        );
    }

    @Test
    @DisplayName("IDOR Protection: Bob attempting to reveal Alice's password MUST be rejected")
    void testIdorRevealPasswordProtection() {
        Long aliceEntryId = aliceEntry.getId();
        Long bobUserId = bob.getId();

        when(userService.getById(bobUserId)).thenReturn(bob);
        when(vaultEntryRepository.findByIdAndUserId(aliceEntryId, bobUserId)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedAccessException.class, () ->
            vaultService.revealPassword(bobUserId, aliceEntryId)
        );
        verify(encryptionService, never()).decrypt(anyString(), anyString());
    }

    @Test
    @DisplayName("IDOR Protection: Bob attempting to delete Alice's vault entry MUST be rejected")
    void testIdorDeleteProtection() {
        Long aliceEntryId = aliceEntry.getId();
        Long bobUserId = bob.getId();

        when(userService.getById(bobUserId)).thenReturn(bob);
        when(vaultEntryRepository.findByIdAndUserId(aliceEntryId, bobUserId)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedAccessException.class, () ->
            vaultService.deleteEntry(bobUserId, aliceEntryId)
        );
        verify(vaultEntryRepository, never()).delete(any(VaultEntry.class));
    }

    @Test
    @DisplayName("Owner should successfully reveal their own decrypted secret")
    void testOwnerRevealPassword() {
        when(userService.getById(alice.getId())).thenReturn(alice);
        when(vaultEntryRepository.findByIdAndUserId(aliceEntry.getId(), alice.getId())).thenReturn(Optional.of(aliceEntry));
        when(encryptionService.decrypt(aliceEntry.getEncryptedPassword(), aliceEntry.getNonce())).thenReturn("DecryptedPlainTextBankPassword");

        String revealed = vaultService.revealPassword(alice.getId(), aliceEntry.getId());

        assertEquals("DecryptedPlainTextBankPassword", revealed);
        verify(encryptionService, times(1)).decrypt(aliceEntry.getEncryptedPassword(), aliceEntry.getNonce());
    }
}
