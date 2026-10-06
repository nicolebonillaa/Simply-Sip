package com.simplysip.controller;

import com.simplysip.dto.CreateUserRequest;
import com.simplysip.dto.UserResponse;
import com.simplysip.model.User;
import com.simplysip.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a user from name, email, and password. The password is hashed before it is stored.
     */
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User saved = userService.create(request.getName(), request.getEmail(), request.getPassword());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(new UserResponse(saved));
    }

    @GetMapping("/me")
    public ResponseEntity<User> me(Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(
            @PathVariable Long id,
            @RequestBody User user,
            Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .filter(current -> current.getId().equals(id))
                .flatMap(current -> userService.update(id, user))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }
}
