package com.simplysip.controller;

import com.simplysip.model.User;
import com.simplysip.service.FavoriteService;
import com.simplysip.service.UserService;
import com.simplysip.service.ProfilePhotoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserFavoritesOwnershipTest {
    private final UserService users = mock(UserService.class);
    private final FavoriteService favorites = mock(FavoriteService.class);
    private final ProfilePhotoService photos = mock(ProfilePhotoService.class);
    private final UserController controller = new UserController(users, favorites, photos);
    private final UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("alice@example.com", "unused");

    private void signedInAsAlice() {
        User alice = new User();
        alice.setId(1L);
        when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
    }

    @Test
    void deniesOtherUsersFavorites() {
        signedInAsAlice();
        assertEquals(HttpStatus.FORBIDDEN, controller.getFavorites(2L, auth).getStatusCode());
        verifyNoInteractions(favorites);
    }

    @Test
    void listsOwnFavorites() {
        signedInAsAlice();
        when(favorites.findResponsesByUserId(1L)).thenReturn(List.of());
        assertEquals(HttpStatus.OK, controller.getFavorites(1L, auth).getStatusCode());
        verify(favorites).findResponsesByUserId(1L);
    }
}
