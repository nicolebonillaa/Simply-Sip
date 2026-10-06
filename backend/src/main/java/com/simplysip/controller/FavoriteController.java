package com.simplysip.controller;

import com.simplysip.dto.CreateFavoriteRequest;
import com.simplysip.model.Favorite;
import com.simplysip.model.User;
import com.simplysip.repository.UserRepository;
import com.simplysip.service.FavoriteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

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

    /**
     * Saves a favorite for a user and drink. Returns 404 if either id is missing and 409 if the pair already exists.
     */
    @PostMapping
    public ResponseEntity<Favorite> create(@Valid @RequestBody CreateFavoriteRequest request) {
        Favorite created = favoriteService.create(request.getUserId(), request.getDrinkId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{userId}/{drinkId}")
                .buildAndExpand(request.getUserId(), request.getDrinkId())
                .toUri();
        return ResponseEntity.created(location).body(created);
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
}
