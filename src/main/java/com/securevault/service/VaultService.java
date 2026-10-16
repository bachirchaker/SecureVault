package com.securevault.service;

import com.securevault.dto.*;
import com.securevault.entity.Category;
import com.securevault.entity.SecurityEventType;
import com.securevault.entity.Severity;
import com.securevault.entity.User;
import com.securevault.entity.VaultEntry;
import com.securevault.exception.ResourceNotFoundException;
import com.securevault.exception.UnauthorizedAccessException;
import com.securevault.mapper.VaultEntryMapper;
import com.securevault.repository.VaultEntryRepository;
import com.securevault.security.EncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class VaultService {

    private static final Logger log = LoggerFactory.getLogger(VaultService.class);

    private final VaultEntryRepository vaultEntryRepository;
    private final EncryptionService encryptionService;
    private final PasswordStrengthService strengthService;
    private final SecurityAuditService auditService;
    private final VaultEntryMapper mapper;
    private final UserService userService;

    public VaultService(VaultEntryRepository vaultEntryRepository,
                        EncryptionService encryptionService,
                        PasswordStrengthService strengthService,
                        SecurityAuditService auditService,
                        VaultEntryMapper mapper,
                        UserService userService) {
        this.vaultEntryRepository = vaultEntryRepository;
        this.encryptionService = encryptionService;
        this.strengthService = strengthService;
        this.auditService = auditService;
        this.mapper = mapper;
        this.userService = userService;
    }

    @Transactional
    public VaultEntryResponseDto createEntry(Long userId, VaultEntryRequestDto dto) {
        User user = userService.getById(userId);

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required when creating a vault entry");
        }

        // Authenticated AES-256-GCM encryption
        EncryptionService.EncryptedData encryptedData = encryptionService.encrypt(dto.getPassword());
        PasswordStrengthResultDto strength = strengthService.evaluateStrength(dto.getPassword());

        VaultEntry entry = new VaultEntry(
                user,
                dto.getTitle(),
                dto.getUsername(),
                encryptedData.ciphertext(),
                encryptedData.nonce(),
                dto.getWebsiteUrl(),
                dto.getCategory(),
                dto.getNotes(),
                dto.isFavorite(),
                strength.getScore()
        );

        VaultEntry saved = vaultEntryRepository.save(entry);
        auditService.recordEvent(user, SecurityEventType.VAULT_ENTRY_CREATED,
                "Created credential entry: " + saved.getTitle() + " (" + saved.getCategory().getDisplayName() + ")",
                Severity.INFO);

        return mapper.toResponseDto(saved);
    }

    @Transactional
    public VaultEntryResponseDto updateEntry(Long userId, Long entryId, VaultEntryRequestDto dto) {
        User user = userService.getById(userId);

        // IDOR protection: strictly scope lookup to the authenticated user ID
        VaultEntry entry = vaultEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new UnauthorizedAccessException("You do not have access to this vault entry or it does not exist"));

        entry.setTitle(dto.getTitle());
        entry.setUsername(dto.getUsername());
        entry.setWebsiteUrl(dto.getWebsiteUrl());
        entry.setCategory(dto.getCategory() != null ? dto.getCategory() : Category.LOGIN);
        entry.setNotes(dto.getNotes());
        entry.setFavorite(dto.isFavorite());

        // Only update password if a new one was provided
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            EncryptionService.EncryptedData encryptedData = encryptionService.encrypt(dto.getPassword());
            PasswordStrengthResultDto strength = strengthService.evaluateStrength(dto.getPassword());
            entry.setEncryptedPassword(encryptedData.ciphertext());
            entry.setNonce(encryptedData.nonce());
            entry.setPasswordStrengthScore(strength.getScore());
        }

        VaultEntry updated = vaultEntryRepository.save(entry);
        auditService.recordEvent(user, SecurityEventType.VAULT_ENTRY_UPDATED,
                "Updated credential entry: " + updated.getTitle(), Severity.INFO);

        return mapper.toResponseDto(updated);
    }

    @Transactional
    public void deleteEntry(Long userId, Long entryId) {
        User user = userService.getById(userId);

        // IDOR protection: verifies that the target entry belongs strictly to the authenticated user
        VaultEntry entry = vaultEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new UnauthorizedAccessException("You do not have access to delete this vault entry"));

        String title = entry.getTitle();
        vaultEntryRepository.delete(entry);

        auditService.recordEvent(user, SecurityEventType.VAULT_ENTRY_DELETED,
                "Deleted credential entry: " + title, Severity.INFO);
    }

    @Transactional(readOnly = true)
    public VaultEntryResponseDto getEntryById(Long userId, Long entryId) {
        VaultEntry entry = vaultEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new UnauthorizedAccessException("Entry not found or access denied"));
        return mapper.toResponseDto(entry);
    }

    @Transactional
    public String revealPassword(Long userId, Long entryId) {
        User user = userService.getById(userId);

        VaultEntry entry = vaultEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new UnauthorizedAccessException("Entry not found or access denied"));

        String decrypted = encryptionService.decrypt(entry.getEncryptedPassword(), entry.getNonce());

        auditService.recordEvent(user, SecurityEventType.VAULT_PASSWORD_REVEALED,
                "Revealed password for credential entry: " + entry.getTitle(), Severity.WARNING);

        return decrypted;
    }

    @Transactional
    public void toggleFavorite(Long userId, Long entryId) {
        VaultEntry entry = vaultEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new UnauthorizedAccessException("Entry not found or access denied"));
        entry.setFavorite(!entry.isFavorite());
        vaultEntryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<VaultEntryResponseDto> getEntries(Long userId, Category category, String query, Boolean favoritesOnly, String sortField) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("title".equalsIgnoreCase(sortField)) {
            sort = Sort.by(Sort.Direction.ASC, "title");
        } else if ("strength".equalsIgnoreCase(sortField)) {
            sort = Sort.by(Sort.Direction.ASC, "passwordStrengthScore");
        } else if ("updated".equalsIgnoreCase(sortField)) {
            sort = Sort.by(Sort.Direction.DESC, "updatedAt");
        }

        List<VaultEntry> entries;

        if (favoritesOnly != null && favoritesOnly) {
            entries = vaultEntryRepository.findByUserIdAndFavoriteTrue(userId, sort);
        } else if (query != null && !query.isBlank()) {
            if (category != null) {
                entries = vaultEntryRepository.searchByUserAndCategoryAndQuery(userId, category, query.trim(), sort);
            } else {
                entries = vaultEntryRepository.searchByUserAndQuery(userId, query.trim(), sort);
            }
        } else if (category != null) {
            entries = vaultEntryRepository.findByUserIdAndCategory(userId, category, sort);
        } else {
            entries = vaultEntryRepository.findByUserId(userId, sort);
        }

        return entries.stream().map(mapper::toResponseDto).toList();
    }

    @Transactional
    public List<VaultEntryExportDto> exportVault(Long userId, boolean includePasswords) {
        User user = userService.getById(userId);
        List<VaultEntry> entries = vaultEntryRepository.findByUserId(userId, Sort.by(Sort.Direction.ASC, "title"));

        List<VaultEntryExportDto> exportList = new ArrayList<>();
        for (VaultEntry entry : entries) {
            String decryptedPassword = null;
            if (includePasswords) {
                decryptedPassword = encryptionService.decrypt(entry.getEncryptedPassword(), entry.getNonce());
            }
            exportList.add(mapper.toExportDto(entry, decryptedPassword));
        }

        auditService.recordEvent(user, SecurityEventType.VAULT_EXPORTED,
                "Exported vault items (include passwords: " + includePasswords + ")",
                includePasswords ? Severity.CRITICAL : Severity.INFO);

        return exportList;
    }

    @Transactional(readOnly = true)
    public SecurityStatsDto getSecurityDashboardStats(Long userId) {
        List<VaultEntry> entries = vaultEntryRepository.findByUserId(userId, Sort.by(Sort.Direction.DESC, "createdAt"));
        SecurityStatsDto stats = new SecurityStatsDto();
        stats.setTotalCredentials(entries.size());

        if (entries.isEmpty()) {
            stats.setHealthScore(100);
            stats.getSecurityRecommendations().add("Your vault is empty. Start adding your passwords to protect your online accounts!");
            stats.setRecentEvents(auditService.getRecentEvents(userId));
            return stats;
        }

        long weakCount = 0;
        Map<String, Integer> strengthDist = new HashMap<>();
        strengthDist.put("Very Weak", 0);
        strengthDist.put("Weak", 0);
        strengthDist.put("Fair", 0);
        strengthDist.put("Strong", 0);
        strengthDist.put("Very Strong", 0);

        Map<String, Long> categoryCounts = new HashMap<>();

        // Safe in-memory password reuse detection (isolated to this authorized user)
        Map<String, Integer> passwordOccurrences = new HashMap<>();

        for (VaultEntry entry : entries) {
            int score = entry.getPasswordStrengthScore();
            if (score <= 1) {
                weakCount++;
            }

            switch (score) {
                case 0 -> strengthDist.put("Very Weak", strengthDist.get("Very Weak") + 1);
                case 1 -> strengthDist.put("Weak", strengthDist.get("Weak") + 1);
                case 2 -> strengthDist.put("Fair", strengthDist.get("Fair") + 1);
                case 3 -> strengthDist.put("Strong", strengthDist.get("Strong") + 1);
                case 4 -> strengthDist.put("Very Strong", strengthDist.get("Very Strong") + 1);
            }

            String catName = entry.getCategory().getDisplayName();
            categoryCounts.put(catName, categoryCounts.getOrDefault(catName, 0L) + 1);

            try {
                String plain = encryptionService.decrypt(entry.getEncryptedPassword(), entry.getNonce());
                passwordOccurrences.put(plain, passwordOccurrences.getOrDefault(plain, 0) + 1);
            } catch (Exception e) {
                log.warn("Could not decrypt password during reuse check for entry id: {}", entry.getId());
            }
        }

        long reusedCount = passwordOccurrences.values().stream().filter(count -> count > 1).count();

        stats.setWeakPasswords(weakCount);
        stats.setReusedPasswords(reusedCount);
        stats.setStrengthDistribution(strengthDist);
        stats.setCategoryCounts(categoryCounts);

        // Calculate holistic health score (0-100)
        double total = entries.size();
        double weakPenalty = (weakCount / total) * 45.0;
        double reusePenalty = (reusedCount / total) * 35.0;
        int healthScore = (int) Math.max(0, Math.round(100.0 - weakPenalty - reusePenalty));
        stats.setHealthScore(healthScore);

        // Security recommendations
        List<String> recommendations = new ArrayList<>();
        if (weakCount > 0) {
            recommendations.add("You have " + weakCount + " weak or very weak password(s). Update them with generated 16+ character passwords.");
        }
        if (reusedCount > 0) {
            recommendations.add("You have " + reusedCount + " reused password(s) across different services. Credential reuse is a major attack vector.");
        }
        if (strengthDist.get("Very Strong") + strengthDist.get("Strong") == entries.size() && reusedCount == 0) {
            recommendations.add("Excellent vault hygiene! All stored credentials meet enterprise security standards.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Consider setting up reminders to periodically rotate sensitive credentials.");
        }
        stats.setSecurityRecommendations(recommendations);

        // Recent items
        stats.setRecentEntries(entries.stream().limit(5).map(mapper::toResponseDto).toList());
        stats.setRecentEvents(auditService.getRecentEvents(userId));

        return stats;
    }
}
