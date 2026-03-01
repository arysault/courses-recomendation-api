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

    // ----------------------------------------------------------------
    // POST e PUT /users/preferences
    // ----------------------------------------------------------------
    @Transactional
    public UserPreferenceResponse createOrUpdatePreferences(
            String email,
            UserPreferenceRequest request) {

        log.info("Saving preferences for: {}", email);
        User user = findUserByEmail(email);

        // 1. Valida se a tecnologia existe no banco e já busca os conceitos dela
        TechnologyConcept technologyConcept = technologyConceptRepository
                .findByTechnology(request.getTechnology())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Technology not found: " + request.getTechnology()
                ));

        // 2. Valida máximo de 3 conceitos
        if (request.getConceptsOfInterest() != null
                && request.getConceptsOfInterest().size() > 3) {
            throw new IllegalArgumentException(
                    "Maximum 3 concepts of interest allowed"
            );
        }

        // 3. Valida se os conceitos pertencem à tecnologia escolhida
        if (request.getConceptsOfInterest() != null
                && !request.getConceptsOfInterest().isEmpty()) {

            List<String> validConcepts = technologyConcept.getConceptsList();

            List<String> invalidConcepts = request.getConceptsOfInterest()
                    .stream()
                    .filter(concept -> !validConcepts.contains(concept))
                    .toList();

            if (!invalidConcepts.isEmpty()) {
                throw new IllegalArgumentException(
                        "Invalid concepts for technology '"
                        + request.getTechnology()
                        + "': " + invalidConcepts
                );
            }
        }

        // 4. Busca preferência existente ou cria nova
        UserPreference preference = userPreferenceRepository
                .findByUserId(user.getId())
                .orElse(UserPreference.builder().user(user).build());

        // Technology → String (só uma)
        preference.setTechnologies(request.getTechnology());

        // Concepts → "REST API, Security, JPA" (TEXT no banco)
        if (request.getConceptsOfInterest() != null) {
            preference.setConceptsOfInterest(
                    String.join(", ", request.getConceptsOfInterest())
            );
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

        userPreferenceRepository.save(preference);
        log.info("Preferences saved for userId: {}", user.getId());


        return toResponse(preference);
    }

    // ----------------------------------------------------------------
    // GET /users/preferences
    // ----------------------------------------------------------------
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
