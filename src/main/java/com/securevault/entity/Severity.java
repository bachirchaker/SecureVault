package com.securevault.entity;

public enum Severity {
    INFO("info", "bi-info-circle"),
    WARNING("warning", "bi-exclamation-triangle"),
    CRITICAL("danger", "bi-shield-x");

    private final String badgeClass;
    private final String iconClass;

    Severity(String badgeClass, String iconClass) {
        this.badgeClass = badgeClass;
        this.iconClass = iconClass;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public String getIconClass() {
        return iconClass;
    }
}
