package org.challengegroup.coursesrecomendation.dto;
import lombok.Data;


@Data
public class TopCoursesRequest {

    private String       technology;  
    private Integer      limit = 5;  
}
