package com.simplysip.controller;

import com.simplysip.dto.CreateLocationRequest;
import com.simplysip.model.Location;
import com.simplysip.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public List<Location> getAll() {
        return locationService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getById(@PathVariable Long id) {
        return locationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a location. Latitude must be -90 to 90 and longitude -180 to 180.
     */
    @PostMapping
    public ResponseEntity<Location> create(@Valid @RequestBody CreateLocationRequest request) {
        Location created = locationService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Location> update(@PathVariable Long id, @Valid @RequestBody Location location) {
        return locationService.update(id, location)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!locationService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
