package com.securevault.service;

import com.securevault.dto.SecurityEventDto;
import com.securevault.entity.SecurityEvent;
import com.securevault.entity.SecurityEventType;
import com.securevault.entity.Severity;
import com.securevault.entity.User;
import com.securevault.mapper.UserMapper;
import com.securevault.repository.SecurityEventRepository;
import com.securevault.repository.UserRepository;
import com.securevault.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityAuditService {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditService.class);

    private final SecurityEventRepository securityEventRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public SecurityAuditService(SecurityEventRepository securityEventRepository,
                                UserRepository userRepository,
                                UserMapper userMapper) {
        this.securityEventRepository = securityEventRepository;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public void recordEvent(User user, SecurityEventType eventType, String description, Severity severity) {
        String ip = SecurityUtils.getClientIpAddress();
        String ua = SecurityUtils.getClientUserAgent();

        SecurityEvent event = new SecurityEvent(user, eventType, description, ip, ua, severity);
        securityEventRepository.save(event);
        log.info("Security Audit Event: [{}] User: {} - IP: {} - Severity: {}",
                eventType, (user != null ? user.getUsername() : "ANONYMOUS"), ip, severity);
    }

    @Transactional
    public void recordEvent(Long userId, SecurityEventType eventType, String description, Severity severity) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        recordEvent(user, eventType, description, severity);
    }

    @Transactional(readOnly = true)
    public List<SecurityEventDto> getRecentEvents(Long userId) {
        return securityEventRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(userMapper::toEventDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<SecurityEventDto> getEvents(Long userId, Pageable pageable) {
        return securityEventRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(userMapper::toEventDto);
    }

    @Transactional
    public int purgeAuditLogsOlderThan(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        int deleted = securityEventRepository.deleteEventsOlderThan(cutoff);
        log.info("Purged {} security audit records older than {} days (retention policy).", deleted, days);
        return deleted;
    }
}
