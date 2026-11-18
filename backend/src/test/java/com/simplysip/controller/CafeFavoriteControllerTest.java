package com.simplysip.controller;

import com.simplysip.dto.CreateCafeFavoriteRequest;
import com.simplysip.exception.ConflictException;
import com.simplysip.model.User;
import com.simplysip.repository.UserRepository;
import com.simplysip.service.CafeFavoriteService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CafeFavoriteControllerTest {
    private final CafeFavoriteService service = mock(CafeFavoriteService.class);
    private final UserRepository users = mock(UserRepository.class);
    private final CafeFavoriteController controller = new CafeFavoriteController(service, users);
    private final UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("alice@example.com", "unused");

    private void signedIn() {
        User user = new User();
        user.setId(1L);
        when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void listsOnlyOwnCafeFavorites() {
        signedIn();
        when(service.list(1L)).thenReturn(List.of());
        assertEquals(List.of(), controller.list(auth));
        verify(service).list(1L);
    }

    @Test
    void savesAndRemovesUsingAuthenticatedUser() {
        signedIn();
        assertEquals(HttpStatus.CREATED, controller.save(new CreateCafeFavoriteRequest(4L), auth).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.remove(4L, auth).getStatusCode());
        verify(service).save(1L, 4L);
        verify(service).remove(1L, 4L);
    }

    @Test
    void duplicateSaveIsRejected() {
        signedIn();
        when(service.save(1L, 4L)).thenThrow(new ConflictException("Café already saved"));
        assertThrows(ConflictException.class,
                () -> controller.save(new CreateCafeFavoriteRequest(4L), auth));
    }
}
