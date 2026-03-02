package org.challengegroup.coursesrecomendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceOptionsResponse {
    private List<String> technologies;
    private List<String> platforms;
    private List<String> languages;
    private Map<String, List<String>> concepts; 
}
