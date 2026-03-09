package org.challengegroup.coursesrecomendation.security;

import org.challengegroup.coursesrecomendation.entity.Role;
import org.challengegroup.coursesrecomendation.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User user;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();

        // Injeta os valores que viriam do application.properties
        ReflectionTestUtils.setField(
                jwtTokenProvider,
                "jwtSecret",
                "chave-super-secreta-para-testes-unitarios-deve-ter-256bits"
        );
        ReflectionTestUtils.setField(
                jwtTokenProvider,
                "JwtExpiration",
                3600000L // 1 hora em ms
        );

        user = User.builder()
                .id(1L)
                .name("João Silva")
                .email("joao@email.com")
                .passwordHash("hashedPassword")
                .role(Role.USER)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("generateToken → retorna token não nulo")
    void generateToken_returnsToken() {
        String token = jwtTokenProvider.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("getTokenEmail → retorna email correto")
    void getTokenEmail_returnsCorrectEmail() {
        String token = jwtTokenProvider.generateToken(user);

        String email = jwtTokenProvider.getTokenEmail(token);

        assertEquals("joao@email.com", email);
    }

    @Test
    @DisplayName("getTokenUserId → retorna userId correto")
    void getTokenUserId_returnsCorrectUserId() {
        String token = jwtTokenProvider.generateToken(user);

        Long userId = jwtTokenProvider.getTokenUserId(token);

        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("validateToken → token válido → retorna true")
    void validateToken_validToken_returnsTrue() {
        String token = jwtTokenProvider.generateToken(user);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("validateToken → token inválido → retorna false")
    void validateToken_invalidToken_returnsFalse() {
        boolean isValid = jwtTokenProvider.validateToken("token.invalido.aqui");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken → token expirado → retorna false")
    void validateToken_expiredToken_returnsFalse() {
        // Seta expiração para 0 (já expirado)
        ReflectionTestUtils.setField(jwtTokenProvider, "JwtExpiration", -1000L);

        String token = jwtTokenProvider.generateToken(user);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken → token nulo → retorna false")
    void validateToken_nullToken_returnsFalse() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertFalse(isValid);
    }
}
