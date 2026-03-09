package org.challengegroup.coursesrecomendation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.challengegroup.coursesrecomendation.dto.CourseResponse;
import org.challengegroup.coursesrecomendation.dto.UserPreferenceRequest;
import org.challengegroup.coursesrecomendation.exception.GlobalExceptionHandler;
import org.challengegroup.coursesrecomendation.exception.ResourceNotFoundException;
import org.challengegroup.coursesrecomendation.service.RecommendationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationController recommendationController;

    private List<CourseResponse> mockCourses;
    private UserPreferenceRequest preferenceRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(recommendationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                // ← REGISTRA o resolver do @AuthenticationPrincipal
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver()
                )
                .build();

        objectMapper = new ObjectMapper();

        // ── Configura SecurityContext com usuário autenticado ──────
        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User(
                        "joao@email.com",
                        "senha123",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()
                )
        );

        // ── Monta cursos mock ──────────────────────────────────────
        CourseResponse course = new CourseResponse();
        course.setId(1);
        course.setTitle("Java Completo");
        course.setTechnology("Java");
        course.setPlatform("Udemy");
        course.setScore(0.95);
        mockCourses = List.of(course);

        preferenceRequest = new UserPreferenceRequest();
        preferenceRequest.setTechnology("Java");
        preferenceRequest.setLanguages(List.of("Português"));
        preferenceRequest.setPlatforms(List.of("Udemy"));
        preferenceRequest.setConceptsOfInterest(List.of("POO", "REST API"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /recommendations → 200 + lista de cursos")
    void recommendFromSaved_returns200() throws Exception {
        when(recommendationService.recommendFromSavedPreferences("joao@email.com"))
                .thenReturn(mockCourses);

        mockMvc.perform(get("/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Completo"))
                .andExpect(jsonPath("$[0].technology").value("Java"));
    }

    @Test
    @DisplayName("GET /recommendations → sem preferências salvas → 404")
    void recommendFromSaved_noPreferences_returns404() throws Exception {
        when(recommendationService.recommendFromSavedPreferences("joao@email.com"))
                .thenThrow(new ResourceNotFoundException(
                        "No saved preferences for user: joao@email.com"
                ));

        mockMvc.perform(get("/recommendations"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /recommendations → Python retorna vazio → 200 com lista vazia")
    void recommendFromSaved_emptyList_returns200() throws Exception {
        when(recommendationService.recommendFromSavedPreferences("joao@email.com"))
                .thenReturn(List.of());

        mockMvc.perform(get("/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("POST /recommendations → 200 + lista de cursos")
    void recommend_returns200() throws Exception {
        when(recommendationService.recommend(eq("joao@email.com"), any()))
                .thenReturn(mockCourses);

        mockMvc.perform(post("/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preferenceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Completo"))
                .andExpect(jsonPath("$[0].score").value(0.95));
    }

    @Test
    @DisplayName("POST /recommendations → Python retorna vazio → 200 com lista vazia")
    void recommend_emptyList_returns200() throws Exception {
        when(recommendationService.recommend(eq("joao@email.com"), any()))
                .thenReturn(List.of());

        mockMvc.perform(post("/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preferenceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
