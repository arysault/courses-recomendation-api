package org.challengegroup.coursesrecomendation.service;

import org.challengegroup.coursesrecomendation.dto.UserMeResponse;
import org.challengegroup.coursesrecomendation.dto.UserPreferenceRequest;
import org.challengegroup.coursesrecomendation.dto.UserPreferenceResponse;
import org.challengegroup.coursesrecomendation.entity.Role;
import org.challengegroup.coursesrecomendation.entity.TechnologyConcept;
import org.challengegroup.coursesrecomendation.entity.User;
import org.challengegroup.coursesrecomendation.entity.UserPreference;
import org.challengegroup.coursesrecomendation.exception.ResourceNotFoundException;
import org.challengegroup.coursesrecomendation.repository.TechnologyConceptRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private TechnologyConceptRepository technologyConceptRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserPreference userPreference;
    private UserPreferenceRequest preferenceRequest;
    private TechnologyConcept technologyConcept;

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

        technologyConcept = TechnologyConcept.builder()
                .id(1L)
                .technology("Java")
                .conceptsOfInterest(new String[]{"POO", "REST API", "JPA"})
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
    }

    // GET ME
    @Test
    @DisplayName("getMe → sucesso → retorna dados do usuário")
    void getMe_success() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(userPreferenceRepository.existsByUserId(1L))
                .thenReturn(true);

        UserMeResponse response = userService.getMe("joao@email.com");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("João Silva", response.getName());
        assertEquals("joao@email.com", response.getEmail());
        assertEquals("USER", response.getRole());
        assertTrue(response.isHasPreferences());
    }

    @Test
    @DisplayName("getMe → sem preferências → hasPreferences false")
    void getMe_noPreferences_returnsFalse() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(userPreferenceRepository.existsByUserId(1L))
                .thenReturn(false);

        UserMeResponse response = userService.getMe("joao@email.com");

        assertFalse(response.isHasPreferences());
    }

    @Test
    @DisplayName("getMe → usuário não encontrado → lança ResourceNotFoundException")
    void getMe_userNotFound_throwsException() {
        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getMe("naoexiste@email.com"));
    }

    // GET PREFERENCES
    @Test
    @DisplayName("getPreferences → sucesso → retorna preferências sem cursos")
    void getPreferences_success() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(Optional.of(userPreference));

        UserPreferenceResponse response =
                userService.getPreferences("joao@email.com");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals("Java", response.getTechnology());
        assertEquals(List.of("Português"), response.getLanguages());
        assertEquals(List.of("Udemy"), response.getPlatforms());
        assertEquals(List.of("POO", "REST API"), response.getConceptsOfInterest());
    }

    @Test
    @DisplayName("getPreferences → não encontrado → lança ResourceNotFoundException")
    void getPreferences_notFound_throwsException() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getPreferences("joao@email.com"));
    }

    // CREATE OR UPDATE PREFERENCES
    @Test
    @DisplayName("createOrUpdatePreferences → cria nova → salva e retorna")
    void createOrUpdatePreferences_creates_new() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(technologyConceptRepository.findByTechnology("Java"))
                .thenReturn(Optional.of(technologyConcept));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(Optional.empty()); // não existe ainda
        when(userPreferenceRepository.save(any()))
                .thenReturn(userPreference);

        UserPreferenceResponse response =
                userService.createOrUpdatePreferences("joao@email.com", preferenceRequest);

        assertNotNull(response);
        assertEquals("Java", response.getTechnology());
        verify(userPreferenceRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("createOrUpdatePreferences → atualiza existente → salva e retorna")
    void createOrUpdatePreferences_updates_existing() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(technologyConceptRepository.findByTechnology("Java"))
                .thenReturn(Optional.of(technologyConcept));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(Optional.of(userPreference)); // já existe
        when(userPreferenceRepository.save(any()))
                .thenReturn(userPreference);

        UserPreferenceResponse response =
                userService.createOrUpdatePreferences("joao@email.com", preferenceRequest);

        assertNotNull(response);
        verify(userPreferenceRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("createOrUpdatePreferences → tecnologia inválida → lança ResourceNotFoundException")
    void createOrUpdatePreferences_invalidTechnology_throwsException() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(technologyConceptRepository.findByTechnology("TecnologiaInvalida"))
                .thenReturn(Optional.empty());

        preferenceRequest.setTechnology("TecnologiaInvalida");

        assertThrows(ResourceNotFoundException.class,
                () -> userService.createOrUpdatePreferences(
                        "joao@email.com", preferenceRequest
                ));

        verify(userPreferenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrUpdatePreferences → conceitos inválidos → lança IllegalArgumentException")
    void createOrUpdatePreferences_invalidConcepts_throwsException() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(technologyConceptRepository.findByTechnology("Java"))
                .thenReturn(Optional.of(technologyConcept));

        // Conceito que não pertence ao Java
        preferenceRequest.setConceptsOfInterest(List.of("NgRx", "Redux"));

        assertThrows(IllegalArgumentException.class,
                () -> userService.createOrUpdatePreferences(
                        "joao@email.com", preferenceRequest
                ));

        verify(userPreferenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrUpdatePreferences → mais de 3 conceitos → lança IllegalArgumentException")
    void createOrUpdatePreferences_moreThan3Concepts_throwsException() {
        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(technologyConceptRepository.findByTechnology("Java"))
                .thenReturn(Optional.of(technologyConcept));

        // 4 conceitos → inválido
        preferenceRequest.setConceptsOfInterest(
                List.of("POO", "REST API", "JPA", "Threads")
        );

        assertThrows(IllegalArgumentException.class,
                () -> userService.createOrUpdatePreferences(
                        "joao@email.com", preferenceRequest
                ));

        verify(userPreferenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrUpdatePreferences → usuário não encontrado → lança ResourceNotFoundException")
    void createOrUpdatePreferences_userNotFound_throwsException() {
        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.createOrUpdatePreferences(
                        "naoexiste@email.com", preferenceRequest
                ));

        verify(userPreferenceRepository, never()).save(any());
    }
}
