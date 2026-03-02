package org.challengegroup.coursesrecomendation.controller;

import java.util.List;

import org.challengegroup.coursesrecomendation.dto.CourseResponse;
import org.challengegroup.coursesrecomendation.dto.UserPreferenceRequest;  // ← IMPORT
import org.challengegroup.coursesrecomendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Recommendations", description = "Recomendação de cursos")
@SecurityRequirement(name = "Bearer Authentication")  // ← ADICIONE ISSO
@Slf4j
@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(
            summary = "Recomenda cursos usando preferências salvas",
            description = "Busca as preferências do usuário no banco e chama o Python para recomendar",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cursos recomendados com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
                    @ApiResponse(responseCode = "404", description = "Preferências não encontradas")
            }
    )
    @GetMapping
    public ResponseEntity<List<CourseResponse>> recommendFromSaved(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GET /recommendations | user={}", userDetails.getUsername());

        List<CourseResponse> courses = recommendationService
                .recommendFromSavedPreferences(userDetails.getUsername());

        return ResponseEntity.ok(courses);
    }

    @Operation(
            summary = "Recomenda cursos com preferências do body",
            description = "Recebe preferências no body e chama o Python para recomendar",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cursos recomendados com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
            }
    )
    @PostMapping
    public ResponseEntity<List<CourseResponse>> recommend(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserPreferenceRequest request) {

        log.info("POST /recommendations | user={} | technology={}",
                userDetails.getUsername(), request.getTechnology());

        List<CourseResponse> courses = recommendationService
                .recommend(userDetails.getUsername(), request);

        return ResponseEntity.ok(courses);
    }
}
