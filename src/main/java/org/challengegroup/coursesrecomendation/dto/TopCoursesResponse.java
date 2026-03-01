package org.challengegroup.coursesrecomendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TopCoursesResponse {

    private String technology;

    @JsonProperty("total_returned")
    private Integer totalReturned;

    private List<TopCourseItem> courses;

    @JsonProperty("available_technologies")
    private List<String> availableTechnologies;
}
