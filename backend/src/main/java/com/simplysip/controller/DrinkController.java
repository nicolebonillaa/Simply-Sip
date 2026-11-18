package com.simplysip.controller;

import com.simplysip.dto.CreateDrinkRequest;
import com.simplysip.dto.CreateNutritionFactsRequest;
import com.simplysip.dto.DrinkFilterOptions;
import com.simplysip.dto.DrinkResponse;
import com.simplysip.exception.NotFoundException;
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
            @RequestParam(required = false) String excludeAllergens,
            @RequestParam(required = false) Boolean includeUnknownAllergens,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double maxDistance) {
        return drinkService.findResponses(DrinkFilterOptions.builder()
                .category(category)
                .locationId(locationId)
                .q(q)
                .maxCalories(maxCalories)
                .maxSugar(maxSugar)
                .maxCaffeine(maxCaffeine)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .excludeAllergens(excludeAllergens)
                .includeUnknownAllergens(includeUnknownAllergens)
                .lat(lat)
                .lng(lng)
                .maxDistance(maxDistance)
                .build());
    }

    @GetMapping("/{id}")
    public DrinkResponse getById(@PathVariable Long id) {
        return drinkService.getResponseById(id);
    }

    @GetMapping("/sizes/{drinkSizeId}/nutrition-facts")
    public NutritionFacts getNutritionFacts(@PathVariable Long drinkSizeId) {
        return drinkService.getNutritionFactsForSize(drinkSizeId);
    }

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

    @PostMapping("/nutrition-facts")
    public ResponseEntity<NutritionFacts> createNutritionFacts(
            @Valid @RequestBody CreateNutritionFactsRequest request) {
        NutritionFacts created = drinkService.addNutritionFacts(request);
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
                .orElseThrow(() -> new NotFoundException("Drink not found: " + id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!drinkService.delete(id)) {
            throw new NotFoundException("Drink not found: " + id);
        }
        return ResponseEntity.noContent().build();
    }
}
