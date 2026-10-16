package com.securevault.controller;

import com.securevault.dto.VaultEntryResponseDto;
import com.securevault.entity.Category;
import com.securevault.service.VaultService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("VaultController Integration Tests - Security Enforcement & CSRF Defense")
class VaultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VaultService vaultService;

    @Test
    @WithAnonymousUser
    @DisplayName("Unauthenticated user accessing /vault must be redirected to /login")
    void testUnauthenticatedAccessRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/vault"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "analyst", roles = {"USER"})
    @DisplayName("Authenticated user accessing /vault should view vault list")
    void testAuthenticatedUserCanAccessVault() throws Exception {
        when(vaultService.getEntries(any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/vault"))
                .andExpect(status().isOk())
                .andExpect(view().name("vault/list"))
                .andExpect(model().attributeExists("entries"));
    }

    @Test
    @WithMockUser(username = "analyst", roles = {"USER"})
    @DisplayName("Mutating POST /vault/new without CSRF token MUST be rejected with 403 Forbidden")
    void testCsrfProtectionRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/vault/new")
                .param("title", "GitHub")
                .param("password", "SecureP@ss123!")
                .param("category", "LOGIN"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "analyst", roles = {"USER"})
    @DisplayName("POST /vault/new with valid CSRF token and valid data should succeed")
    void testCreateVaultEntryWithCsrfSucceeds() throws Exception {
        VaultEntryResponseDto mockDto = new VaultEntryResponseDto();
        mockDto.setId(1L);
        mockDto.setTitle("GitHub");
        mockDto.setCategory(Category.LOGIN);

        when(vaultService.createEntry(any(), any())).thenReturn(mockDto);

        mockMvc.perform(post("/vault/new")
                .with(csrf())
                .param("title", "GitHub")
                .param("username", "analyst")
                .param("password", "SuperSecureP@ss123!")
                .param("category", "LOGIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vault"));
    }
}
