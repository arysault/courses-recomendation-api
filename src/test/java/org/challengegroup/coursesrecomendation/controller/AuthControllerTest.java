package org.challengegroup.coursesrecomendation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.challengegroup.coursesrecomendation.dto.AuthResponse;
import org.challengegroup.coursesrecomendation.dto.LoginRequest;
import org.challengegroup.coursesrecomendation.dto.RegisterRequest;
import org.challengegroup.coursesrecomendation.exception.GlobalExceptionHandler;
import org.challengegroup.coursesrecomendation.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler()) // ← registra handler
                .build();

        objectMapper = new ObjectMapper();

        authResponse = AuthResponse.builder()
                .token("jwt-token")
                .type("Bearer")
                .userId(1L)
                .name("João Silva")
                .email("joao@email.com")
                .role("USER")
                .build();
    }

    @Test
    @DisplayName("POST /auth/register → 201 + AuthResponse")
    void register_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("João Silva");
        request.setEmail("joao@email.com");
        request.setPassword("senha123");

        when(authService.register(any())).thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /auth/register → campos inválidos → 400")
    void register_invalidFields_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("");
        request.setEmail("email-invalido");
        request.setPassword("123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login → 200 + AuthResponse")
    void login_returns200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("joao@email.com");
        request.setPassword("senha123");

        when(authService.login(any())).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    @DisplayName("POST /auth/login → credenciais inválidas → 401")
    void login_badCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("joao@email.com");
        request.setPassword("senhaerrada");

        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login → email inválido → 400")
    void login_invalidEmail_returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("email-invalido");
        request.setPassword("senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
