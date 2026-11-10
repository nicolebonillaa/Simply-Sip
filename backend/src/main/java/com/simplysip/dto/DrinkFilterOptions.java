package com.simplysip.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DrinkFilterOptions {

    private final String category;
    private final Long locationId;
    private final String q;
    private final Integer maxCalories;
    private final Double maxSugar;
    private final Double maxCaffeine;
    private final Double minPrice;
    private final Double maxPrice;
    private final String excludeAllergens;
    private final Boolean includeUnknownAllergens;
    private final Double lat;
    private final Double lng;
    private final Double maxDistance;
}
