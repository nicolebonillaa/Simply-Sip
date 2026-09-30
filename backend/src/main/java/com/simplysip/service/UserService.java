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

    public User create(User user) {
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
