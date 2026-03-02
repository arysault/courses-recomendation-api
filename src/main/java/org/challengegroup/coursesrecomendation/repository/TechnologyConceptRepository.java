package org.challengegroup.coursesrecomendation.repository;

import org.challengegroup.coursesrecomendation.entity.TechnologyConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnologyConceptRepository extends JpaRepository<TechnologyConcept, Long> {

    Optional<TechnologyConcept> findByTechnology(String technology);

    @Query("SELECT t.technology FROM TechnologyConcept t ORDER BY t.technology")
    List<String> findAllTechnologies();
}
