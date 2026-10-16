package com.securevault.mapper;

import com.securevault.dto.SecurityEventDto;
import com.securevault.dto.UserProfileDto;
import com.securevault.entity.SecurityEvent;
import com.securevault.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileDto toProfileDto(User user, long totalEntries, int healthScore) {
        if (user == null) {
            return null;
        }
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name().replace("ROLE_", ""));
        dto.setCreatedAt(user.getCreatedAt());
        dto.setTotalVaultEntries(totalEntries);
        dto.setHealthScore(healthScore);
        return dto;
    }

    public SecurityEventDto toEventDto(SecurityEvent event) {
        if (event == null) {
            return null;
        }
        SecurityEventDto dto = new SecurityEventDto();
        dto.setId(event.getId());
        dto.setEventType(event.getEventType().name());
        dto.setEventName(event.getEventType().getDescription());
        dto.setDescription(event.getDescription());
        dto.setIpAddress(event.getIpAddress() != null ? event.getIpAddress() : "127.0.0.1");
        dto.setUserAgent(event.getUserAgent());
        dto.setSeverity(event.getSeverity().name());
        dto.setBadgeClass(event.getSeverity().getBadgeClass());
        dto.setIconClass(event.getSeverity().getIconClass());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }
}
