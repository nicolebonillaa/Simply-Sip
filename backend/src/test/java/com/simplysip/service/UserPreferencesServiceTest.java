package com.simplysip.service;

import com.simplysip.dto.UpdatePreferencesRequest;
import com.simplysip.model.User;
import com.simplysip.model.UserPreferences;
import com.simplysip.repository.UserPreferencesRepository;
import com.simplysip.repository.UserRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserPreferencesServiceTest {
    private final UserPreferencesRepository repo = mock(UserPreferencesRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final UserPreferencesService service = new UserPreferencesService(repo, users);

    @Test
    void returnsEmptyPreferencesBeforeFirstSave() {
        assertEquals(List.of(), service.get(1L).excludedAllergens());
    }

    @Test
    void savesAndReadsNormalizedSelections() {
        User user = new User();
        user.setId(1L);
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(repo.save(any(UserPreferences.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.update(1L, new UpdatePreferencesRequest(
                List.of(" Vegan ", "Vegan"), List.of("Milk", "Peanuts")));
        assertEquals(List.of("Vegan"), result.dietaryPreferences());
        assertEquals(List.of("Milk", "Peanuts"), result.excludedAllergens());
        var saved = org.mockito.ArgumentCaptor.forClass(UserPreferences.class);
        verify(repo).save(saved.capture());
        when(repo.findById(1L)).thenReturn(Optional.of(saved.getValue()));
        assertEquals(result, service.get(1L));
    }

    @Test
    void rejectsEmptyAllergenName() {
        assertThrows(IllegalArgumentException.class, () -> service.update(1L,
                new UpdatePreferencesRequest(List.of(), List.of(" "))));
        verify(repo, never()).save(any());
    }
}
