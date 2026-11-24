package com.simplysip.service.recommendation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Aggregated taste signal from a user's favorites (and saved restrictions). */
public final class TasteProfile {

    private final Map<String, Double> categoryWeights;
    private final Map<String, String> categoryExampleFavorite;
    private final Map<String, Double> cafeWeights;
    private final Double avgCalories;
    private final Double avgSugarG;
    private final Double avgCaffeineMg;
    private final Set<String> allergenRestrictions;
    private final Integer maxCalories;
    private final Double maxSugarG;
    private final Double maxCaffeineMg;
    private final boolean personalized;

    public TasteProfile(
            Map<String, Double> categoryWeights,
            Map<String, String> categoryExampleFavorite,
            Map<String, Double> cafeWeights,
            Double avgCalories,
            Double avgSugarG,
            Double avgCaffeineMg,
            Set<String> allergenRestrictions,
            Integer maxCalories,
            Double maxSugarG,
            Double maxCaffeineMg,
            boolean personalized) {
        this.categoryWeights = categoryWeights == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(categoryWeights));
        this.categoryExampleFavorite = categoryExampleFavorite == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(categoryExampleFavorite));
        this.cafeWeights = cafeWeights == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(cafeWeights));
        this.avgCalories = avgCalories;
        this.avgSugarG = avgSugarG;
        this.avgCaffeineMg = avgCaffeineMg;
        this.allergenRestrictions = allergenRestrictions == null
                ? Set.of()
                : Set.copyOf(allergenRestrictions);
        this.maxCalories = maxCalories;
        this.maxSugarG = maxSugarG;
        this.maxCaffeineMg = maxCaffeineMg;
        this.personalized = personalized;
    }

    public static TasteProfile coldStart(
            Set<String> allergenRestrictions,
            Integer maxCalories,
            Double maxSugarG,
            Double maxCaffeineMg) {
        return new TasteProfile(
                Map.of(),
                Map.of(),
                Map.of(),
                null,
                null,
                null,
                allergenRestrictions,
                maxCalories,
                maxSugarG,
                maxCaffeineMg,
                false);
    }

    public Map<String, Double> getCategoryWeights() {
        return categoryWeights;
    }

    public Map<String, String> getCategoryExampleFavorite() {
        return categoryExampleFavorite;
    }

    public Map<String, Double> getCafeWeights() {
        return cafeWeights;
    }

    public Double getAvgCalories() {
        return avgCalories;
    }

    public Double getAvgSugarG() {
        return avgSugarG;
    }

    public Double getAvgCaffeineMg() {
        return avgCaffeineMg;
    }

    public Set<String> getAllergenRestrictions() {
        return allergenRestrictions;
    }

    public Integer getMaxCalories() {
        return maxCalories;
    }

    public Double getMaxSugarG() {
        return maxSugarG;
    }

    public Double getMaxCaffeineMg() {
        return maxCaffeineMg;
    }

    public boolean isPersonalized() {
        return personalized;
    }
}
