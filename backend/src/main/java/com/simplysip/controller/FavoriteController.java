package com.simplysip.controller;

import com.simplysip.model.Favorite;
import com.simplysip.model.User;
import com.simplysip.repository.UserRepository;
import com.simplysip.service.FavoriteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    public FavoriteController(FavoriteService favoriteService, UserRepository userRepository) {
        this.favoriteService = favoriteService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Favorite> getAll(Authentication authentication) {
        User user = currentUser(authentication);
        return favoriteService.findByUserId(user.getId());
    }

    @GetMapping("/{userId}/{drinkId}")
    public ResponseEntity<Favorite> getById(
            @PathVariable Long userId,
            @PathVariable Long drinkId,
            Authentication authentication) {
        User user = currentUser(authentication);
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return favoriteService.findById(userId, drinkId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody FavoriteRequest request,
            Authentication authentication) {
        try {
            User user = currentUser(authentication);
            Long userId = request.getUserId() != null ? request.getUserId() : user.getId();
            if (!user.getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Cannot favorite for another user"));
            }
            Favorite created = favoriteService.create(userId, request.getDrinkId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{userId}/{drinkId}")
    public ResponseEntity<Favorite> update(
            @PathVariable Long userId,
            @PathVariable Long drinkId,
            @RequestBody(required = false) Favorite body,
            Authentication authentication) {
        User user = currentUser(authentication);
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return favoriteService.update(userId, drinkId, body)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{userId}/{drinkId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long userId,
            @PathVariable Long drinkId,
            Authentication authentication) {
        User user = currentUser(authentication);
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!favoriteService.delete(userId, drinkId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    @Getter
    @Setter
    public static class FavoriteRequest {
        private Long userId;

        @NotNull
        private Long drinkId;
    }
}
