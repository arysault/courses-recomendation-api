package org.challengegroup.coursesrecomendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CourseResponse {

    private Integer id;
    private String title;
    private String technology;
    private String platform;
    private String instructor;
    private Double rating;
    private String language;
    private String link;

    private String description;

    private Double score;

    @JsonProperty("final_score")
    private Double finalScore;

    @JsonProperty("colbert_score")
    private Double colbertScore;

    @JsonProperty("ontology_score")
    private Double ontologyScore;
}
