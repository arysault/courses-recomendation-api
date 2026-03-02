package org.challengegroup.coursesrecomendation.controller;

import org.challengegroup.coursesrecomendation.dto.TopCoursesResponse;
import org.challengegroup.coursesrecomendation.service.TopCoursesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Top Courses", description = "Top N cursos por tecnologia")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
@RestController
@RequestMapping("/courses/top")
@RequiredArgsConstructor
public class TopCoursesController {

    private final TopCoursesService topCoursesService;

    @Operation(
            summary = "Top 5 cursos por tecnologia",
            description = "Retorna os 5 cursos mais bem avaliados de uma tecnologia",
            parameters = {
                    @Parameter(
                            name        = "technology",
                            description = "Nome da tecnologia (ex: Java, Angular, Spring Boot)",
                            required    = true,
                            example     = "Java"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cursos retornados com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
            }
    )
    @GetMapping("/{technology}")
    public ResponseEntity<TopCoursesResponse> getTop(
            @PathVariable String technology) {

        log.info("GET /courses/top/{}", technology);

        TopCoursesResponse response = topCoursesService
                .getTopByTechnology(technology);

        return ResponseEntity.ok(response);
    }
}
