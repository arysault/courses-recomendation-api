package org.challengegroup.coursesrecomendation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "technology_concepts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnologyConcept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String technology;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Array(length = 500)
    @Column(
        name = "concepts_of_interest",
        columnDefinition = "TEXT[]",
        nullable = false
    )
    private String[] conceptsOfInterest;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Helper para retornar como List
    @Transient
    public List<String> getConceptsList() {
        if (conceptsOfInterest == null) return List.of();
        return List.of(conceptsOfInterest);
    }
}
