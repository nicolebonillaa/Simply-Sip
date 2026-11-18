package com.simplysip.service;

import com.simplysip.dto.CreateNutritionFactsRequest;
import com.simplysip.dto.DrinkFilterOptions;
import com.simplysip.dto.DrinkResponse;
import com.simplysip.exception.ConflictException;
import com.simplysip.exception.NotFoundException;
import com.simplysip.model.Drink;
import com.simplysip.model.DrinkSize;
import com.simplysip.model.Location;
import com.simplysip.model.NutritionFacts;
import com.simplysip.repository.AllergenRepository;
import com.simplysip.repository.DrinkRepository;
import com.simplysip.repository.DrinkSizeRepository;
import com.simplysip.repository.LocationRepository;
import com.simplysip.repository.NutritionFactsRepository;
import com.simplysip.util.DistanceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DrinkService {

    private final DrinkRepository drinkRepository;
    private final LocationRepository locationRepository;
    private final NutritionFactsRepository nutritionFactsRepository;
    private final DrinkSizeRepository drinkSizeRepository;
    private final AllergenRepository allergenRepository;

    public DrinkService(
            DrinkRepository drinkRepository,
            LocationRepository locationRepository,
            NutritionFactsRepository nutritionFactsRepository,
            DrinkSizeRepository drinkSizeRepository,
            AllergenRepository allergenRepository) {
        this.drinkRepository = drinkRepository;
        this.locationRepository = locationRepository;
        this.nutritionFactsRepository = nutritionFactsRepository;
        this.drinkSizeRepository = drinkSizeRepository;
        this.allergenRepository = allergenRepository;
    }

    public List<DrinkResponse> findAllResponses() {
        return drinkRepository.findAll().stream().map(DrinkResponse::new).toList();
    }

    public List<DrinkResponse> findResponses(DrinkFilterOptions filters) {
        return findDrinks(filters).stream().map(DrinkResponse::new).toList();
    }

    public List<Drink> findDrinks(DrinkFilterOptions filters) {
        DrinkFilterOptions options = filters == null ? DrinkFilterOptions.builder().build() : filters;
        validateDistanceParams(options.getLat(), options.getLng(), options.getMaxDistance());

        boolean hasSearchFilters = options.getCategory() != null
                || options.getLocationId() != null
                || options.getQ() != null
                || options.getMaxCalories() != null
                || options.getMaxSugar() != null
                || options.getMaxCaffeine() != null
                || options.getMinPrice() != null
                || options.getMaxPrice() != null
                || (options.getExcludeAllergens() != null && !options.getExcludeAllergens().isBlank())
                || options.getIncludeUnknownAllergens() != null;

        List<Drink> drinks = hasSearchFilters
                ? search(
                        options.getCategory(),
                        options.getLocationId(),
                        options.getQ(),
                        options.getMaxCalories(),
                        options.getMaxSugar(),
                        options.getMaxCaffeine(),
                        options.getMinPrice(),
                        options.getMaxPrice(),
                        options.getExcludeAllergens(),
                        options.getIncludeUnknownAllergens())
                : drinkRepository.findAll();

        return applyDistanceFilter(drinks, options.getLat(), options.getLng(), options.getMaxDistance());
    }

    public List<DrinkResponse> searchResponses(
            String category,
            Long locationId,
            String q,
            Integer maxCalories,
            Double maxSugar,
            Double maxCaffeine,
            Double minPrice,
            Double maxPrice,
            String excludeAllergens,
            Boolean includeUnknownAllergens) {
        return searchResponses(
                category,
                locationId,
                q,
                maxCalories,
                maxSugar,
                maxCaffeine,
                minPrice,
                maxPrice,
                excludeAllergens,
                includeUnknownAllergens,
                null,
                null,
                null);
    }

    public List<DrinkResponse> searchResponses(
            String category,
            Long locationId,
            String q,
            Integer maxCalories,
            Double maxSugar,
            Double maxCaffeine,
            Double minPrice,
            Double maxPrice,
            String excludeAllergens,
            Boolean includeUnknownAllergens,
            Double lat,
            Double lng,
            Double maxDistance) {
        return findResponses(DrinkFilterOptions.builder()
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

    public DrinkResponse getResponseById(Long id) {
        return drinkRepository.findById(id)
                .map(DrinkResponse::new)
                .orElseThrow(() -> new NotFoundException("Drink not found: " + id));
    }

    public List<Drink> search(
            String category,
            Long locationId,
            String q,
            Integer maxCalories,
            Double maxSugar,
            Double maxCaffeine,
            Double minPrice,
            Double maxPrice,
            String excludeAllergens,
            Boolean includeUnknownAllergens) {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }

        List<String> names = parseCsvLower(excludeAllergens);
        if (!names.isEmpty()) {
            List<String> unknown = new ArrayList<>();
            for (String name : names) {
                if (allergenRepository.findByNameIgnoreCase(name).isEmpty()) {
                    unknown.add(name);
                }
            }
            if (!unknown.isEmpty()) {
                throw new IllegalArgumentException("Unknown allergen: " + String.join(", ", unknown));
            }
        }

        boolean includeUnknown = Boolean.TRUE.equals(includeUnknownAllergens);

        return drinkRepository.search(
                blankToNull(category),
                locationId,
                blankToNull(q),
                maxCalories,
                maxSugar,
                maxCaffeine,
                minPrice,
                maxPrice,
                !names.isEmpty(),
                names.isEmpty() ? List.of("__none__") : names,
                includeUnknown);
    }

    public static void validateDistanceParams(Double lat, Double lng, Double maxDistance) {
        boolean any = lat != null || lng != null || maxDistance != null;
        boolean all = lat != null && lng != null && maxDistance != null;
        if (any && !all) {
            throw new IllegalArgumentException("lat, lng, and maxDistance must all be provided together");
        }
        if (!all) {
            return;
        }
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException("lat must be between -90 and 90");
        }
        if (lng < -180 || lng > 180) {
            throw new IllegalArgumentException("lng must be between -180 and 180");
        }
        if (maxDistance < 0) {
            throw new IllegalArgumentException("maxDistance must be non-negative");
        }
    }

    public static List<Drink> applyDistanceFilter(
            List<Drink> drinks,
            Double lat,
            Double lng,
            Double maxDistance) {
        if (lat == null || lng == null || maxDistance == null) {
            return drinks;
        }
        List<Drink> filtered = new ArrayList<>();
        for (Drink drink : drinks) {
            Location location = drink.getLocation();
            if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
                continue;
            }
            double miles = DistanceUtils.miles(
                    lat,
                    lng,
                    location.getLatitude(),
                    location.getLongitude());
            if (miles <= maxDistance) {
                filtered.add(drink);
            }
        }
        return filtered;
    }

    private static List<String> parseCsvLower(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .map(String::toLowerCase)
                .distinct()
                .toList();
    }

    public Optional<Drink> findById(Long id) {
        return drinkRepository.findById(id);
    }

    public NutritionFacts getNutritionFactsForSize(Long drinkSizeId) {
        if (!drinkSizeRepository.existsById(drinkSizeId)) {
            throw new NotFoundException("Drink size not found: " + drinkSizeId);
        }
        return nutritionFactsRepository.findByDrinkSize_Id(drinkSizeId)
                .orElseThrow(() -> new NotFoundException("Nutrition facts not found for drink size " + drinkSizeId));
    }

    public Drink create(String name, String category, Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException("Location not found: " + locationId));
        Drink drink = new Drink();
        drink.setName(name);
        drink.setCategory(category);
        drink.setLocation(location);
        drink.setAllergensKnown(false);
        return drinkRepository.save(drink);
    }

    public DrinkResponse createResponse(String name, String category, Long locationId) {
        return new DrinkResponse(create(name, category, locationId));
    }

    public NutritionFacts addNutritionFacts(CreateNutritionFactsRequest request) {
        DrinkSize size = drinkSizeRepository.findById(request.getDrinkSizeId())
                .orElseThrow(() -> new NotFoundException("Drink size not found: " + request.getDrinkSizeId()));
        if (nutritionFactsRepository.findByDrinkSize(size).isPresent()) {
            throw new ConflictException("Nutrition facts already exist for drink size " + size.getId());
        }
        NutritionFacts facts = new NutritionFacts();
        facts.setCalories(request.getCalories());
        facts.setTotalFatG(request.getTotalFatG());
        facts.setCholesterolMg(request.getCholesterolMg());
        facts.setSodiumMg(request.getSodiumMg());
        facts.setCarbsG(request.getCarbsG());
        facts.setSugarsG(request.getSugarsG());
        facts.setProteinG(request.getProteinG());
        facts.setCaffeineMg(request.getCaffeineMg());
        size.setNutritionFacts(facts);
        return nutritionFactsRepository.save(facts);
    }

    public Optional<Drink> update(Long id, Drink updates) {
        return drinkRepository.findById(id).map(existing -> {
            existing.setName(updates.getName());
            existing.setCategory(updates.getCategory());
            existing.setDescription(updates.getDescription());
            existing.setSourceUrl(updates.getSourceUrl());
            existing.setImageUrl(updates.getImageUrl());
            existing.setAllergensKnown(updates.isAllergensKnown());

            if (updates.getLocation() != null && updates.getLocation().getId() != null) {
                Location location = locationRepository.findById(updates.getLocation().getId())
                        .orElseThrow(() -> new NotFoundException("Location not found: " + updates.getLocation().getId()));
                existing.setLocation(location);
            } else {
                existing.setLocation(null);
            }

            return drinkRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (!drinkRepository.existsById(id)) {
            return false;
        }
        drinkRepository.deleteById(id);
        return true;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
