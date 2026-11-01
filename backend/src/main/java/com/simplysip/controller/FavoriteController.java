package com.simplysip.controller;

import com.simplysip.dto.ApiError;
import com.simplysip.dto.CreateFavoriteRequest;
import com.simplysip.dto.FavoriteResponse;
import com.simplysip.exception.NotFoundException;
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
    public List<FavoriteResponse> getAll(Authentication authentication) {
        User user = currentUser(authentication);
        return favoriteService.findByUserId(user.getId()).stream()
                .map(FavoriteResponse::new)
                .toList();
    }

    /**
     * Returns one favorite. A missing pair is a JSON 404.
     */
    @GetMapping("/{userId}/{drinkId}")
    public ResponseEntity<?> getById(
            @PathVariable Long userId,
            @PathVariable Long drinkId,
            Authentication authentication) {
        User user = currentUser(authentication);
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiError(HttpStatus.FORBIDDEN.value(), "Not allowed to view another user's favorite"));
        }
        Favorite favorite = favoriteService.findById(userId, drinkId)
                .orElseThrow(() -> new NotFoundException(
                        "Favorite not found for user " + userId + " and drink " + drinkId));
        return ResponseEntity.ok(new FavoriteResponse(favorite));
    }

    /**
     * Saves a favorite for a user and drink. Returns 404 if either id is missing and 409 if the pair already exists.
     */
    @PostMapping
    public ResponseEntity<FavoriteResponse> create(@Valid @RequestBody CreateFavoriteRequest request) {
        FavoriteResponse created = new FavoriteResponse(
                favoriteService.create(request.getUserId(), request.getDrinkId()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{userId}/{drinkId}")
                .buildAndExpand(request.getUserId(), request.getDrinkId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{userId}/{drinkId}")
    public ResponseEntity<?> update(
            @PathVariable Long userId,
            @PathVariable Long drinkId,
            @RequestBody(required = false) Favorite body,
            Authentication authentication) {
        User user = currentUser(authentication);
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiError(HttpStatus.FORBIDDEN.value(), "Not allowed to update another user's favorite"));
        }
        Favorite favorite = favoriteService.update(userId, drinkId, body)
                .orElseThrow(() -> new NotFoundException(
                        "Favorite not found for user " + userId + " and drink " + drinkId));
        return ResponseEntity.ok(new FavoriteResponse(favorite));
    }

    @DeleteMapping("/{userId}/{drinkId}")
    public ResponseEntity<?> delete(
            @PathVariable Long userId,
            @PathVariable Long drinkId,
            Authentication authentication) {
        User user = currentUser(authentication);
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiError(HttpStatus.FORBIDDEN.value(), "Not allowed to delete another user's favorite"));
        }
        if (!favoriteService.delete(userId, drinkId)) {
            throw new NotFoundException(
                    "Favorite not found for user " + userId + " and drink " + drinkId);
        }
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}
