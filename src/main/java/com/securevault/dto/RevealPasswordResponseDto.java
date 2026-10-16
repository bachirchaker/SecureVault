package com.securevault.dto;

public class RevealPasswordResponseDto {

    private boolean success;
    private Long id;
    private String password;
    private String error;

    public RevealPasswordResponseDto() {
    }

    public static RevealPasswordResponseDto ofSuccess(Long id, String password) {
        RevealPasswordResponseDto dto = new RevealPasswordResponseDto();
        dto.setSuccess(true);
        dto.setId(id);
        dto.setPassword(password);
        return dto;
    }

    public static RevealPasswordResponseDto ofError(String error) {
        RevealPasswordResponseDto dto = new RevealPasswordResponseDto();
        dto.setSuccess(false);
        dto.setError(error);
        return dto;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
