package com.simplysip.dto;

import lombok.Getter;

@Getter
public class ScoredDrinkResponse {

    private final DrinkResponse drink;
    private final double score;

    public ScoredDrinkResponse(DrinkResponse drink, double score) {
        this.drink = drink;
        this.score = score;
    }
}
