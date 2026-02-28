package org.challengegroup.coursesrecomendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RecommendationResponse {

    @JsonProperty("user_id")
    private Integer userId;

    private Integer total;
    private String algorithm;
    private List<CourseResponse> courses;
}
