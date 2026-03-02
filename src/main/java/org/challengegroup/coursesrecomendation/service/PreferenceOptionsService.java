package org.challengegroup.coursesrecomendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.challengegroup.coursesrecomendation.dto.PreferenceOptionsResponse;
import org.challengegroup.coursesrecomendation.entity.TechnologyConcept;
import org.challengegroup.coursesrecomendation.repository.CourseRepository;
import org.challengegroup.coursesrecomendation.repository.TechnologyConceptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenceOptionsService {

    private final CourseRepository courseRepository;
    private final TechnologyConceptRepository technologyConceptRepository;

    @Transactional(readOnly = true)
    public PreferenceOptionsResponse getOptions() {
        log.info("Fetching preference options from database");

        Map<String, List<String>> conceptsMap = technologyConceptRepository
                .findAll()
                .stream()
                .collect(Collectors.toMap(
                        TechnologyConcept::getTechnology,
                        TechnologyConcept::getConceptsList
                ));

        return PreferenceOptionsResponse.builder()
                .technologies(technologyConceptRepository.findAllTechnologies())
                .platforms(courseRepository.findDistinctPlatforms())
                .languages(courseRepository.findDistinctLanguages())
                .concepts(conceptsMap)
                .build();
    }
}
