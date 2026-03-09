package org.challengegroup.coursesrecomendation.service;

import org.challengegroup.coursesrecomendation.dto.CourseResponse;
import org.challengegroup.coursesrecomendation.dto.UserPreferenceRequest;
import org.challengegroup.coursesrecomendation.entity.Role;
import org.challengegroup.coursesrecomendation.entity.User;
import org.challengegroup.coursesrecomendation.entity.UserPreference;
import org.challengegroup.coursesrecomendation.exception.ResourceNotFoundException;
import org.challengegroup.coursesrecomendation.repository.UserPreferenceRepository;
import org.challengegroup.coursesrecomendation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private PythonService pythonService;

    @InjectMocks
    private RecommendationService recommendationService;

    private User user;
    private UserPreference userPreference;
    private UserPreferenceRequest preferenceRequest;
    private List<CourseResponse> mockCourses;

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

        userPreference = UserPreference.builder()
                .id(1L)
                .user(user)
                .technologies("Java")
                .languages("Português")
                .platforms("Udemy")
                .conceptsOfInterest("POO, REST API")
                .build();

        preferenceRequest = new UserPreferenceRequest();
        preferenceRequest.setTechnology("Java");
        preferenceRequest.setLanguages(List.of("Português"));
        preferenceRequest.setPlatforms(List.of("Udemy"));
        preferenceRequest.setConceptsOfInterest(List.of("POO", "REST API"));

        CourseResponse course = new CourseResponse();
        course.setId(1);
        course.setTitle("Java Completo");
        course.setTechnology("Java");
        course.setPlatform("Udemy");
        course.setScore(0.95);
        mockCourses = List.of(course);
    }


    @Test
    @DisplayName("recommend → sucesso → chama PythonService e retorna cursos")
    void recommend_success() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(pythonService.getRecommendations(eq(1L), any()))
                .thenReturn(mockCourses);

        List<CourseResponse> result = recommendationService
                .recommend("joao@email.com", preferenceRequest);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java Completo", result.get(0).getTitle());
        verify(pythonService, times(1)).getRecommendations(eq(1L), any());
    }

    @Test
    @DisplayName("recommend → usuário não encontrado → lança ResourceNotFoundException")
    void recommend_userNotFound_throwsException() {
        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> recommendationService.recommend(
                        "naoexiste@email.com", preferenceRequest
                ));

        verify(pythonService, never()).getRecommendations(any(), any());
    }

    @Test
    @DisplayName("recommend → Python retorna lista vazia → retorna lista vazia")
    void recommend_pythonReturnsEmpty() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(pythonService.getRecommendations(any(), any()))
                .thenReturn(List.of());

        List<CourseResponse> result = recommendationService
                .recommend("joao@email.com", preferenceRequest);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("recommendFromSavedPreferences → sucesso → usa prefs salvas e retorna cursos")
    void recommendFromSaved_success() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(Optional.of(userPreference));
        when(pythonService.getRecommendations(eq(1L), any()))
                .thenReturn(mockCourses);

        List<CourseResponse> result = recommendationService
                .recommendFromSavedPreferences("joao@email.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pythonService, times(1)).getRecommendations(eq(1L), any());
    }

    @Test
    @DisplayName("recommendFromSavedPreferences → usuário não encontrado → lança ResourceNotFoundException")
    void recommendFromSaved_userNotFound_throwsException() {
        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> recommendationService
                        .recommendFromSavedPreferences("naoexiste@email.com"));

        verify(pythonService, never()).getRecommendations(any(), any());
    }

    @Test
    @DisplayName("recommendFromSavedPreferences → sem preferências salvas → lança ResourceNotFoundException")
    void recommendFromSaved_noPreferences_throwsException() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> recommendationService
                        .recommendFromSavedPreferences("joao@email.com"));

        verify(pythonService, never()).getRecommendations(any(), any());
    }

    @Test
    @DisplayName("recommendFromSavedPreferences → Python retorna vazio → retorna lista vazia")
    void recommendFromSaved_pythonReturnsEmpty() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(Optional.of(userPreference));
        when(pythonService.getRecommendations(any(), any()))
                .thenReturn(List.of());

        List<CourseResponse> result = recommendationService
                .recommendFromSavedPreferences("joao@email.com");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
