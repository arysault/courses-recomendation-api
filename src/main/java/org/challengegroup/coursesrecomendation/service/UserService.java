package org.challengegroup.coursesrecomendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.challengegroup.coursesrecomendation.dto.*;
import org.challengegroup.coursesrecomendation.entity.User;
import org.challengegroup.coursesrecomendation.entity.UserPreference;
import org.challengegroup.coursesrecomendation.exception.ResourceNotFoundException;
import org.challengegroup.coursesrecomendation.repository.UserPreferenceRepository;
import org.challengegroup.coursesrecomendation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PythonService pythonService;

    // GET /users/me
    @Transactional(readOnly = true)
    public UserMeResponse getMe(String email) {
        log.info("Getting user info: {}", email);
        User user = findUserByEmail(email);

        return UserMeResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .lastAccess(user.getLastAccess())
                .hasPreferences(
                        userPreferenceRepository.existsByUserId(user.getId())
                )
                .build();
    }

    // POST /users/preferences → salva + chama Python → retorna cursos
    @Transactional
    public UserPreferenceResponse createOrUpdatePreferences(
            String email,
            UserPreferenceRequest request) {

        log.info("Saving preferences for: {}", email);
        User user = findUserByEmail(email);

        // Busca preferência existente ou cria nova
        UserPreference preference = userPreferenceRepository
                .findByUserId(user.getId())
                .orElse(UserPreference.builder().user(user).build());

        // Atualiza campos (só atualiza o que veio preenchido)
        if (request.getLanguages() != null) {
            preference.setLanguages(request.getLanguages());
        }
        if (request.getTechnologies() != null) {
            preference.setTechnologies(request.getTechnologies());
        }
        if (request.getPlatforms() != null) {
            preference.setPlatforms(request.getPlatforms());
        }
        if (request.getLevel() != null) {
            preference.setLevel(request.getLevel());
        }
        if (request.getMinimumRating() != null) {
            preference.setMinimumRating(request.getMinimumRating());
        }

        userPreferenceRepository.save(preference);
        log.info("Preferences saved for userId: {}", user.getId());

        // Chama Python e retorna cursos recomendados
        List<CourseResponse> courses = pythonService
                .getRecommendations(user.getId(), request);

        return toResponse(preference, courses);
    }

    // GET /users/preferences → busca preferências
    @Transactional(readOnly = true)
    public UserPreferenceResponse getPreferences(String email) {
        log.info("Getting preferences for: {}", email);
        User user = findUserByEmail(email);

        UserPreference preference = userPreferenceRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Preferences not found for user: " + email
                ));

        return toResponse(preference, null);
    }

    // Helper
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email
                ));
    }

    private UserPreferenceResponse toResponse(
            UserPreference preference,
            List<CourseResponse> courses) {

        return UserPreferenceResponse.builder()
                .id(preference.getId())
                .userId(preference.getUser().getId())
                .languages(preference.getLanguages())
                .technologies(preference.getTechnologies())
                .platforms(preference.getPlatforms())
                .level(preference.getLevel())
                .minimumRating(preference.getMinimumRating())
                .courses(courses)
                .build();
    }
}
