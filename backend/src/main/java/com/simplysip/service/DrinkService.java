package com.simplysip.service;

import com.simplysip.dto.CreateNutritionFactsRequest;
import com.simplysip.dto.DrinkResponse;
import com.simplysip.exception.ConflictException;
import com.simplysip.exception.NotFoundException;
import com.simplysip.model.Drink;
import com.simplysip.model.Location;
import com.simplysip.model.NutritionFacts;
import com.simplysip.repository.AllergenRepository;
import com.simplysip.repository.DrinkRepository;
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
    private final AllergenRepository allergenRepository;

    public DrinkService(
            DrinkRepository drinkRepository,
            LocationRepository locationRepository,
            NutritionFactsRepository nutritionFactsRepository,
            AllergenRepository allergenRepository) {
        this.drinkRepository = drinkRepository;
        this.locationRepository = locationRepository;
        this.nutritionFactsRepository = nutritionFactsRepository;
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
            String tag,
            String excludeAllergens,
            String milkTypes) {
        return search(
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
                milkTypes).stream().map(DrinkResponse::new).toList();
    }

    public DrinkResponse getResponseById(Long id) {
        return drinkRepository.findById(id)
                .map(DrinkResponse::new)
                .orElseThrow(() -> new NotFoundException("Drink not found: " + id));
    }

    public List<Drink> findAll() {
        return drinkRepository.findAll();
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
            String tag,
            String excludeAllergens,
            String milkTypes) {
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

        List<String> milks = parseCsvTrimmed(milkTypes);
        if (milks.size() > 6) {
            throw new IllegalArgumentException("At most 6 milkTypes are allowed");
        }
        String[] milkSlots = new String[6];
        for (int i = 0; i < milks.size(); i++) {
            milkSlots[i] = milks.get(i).toLowerCase().replace(" ", "");
        }

        return drinkRepository.search(
                blankToNull(category),
                locationId,
                blankToNull(q),
                maxCalories,
                maxSugar,
                maxCaffeine,
                minPrice,
                maxPrice,
                blankToNull(tag),
                !names.isEmpty(),
                names.isEmpty() ? List.of("__none__") : names,
                !milks.isEmpty(),
                milkSlots[0],
                milkSlots[1],
                milkSlots[2],
                milkSlots[3],
                milkSlots[4],
                milkSlots[5]);
    }

    private static List<String> parseCsvLower(String csv) {
        return parseCsvTrimmed(csv).stream().map(String::toLowerCase).distinct().toList();
    }

    private static List<String> parseCsvTrimmed(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .distinct()
                .toList();
    }

    public Optional<Drink> findById(Long id) {
        return drinkRepository.findById(id);
    }

    public NutritionFacts getNutritionFacts(Long drinkId) {
        if (!drinkRepository.existsById(drinkId)) {
            throw new NotFoundException("Drink not found: " + drinkId);
        }
        return nutritionFactsRepository.findByDrink_Id(drinkId)
                .orElseThrow(() -> new NotFoundException("Nutrition facts not found for drink " + drinkId));
    }

    public Drink create(String name, String category, Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException("Location not found: " + locationId));
        Drink drink = new Drink();
        drink.setName(name);
        drink.setCategory(category);
        drink.setLocation(location);
        return drinkRepository.save(drink);
    }

    public DrinkResponse createResponse(String name, String category, Long locationId) {
        return new DrinkResponse(create(name, category, locationId));
    }

    public NutritionFacts addNutritionFacts(Long drinkId, CreateNutritionFactsRequest request) {
        Drink drink = drinkRepository.findById(drinkId)
                .orElseThrow(() -> new NotFoundException("Drink not found: " + drinkId));
        if (nutritionFactsRepository.findByDrink_Id(drinkId).isPresent()) {
            throw new ConflictException("Nutrition facts already exist for drink " + drinkId);
        }
        NutritionFacts facts = new NutritionFacts();
        facts.setCalories(request.getCalories());
        facts.setSugar(request.getSugar());
        facts.setCaffeine(request.getCaffeine());
        facts.setDrink(drink);
        return nutritionFactsRepository.save(facts);
    }

    public Optional<Drink> update(Long id, Drink updates) {
        return drinkRepository.findById(id).map(existing -> {
            existing.setName(updates.getName());
            existing.setCategory(updates.getCategory());
            existing.setTags(updates.getTags());
            existing.setPrice(updates.getPrice());
            existing.setMilkTypes(updates.getMilkTypes());

            if (updates.getLocation() != null && updates.getLocation().getId() != null) {
                Location location = locationRepository.findById(updates.getLocation().getId())
                        .orElseThrow(() -> new NotFoundException("Location not found: " + updates.getLocation().getId()));
                existing.setLocation(location);
            } else {
                existing.setLocation(null);
            }

            if (updates.getNutritionFacts() != null) {
                NutritionFacts incoming = updates.getNutritionFacts();
                if (existing.getNutritionFacts() == null) {
                    NutritionFacts facts = new NutritionFacts();
                    facts.setCalories(incoming.getCalories());
                    facts.setSugar(incoming.getSugar());
                    facts.setCaffeine(incoming.getCaffeine());
                    existing.setNutritionFacts(facts);
                } else {
                    existing.getNutritionFacts().setCalories(incoming.getCalories());
                    existing.getNutritionFacts().setSugar(incoming.getSugar());
                    existing.getNutritionFacts().setCaffeine(incoming.getCaffeine());
                }
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
