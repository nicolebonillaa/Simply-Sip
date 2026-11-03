package com.simplysip.dto;

import com.simplysip.model.Allergen;
import com.simplysip.model.Drink;
import com.simplysip.model.DrinkSize;
import com.simplysip.model.Location;
import com.simplysip.model.NutritionFacts;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
public class DrinkResponse {

    private final Long id;
    private final String name;
    private final String category;
    private final String description;
    private final String sourceUrl;
    private final boolean allergensKnown;
    private final List<String> allergens;
    private final LocationView location;
    private final List<DrinkSizeView> sizes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public DrinkResponse(Drink drink) {
        this.id = drink.getId();
        this.name = drink.getName();
        this.category = drink.getCategory();
        this.description = drink.getDescription();
        this.sourceUrl = drink.getSourceUrl();
        this.allergensKnown = drink.isAllergensKnown();
        this.allergens = drink.getAllergens() == null
                ? List.of()
                : drink.getAllergens().stream().map(Allergen::getName).sorted().toList();
        this.location = drink.getLocation() == null ? null : new LocationView(drink.getLocation());
        this.sizes = drink.getSizes() == null
                ? List.of()
                : drink.getSizes().stream()
                        .sorted(Comparator.comparing(DrinkSize::getSizeName, Comparator.nullsLast(String::compareToIgnoreCase)))
                        .map(DrinkSizeView::new)
                        .toList();
        this.createdAt = drink.getCreatedAt();
        this.updatedAt = drink.getUpdatedAt();
    }

    @Getter
    public static class LocationView {
        private final Long id;
        private final String cafeName;
        private final String locationName;
        private final String streetAddress;
        private final String city;
        private final String state;
        private final String zip;
        private final Double latitude;
        private final Double longitude;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;

        public LocationView(Location location) {
            this.id = location.getId();
            this.cafeName = location.getCafeName();
            this.locationName = location.getLocationName();
            this.streetAddress = location.getStreetAddress();
            this.city = location.getCity();
            this.state = location.getState();
            this.zip = location.getZip();
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            this.createdAt = location.getCreatedAt();
            this.updatedAt = location.getUpdatedAt();
        }
    }

    @Getter
    public static class DrinkSizeView {
        private final Long id;
        private final String sizeName;
        private final Double ounces;
        private final BigDecimal price;
        private final NutritionFactsView nutritionFacts;

        public DrinkSizeView(DrinkSize size) {
            this.id = size.getId();
            this.sizeName = size.getSizeName();
            this.ounces = size.getOunces();
            this.price = size.getPrice();
            this.nutritionFacts = size.getNutritionFacts() == null
                    ? null
                    : new NutritionFactsView(size.getNutritionFacts());
        }
    }

    @Getter
    public static class NutritionFactsView {
        private final Long id;
        private final Integer calories;
        private final Double totalFatG;
        private final Double cholesterolMg;
        private final Double sodiumMg;
        private final Double carbsG;
        private final Double sugarsG;
        private final Double proteinG;
        private final Double caffeineMg;

        public NutritionFactsView(NutritionFacts facts) {
            this.id = facts.getId();
            this.calories = facts.getCalories();
            this.totalFatG = facts.getTotalFatG();
            this.cholesterolMg = facts.getCholesterolMg();
            this.sodiumMg = facts.getSodiumMg();
            this.carbsG = facts.getCarbsG();
            this.sugarsG = facts.getSugarsG();
            this.proteinG = facts.getProteinG();
            this.caffeineMg = facts.getCaffeineMg();
        }
    }
}
