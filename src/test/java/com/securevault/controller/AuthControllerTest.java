package com.securevault.controller;

import com.securevault.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests - Registration & Login Endpoints")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @WithAnonymousUser
    @DisplayName("GET /login should display the login page with status 200")
    void testLoginPageRenders() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /register should display the register page with status 200")
    void testRegisterPageRenders() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("POST /register should reject weak password and render validation errors")
    void testRegisterWeakPasswordRejection() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "security_guy")
                .param("email", "guy@example.com")
                .param("password", "weak")
                .param("confirmPassword", "weak"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("POST /register should reject mismatched password confirmation")
    void testRegisterMismatchedPasswordsRejection() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "security_guy")
                .param("email", "guy@example.com")
                .param("password", "V@lidP@ssword123!")
                .param("confirmPassword", "DifferentPassword123!"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("POST /register with valid credentials should succeed and redirect to /login")
    void testRegisterSuccess() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "cyber_ninja")
                .param("email", "ninja@cybervault.io")
                .param("password", "SuperSecureP@ss123!")
                .param("confirmPassword", "SuperSecureP@ss123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService, times(1)).registerUser(any());
    }
}
