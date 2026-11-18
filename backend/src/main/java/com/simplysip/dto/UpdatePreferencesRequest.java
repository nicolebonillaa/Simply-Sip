package com.simplysip.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdatePreferencesRequest(@NotNull List<String> dietaryPreferences,
                                       @NotNull List<String> excludedAllergens) {
}
