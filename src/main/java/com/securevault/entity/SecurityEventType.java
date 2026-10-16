package com.securevault.entity;

public enum SecurityEventType {
    LOGIN_SUCCESS("Successful Login"),
    LOGIN_FAILED("Failed Login Attempt"),
    ACCOUNT_LOCKED("Account Locked"),
    LOGOUT("User Logged Out"),
    REGISTRATION("User Registered"),
    PASSWORD_CHANGED("Master Password Changed"),
    PROFILE_UPDATED("Profile Settings Updated"),
    VAULT_ENTRY_CREATED("Vault Entry Created"),
    VAULT_ENTRY_UPDATED("Vault Entry Updated"),
    VAULT_ENTRY_DELETED("Vault Entry Deleted"),
    VAULT_PASSWORD_REVEALED("Vault Password Revealed"),
    VAULT_EXPORTED("Vault Data Exported");

    private final String description;

    SecurityEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
