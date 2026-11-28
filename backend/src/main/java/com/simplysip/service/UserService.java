package com.simplysip.service;

import com.simplysip.exception.ConflictException;
import com.simplysip.model.User;
import com.simplysip.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private static final String USERNAME_TAKEN = "That username is already taken.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User create(String name, String email, String rawPassword) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        String normalized = email.trim().toLowerCase();
        if (userRepository.findByEmail(normalized).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(normalized);
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public User updateProfile(User user, String name, String username) {
        String cleanName = name.trim();
        String cleanUsername = username.trim();

        userRepository.findByUsernameIgnoreCase(cleanUsername)
                .filter(other -> !other.getId().equals(user.getId()))
                .ifPresent(other -> {
                    throw new ConflictException(USERNAME_TAKEN);
                });

        user.setName(cleanName);
        user.setUsername(cleanUsername);
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(USERNAME_TAKEN);
        }
    }

    public void changePassword(User user, String currentPassword, String newPassword) {
        if (user.getPassword() == null
                || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Your current password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean delete(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }
}