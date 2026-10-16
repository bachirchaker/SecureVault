package com.securevault.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VaultEntryExportDto {

    private String title;
    private String username;
    private String websiteUrl;
    private String category;
    private String notes;
    private LocalDateTime createdAt;
    private String password; // Null unless explicitly requested

    public VaultEntryExportDto() {
    }

    public VaultEntryExportDto(String title, String username, String websiteUrl, String category, String notes, LocalDateTime createdAt) {
        this.title = title;
        this.username = username;
        this.websiteUrl = websiteUrl;
        this.category = category;
        this.notes = notes;
        this.createdAt = createdAt;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
