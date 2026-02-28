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

        preference = userPreferenceRepository.save(preference);
        log.info("Preferences updated for userId: {}", user.getId());

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
