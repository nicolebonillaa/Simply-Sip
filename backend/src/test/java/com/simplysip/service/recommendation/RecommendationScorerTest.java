package com.simplysip.service.recommendation;

import com.simplysip.model.Allergen;
import com.simplysip.model.Drink;
import com.simplysip.model.DrinkSize;
import com.simplysip.model.Favorite;
import com.simplysip.model.Location;
import com.simplysip.model.NutritionFacts;
import com.simplysip.model.User;
import com.simplysip.service.PersonalizedRecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationScorerTest {

    private static final RecommendationScorer.Weights WEIGHTS = RecommendationScorer.Weights.defaults();

    @Test
    @DisplayName("No favorites uses cold-start popularity and distance ranking signals")
    void noFavoritesColdStart() {
        TasteProfile cold = TasteProfile.coldStart(Set.of(), null, null, null);
        RecommendationScorer.CandidateFeatures popularClose = features(
                "Latte", "Coffee", "Cafe A", 120, 10, 100, 10, 10, 0.5, 2.0);
        RecommendationScorer.CandidateFeatures obscureFar = features(
                "Slush", "Slush", "Cafe B", 300, 40, 0, 0, 10, 1.9, 2.0);

        RecommendationScorer.ScoreResult popular = RecommendationScorer.score(popularClose, cold, WEIGHTS);
        RecommendationScorer.ScoreResult obscure = RecommendationScorer.score(obscureFar, cold, WEIGHTS);

        assertTrue(popular.score() > obscure.score());
        assertTrue(popular.reasons().stream().anyMatch(r -> r.toLowerCase().contains("popular")));
        assertFalse(cold.isPersonalized());
    }

    @Test
    @DisplayName("One favorite boosts same-category drinks and explains the reason")
    void oneFavoriteCategoryMatch() {
        TasteProfile profile = new TasteProfile(
                Map.of("tea", 1.0),
                Map.of("tea", "Thai Tea"),
                Map.of("boba time", 1.0),
                200.0,
                25.0,
                40.0,
                Set.of(),
                null,
                null,
                null,
                true);

        RecommendationScorer.CandidateFeatures tea = features(
                "Jasmine Tea", "Tea", "Boba Time", 210, 24, 35, 2, 5, 0.8, 2.0);
        RecommendationScorer.CandidateFeatures coffee = features(
                "Espresso", "Coffee", "Other", 5, 0, 80, 2, 5, 0.8, 2.0);

        RecommendationScorer.ScoreResult teaScore = RecommendationScorer.score(tea, profile, WEIGHTS);
        RecommendationScorer.ScoreResult coffeeScore = RecommendationScorer.score(coffee, profile, WEIGHTS);

        assertTrue(teaScore.score() > coffeeScore.score());
        assertTrue(teaScore.reasons().stream().anyMatch(r -> r.contains("Thai Tea")));
    }

    @Test
    @DisplayName("Allergen restrictions exclude matching drinks and unknown allergen info")
    void allergenExclusionIncludingUnknown() {
        TasteProfile profile = TasteProfile.coldStart(Set.of("soy"), 500, null, null);

        Drink withSoy = drink("Matcha", "Tea", true, Set.of(allergen("Soy")), 200, 20, 40);
        Drink unknown = drink("Mystery", "Tea", false, Set.of(), 150, 10, 20);
        Drink safe = drink("Black Tea", "Tea", true, Set.of(), 100, 5, 30);

        assertFalse(PersonalizedRecommendationService.passesRestrictions(withSoy, profile));
        assertFalse(PersonalizedRecommendationService.passesRestrictions(unknown, profile));
        assertTrue(PersonalizedRecommendationService.passesRestrictions(safe, profile));
    }

    @Test
    @DisplayName("Outside the radius scores as farther / candidates use distance signal")
    void outsideRadiusDistanceScoreLower() {
        TasteProfile cold = TasteProfile.coldStart(Set.of(), null, null, null);
        RecommendationScorer.CandidateFeatures near = features(
                "Near", "Coffee", "A", 100, 10, 50, 5, 5, 0.2, 2.0);
        RecommendationScorer.CandidateFeatures far = features(
                "Far", "Coffee", "B", 100, 10, 50, 5, 5, 5.0, 2.0);

        double nearScore = RecommendationScorer.score(near, cold, WEIGHTS).score();
        double farScore = RecommendationScorer.score(far, cold, WEIGHTS).score();
        assertTrue(nearScore > farScore);
        assertEquals(0.0, RecommendationScorer.distanceScore(2.0, 2.0), 1e-9);
        assertTrue(RecommendationScorer.distanceScore(5.0, 2.0) <= 0.0);
    }

    @Test
    @DisplayName("Missing coordinates use neutral distance score")
    void locationWithNoCoordinatesUsesNeutralDistance() {
        assertEquals(0.5, RecommendationScorer.distanceScore(null, 2.0), 1e-9);
    }

    @Test
    @DisplayName("Ties keep deterministic equal scores for identical features")
    void tiesProduceEqualScores() {
        TasteProfile cold = TasteProfile.coldStart(Set.of(), null, null, null);
        RecommendationScorer.CandidateFeatures a = features(
                "A", "Coffee", "Cafe", 100, 10, 50, 3, 3, 1.0, 2.0);
        RecommendationScorer.CandidateFeatures b = features(
                "B", "Coffee", "Cafe", 100, 10, 50, 3, 3, 1.0, 2.0);
        assertEquals(
                RecommendationScorer.score(a, cold, WEIGHTS).score(),
                RecommendationScorer.score(b, cold, WEIGHTS).score(),
                1e-9);
    }

    @Test
    @DisplayName("Empty favorites profile is cold start")
    void emptyFavoritesProfile() {
        User user = new User();
        user.setMaxCalories(400);
        TasteProfile profile = PersonalizedRecommendationService.buildProfile(user, List.of());
        assertFalse(profile.isPersonalized());
        assertEquals(400, profile.getMaxCalories());
    }

    @Test
    @DisplayName("Calorie limit excludes drinks above the user's max")
    void calorieLimitExclusion() {
        TasteProfile profile = TasteProfile.coldStart(Set.of(), 150, null, null);
        Drink light = drink("Light", "Coffee", true, Set.of(), 120, 5, 80);
        Drink heavy = drink("Heavy", "Coffee", true, Set.of(), 300, 30, 100);
        assertTrue(PersonalizedRecommendationService.passesRestrictions(light, profile));
        assertFalse(PersonalizedRecommendationService.passesRestrictions(heavy, profile));
    }

    private static RecommendationScorer.CandidateFeatures features(
            String name,
            String category,
            String cafe,
            double calories,
            double sugar,
            double caffeine,
            long pop,
            long maxPop,
            Double distance,
            double radius) {
        return new RecommendationScorer.CandidateFeatures(
                name, category, cafe, calories, sugar, caffeine, pop, maxPop, distance, radius);
    }

    private static Allergen allergen(String name) {
        Allergen allergen = new Allergen();
        allergen.setName(name);
        return allergen;
    }

    private static Drink drink(
            String name,
            String category,
            boolean allergensKnown,
            Set<Allergen> allergens,
            int calories,
            double sugar,
            double caffeine) {
        Drink drink = new Drink();
        drink.setName(name);
        drink.setCategory(category);
        drink.setAllergensKnown(allergensKnown);
        drink.setAllergens(allergens);
        Location location = new Location();
        location.setCafeName("Test Cafe");
        location.setLatitude(34.24);
        location.setLongitude(-118.53);
        drink.setLocation(location);

        DrinkSize size = new DrinkSize();
        size.setSizeName("Regular");
        NutritionFacts facts = new NutritionFacts();
        facts.setCalories(calories);
        facts.setSugarsG(sugar);
        facts.setCaffeineMg(caffeine);
        size.setNutritionFacts(facts);
        drink.addSize(size);
        return drink;
    }
}
