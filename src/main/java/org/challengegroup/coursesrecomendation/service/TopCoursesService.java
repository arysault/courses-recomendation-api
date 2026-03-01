package org.challengegroup.coursesrecomendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.challengegroup.coursesrecomendation.dto.TopCourseItem;
import org.challengegroup.coursesrecomendation.dto.TopCoursesResponse;
import org.challengegroup.coursesrecomendation.entity.Course;
import org.challengegroup.coursesrecomendation.repository.CourseRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopCoursesService {

    private final CourseRepository courseRepository;

    private static final int DEFAULT_LIMIT = 5;

    @Transactional(readOnly = true)
    public TopCoursesResponse getTopByTechnology(String technology) {

        log.info("TopCourses | technology={} | limit={}", technology, DEFAULT_LIMIT);

        // ── Top 5 cursos da tecnologia filtrada ───────────────────
        List<Course> courses = courseRepository.findTopByTechnologyOrderByRating(
                technology,
                PageRequest.of(0, DEFAULT_LIMIT)
        );

        // ── Todas as tecnologias disponíveis no banco ─────────────
        List<String> availableTechnologies = courseRepository.findDistinctTechnologies();

        log.info("TopCourses found {} courses | technology={}", courses.size(), technology);
        log.info("Available technologies: {}", availableTechnologies);

        List<TopCourseItem> items = courses.stream()
                .map(this::toItem)
                .toList();

        items.forEach(c -> log.info(
                "  [{}] {} | rating={}",
                c.getId(), c.getTitle(), c.getRating()
        ));

        return TopCoursesResponse.builder()
                .technology(technology)
                .totalReturned(items.size())
                .courses(items)
                .availableTechnologies(availableTechnologies)  
                .build();
    }

    private TopCourseItem toItem(Course course) {
        return TopCourseItem.builder()
                .id(course.getId())
                .title(course.getTitle())
                .technology(course.getTechnology())
                .platform(course.getPlatform())
                .instructor(course.getInstructor())
                .rating(course.getRating())
                .language(course.getLanguage())
                .link(course.getLink())
                .description(course.getColbertDescription())
                .build();
    }
}
