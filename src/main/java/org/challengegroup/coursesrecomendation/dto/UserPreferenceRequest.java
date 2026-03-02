package org.challengegroup.coursesrecomendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserPreferenceRequest {

    @NotBlank(message = "Technology is required")
    @Schema(description = "Tecnologia escolhida (apenas uma)", example = "Spring Boot")
    private String technology;

    @Size(max = 3, message = "Maximum 3 concepts of interest allowed")
    @Schema(
        description = "Conceitos de interesse (máximo 3)",
        example = "[\"REST API\", \"Security\", \"JPA\"]"
    )
    private List<String> conceptsOfInterest;

    @Schema(
        description = "Idiomas desejados (todos opcionais)",
        example = "[\"English\", \"Português\"]"
    )
    private List<String> languages;

    @Schema(
        description = "Plataformas desejadas (todas opcionais)",
        example = "[\"Udemy\", \"Coursera\"]"
    )
    private List<String> platforms;
}
