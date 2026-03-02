package org.challengegroup.coursesrecomendation.repository;

import org.challengegroup.coursesrecomendation.entity.Course;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT DISTINCT c.technology FROM Course c ORDER BY c.technology")
    List<String> findDistinctTechnologies();

    @Query("SELECT DISTINCT c.platform FROM Course c ORDER BY c.platform")
    List<String> findDistinctPlatforms();

    @Query("SELECT DISTINCT c.language FROM Course c ORDER BY c.language")
    List<String> findDistinctLanguages();

    @Query("""
            SELECT c FROM Course c
            WHERE LOWER(c.technology) = LOWER(:technology)
              AND c.rating IS NOT NULL
            ORDER BY c.rating DESC
            """)
    List<Course> findTopByTechnologyOrderByRating(
            @Param("technology") String technology,
            Pageable pageable
    );
}
