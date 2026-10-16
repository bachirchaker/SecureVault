package com.securevault.dto;

import java.util.ArrayList;
import java.util.List;

public class PasswordStrengthResultDto {

    private int score; // 0 to 4
    private String label; // Very Weak, Weak, Fair, Strong, Very Strong
    private String colorClass; // danger, warning, info, success
    private int entropyBits;
    private String estimatedCrackTime;
    private List<String> suggestions = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public PasswordStrengthResultDto() {
    }

    public PasswordStrengthResultDto(int score, String label, String colorClass, int entropyBits, String estimatedCrackTime) {
        this.score = score;
        this.label = label;
        this.colorClass = colorClass;
        this.entropyBits = entropyBits;
        this.estimatedCrackTime = estimatedCrackTime;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getColorClass() {
        return colorClass;
    }

    public void setColorClass(String colorClass) {
        this.colorClass = colorClass;
    }

    public int getEntropyBits() {
        return entropyBits;
    }

    public void setEntropyBits(int entropyBits) {
        this.entropyBits = entropyBits;
    }

    public String getEstimatedCrackTime() {
        return estimatedCrackTime;
    }

    public void setEstimatedCrackTime(String estimatedCrackTime) {
        this.estimatedCrackTime = estimatedCrackTime;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
