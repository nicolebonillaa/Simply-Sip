package com.simplysip.controller;

import com.simplysip.dto.PreferencesResponse;
import com.simplysip.dto.UpdatePreferencesRequest;
import com.simplysip.exception.NotFoundException;
import com.simplysip.model.User;
import com.simplysip.repository.UserRepository;
import com.simplysip.service.UserPreferencesService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me/preferences")
public class UserPreferencesController {
    private final UserPreferencesService service;
    private final UserRepository users;

    public UserPreferencesController(UserPreferencesService service, UserRepository users) {
        this.service = service;
        this.users = users;
    }

    @GetMapping
    public PreferencesResponse get(Authentication authentication) {
        return service.get(currentUser(authentication).getId());
    }

    @PutMapping
    public PreferencesResponse update(@Valid @RequestBody UpdatePreferencesRequest request,
                                      Authentication authentication) {
        return service.update(currentUser(authentication).getId(), request);
    }

    private User currentUser(Authentication authentication) {
        return users.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
    }
}
