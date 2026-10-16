package com.securevault.security;

import com.securevault.config.VaultProperties;
import com.securevault.entity.User;
import com.securevault.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final VaultProperties vaultProperties;

    public CustomUserDetailsService(UserRepository userRepository, VaultProperties vaultProperties) {
        this.userRepository = userRepository;
        this.vaultProperties = vaultProperties;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        // Check if lock duration has expired
        if (!user.isAccountNonLocked() && user.getLockTime() != null) {
            int lockMinutes = vaultProperties.getSecurity().getLockoutDurationMinutes();
            if (user.getLockTime().plusMinutes(lockMinutes).isBefore(LocalDateTime.now())) {
                user.setAccountNonLocked(true);
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
            }
        }

        return new CustomUserDetails(user);
    }
}
