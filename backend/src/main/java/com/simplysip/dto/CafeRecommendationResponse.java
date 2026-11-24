package com.simplysip.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CafeRecommendationResponse {

    private final Long locationId;
    private final String name;
    private final String address;
    private final String imageUrl;
    private final Double distanceMiles;
    private final double score;
    private final List<ScoredDrinkResponse> topDrinks;

    public CafeRecommendationResponse(
            Long locationId,
            String name,
            String address,
            String imageUrl,
            Double distanceMiles,
            double score,
            List<ScoredDrinkResponse> topDrinks) {
        this.locationId = locationId;
        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
        this.distanceMiles = distanceMiles;
        this.score = score;
        this.topDrinks = topDrinks == null ? List.of() : List.copyOf(topDrinks);
    }
}
