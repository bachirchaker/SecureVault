package com.securevault;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Spring Context Bootstrap Test")
class SecureVaultApplicationTests {

    @Test
    void contextLoads() {
        // Verifies complete dependency injection graph and configuration beans
    }
}
