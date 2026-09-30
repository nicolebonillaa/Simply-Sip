package com.simplysip.controller;

import com.simplysip.model.User;
import com.simplysip.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
