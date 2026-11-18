package com.simplysip.service;

import com.simplysip.dto.DrinkFilterOptions;
import com.simplysip.dto.ScoredDrinkResponse;

import java.util.List;

public interface RecommendationService {

    List<ScoredDrinkResponse> recommend(Long userId, DrinkFilterOptions filters, Double lat, Double lng);
}
