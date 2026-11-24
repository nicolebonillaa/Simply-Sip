package com.simplysip.service.recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pure scoring for SCRUM-64. No Spring / DB — unit-testable.
 * Weights: category 0.40, nutrition 0.30, popularity 0.20, distance 0.10 (configurable).
 */
public final class RecommendationScorer {

    public record Weights(
            double category,
            double nutrition,
            double popularity,
            double distance) {
        public static Weights defaults() {
            return new Weights(0.40, 0.30, 0.20, 0.10);
        }
    }

    public record CandidateFeatures(
            String drinkName,
            String category,
            String cafeName,
            Double calories,
            Double sugarG,
            Double caffeineMg,
            long popularityCount,
            long maxPopularityCount,
            Double distanceMiles,
            double radiusMiles) {
    }

    public record ScoreResult(double score, List<String> reasons) {
    }

    private RecommendationScorer() {
    }

    public static ScoreResult score(CandidateFeatures features, TasteProfile profile, Weights weights) {
        if (profile == null || !profile.isPersonalized()) {
            return coldStartScore(features, weights);
        }
        return personalizedScore(features, profile, weights);
    }

    private static ScoreResult coldStartScore(CandidateFeatures features, Weights weights) {
        double popularity = popularityNorm(features.popularityCount(), features.maxPopularityCount());
        double distance = distanceScore(features.distanceMiles(), features.radiusMiles());
        double totalWeight = weights.popularity() + weights.distance();
        double score = totalWeight <= 0
                ? 0
                : (popularity * weights.popularity() + distance * weights.distance()) / totalWeight;

        List<String> reasons = new ArrayList<>();
        if (popularity >= 0.5) {
            reasons.add("Popular nearby");
        } else if (features.popularityCount() > 0) {
            reasons.add("Liked by other users");
        }
        if (features.distanceMiles() != null && features.distanceMiles() <= features.radiusMiles()) {
            reasons.add("Close to you");
        }
        if (reasons.isEmpty()) {
            reasons.add("Nearby pick");
        }
        return new ScoreResult(clamp01(score), List.copyOf(reasons));
    }

    private static ScoreResult personalizedScore(
            CandidateFeatures features,
            TasteProfile profile,
            Weights weights) {
        String categoryKey = normalizeKey(features.category());
        double category = categoryKey == null
                ? 0
                : profile.getCategoryWeights().getOrDefault(categoryKey, 0.0);

        double nutrition = nutritionSimilarity(features, profile);
        double popularity = popularityNorm(features.popularityCount(), features.maxPopularityCount());
        double distance = distanceScore(features.distanceMiles(), features.radiusMiles());

        double score = clamp01(
                category * weights.category()
                        + nutrition * weights.nutrition()
                        + popularity * weights.popularity()
                        + distance * weights.distance());

        List<String> reasons = new ArrayList<>();
        if (category >= 0.2 && categoryKey != null) {
            String example = profile.getCategoryExampleFavorite().get(categoryKey);
            if (example != null && !example.isBlank()) {
                reasons.add("Same category as your favorite " + example);
            } else if (features.category() != null) {
                reasons.add("Matches your taste for " + features.category());
            }
        }
        if (nutrition >= 0.7) {
            reasons.add("Similar nutrition to your favorites");
        }
        String cafeKey = normalizeKey(features.cafeName());
        if (cafeKey != null && profile.getCafeWeights().getOrDefault(cafeKey, 0.0) >= 0.2) {
            reasons.add("From a cafe you like");
        }
        if (popularity >= 0.5) {
            reasons.add("Popular nearby");
        }
        if (features.distanceMiles() != null && features.distanceMiles() <= 1.0) {
            reasons.add("Very close to you");
        } else if (features.distanceMiles() != null && features.distanceMiles() <= features.radiusMiles()) {
            reasons.add("Nearby");
        }
        if (reasons.isEmpty()) {
            reasons.add("Recommended for you");
        }
        return new ScoreResult(score, List.copyOf(reasons));
    }

    static double popularityNorm(long count, long max) {
        if (max <= 0) {
            return 0;
        }
        return clamp01((double) count / (double) max);
    }

    static double distanceScore(Double distanceMiles, double radiusMiles) {
        if (distanceMiles == null || radiusMiles <= 0) {
            return 0.5;
        }
        if (distanceMiles <= 0) {
            return 1.0;
        }
        return clamp01(1.0 - (distanceMiles / radiusMiles));
    }

    static double nutritionSimilarity(CandidateFeatures features, TasteProfile profile) {
        List<Double> sims = new ArrayList<>();
        addMetricSimilarity(sims, features.calories(), profile.getAvgCalories(), 400.0);
        addMetricSimilarity(sims, features.sugarG(), profile.getAvgSugarG(), 40.0);
        addMetricSimilarity(sims, features.caffeineMg(), profile.getAvgCaffeineMg(), 150.0);
        if (sims.isEmpty()) {
            return 0.5;
        }
        double sum = 0;
        for (double s : sims) {
            sum += s;
        }
        return sum / sims.size();
    }

    private static void addMetricSimilarity(
            List<Double> sims,
            Double actual,
            Double target,
            double scale) {
        if (actual == null || target == null || scale <= 0) {
            return;
        }
        double diff = Math.abs(actual - target);
        sims.add(clamp01(1.0 - (diff / scale)));
    }

    public static String normalizeKey(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    static double clamp01(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > 1) {
            return 1;
        }
        return value;
    }
}
