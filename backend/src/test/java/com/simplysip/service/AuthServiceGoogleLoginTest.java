package com.simplysip.service;

import com.simplysip.dto.AuthResponse;
import com.simplysip.model.User;
import com.simplysip.repository.UserRepository;
import com.simplysip.security.GoogleIdentity;
import com.simplysip.security.GoogleTokenVerifier;
import com.simplysip.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceGoogleLoginTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JwtService jwt = mock(JwtService.class);
    private final AuthenticationManager authManager = mock(AuthenticationManager.class);
    private final GoogleTokenVerifier google = mock(GoogleTokenVerifier.class);
    private final AuthService service = new AuthService(users, encoder, jwt, authManager, google);

    private GoogleIdentity identity(boolean emailVerified) {
        return new GoogleIdentity("google-123", "Alice@Gmail.com", emailVerified, "Alice Smith");
    }

    private void stubSave() {
        when(users.save(any(User.class))).thenAnswer(call -> {
            User saved = call.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(7L);
            }
            return saved;
        });
        when(jwt.generateToken(anyLong(), anyString())).thenReturn("app-token");
    }

    @Test
    void newGoogleUserIsCreatedWithNoUsablePassword() {
        when(google.verify("id-token")).thenReturn(identity(true));
        when(users.findByGoogleSubject("google-123")).thenReturn(Optional.empty());
        when(users.findByEmail("alice@gmail.com")).thenReturn(Optional.empty());
        when(encoder.encode(anyString())).thenReturn("random-hash");
        stubSave();

        AuthResponse response = service.googleLogin("id-token");

        assertEquals("app-token", response.getToken());
        assertEquals("alice@gmail.com", response.getEmail());
        assertEquals("Alice Smith", response.getName());
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertEquals("GOOGLE", saved.getValue().getAuthProvider());
        assertEquals("google-123", saved.getValue().getGoogleSubject());
        assertEquals("random-hash", saved.getValue().getPassword());
    }

    @Test
    void returningGoogleUserIsFoundByGoogleIdAndNothingIsCreated() {
        User alice = new User();
        alice.setId(3L);
        alice.setName("Alice");
        alice.setEmail("alice@gmail.com");
        when(google.verify("id-token")).thenReturn(identity(true));
        when(users.findByGoogleSubject("google-123")).thenReturn(Optional.of(alice));
        when(jwt.generateToken(3L, "alice@gmail.com")).thenReturn("app-token");

        AuthResponse response = service.googleLogin("id-token");

        assertEquals(3L, response.getUserId());
        verify(users, never()).save(any());
    }

    @Test
    void googleIsLinkedToAnExistingPasswordAccountWithTheSameVerifiedEmail() {
        User existing = new User();
        existing.setId(5L);
        existing.setName("Alice");
        existing.setEmail("alice@gmail.com");
        existing.setPassword("her-real-hash");
        when(google.verify("id-token")).thenReturn(identity(true));
        when(users.findByGoogleSubject("google-123")).thenReturn(Optional.empty());
        when(users.findByEmail("alice@gmail.com")).thenReturn(Optional.of(existing));
        stubSave();

        service.googleLogin("id-token");

        assertEquals("google-123", existing.getGoogleSubject());
        assertEquals("her-real-hash", existing.getPassword());
        assertNull(existing.getAuthProvider());
        verify(encoder, never()).encode(anyString());
    }

    @Test
    void unverifiedGoogleEmailIsRejectedAndNothingIsSaved() {
        when(google.verify("id-token")).thenReturn(identity(false));
        when(users.findByGoogleSubject("google-123")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.googleLogin("id-token"));

        verify(users, never()).save(any());
    }
}