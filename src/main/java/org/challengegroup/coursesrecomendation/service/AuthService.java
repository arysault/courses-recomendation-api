package org.challengegroup.coursesrecomendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.challengegroup.coursesrecomendation.dto.AuthResponse;
import org.challengegroup.coursesrecomendation.dto.LoginRequest;
import org.challengegroup.coursesrecomendation.dto.RegisterRequest;
import org.challengegroup.coursesrecomendation.entity.User;
import org.challengegroup.coursesrecomendation.repository.UserRepository;
import org.challengegroup.coursesrecomendation.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already registered: " + request.getEmail()
            );
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);
        log.info("Created user successfully with id: {}", user.getId());

        // Gera token
        String token = tokenProvider.generateToken(user);

        return buildAuthResponse(token, user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());

        // Autentica com Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        // Busca usuário atualizado - passando true para isActive
        User user = userRepository
                .findByEmailAndIsActive(request.getEmail().toLowerCase().trim(), true)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Atualiza last_access
        userRepository.updateLastAccess(user.getId(), LocalDateTime.now());

        // Gera token
        String token = tokenProvider.generateToken(user);

        log.info("Login completed: {}", user.getEmail());

        return buildAuthResponse(token, user);
    }


    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
