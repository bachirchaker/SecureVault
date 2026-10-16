package com.securevault.dto;

import com.securevault.validation.PasswordMatches;
import com.securevault.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

@PasswordMatches
public class ChangePasswordDto {

    @NotBlank(message = "Current master password is required")
    private String currentPassword;

    @NotBlank(message = "New master password is required")
    @ValidPassword
    private String newPassword;

    @NotBlank(message = "Please confirm your new master password")
    private String confirmNewPassword;

    public ChangePasswordDto() {
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}
