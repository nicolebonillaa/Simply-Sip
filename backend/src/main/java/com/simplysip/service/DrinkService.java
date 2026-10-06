package com.simplysip.service;

import com.simplysip.dto.CreateNutritionFactsRequest;
import com.simplysip.exception.ConflictException;
import com.simplysip.exception.NotFoundException;
import com.simplysip.model.Drink;
import com.simplysip.model.Location;
import com.simplysip.model.NutritionFacts;
import com.simplysip.repository.DrinkRepository;
import com.simplysip.repository.LocationRepository;
import com.simplysip.repository.NutritionFactsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DrinkService {

    private final DrinkRepository drinkRepository;
    private final LocationRepository locationRepository;
    private final NutritionFactsRepository nutritionFactsRepository;

    public DrinkService(
            DrinkRepository drinkRepository,
            LocationRepository locationRepository,
            NutritionFactsRepository nutritionFactsRepository) {
        this.drinkRepository = drinkRepository;
        this.locationRepository = locationRepository;
        this.nutritionFactsRepository = nutritionFactsRepository;
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
            String tag) {
        String categoryParam = blankToNull(category);
        String qParam = blankToNull(q);
        String tagParam = blankToNull(tag);
        return drinkRepository.search(categoryParam, locationId, qParam, maxCalories, maxSugar, tagParam);
    }

    public Optional<Drink> findById(Long id) {
        return drinkRepository.findById(id);
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
