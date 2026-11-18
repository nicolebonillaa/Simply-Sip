package com.simplysip.dto;

import com.simplysip.model.UserPreferences;
import java.util.List;

public record PreferencesResponse(List<String> dietaryPreferences, List<String> excludedAllergens) {
    public PreferencesResponse(UserPreferences value) {
        this(value.getDietaryPreferences().stream().sorted().toList(),
             value.getExcludedAllergens().stream().sorted().toList());
    }
}
