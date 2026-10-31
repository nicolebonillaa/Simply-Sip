package com.simplysip.dto;

import com.simplysip.model.Allergen;
import com.simplysip.model.Drink;
import com.simplysip.model.Location;
import com.simplysip.model.NutritionFacts;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
public class DrinkResponse {

    private final Long id;
    private final String name;
    private final String category;
    private final String tags;
    private final Double price;
    private final List<String> milkTypes;
    private final List<String> allergens;
    private final LocationView location;
    private final NutritionFactsView nutritionFacts;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public DrinkResponse(Drink drink) {
        this.id = drink.getId();
        this.name = drink.getName();
        this.category = drink.getCategory();
        this.tags = drink.getTags();
        this.price = drink.getPrice();
        this.milkTypes = splitCsv(drink.getMilkTypes());
        this.allergens = drink.getAllergens() == null
                ? List.of()
                : drink.getAllergens().stream().map(Allergen::getName).sorted().toList();
        this.location = drink.getLocation() == null ? null : new LocationView(drink.getLocation());
        this.nutritionFacts = drink.getNutritionFacts() == null
                ? null
                : new NutritionFactsView(drink.getNutritionFacts());
        this.createdAt = drink.getCreatedAt();
        this.updatedAt = drink.getUpdatedAt();
    }

    private static List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .toList();
    }

    @Getter
    public static class LocationView {
        private final Long id;
        private final String name;
        private final String address;
        private final Double latitude;
        private final Double longitude;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;

        public LocationView(Location location) {
            this.id = location.getId();
            this.name = location.getName();
            this.address = location.getAddress();
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            this.createdAt = location.getCreatedAt();
            this.updatedAt = location.getUpdatedAt();
        }
    }

    @Getter
    public static class NutritionFactsView {
        private final Long id;
        private final Integer calories;
        private final Double sugar;
        private final Double caffeine;

        public NutritionFactsView(NutritionFacts facts) {
            this.id = facts.getId();
            this.calories = facts.getCalories();
            this.sugar = facts.getSugar();
            this.caffeine = facts.getCaffeine();
        }
    }
}
