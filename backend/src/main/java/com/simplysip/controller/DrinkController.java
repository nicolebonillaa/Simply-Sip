package com.simplysip.controller;

import com.simplysip.dto.CreateDrinkRequest;
import com.simplysip.dto.CreateNutritionFactsRequest;
import com.simplysip.dto.DrinkResponse;
import com.simplysip.model.NutritionFacts;
import com.simplysip.service.DrinkService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/drinks")
public class DrinkController {

    private final DrinkService drinkService;

    public DrinkController(DrinkService drinkService) {
        this.drinkService = drinkService;
    }

    /**
     * Lists drinks as DrinkResponse. Filters are optional and combinable.
     */
    @GetMapping
    public List<DrinkResponse> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer maxCalories,
            @RequestParam(required = false) Double maxSugar,
            @RequestParam(required = false) Double maxCaffeine,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String excludeAllergens,
            @RequestParam(required = false) String milkTypes) {
        if (category == null && locationId == null && q == null
                && maxCalories == null && maxSugar == null && maxCaffeine == null
                && minPrice == null && maxPrice == null
                && tag == null
                && (excludeAllergens == null || excludeAllergens.isBlank())
                && (milkTypes == null || milkTypes.isBlank())) {
            return drinkService.findAllResponses();
        }
        return drinkService.searchResponses(
                category,
                locationId,
                q,
                maxCalories,
                maxSugar,
                maxCaffeine,
                minPrice,
                maxPrice,
                tag,
                excludeAllergens,
                milkTypes);
    }

    /**
     * Returns one drink as DrinkResponse. A missing id is a JSON 404.
     */
    @GetMapping("/{id}")
    public DrinkResponse getById(@PathVariable Long id) {
        return drinkService.getResponseById(id);
    }

    /**
     * Returns the nutrition facts for one drink. Missing drink or facts are a JSON 404.
     */
    @GetMapping("/{drinkId}/nutrition-facts")
    public NutritionFacts getNutritionFacts(@PathVariable Long drinkId) {
        return drinkService.getNutritionFacts(drinkId);
    }

    /**
     * Creates a drink at an existing location. Returns 404 when locationId is unknown.
     */
    @PostMapping
    public ResponseEntity<DrinkResponse> create(@Valid @RequestBody CreateDrinkRequest request) {
        DrinkResponse created = drinkService.createResponse(
                request.getName(), request.getCategory(), request.getLocationId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Adds the single nutrition-facts row for a drink. Returns 409 if one already exists.
     */
    @PostMapping("/{drinkId}/nutrition-facts")
    public ResponseEntity<NutritionFacts> createNutritionFacts(
            @PathVariable Long drinkId,
            @Valid @RequestBody CreateNutritionFactsRequest request) {
        NutritionFacts created = drinkService.addNutritionFacts(drinkId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DrinkResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody com.simplysip.model.Drink drink) {
        return drinkService.update(id, drink)
                .map(DrinkResponse::new)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new com.simplysip.exception.NotFoundException("Drink not found: " + id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!drinkService.delete(id)) {
            throw new com.simplysip.exception.NotFoundException("Drink not found: " + id);
        }
        return ResponseEntity.noContent().build();
    }
}
