package org.challengegroup.coursesrecomendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.challengegroup.coursesrecomendation.dto.*;
import org.challengegroup.coursesrecomendation.service.UserService;
import org.challengegroup.coursesrecomendation.service.PreferenceOptionsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "Gerenciamento de usuários e preferências")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PreferenceOptionsService preferenceOptionsService;

    @Operation(
            summary = "Dados do usuário logado",
            description = "Retorna os dados do usuário autenticado",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getMe(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /users/me - {}", userDetails.getUsername());
        return ResponseEntity.ok(userService.getMe(userDetails.getUsername()));
    }

    @Operation(
            summary = "Buscar preferências + cursos recomendados",
            description = "Retorna as preferências salvas e os cursos recomendados pelo Python",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Preferências e cursos retornados"),
                    @ApiResponse(responseCode = "404", description = "Preferências não encontradas")
            }
    )
    @GetMapping("/preferences")
    public ResponseEntity<UserPreferenceResponse> getPreferences(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /users/preferences - {}", userDetails.getUsername());
        return ResponseEntity.ok(
                userService.getPreferences(userDetails.getUsername())
        );
    }

    @Operation(
            summary = "Salvar preferências",
            description = "Salva as preferências e retorna cursos recomendados pelo Python",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Preferências salvas e cursos retornados"),
                    @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
            }
    )
    @PostMapping("/preferences")
    public ResponseEntity<UserPreferenceResponse> savePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserPreferenceRequest request) {
        log.info("POST /users/preferences - {}", userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createPreferences(
                        userDetails.getUsername(), request
                ));
    }

    @Operation(
            summary = "Atualizar preferências",
            description = "Atualiza as preferências e retorna cursos novos recomendados",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Preferências atualizadas e cursos retornados"),
                    @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
            }
    )
    @PutMapping("/preferences")
    public ResponseEntity<UserPreferenceResponse> updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserPreferenceRequest request) {
        log.info("PUT /users/preferences - {}", userDetails.getUsername());
        return ResponseEntity.ok(
                userService.updatePreferences(
                        userDetails.getUsername(), request
                )
        );
    }

    @Operation(
            summary = "Opções do wizard",
            description = "Retorna tecnologias, plataformas, idiomas e níveis disponíveis",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Opções retornadas com sucesso")
            }
    )
    @GetMapping("/preferences/options")
    public ResponseEntity<PreferenceOptionsResponse> getOptions() {
        log.info("GET /users/preferences/options");
        return ResponseEntity.ok(preferenceOptionsService.getOptions());
    }
}
