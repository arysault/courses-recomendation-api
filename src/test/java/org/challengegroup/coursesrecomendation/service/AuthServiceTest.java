package org.challengegroup.coursesrecomendation.service;

import org.challengegroup.coursesrecomendation.dto.AuthResponse;
import org.challengegroup.coursesrecomendation.dto.LoginRequest;
import org.challengegroup.coursesrecomendation.dto.RegisterRequest;
import org.challengegroup.coursesrecomendation.entity.Role;
import org.challengegroup.coursesrecomendation.entity.User;
import org.challengegroup.coursesrecomendation.repository.UserRepository;
import org.challengegroup.coursesrecomendation.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("João Silva")
                .email("joao@email.com")
                .passwordHash("hashedPassword")
                .role(Role.USER)
                .isActive(true)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setName("João Silva");
        registerRequest.setEmail("joao@email.com");
        registerRequest.setPassword("senha123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("joao@email.com");
        loginRequest.setPassword("senha123");
    }



    // REGISTER
    @Test
    @DisplayName("register → sucesso → retorna AuthResponse com token")
    void register_success() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any())).thenReturn(user);
        when(tokenProvider.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("joao@email.com", response.getEmail());
        assertEquals("João Silva", response.getName());
        assertEquals("USER", response.getRole());
    }

    @Test
    @DisplayName("register → email duplicado → lança IllegalArgumentException")
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register → chama passwordEncoder para hash da senha")
    void register_encodesPassword() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hashedPassword");
        when(userRepository.save(any())).thenReturn(user);
        when(tokenProvider.generateToken(any())).thenReturn("jwt-token");

        authService.register(registerRequest);

        verify(passwordEncoder, times(1)).encode("senha123");
    }

    // LOGIN
    @Test
    @DisplayName("login → sucesso → retorna AuthResponse com token")
    void login_success() {
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("joao@email.com", "senha123")
        );
        when(userRepository.findByEmailAndIsActive("joao@email.com", true))
                .thenReturn(Optional.of(user));
        when(tokenProvider.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("joao@email.com", response.getEmail());
    }

    @Test
    @DisplayName("login → credenciais inválidas → lança BadCredentialsException")
    void login_invalidCredentials_throwsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("login → atualiza lastAccess")
    void login_updatesLastAccess() {
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("joao@email.com", "senha123")
        );
        when(userRepository.findByEmailAndIsActive("joao@email.com", true))
                .thenReturn(Optional.of(user));
        when(tokenProvider.generateToken(any())).thenReturn("jwt-token");

        authService.login(loginRequest);

        verify(userRepository, times(1)).updateLastAccess(eq(1L), any());
    }
}
