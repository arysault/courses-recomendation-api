package org.challengegroup.coursesrecomendation.controller;

import org.challengegroup.coursesrecomendation.dto.TopCourseItem;
import org.challengegroup.coursesrecomendation.dto.TopCoursesResponse;
import org.challengegroup.coursesrecomendation.exception.GlobalExceptionHandler;
import org.challengegroup.coursesrecomendation.service.TopCoursesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TopCoursesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TopCoursesService topCoursesService;

    @InjectMocks
    private TopCoursesController topCoursesController;

    private TopCoursesResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(topCoursesController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        TopCourseItem item = TopCourseItem.builder()
                .id(1)
                .title("Java COMPLETO")
                .technology("Java")
                .platform("Udemy")
                .instructor("Nélio Alves")
                .rating(4.9)
                .language("Português")
                .link("https://udemy.com/java")
                .build();

        mockResponse = TopCoursesResponse.builder()
                .technology("Java")
                .totalReturned(1)
                .courses(List.of(item))
                .availableTechnologies(List.of("Java", "Angular", "Spring Boot"))
                .build();
    }

    @Test
    @DisplayName("GET /courses/top/{technology} → 200 + TopCoursesResponse")
    void getTop_returns200() throws Exception {
        when(topCoursesService.getTopByTechnology("Java"))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/courses/top/Java")
                        .with(user("joao@email.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technology").value("Java"))
                .andExpect(jsonPath("$.total_returned").value(1))
                .andExpect(jsonPath("$.courses[0].title").value("Java COMPLETO"))
                .andExpect(jsonPath("$.courses[0].rating").value(4.9))
                .andExpect(jsonPath("$.available_technologies").isArray());
    }

    @Test
    @DisplayName("GET /courses/top/{technology} → tecnologia sem cursos → 200 com lista vazia")
    void getTop_emptyCourses_returns200() throws Exception {
        TopCoursesResponse emptyResponse = TopCoursesResponse.builder()
                .technology("Kotlin")
                .totalReturned(0)
                .courses(List.of())
                .availableTechnologies(List.of("Java", "Angular"))
                .build();

        when(topCoursesService.getTopByTechnology("Kotlin"))
                .thenReturn(emptyResponse);

        mockMvc.perform(get("/courses/top/Kotlin")
                        .with(user("joao@email.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_returned").value(0))
                .andExpect(jsonPath("$.courses").isArray())
                .andExpect(jsonPath("$.courses").isEmpty());
    }

    @Test
    @DisplayName("GET /courses/top/{technology} → erro interno → 500")
    void getTop_internalError_returns500() throws Exception {
        when(topCoursesService.getTopByTechnology(any()))
                .thenThrow(new RuntimeException("Erro inesperado"));

        mockMvc.perform(get("/courses/top/Java")
                        .with(user("joao@email.com").roles("USER")))
                .andExpect(status().isInternalServerError());
    }
}
