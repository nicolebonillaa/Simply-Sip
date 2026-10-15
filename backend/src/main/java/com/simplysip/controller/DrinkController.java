package com.simplysip.controller;

import com.simplysip.dto.CreateDrinkRequest;
import com.simplysip.dto.CreateNutritionFactsRequest;
import com.simplysip.exception.NotFoundException;
import com.simplysip.model.Drink;
import com.simplysip.model.NutritionFacts;
import com.simplysip.service.DrinkService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drinks")
public class DrinkController {

    private final DrinkService drinkService;

    public DrinkController(DrinkService drinkService) {
        this.drinkService = drinkService;
    }

    /**
     * Lists drinks. Nutrition and allergen filters are optional and can be combined.
     */
    @GetMapping
    public List<Drink> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer maxCalories,
            @RequestParam(required = false) Double maxSugar,
            @RequestParam(required = false) Double maxCaffeine,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String excludeAllergens) {
        if (category == null && locationId == null && q == null
                && maxCalories == null && maxSugar == null && maxCaffeine == null
                && tag == null && (excludeAllergens == null || excludeAllergens.isBlank())) {
            return drinkService.findAll();
        }
        return drinkService.search(
                category, locationId, q, maxCalories, maxSugar, maxCaffeine, tag, excludeAllergens);
    }

    /**
     * Returns one drink, including its allergens. A missing id is a JSON 404.
     */
    @GetMapping("/{id}")
    public Drink getById(@PathVariable Long id) {
        return drinkService.findById(id)
                .orElseThrow(() -> new NotFoundException("Drink not found: " + id));
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
    public ResponseEntity<Drink> create(@Valid @RequestBody CreateDrinkRequest request) {
        Drink created = drinkService.create(request.getName(), request.getCategory(), request.getLocationId());
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
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Drink drink) {
        try {
            return drinkService.update(id, drink)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!drinkService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
