package com.simplysip.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ScoredDrinkResponse {

    private final DrinkResponse drink;
    private final double score;
    private final Double distanceMiles;
    private final List<String> reasons;

    public ScoredDrinkResponse(DrinkResponse drink, double score) {
        this(drink, score, null, List.of());
    }

    public ScoredDrinkResponse(
            DrinkResponse drink,
            double score,
            Double distanceMiles,
            List<String> reasons) {
        this.drink = drink;
        this.score = score;
        this.distanceMiles = distanceMiles;
        this.reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
