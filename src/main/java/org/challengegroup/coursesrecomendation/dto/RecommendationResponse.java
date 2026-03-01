package org.challengegroup.coursesrecomendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class RecommendationResponse {

    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("total_returned")  
    private Integer total;

    private List<CourseResponse> courses;
}
