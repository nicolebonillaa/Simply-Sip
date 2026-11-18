package com.simplysip.service;

import com.simplysip.dto.DrinkFilterOptions;
import com.simplysip.dto.DrinkResponse;
import com.simplysip.dto.ScoredDrinkResponse;
import com.simplysip.exception.NotFoundException;
import com.simplysip.model.Drink;
import com.simplysip.repository.FavoriteRepository;
import com.simplysip.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PopularityRecommendationService implements RecommendationService {

    private final UserRepository userRepository;
    private final DrinkService drinkService;
    private final FavoriteRepository favoriteRepository;

    public PopularityRecommendationService(
            UserRepository userRepository,
            DrinkService drinkService,
            FavoriteRepository favoriteRepository) {
        this.userRepository = userRepository;
        this.drinkService = drinkService;
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    public List<ScoredDrinkResponse> recommend(Long userId, DrinkFilterOptions filters, Double lat, Double lng) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }

        Double maxDistance = filters == null ? null : filters.getMaxDistance();
        // Distance filter only when the full trio is present (lat/lng alone are ignored for this placeholder).
        boolean applyDistance = lat != null && lng != null && maxDistance != null;
        if (!applyDistance && (lat != null || lng != null || maxDistance != null) && maxDistance != null) {
            DrinkService.validateDistanceParams(lat, lng, maxDistance);
        }

        DrinkFilterOptions.DrinkFilterOptionsBuilder builder = DrinkFilterOptions.builder();
        if (filters != null) {
            builder.category(filters.getCategory())
                    .locationId(filters.getLocationId())
                    .q(filters.getQ())
                    .maxCalories(filters.getMaxCalories())
                    .maxSugar(filters.getMaxSugar())
                    .maxCaffeine(filters.getMaxCaffeine())
                    .minPrice(filters.getMinPrice())
                    .maxPrice(filters.getMaxPrice())
                    .excludeAllergens(filters.getExcludeAllergens())
                    .includeUnknownAllergens(filters.getIncludeUnknownAllergens());
        }
        if (applyDistance) {
            builder.lat(lat).lng(lng).maxDistance(maxDistance);
        }

        List<Drink> drinks = drinkService.findDrinks(builder.build());
        Map<Long, Long> favoriteCounts = loadFavoriteCounts();

        return drinks.stream()
                .map(drink -> {
                    long count = favoriteCounts.getOrDefault(drink.getId(), 0L);
                    // SCRUM-64: replace popularity score with personalized recommendation algorithm
                    double score = count;
                    return new ScoredDrinkResponse(new DrinkResponse(drink), score);
                })
                .sorted(Comparator
                        .comparingDouble(ScoredDrinkResponse::getScore).reversed()
                        .thenComparing(scored -> scored.getDrink().getName(),
                                Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private Map<Long, Long> loadFavoriteCounts() {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : favoriteRepository.countGroupedByDrinkId()) {
            Long drinkId = (Long) row[0];
            Long count = (Long) row[1];
            counts.put(drinkId, count);
        }
        return counts;
    }
}
