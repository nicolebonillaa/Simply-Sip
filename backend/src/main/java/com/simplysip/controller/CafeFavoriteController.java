package com.simplysip.controller;

import com.simplysip.dto.CafeFavoriteResponse;
import com.simplysip.dto.CreateCafeFavoriteRequest;
import com.simplysip.exception.NotFoundException;
import com.simplysip.model.User;
import com.simplysip.repository.UserRepository;
import com.simplysip.service.CafeFavoriteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cafe-favorites")
public class CafeFavoriteController {
    private final CafeFavoriteService service;
    private final UserRepository users;

    public CafeFavoriteController(CafeFavoriteService service, UserRepository users) {
        this.service = service;
        this.users = users;
    }

    @GetMapping
    public List<CafeFavoriteResponse> list(Authentication authentication) {
        return service.list(currentUser(authentication).getId());
    }

    @PostMapping
    public ResponseEntity<CafeFavoriteResponse> save(@Valid @RequestBody CreateCafeFavoriteRequest request,
                                                     Authentication authentication) {
        CafeFavoriteResponse result = service.save(currentUser(authentication).getId(), request.locationId());
        return ResponseEntity.created(URI.create("/api/cafe-favorites/" + request.locationId())).body(result);
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> remove(@PathVariable Long locationId, Authentication authentication) {
        service.remove(currentUser(authentication).getId(), locationId);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return users.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
    }
}
