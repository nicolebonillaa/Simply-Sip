package com.simplysip.controller;

import com.simplysip.dto.CreateLocationRequest;
import com.simplysip.dto.LocationResponse;
import com.simplysip.exception.NotFoundException;
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
    public List<LocationResponse> getAll() {
        return locationService.findAllResponses();
    }

    /**
     * Returns one location. A missing id is a JSON 404.
     */
    @GetMapping("/{id}")
    public LocationResponse getById(@PathVariable Long id) {
        return locationService.findResponseById(id)
                .orElseThrow(() -> new NotFoundException("Location not found: " + id));
    }

    /**
     * Creates a location. Latitude must be -90 to 90 and longitude -180 to 180.
     */
    @PostMapping
    public ResponseEntity<LocationResponse> create(@Valid @RequestBody CreateLocationRequest request) {
        LocationResponse created = locationService.createResponse(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public LocationResponse update(@PathVariable Long id, @Valid @RequestBody Location location) {
        return locationService.update(id, location)
                .map(LocationResponse::new)
                .orElseThrow(() -> new NotFoundException("Location not found: " + id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!locationService.delete(id)) {
            throw new NotFoundException("Location not found: " + id);
        }
        return ResponseEntity.noContent().build();
    }
}
