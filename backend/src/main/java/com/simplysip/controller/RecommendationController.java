package com.simplysip.controller;

import com.simplysip.dto.DrinkFilterOptions;
import com.simplysip.dto.ScoredDrinkResponse;
import com.simplysip.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public List<ScoredDrinkResponse> recommend(
            @RequestParam Long userId,
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
        // Only forward coordinates when distance filtering is requested (maxDistance present).
        Double filterLat = maxDistance == null ? null : lat;
        Double filterLng = maxDistance == null ? null : lng;
        DrinkFilterOptions filters = DrinkFilterOptions.builder()
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
                .maxDistance(maxDistance)
                .build();
        return recommendationService.recommend(userId, filters, filterLat, filterLng);
    }
}
