package com.securevault.dto;

import com.securevault.entity.Category;
import java.time.LocalDateTime;

public class VaultEntryResponseDto {

    private Long id;
    private String title;
    private String username;
    private String websiteUrl;
    private Category category;
    private String notes;
    private boolean favorite;
    private int passwordStrengthScore;
    private String strengthLabel;
    private String strengthBadgeClass;
    private String nonce;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String maskedPassword = "••••••••••••";

    public VaultEntryResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public int getPasswordStrengthScore() {
        return passwordStrengthScore;
    }

    public void setPasswordStrengthScore(int passwordStrengthScore) {
        this.passwordStrengthScore = passwordStrengthScore;
    }

    public String getStrengthLabel() {
        return strengthLabel;
    }

    public void setStrengthLabel(String strengthLabel) {
        this.strengthLabel = strengthLabel;
    }

    public String getStrengthBadgeClass() {
        return strengthBadgeClass;
    }

    public void setStrengthBadgeClass(String strengthBadgeClass) {
        this.strengthBadgeClass = strengthBadgeClass;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMaskedPassword() {
        return maskedPassword;
    }

    public void setMaskedPassword(String maskedPassword) {
        this.maskedPassword = maskedPassword;
    }
}
