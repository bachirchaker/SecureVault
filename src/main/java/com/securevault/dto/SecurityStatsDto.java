package com.securevault.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityStatsDto {

    private long totalCredentials = 0;
    private long weakPasswords = 0;
    private long reusedPasswords = 0;
    private int healthScore = 100;

    // Distribution: 0 (Very Weak), 1 (Weak), 2 (Fair), 3 (Strong), 4 (Very Strong)
    private Map<String, Integer> strengthDistribution = new HashMap<>();

    // Category counts
    private Map<String, Long> categoryCounts = new HashMap<>();

    // Recommendations list
    private List<String> securityRecommendations = new ArrayList<>();

    // Recent items
    private List<VaultEntryResponseDto> recentEntries = new ArrayList<>();
    private List<SecurityEventDto> recentEvents = new ArrayList<>();

    public SecurityStatsDto() {
        strengthDistribution.put("Very Weak", 0);
        strengthDistribution.put("Weak", 0);
        strengthDistribution.put("Fair", 0);
        strengthDistribution.put("Strong", 0);
        strengthDistribution.put("Very Strong", 0);
    }

    public long getTotalCredentials() {
        return totalCredentials;
    }

    public void setTotalCredentials(long totalCredentials) {
        this.totalCredentials = totalCredentials;
    }

    public long getWeakPasswords() {
        return weakPasswords;
    }

    public void setWeakPasswords(long weakPasswords) {
        this.weakPasswords = weakPasswords;
    }

    public long getReusedPasswords() {
        return reusedPasswords;
    }

    public void setReusedPasswords(long reusedPasswords) {
        this.reusedPasswords = reusedPasswords;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public Map<String, Integer> getStrengthDistribution() {
        return strengthDistribution;
    }

    public void setStrengthDistribution(Map<String, Integer> strengthDistribution) {
        this.strengthDistribution = strengthDistribution;
    }

    public Map<String, Long> getCategoryCounts() {
        return categoryCounts;
    }

    public void setCategoryCounts(Map<String, Long> categoryCounts) {
        this.categoryCounts = categoryCounts;
    }

    public List<String> getSecurityRecommendations() {
        return securityRecommendations;
    }

    public void setSecurityRecommendations(List<String> securityRecommendations) {
        this.securityRecommendations = securityRecommendations;
    }

    public List<VaultEntryResponseDto> getRecentEntries() {
        return recentEntries;
    }

    public void setRecentEntries(List<VaultEntryResponseDto> recentEntries) {
        this.recentEntries = recentEntries;
    }

    public List<SecurityEventDto> getRecentEvents() {
        return recentEvents;
    }

    public void setRecentEvents(List<SecurityEventDto> recentEvents) {
        this.recentEvents = recentEvents;
    }
}
