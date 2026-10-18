package com.simplysip.service;

import com.simplysip.model.User;
import com.simplysip.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

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

    /**
     * Creates a user with a BCrypt password hash. Rejects a blank or already-used email.
     */
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

    public Optional<User> update(Long id, User updates) {
        return userRepository.findById(id).map(existing -> {
            if (updates.getName() != null) {
                existing.setName(updates.getName());
            }
            if (updates.getEmail() != null) {
                existing.setEmail(updates.getEmail().trim().toLowerCase());
            }
            if (updates.getPassword() != null && !updates.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(updates.getPassword()));
            }
            return userRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }
}
