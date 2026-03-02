package org.challengegroup.coursesrecomendation.service;

import java.util.Arrays;
import java.util.List;
import org.challengegroup.coursesrecomendation.dto.UserMeResponse;
import org.challengegroup.coursesrecomendation.dto.UserPreferenceRequest;
import org.challengegroup.coursesrecomendation.dto.UserPreferenceResponse;
import org.challengegroup.coursesrecomendation.entity.TechnologyConcept;
import org.challengegroup.coursesrecomendation.entity.User;
import org.challengegroup.coursesrecomendation.entity.UserPreference;
import org.challengegroup.coursesrecomendation.exception.ResourceNotFoundException;
import org.challengegroup.coursesrecomendation.repository.TechnologyConceptRepository;
import org.challengegroup.coursesrecomendation.repository.UserPreferenceRepository;
import org.challengegroup.coursesrecomendation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final TechnologyConceptRepository technologyConceptRepository;

    // ----------------------------------------------------------------
    // GET /users/me
    // ----------------------------------------------------------------
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
                .hasPreferences(userPreferenceRepository.existsByUserId(user.getId()))
                .build();
    }

    // POST → /users/preferences
    @Transactional
    public UserPreferenceResponse createPreferences(
            String email,
            UserPreferenceRequest request) {

        log.info("Creating preferences for: {}", email);
        User user = findUserByEmail(email);

        // Se já existe → erro
        if (userPreferenceRepository.existsByUserId(user.getId())) {
            throw new IllegalArgumentException(
                    "Preferences already exist for user: " + email
            );
        }

        UserPreference preference = UserPreference.builder()
                .user(user)
                .languages(request.getLanguages())
                .technologies(request.getTechnologies())
                .platforms(request.getPlatforms())
                .level(request.getLevel())
                .minimumRating(request.getMinimumRating())
                .build();

        preference = userPreferenceRepository.save(preference);
        log.info("Preferences created for userId: {}", user.getId());

        List<CourseResponse> courses = pythonService
                .getRecommendations(user.getId(), request);

        return toResponse(preference, courses);
    }

    // PUT → /users/preferences
    @Transactional
    public UserPreferenceResponse updatePreferences(
            String email,
            UserPreferenceRequest request) {

        log.info("Updating preferences for: {}", email);
        User user = findUserByEmail(email);

        UserPreference preference = userPreferenceRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Preferences not found for user: " + email
                ));

        if (request.getLanguages() != null) {
            preference.setLanguages(request.getLanguages());
        }

        // Languages → "English, Português" (TEXT no banco)
        if (request.getLanguages() != null) {
            preference.setLanguages(
                    String.join(", ", request.getLanguages())
            );
        }

        // Platforms → "Udemy, Coursera" (TEXT no banco)
        if (request.getPlatforms() != null) {
            preference.setPlatforms(
                    String.join(", ", request.getPlatforms())
            );
        }

        preference = userPreferenceRepository.save(preference);
        log.info("Preferences updated for userId: {}", user.getId());

        List<CourseResponse> courses = pythonService
                .getRecommendations(user.getId(), request);

        return toResponse(preference);
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

        // GET não chama Python, retorna sem cursos
        return toResponse(preference);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email
                ));
    }

    // Converte "REST API, Security" → ["REST API", "Security"]
    private List<String> splitToList(String value) {
        if (value == null || value.isBlank()) return null;
        return Arrays.stream(value.split(",\\s*")).toList();
    }

    private UserPreferenceResponse toResponse(UserPreference preference) {
        return UserPreferenceResponse.builder()
                .id(preference.getId())
                .userId(preference.getUser().getId())
                .technology(preference.getTechnologies())
                .conceptsOfInterest(splitToList(preference.getConceptsOfInterest()))
                .languages(splitToList(preference.getLanguages()))
                .platforms(splitToList(preference.getPlatforms()))
                .build();
    }
}
