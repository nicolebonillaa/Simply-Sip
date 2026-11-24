package com.simplysip.service;

import com.simplysip.config.RecommendationProperties;
import com.simplysip.dto.CafeRecommendationResponse;
import com.simplysip.dto.DrinkFilterOptions;
import com.simplysip.dto.DrinkResponse;
import com.simplysip.dto.HomeRecommendationResponse;
import com.simplysip.dto.LocationResponse;
import com.simplysip.dto.ScoredDrinkResponse;
import com.simplysip.exception.NotFoundException;
import com.simplysip.model.Allergen;
import com.simplysip.model.Drink;
import com.simplysip.model.DrinkSize;
import com.simplysip.model.Favorite;
import com.simplysip.model.Location;
import com.simplysip.model.NutritionFacts;
import com.simplysip.model.User;
import com.simplysip.repository.DrinkRepository;
import com.simplysip.repository.FavoriteRepository;
import com.simplysip.repository.UserRepository;
import com.simplysip.service.recommendation.RecommendationScorer;
import com.simplysip.service.recommendation.TasteProfile;
import com.simplysip.util.DistanceUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Primary
@Transactional(readOnly = true)
public class PersonalizedRecommendationService implements RecommendationService {

    private final UserRepository userRepository;
    private final DrinkRepository drinkRepository;
    private final FavoriteRepository favoriteRepository;
    private final RecommendationProperties properties;

    public PersonalizedRecommendationService(
            UserRepository userRepository,
            DrinkRepository drinkRepository,
            FavoriteRepository favoriteRepository,
            RecommendationProperties properties) {
        this.userRepository = userRepository;
        this.drinkRepository = drinkRepository;
        this.favoriteRepository = favoriteRepository;
        this.properties = properties;
    }

    @Override
    public List<ScoredDrinkResponse> recommend(Long userId, DrinkFilterOptions filters, Double lat, Double lng) {
        Double radius = filters != null && filters.getMaxDistance() != null
                ? filters.getMaxDistance()
                : properties.getDefaultRadiusMiles();
        return recommendDrinks(userId, lat, lng, radius, properties.getDefaultLimit());
    }

    @Override
    public List<ScoredDrinkResponse> recommendDrinks(
            Long userId,
            Double lat,
            Double lng,
            Double radiusMiles,
            Integer limit) {
        User user = requireUser(userId);
        double radius = resolveRadius(radiusMiles);
        validateLatLngPair(lat, lng);
        int topN = resolveLimit(limit);

        List<Favorite> favorites = favoriteRepository.findByUser_Id(userId);
        TasteProfile profile = buildProfile(user, favorites);
        Map<Long, Long> popularity = loadFavoriteCounts();
        long maxPop = popularity.values().stream().mapToLong(Long::longValue).max().orElse(0L);

        Set<Long> favoritedDrinkIds = favorites.stream()
                .map(f -> f.getDrink().getId())
                .collect(Collectors.toSet());

        List<ScoredCandidate> scored = scoreCandidates(
                drinkRepository.findAll(),
                profile,
                favoritedDrinkIds,
                popularity,
                maxPop,
                lat,
                lng,
                radius);

        return scored.stream()
                .limit(topN)
                .map(ScoredCandidate::toResponse)
                .toList();
    }

    @Override
    public List<CafeRecommendationResponse> recommendCafes(
            Long userId,
            Double lat,
            Double lng,
            Double radiusMiles,
            Integer limit) {
        User user = requireUser(userId);
        double radius = resolveRadius(radiusMiles);
        validateLatLngPair(lat, lng);
        int topN = resolveLimit(limit);

        List<Favorite> favorites = favoriteRepository.findByUser_Id(userId);
        TasteProfile profile = buildProfile(user, favorites);
        Map<Long, Long> popularity = loadFavoriteCounts();
        long maxPop = popularity.values().stream().mapToLong(Long::longValue).max().orElse(0L);
        Set<Long> favoritedDrinkIds = favorites.stream()
                .map(f -> f.getDrink().getId())
                .collect(Collectors.toSet());

        List<ScoredCandidate> scored = scoreCandidates(
                drinkRepository.findAll(),
                profile,
                favoritedDrinkIds,
                popularity,
                maxPop,
                lat,
                lng,
                radius);

        Map<Long, List<ScoredCandidate>> byLocation = new LinkedHashMap<>();
        for (ScoredCandidate candidate : scored) {
            Location location = candidate.drink().getLocation();
            if (location == null || location.getId() == null) {
                continue;
            }
            byLocation.computeIfAbsent(location.getId(), id -> new ArrayList<>()).add(candidate);
        }

        List<CafeRecommendationResponse> cafes = new ArrayList<>();
        for (List<ScoredCandidate> drinksAtCafe : byLocation.values()) {
            drinksAtCafe.sort(CANDIDATE_ORDER);
            List<ScoredCandidate> top3 = drinksAtCafe.stream().limit(3).toList();
            double cafeScore = top3.stream().mapToDouble(ScoredCandidate::score).sum();
            ScoredCandidate first = top3.get(0);
            Location location = first.drink().getLocation();
            LocationResponse locView = new LocationResponse(location);
            cafes.add(new CafeRecommendationResponse(
                    location.getId(),
                    locView.getName(),
                    locView.getAddress(),
                    locView.getImageUrl(),
                    first.distanceMiles(),
                    cafeScore,
                    top3.stream().map(ScoredCandidate::toResponse).toList()));
        }

        cafes.sort(Comparator
                .comparingDouble(CafeRecommendationResponse::getScore).reversed()
                .thenComparing(c -> c.getDistanceMiles() == null ? Double.MAX_VALUE : c.getDistanceMiles())
                .thenComparing(CafeRecommendationResponse::getName, Comparator.nullsLast(String::compareToIgnoreCase)));

        return cafes.stream().limit(topN).toList();
    }

    @Override
    public HomeRecommendationResponse recommendHome(Long userIdOrNull, Double lat, Double lng) {
        validateLatLngPair(lat, lng);
        double radius = properties.getDefaultRadiusMiles();
        int limit = properties.getDefaultLimit();

        if (userIdOrNull == null) {
            TasteProfile cold = TasteProfile.coldStart(Set.of(), null, null, null);
            Map<Long, Long> popularity = loadFavoriteCounts();
            long maxPop = popularity.values().stream().mapToLong(Long::longValue).max().orElse(0L);
            List<ScoredCandidate> scored = scoreCandidates(
                    drinkRepository.findAll(),
                    cold,
                    Set.of(),
                    popularity,
                    maxPop,
                    lat,
                    lng,
                    radius);
            List<ScoredDrinkResponse> drinks = scored.stream().limit(limit).map(ScoredCandidate::toResponse).toList();
            List<CafeRecommendationResponse> cafes = buildCafesFromScored(scored, limit);
            return new HomeRecommendationResponse(drinks, cafes, false);
        }

        List<ScoredDrinkResponse> drinks = recommendDrinks(userIdOrNull, lat, lng, radius, limit);
        List<CafeRecommendationResponse> cafes = recommendCafes(userIdOrNull, lat, lng, radius, limit);
        User user = requireUser(userIdOrNull);
        boolean personalized = !favoriteRepository.findByUser_Id(user.getId()).isEmpty();
        return new HomeRecommendationResponse(drinks, cafes, personalized);
    }

    private List<CafeRecommendationResponse> buildCafesFromScored(List<ScoredCandidate> scored, int limit) {
        Map<Long, List<ScoredCandidate>> byLocation = new LinkedHashMap<>();
        for (ScoredCandidate candidate : scored) {
            Location location = candidate.drink().getLocation();
            if (location == null || location.getId() == null) {
                continue;
            }
            byLocation.computeIfAbsent(location.getId(), id -> new ArrayList<>()).add(candidate);
        }
        List<CafeRecommendationResponse> cafes = new ArrayList<>();
        for (List<ScoredCandidate> drinksAtCafe : byLocation.values()) {
            drinksAtCafe.sort(CANDIDATE_ORDER);
            List<ScoredCandidate> top3 = drinksAtCafe.stream().limit(3).toList();
            double cafeScore = top3.stream().mapToDouble(ScoredCandidate::score).sum();
            ScoredCandidate first = top3.get(0);
            Location location = first.drink().getLocation();
            LocationResponse locView = new LocationResponse(location);
            cafes.add(new CafeRecommendationResponse(
                    location.getId(),
                    locView.getName(),
                    locView.getAddress(),
                    locView.getImageUrl(),
                    first.distanceMiles(),
                    cafeScore,
                    top3.stream().map(ScoredCandidate::toResponse).toList()));
        }
        cafes.sort(Comparator
                .comparingDouble(CafeRecommendationResponse::getScore).reversed()
                .thenComparing(c -> c.getDistanceMiles() == null ? Double.MAX_VALUE : c.getDistanceMiles())
                .thenComparing(CafeRecommendationResponse::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
        return cafes.stream().limit(limit).toList();
    }

    private List<ScoredCandidate> scoreCandidates(
            List<Drink> allDrinks,
            TasteProfile profile,
            Set<Long> favoritedDrinkIds,
            Map<Long, Long> popularity,
            long maxPop,
            Double lat,
            Double lng,
            double radiusMiles) {
        RecommendationScorer.Weights weights = new RecommendationScorer.Weights(
                properties.getWeightCategory(),
                properties.getWeightNutrition(),
                properties.getWeightPopularity(),
                properties.getWeightDistance());

        List<ScoredCandidate> results = new ArrayList<>();
        for (Drink drink : allDrinks) {
            if (favoritedDrinkIds.contains(drink.getId())) {
                continue;
            }
            if (!passesRestrictions(drink, profile)) {
                continue;
            }

            Location location = drink.getLocation();
            Double distanceMiles = null;
            if (lat != null && lng != null) {
                if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
                    continue;
                }
                distanceMiles = DistanceUtils.miles(
                        lat, lng, location.getLatitude(), location.getLongitude());
                if (distanceMiles > radiusMiles) {
                    continue;
                }
            }

            NutritionSnapshot nutrition = representativeNutrition(drink);
            String cafeName = location == null ? null : location.getCafeName();
            RecommendationScorer.CandidateFeatures features = new RecommendationScorer.CandidateFeatures(
                    drink.getName(),
                    drink.getCategory(),
                    cafeName,
                    nutrition.calories(),
                    nutrition.sugarG(),
                    nutrition.caffeineMg(),
                    popularity.getOrDefault(drink.getId(), 0L),
                    maxPop,
                    distanceMiles,
                    radiusMiles);
            RecommendationScorer.ScoreResult scored = RecommendationScorer.score(features, profile, weights);
            results.add(new ScoredCandidate(drink, scored.score(), distanceMiles, scored.reasons()));
        }

        results.sort(CANDIDATE_ORDER);
        return results;
    }

    public static boolean passesRestrictions(Drink drink, TasteProfile profile) {
        Set<String> restricted = profile.getAllergenRestrictions();
        if (!restricted.isEmpty()) {
            if (!drink.isAllergensKnown()) {
                return false;
            }
            for (Allergen allergen : drink.getAllergens()) {
                if (restricted.contains(allergen.getName().toLowerCase(Locale.ROOT))) {
                    return false;
                }
            }
        }

        NutritionSnapshot nutrition = representativeNutrition(drink);
        if (profile.getMaxCalories() != null
                && nutrition.calories() != null
                && nutrition.calories() > profile.getMaxCalories()) {
            return false;
        }
        if (profile.getMaxSugarG() != null
                && nutrition.sugarG() != null
                && nutrition.sugarG() > profile.getMaxSugarG()) {
            return false;
        }
        if (profile.getMaxCaffeineMg() != null
                && nutrition.caffeineMg() != null
                && nutrition.caffeineMg() > profile.getMaxCaffeineMg()) {
            return false;
        }
        return true;
    }

    public static TasteProfile buildProfile(User user, List<Favorite> favorites) {
        Set<String> allergens = user.getAllergenRestrictions() == null
                ? Set.of()
                : user.getAllergenRestrictions().stream()
                        .map(a -> a.getName().toLowerCase(Locale.ROOT))
                        .collect(Collectors.toSet());

        if (favorites == null || favorites.isEmpty()) {
            return TasteProfile.coldStart(
                    allergens,
                    user.getMaxCalories(),
                    user.getMaxSugarG(),
                    user.getMaxCaffeineMg());
        }

        Map<String, Integer> categoryCounts = new HashMap<>();
        Map<String, String> categoryExample = new LinkedHashMap<>();
        Map<String, Integer> cafeCounts = new HashMap<>();
        List<Double> calories = new ArrayList<>();
        List<Double> sugars = new ArrayList<>();
        List<Double> caffeines = new ArrayList<>();

        for (Favorite favorite : favorites) {
            Drink drink = favorite.getDrink();
            if (drink == null) {
                continue;
            }
            String categoryKey = RecommendationScorer.normalizeKey(drink.getCategory());
            if (categoryKey != null) {
                categoryCounts.merge(categoryKey, 1, Integer::sum);
                categoryExample.putIfAbsent(categoryKey, drink.getName());
            }
            if (drink.getLocation() != null) {
                String cafeKey = RecommendationScorer.normalizeKey(drink.getLocation().getCafeName());
                if (cafeKey != null) {
                    cafeCounts.merge(cafeKey, 1, Integer::sum);
                }
            }
            NutritionSnapshot nutrition = representativeNutrition(drink);
            if (nutrition.calories() != null) {
                calories.add(nutrition.calories().doubleValue());
            }
            if (nutrition.sugarG() != null) {
                sugars.add(nutrition.sugarG());
            }
            if (nutrition.caffeineMg() != null) {
                caffeines.add(nutrition.caffeineMg());
            }
        }

        return new TasteProfile(
                normalizeCounts(categoryCounts),
                categoryExample,
                normalizeCounts(cafeCounts),
                average(calories),
                average(sugars),
                average(caffeines),
                allergens,
                user.getMaxCalories(),
                user.getMaxSugarG(),
                user.getMaxCaffeineMg(),
                true);
    }

    private static Map<String, Double> normalizeCounts(Map<String, Integer> counts) {
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) {
            return Map.of();
        }
        Map<String, Double> weights = new LinkedHashMap<>();
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> weights.put(e.getKey(), e.getValue() / (double) total));
        return weights;
    }

    private static Double average(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        double sum = 0;
        for (Double v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    /** Prefer the lowest-calorie size with facts as the representative nutrition. */
    static NutritionSnapshot representativeNutrition(Drink drink) {
        if (drink.getSizes() == null || drink.getSizes().isEmpty()) {
            return NutritionSnapshot.empty();
        }
        DrinkSize best = null;
        Integer bestCalories = null;
        for (DrinkSize size : drink.getSizes()) {
            NutritionFacts facts = size.getNutritionFacts();
            if (facts == null) {
                continue;
            }
            Integer calories = facts.getCalories();
            if (best == null) {
                best = size;
                bestCalories = calories;
                continue;
            }
            if (calories != null && (bestCalories == null || calories < bestCalories)) {
                best = size;
                bestCalories = calories;
            }
        }
        if (best == null || best.getNutritionFacts() == null) {
            return NutritionSnapshot.empty();
        }
        NutritionFacts facts = best.getNutritionFacts();
        return new NutritionSnapshot(
                facts.getCalories() == null ? null : facts.getCalories().doubleValue(),
                facts.getSugarsG(),
                facts.getCaffeineMg());
    }

    private Map<Long, Long> loadFavoriteCounts() {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : favoriteRepository.countGroupedByDrinkId()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private double resolveRadius(Double radiusMiles) {
        double radius = radiusMiles == null ? properties.getDefaultRadiusMiles() : radiusMiles;
        if (radius <= 0) {
            throw new IllegalArgumentException("radiusMiles must be greater than 0");
        }
        return radius;
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return properties.getDefaultLimit();
        }
        return limit;
    }

    private static void validateLatLngPair(Double lat, Double lng) {
        if (lat == null && lng == null) {
            return;
        }
        if (lat == null || lng == null) {
            throw new IllegalArgumentException("lat and lng must both be provided together");
        }
        // Reuse DistanceUtils validation against a dummy second point.
        DistanceUtils.miles(lat, lng, lat, lng);
    }

    private static final Comparator<ScoredCandidate> CANDIDATE_ORDER = Comparator
            .comparingDouble(ScoredCandidate::score).reversed()
            .thenComparing(c -> c.distanceMiles() == null ? Double.MAX_VALUE : c.distanceMiles())
            .thenComparing(c -> c.drink().getName(), Comparator.nullsLast(String::compareToIgnoreCase));

    record NutritionSnapshot(Double calories, Double sugarG, Double caffeineMg) {
        static NutritionSnapshot empty() {
            return new NutritionSnapshot(null, null, null);
        }
    }

    record ScoredCandidate(Drink drink, double score, Double distanceMiles, List<String> reasons) {
        ScoredDrinkResponse toResponse() {
            return new ScoredDrinkResponse(new DrinkResponse(drink), score, distanceMiles, reasons);
        }
    }
}
