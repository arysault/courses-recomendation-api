package org.challengegroup.coursesrecomendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.challengegroup.coursesrecomendation.dto.CourseResponse;
import org.challengegroup.coursesrecomendation.dto.RecommendationResponse;
import org.challengegroup.coursesrecomendation.dto.UserPreferenceRequest;
import org.challengegroup.coursesrecomendation.security.InternalJwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PythonService {

    private final RestTemplate restTemplate;
    private final InternalJwtProvider internalJwtProvider;

    @Value("${python.service.url}")
    private String pythonServiceUrl;

    public List<CourseResponse> getRecommendations(
            Long userId,
            UserPreferenceRequest preferences) {

        log.info("Calling Python service for userId: {}", userId);

        try {
            HttpHeaders headers = buildHeaders();
            Map<String, Object> payload = buildPayload(userId, preferences);

            HttpEntity<Map<String, Object>> httpEntity =
                    new HttpEntity<>(payload, headers);

            ResponseEntity<RecommendationResponse> response = restTemplate.exchange(
                    pythonServiceUrl + "/recommend/user",
                    HttpMethod.POST,
                    httpEntity,
                    RecommendationResponse.class
            );

            RecommendationResponse body = response.getBody();

            if (body == null || body.getCourses() == null) {
                log.warn("Python returned empty response for userId: {}", userId);
                return Collections.emptyList();
            }

            log.info("Python returned {} courses for userId: {}",
                    body.getCourses().size(), userId);

            return body.getCourses();

        } catch (Exception e) {
            log.error("Error calling Python service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // Monta headers com JWT interno
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(internalJwtProvider.generateInternalToken());
        return headers;
    }

    // Monta payload no formato que o Python espera
    private Map<String, Object> buildPayload(
            Long userId,
            UserPreferenceRequest preferences) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId);
        payload.put("limit", 10);

        if (preferences.getTechnologies() != null) {
            payload.put("technologies", splitToList(preferences.getTechnologies()));
        }

        if (preferences.getLanguages() != null) {
            payload.put("languages", splitToList(preferences.getLanguages()));
        }

        if (preferences.getMinimumRating() != null) {
            payload.put("minimum_rating", preferences.getMinimumRating());
        }

        log.debug("Payload sent to Python: {}", payload);
        return payload;
    }

    private List<String> splitToList(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
