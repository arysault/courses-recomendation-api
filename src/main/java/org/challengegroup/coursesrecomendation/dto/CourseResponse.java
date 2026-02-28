package org.challengegroup.coursesrecomendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private Long id;
    private String title;
    private String technology;
    private String description;
    private String instructor;
    private Double rating;
    private String platform;
    private String language;
    private String link;
    private Double score;
    private String reason;
}
