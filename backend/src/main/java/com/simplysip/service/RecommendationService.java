package com.simplysip.service;

import com.simplysip.dto.CafeRecommendationResponse;
import com.simplysip.dto.DrinkFilterOptions;
import com.simplysip.dto.HomeRecommendationResponse;
import com.simplysip.dto.ScoredDrinkResponse;

import java.util.List;

public interface RecommendationService {

    /** Legacy endpoint: personalized when possible, still requires userId. */
    List<ScoredDrinkResponse> recommend(Long userId, DrinkFilterOptions filters, Double lat, Double lng);

    List<ScoredDrinkResponse> recommendDrinks(
            Long userId,
            Double lat,
            Double lng,
            Double radiusMiles,
            Integer limit);

    List<CafeRecommendationResponse> recommendCafes(
            Long userId,
            Double lat,
            Double lng,
            Double radiusMiles,
            Integer limit);

    HomeRecommendationResponse recommendHome(Long userIdOrNull, Double lat, Double lng);
}
