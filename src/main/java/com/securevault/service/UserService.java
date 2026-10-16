package com.securevault.service;

import com.securevault.dto.ChangePasswordDto;
import com.securevault.dto.UserProfileDto;
import com.securevault.dto.UserRegistrationDto;
import com.securevault.entity.Role;
import com.securevault.entity.SecurityEventType;
import com.securevault.entity.Severity;
import com.securevault.entity.User;
import com.securevault.exception.ResourceNotFoundException;
import com.securevault.mapper.UserMapper;
import com.securevault.repository.UserRepository;
import com.securevault.repository.VaultEntryRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final VaultEntryRepository vaultEntryRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityAuditService auditService;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       VaultEntryRepository vaultEntryRepository,
                       PasswordEncoder passwordEncoder,
                       SecurityAuditService auditService,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.vaultEntryRepository = vaultEntryRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.userMapper = userMapper;
    }

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        User user = new User(dto.getUsername(), dto.getEmail(), hashedPassword);
        user.setRole(Role.ROLE_USER);
        User savedUser = userRepository.save(user);

        auditService.recordEvent(savedUser, SecurityEventType.REGISTRATION,
                "User account successfully registered", Severity.INFO);

        return savedUser;
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long userId) {
        User user = getById(userId);
        long totalEntries = vaultEntryRepository.countByUserId(userId);
        long weakEntries = vaultEntryRepository.countByUserIdAndPasswordStrengthScoreLessThanEqual(userId, 1);

        int healthScore = 100;
        if (totalEntries > 0) {
            double weakRatio = (double) weakEntries / totalEntries;
            healthScore = Math.max(10, (int) Math.round((1.0 - weakRatio) * 100));
        }

        return userMapper.toProfileDto(user, totalEntries, healthScore);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordDto dto) {
        User user = getById(userId);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            auditService.recordEvent(user, SecurityEventType.PASSWORD_CHANGED,
                    "Failed password change attempt: Incorrect current master password", Severity.WARNING);
            throw new IllegalArgumentException("Current master password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        auditService.recordEvent(user, SecurityEventType.PASSWORD_CHANGED,
                "Master password changed successfully", Severity.INFO);
    }
}
