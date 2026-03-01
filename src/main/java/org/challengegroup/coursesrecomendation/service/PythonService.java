package org.challengegroup.coursesrecomendation.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.challengegroup.coursesrecomendation.dto.CourseResponse;
import org.challengegroup.coursesrecomendation.dto.RecommendationResponse;
import org.challengegroup.coursesrecomendation.dto.UserPreferenceRequest;
import org.challengegroup.coursesrecomendation.security.InternalJwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PythonService {

    private final RestTemplate restTemplate;
    private final InternalJwtProvider internalJwtProvider;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Value("${python.service.url}")
    private String pythonServiceUrl;

    private static final int TOP_N = 3;

    public List<CourseResponse> getRecommendations(
            Long userId,
            UserPreferenceRequest preferences) {

        String url = pythonServiceUrl + "/recommend/user";

        log.info("Calling Python service | url={} | userId={} | limit={}",
                url, userId, TOP_N);

        HttpHeaders headers = buildHeaders();
        Map<String, Object> payload = buildPayload(userId, preferences);
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(payload, headers);

        try {
            // ── STEP 1: Recebe como byte[] para garantir UTF-8 ─────
            ResponseEntity<byte[]> rawResponse = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    byte[].class
            );

            if (rawResponse.getBody() == null) {
                log.warn("Python returned null body | userId={}", userId);
                return Collections.emptyList();
            }

            // ── STEP 2: Converte bytes → String com UTF-8 explícito
            String rawJson = new String(rawResponse.getBody(), StandardCharsets.UTF_8);

            log.info("====================================");
            log.info("RAW JSON recebido do Python (UTF-8):");
            log.info("{}", rawJson);
            log.info("====================================");

            // ── STEP 3: Desserializa com ObjectMapper ──────────────
            RecommendationResponse body = objectMapper.readValue(
                    rawJson,
                    RecommendationResponse.class
            );

            if (body == null || body.getCourses() == null) {
                log.warn("Python returned empty body | userId={}", userId);
                return Collections.emptyList();
            }

            List<CourseResponse> courses = body.getCourses();

            // ── STEP 4: Log de validação ───────────────────────────
            courses.forEach(c -> {
                log.info("----------------------------------");
                log.info("  id          = {}", c.getId());
                log.info("  title       = {}", c.getTitle());
                log.info("  description = {}", c.getDescription());
                log.info("  score       = {}", c.getScore());
                log.info("  finalScore  = {}", c.getFinalScore());
                log.info("  language    = {}", c.getLanguage());
            });
            log.info("====================================");

            courses.forEach(this::enrichCourseResponse);

            log.info("Python returned {} courses | userId={}", courses.size(), userId);
            return courses;

        } catch (ResourceAccessException e) {
            log.error("Timeout or connection refused | userId={} | error={}", userId, e.getMessage());
            return Collections.emptyList();

        } catch (RestClientException e) {
            log.error("HTTP error calling Python | userId={} | error={}", userId, e.getMessage());
            return Collections.emptyList();

        } catch (IllegalArgumentException e) {
            log.error("Invalid request | userId={} | error={}", userId, e.getMessage());
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Erro inesperado | userId={} | error={}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void enrichCourseResponse(CourseResponse course) {
        if (course.getScore() == null && course.getFinalScore() != null) {
            course.setScore(course.getFinalScore());
        }
        if (course.getScore() == null && course.getColbertScore() != null) {
            course.setScore(course.getColbertScore());
        }
    }

    private HttpHeaders buildHeaders() {
    HttpHeaders headers = new HttpHeaders();

    headers.setContentType(
        new MediaType("application", "json", StandardCharsets.UTF_8)
    );
    headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
    headers.set(HttpHeaders.ACCEPT, "application/json;charset=UTF-8");
    headers.setBearerAuth(internalJwtProvider.generateInternalToken());

    return headers;
}

    private Map<String, Object> buildPayload(Long userId, UserPreferenceRequest preferences) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId);
        payload.put("limit",   TOP_N);

        if (preferences.getTechnology() != null) {
            payload.put("technology", preferences.getTechnology());
        }
        if (preferences.getConceptsOfInterest() != null
                && !preferences.getConceptsOfInterest().isEmpty()) {
            payload.put("concepts_of_interest", preferences.getConceptsOfInterest());
        }
        if (preferences.getLanguages() != null
                && !preferences.getLanguages().isEmpty()) {
            payload.put("languages", preferences.getLanguages());
        }
        if (preferences.getPlatforms() != null
                && !preferences.getPlatforms().isEmpty()) {
            payload.put("platforms", preferences.getPlatforms());
        }

        return payload;
    }
}

