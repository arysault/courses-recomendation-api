package org.challengegroup.coursesrecomendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceResponse {

    private Long id;
    private Long userId;
    private String technology;
    private List<String> conceptsOfInterest;
    private List<String> languages;
    private List<String> platforms;
}
