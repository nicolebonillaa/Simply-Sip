package com.simplysip.service;

import com.simplysip.exception.ConflictException;
import com.simplysip.model.User;
import com.simplysip.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final UserService service = new UserService(users, encoder);

    private User user(long id, String username) {
        User user = new User();
        user.setId(id);
        user.setName("Alice");
        user.setEmail("alice@csun.edu");
        user.setPassword("old-hash");
        user.setUsername(username);
        return user;
    }

    @Test
    void updateProfileSavesTrimmedNameAndUsername() {
        User alice = user(1L, null);
        when(users.findByUsernameIgnoreCase("alice_s")).thenReturn(Optional.empty());
        when(users.saveAndFlush(any(User.class))).thenAnswer(call -> call.getArgument(0));

        User saved = service.updateProfile(alice, "  Alice Smith ", " alice_s ");

        assertEquals("Alice Smith", saved.getName());
        assertEquals("alice_s", saved.getUsername());
        assertEquals("alice@csun.edu", saved.getEmail());
        assertEquals("old-hash", saved.getPassword());
    }

    @Test
    void updateProfileRejectsUsernameTakenByAnotherUser() {
        User alice = user(1L, null);
        when(users.findByUsernameIgnoreCase("bobby")).thenReturn(Optional.of(user(2L, "bobby")));

        ConflictException error = assertThrows(ConflictException.class,
                () -> service.updateProfile(alice, "Alice", "bobby"));

        assertEquals("That username is already taken.", error.getMessage());
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void updateProfileAllowsKeepingYourOwnUsername() {
        User alice = user(1L, "alice_s");
        when(users.findByUsernameIgnoreCase("Alice_S")).thenReturn(Optional.of(alice));
        when(users.saveAndFlush(any(User.class))).thenAnswer(call -> call.getArgument(0));

        User saved = service.updateProfile(alice, "Alice", "Alice_S");

        assertEquals("Alice_S", saved.getUsername());
    }

    @Test
    void updateProfileTurnsADatabaseRaceIntoConflict() {
        User alice = user(1L, null);
        when(users.findByUsernameIgnoreCase("racer")).thenReturn(Optional.empty());
        when(users.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(ConflictException.class, () -> service.updateProfile(alice, "Alice", "racer"));
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        User alice = user(1L, "alice_s");
        when(encoder.matches("wrong", "old-hash")).thenReturn(false);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(alice, "wrong", "newpassword1"));

        assertEquals("Your current password is incorrect.", error.getMessage());
        verify(users, never()).save(any());
    }

    @Test
    void changePasswordStoresTheNewHash() {
        User alice = user(1L, "alice_s");
        when(encoder.matches("oldpassword", "old-hash")).thenReturn(true);
        when(encoder.encode("newpassword1")).thenReturn("new-hash");

        service.changePassword(alice, "oldpassword", "newpassword1");

        assertEquals("new-hash", alice.getPassword());
        verify(users).save(alice);
    }
}