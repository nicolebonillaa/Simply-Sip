package com.simplysip.controller;

import com.simplysip.exception.NotFoundException;
import com.simplysip.model.User;
import com.simplysip.repository.UserRepository;
import com.simplysip.service.FavoriteService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteOwnerDeleteTest {
    private final FavoriteService service = mock(FavoriteService.class);
    private final UserRepository users = mock(UserRepository.class);
    private final FavoriteController controller = new FavoriteController(service, users);
    private final UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("alice@example.com", "unused");

    @Test
    void deletesOnlyAuthenticatedUsersDrink() {
        User user = new User();
        user.setId(1L);
        when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(service.delete(1L, 5L)).thenReturn(true);
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteMine(5L, auth).getStatusCode());
        verify(service).delete(1L, 5L);
        verify(service, never()).delete(2L, 5L);
    }

    @Test
    void missingDrinkFavoriteReturnsNotFound() {
        User user = new User();
        user.setId(1L);
        when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        assertThrows(NotFoundException.class, () -> controller.deleteMine(5L, auth));
    }
}
