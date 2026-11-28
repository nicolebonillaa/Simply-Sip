package com.simplysip.controller;

import com.simplysip.dto.ChangePasswordRequest;
import com.simplysip.dto.UpdateProfileRequest;
import com.simplysip.dto.UserResponse;
import com.simplysip.model.User;
import com.simplysip.service.FavoriteService;
import com.simplysip.service.ProfilePhotoService;
import com.simplysip.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class UserAccountEndpointsTest {
    private final UserService users = mock(UserService.class);
    private final FavoriteService favorites = mock(FavoriteService.class);
    private final ProfilePhotoService photos = mock(ProfilePhotoService.class);
    private final UserController controller = new UserController(users, favorites, photos);
    private final UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("alice@example.com", "unused");

    private User signedInAsAlice() {
        User alice = new User();
        alice.setId(1L);
        alice.setName("Alice");
        alice.setEmail("alice@example.com");
        when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        return alice;
    }

    @Test
    void updateMeChangesOnlyNameAndUsername() {
        User alice = signedInAsAlice();
        User updated = new User();
        updated.setId(1L);
        updated.setName("Alice Smith");
        updated.setUsername("alice_s");
        updated.setEmail("alice@example.com");
        when(users.updateProfile(alice, "Alice Smith", "alice_s")).thenReturn(updated);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Alice Smith");
        request.setUsername("alice_s");

        UserResponse response = controller.updateMe(request, auth);

        assertEquals("alice_s", response.getUsername());
        assertEquals("Alice Smith", response.getName());
        assertEquals("alice@example.com", response.getEmail());
    }

    @Test
    void changePasswordReturnsNoContent() {
        User alice = signedInAsAlice();
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldpassword");
        request.setNewPassword("newpassword1");

        var response = controller.changePassword(request, auth);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(users).changePassword(alice, "oldpassword", "newpassword1");
    }

    @Test
    void deleteMeRemovesTheSignedInUserAndReturnsNoContent() {
        signedInAsAlice();

        var response = controller.deleteMe(auth);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(users).delete(1L);
        verifyNoInteractions(photos);
    }

    @Test
    void deleteMeAlsoRemovesTheAvatarFile() {
        User alice = signedInAsAlice();
        alice.setPhotoUrl("/uploads/avatars/1-abc.jpg");

        controller.deleteMe(auth);

        verify(photos).removePhoto(alice);
        verify(users).delete(1L);
    }
}