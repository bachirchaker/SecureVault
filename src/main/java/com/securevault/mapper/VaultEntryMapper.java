package com.securevault.mapper;

import com.securevault.dto.VaultEntryExportDto;
import com.securevault.dto.VaultEntryResponseDto;
import com.securevault.entity.VaultEntry;
import org.springframework.stereotype.Component;

@Component
public class VaultEntryMapper {

    public VaultEntryResponseDto toResponseDto(VaultEntry entry) {
        if (entry == null) {
            return null;
        }

        VaultEntryResponseDto dto = new VaultEntryResponseDto();
        dto.setId(entry.getId());
        dto.setTitle(entry.getTitle());
        dto.setUsername(entry.getUsername());
        dto.setWebsiteUrl(entry.getWebsiteUrl());
        dto.setCategory(entry.getCategory());
        dto.setNotes(entry.getNotes());
        dto.setFavorite(entry.isFavorite());
        dto.setPasswordStrengthScore(entry.getPasswordStrengthScore());
        dto.setNonce(entry.getNonce());
        dto.setCreatedAt(entry.getCreatedAt());
        dto.setUpdatedAt(entry.getUpdatedAt());

        // Strength label and badge mapping
        switch (entry.getPasswordStrengthScore()) {
            case 0 -> {
                dto.setStrengthLabel("Very Weak");
                dto.setStrengthBadgeClass("badge-danger");
            }
            case 1 -> {
                dto.setStrengthLabel("Weak");
                dto.setStrengthBadgeClass("badge-warning");
            }
            case 2 -> {
                dto.setStrengthLabel("Fair");
                dto.setStrengthBadgeClass("badge-info");
            }
            case 3 -> {
                dto.setStrengthLabel("Strong");
                dto.setStrengthBadgeClass("badge-primary");
            }
            case 4 -> {
                dto.setStrengthLabel("Very Strong");
                dto.setStrengthBadgeClass("badge-success");
            }
            default -> {
                dto.setStrengthLabel("Unknown");
                dto.setStrengthBadgeClass("badge-secondary");
            }
        }

        return dto;
    }

    public VaultEntryExportDto toExportDto(VaultEntry entry, String decryptedPassword) {
        if (entry == null) {
            return null;
        }
        VaultEntryExportDto dto = new VaultEntryExportDto();
        dto.setTitle(entry.getTitle());
        dto.setUsername(entry.getUsername());
        dto.setWebsiteUrl(entry.getWebsiteUrl());
        dto.setCategory(entry.getCategory() != null ? entry.getCategory().getDisplayName() : "Other");
        dto.setNotes(entry.getNotes());
        dto.setCreatedAt(entry.getCreatedAt());
        if (decryptedPassword != null) {
            dto.setPassword(decryptedPassword);
        }
        return dto;
    }
}
