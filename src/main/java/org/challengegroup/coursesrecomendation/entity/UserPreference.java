package org.challengegroup.coursesrecomendation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "user_preferences", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String languages;

    @Column(columnDefinition = "TEXT")
    private String technologies;

    @Column(name = "platforms", columnDefinition = "TEXT")
    private String platforms;

    @Column(length = 50)
    private String level;

    @Column(name = "minimum_rating")
    @Builder.Default
    private Double minimumRating = 4.0;
}
