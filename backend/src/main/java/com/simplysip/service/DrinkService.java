package com.simplysip.service;

import com.simplysip.dto.CreateNutritionFactsRequest;
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
        return search(
                category,
                locationId,
                q,
                maxCalories,
                maxSugar,
                maxCaffeine,
                minPrice,
                maxPrice,
                excludeAllergens,
                includeUnknownAllergens).stream().map(DrinkResponse::new).toList();
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
