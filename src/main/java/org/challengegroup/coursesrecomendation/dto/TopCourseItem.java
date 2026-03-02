package org.challengegroup.coursesrecomendation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopCourseItem {

    private Integer id;
    private String  title;
    private String  technology;
    private String  platform;
    private String  instructor;
    private Double  rating;
    private String  language;
    private String  link;
    private String  description;
}
