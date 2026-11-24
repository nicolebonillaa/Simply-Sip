package com.simplysip.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class HomeRecommendationResponse {

    private final List<ScoredDrinkResponse> drinks;
    private final List<CafeRecommendationResponse> cafes;
    private final boolean personalized;

    public HomeRecommendationResponse(
            List<ScoredDrinkResponse> drinks,
            List<CafeRecommendationResponse> cafes,
            boolean personalized) {
        this.drinks = drinks == null ? List.of() : List.copyOf(drinks);
        this.cafes = cafes == null ? List.of() : List.copyOf(cafes);
        this.personalized = personalized;
    }
}
