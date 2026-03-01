package org.challengegroup.coursesrecomendation.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.challengegroup.coursesrecomendation.dto.CourseResponse;
import org.challengegroup.coursesrecomendation.dto.UserPreferenceRequest;
import org.challengegroup.coursesrecomendation.entity.User;
import org.challengegroup.coursesrecomendation.entity.UserPreference;
import org.challengegroup.coursesrecomendation.exception.ResourceNotFoundException;
import org.challengegroup.coursesrecomendation.repository.UserPreferenceRepository;
import org.challengegroup.coursesrecomendation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PythonService pythonService;

    // ----------------------------------------------------------------
    // Recomenda com preferências recebidas na request (sem salvar)
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<CourseResponse> recommend(
            String email,
            UserPreferenceRequest request) {

        User user = findUser(email);

        log.info("Calling Python for recommendations | userId={} | technology={}",
                user.getId(), request.getTechnology());

        List<CourseResponse> courses = pythonService
                .getRecommendations(user.getId(), request);

        log.info("Received {} courses from Python | userId={}",
                courses.size(), user.getId());

        return courses;
    }

    // ----------------------------------------------------------------
    // Recomenda usando preferências já salvas no banco
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<CourseResponse> recommendFromSavedPreferences(String email) {

        User user = findUser(email);

        UserPreference pref = userPreferenceRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No saved preferences for user: " + email
                ));

        // Reconstrói o request a partir do que está salvo no banco
        UserPreferenceRequest request = buildRequestFromSaved(pref);

        log.info("Calling Python with saved prefs | userId={} | technology={}",
                user.getId(), request.getTechnology());

        List<CourseResponse> courses = pythonService
                .getRecommendations(user.getId(), request);

        log.info("Received {} courses from Python | userId={}",
                courses.size(), user.getId());

        return courses;
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email
                ));
    }

    // "REST API, Security, JPA" → UserPreferenceRequest
    private UserPreferenceRequest buildRequestFromSaved(UserPreference pref) {
        UserPreferenceRequest request = new UserPreferenceRequest();

        request.setTechnology(pref.getTechnologies());
        request.setConceptsOfInterest(splitToList(pref.getConceptsOfInterest()));
        request.setLanguages(splitToList(pref.getLanguages()));
        request.setPlatforms(splitToList(pref.getPlatforms()));

        return request;
    }

    private List<String> splitToList(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        return Arrays.stream(value.split(",\\s*")).toList();
    }
}
