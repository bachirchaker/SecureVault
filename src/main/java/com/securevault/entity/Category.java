package com.securevault.entity;

public enum Category {
    LOGIN("Login", "bi-person-badge", "text-primary"),
    CREDIT_CARD("Credit Card", "bi-credit-card", "text-warning"),
    SECURE_NOTE("Secure Note", "bi-journal-text", "text-info"),
    API_KEY("API Key", "bi-key", "text-success"),
    SERVER("Server & SSH", "bi-hdd-network", "text-purple"),
    IDENTITY("Identity", "bi-shield-check", "text-danger"),
    OTHER("Other", "bi-folder", "text-secondary");

    private final String displayName;
    private final String iconClass;
    private final String colorClass;

    Category(String displayName, String iconClass, String colorClass) {
        this.displayName = displayName;
        this.iconClass = iconClass;
        this.colorClass = colorClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconClass() {
        return iconClass;
    }

    public String getColorClass() {
        return colorClass;
    }
}
