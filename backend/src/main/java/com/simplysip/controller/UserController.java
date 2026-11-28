package com.simplysip.controller;

import com.simplysip.dto.ChangePasswordRequest;
import com.simplysip.dto.CreateUserRequest;
import com.simplysip.dto.FavoriteResponse;
import com.simplysip.dto.UpdateProfileRequest;
import com.simplysip.dto.UserResponse;
import com.simplysip.exception.NotFoundException;
import com.simplysip.model.User;
import com.simplysip.service.FavoriteService;
import com.simplysip.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import com.simplysip.service.ProfilePhotoService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FavoriteService favoriteService;
    private final ProfilePhotoService profilePhotoService;

    public UserController(UserService userService, FavoriteService favoriteService, ProfilePhotoService profilePhotoService) {
        this.userService = userService;
        this.favoriteService = favoriteService;
        this.profilePhotoService = profilePhotoService;
    }

    /**
     * Creates a user from name, email, and password. The password is hashed
     * before it is stored.
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

    /**
     * Lists users without passwords.
     */
    @GetMapping
    public List<UserResponse> getAll() {
        return userService.findAll().stream().map(UserResponse::new).toList();
    }

    /**
     * Returns the authenticated user without the password.
     */
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .map(UserResponse::new)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    /**
     * Returns one user without the password. Missing ids are 404.
     */
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(UserResponse::new)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    /**
     * Returns favorites for a user. Missing user ids are 404.
     */
    @GetMapping("/{id}/favorites")
    public ResponseEntity<List<FavoriteResponse>> getFavorites(@PathVariable Long id, Authentication authentication) {
        User current = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!current.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(favoriteService.findResponsesByUserId(id));
    }

    /**
     * Updates the signed-in user's name and username. Email and password in the
     * body are ignored. A username taken by someone else is a 409.
     */
    @PutMapping("/me")
    public UserResponse updateMe(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        User user = currentUser(authentication);
        return new UserResponse(userService.updateProfile(user, request.getName(), request.getUsername()));
    }

    /**
     * Changes the signed-in user's password. A wrong current password is a 400.
     */
    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        userService.changePassword(currentUser(authentication), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes the signed-in user. Favorites, café favorites and preferences are
     * removed with the user (cascade on the User entity); the avatar file is deleted from S3.
     * Returns 204 on success.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(Authentication authentication) {
        User user = currentUser(authentication);
        if (user.getPhotoUrl() != null) {
            profilePhotoService.removePhoto(user);
        }
        userService.delete(user.getId());
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse uploadPhoto(@RequestParam("photo") MultipartFile photo,
            Authentication authentication) throws IOException {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new UserResponse(profilePhotoService.replacePhoto(user, photo));
    }

    @DeleteMapping("/me/photo")
    public UserResponse removePhoto(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new UserResponse(profilePhotoService.removePhoto(user));
    }
}