package com.simplysip.service;

import com.simplysip.dto.PreferencesResponse;
import com.simplysip.dto.UpdatePreferencesRequest;
import com.simplysip.exception.NotFoundException;
import com.simplysip.model.UserPreferences;
import com.simplysip.repository.UserPreferencesRepository;
import com.simplysip.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserPreferencesService {
    private final UserPreferencesRepository preferences;
    private final UserRepository users;

    public UserPreferencesService(UserPreferencesRepository preferences, UserRepository users) {
        this.preferences = preferences;
        this.users = users;
    }

    public PreferencesResponse get(Long userId) {
        return preferences.findById(userId).map(PreferencesResponse::new)
                .orElseGet(() -> new PreferencesResponse(List.of(), List.of()));
    }

    public PreferencesResponse update(Long userId, UpdatePreferencesRequest request) {
        Set<String> dietary = normalize(request.dietaryPreferences());
        Set<String> allergens = normalize(request.excludedAllergens());
        UserPreferences value = preferences.findById(userId).orElseGet(() -> {
            UserPreferences created = new UserPreferences();
            created.setUser(users.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found: " + userId)));
            return created;
        });
        value.getDietaryPreferences().clear();
        value.getDietaryPreferences().addAll(dietary);
        value.getExcludedAllergens().clear();
        value.getExcludedAllergens().addAll(allergens);
        return new PreferencesResponse(preferences.save(value));
    }

    private static Set<String> normalize(List<String> input) {
        if (input == null) throw new IllegalArgumentException("Both preference lists are required");
        Set<String> result = new LinkedHashSet<>();
        for (String value : input) {
            if (value == null || value.isBlank() || value.trim().length() > 80) {
                throw new IllegalArgumentException("Preference values must be 1 to 80 characters");
            }
            result.add(value.trim());
        }
        return result;
    }
}
