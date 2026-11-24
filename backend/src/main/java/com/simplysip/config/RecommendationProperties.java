package com.simplysip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.recommendations")
public class RecommendationProperties {

    private double defaultRadiusMiles = 2.0;
    private int defaultLimit = 10;
    private double weightCategory = 0.40;
    private double weightNutrition = 0.30;
    private double weightPopularity = 0.20;
    private double weightDistance = 0.10;

    public double getDefaultRadiusMiles() {
        return defaultRadiusMiles;
    }

    public void setDefaultRadiusMiles(double defaultRadiusMiles) {
        this.defaultRadiusMiles = defaultRadiusMiles;
    }

    public int getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public double getWeightCategory() {
        return weightCategory;
    }

    public void setWeightCategory(double weightCategory) {
        this.weightCategory = weightCategory;
    }

    public double getWeightNutrition() {
        return weightNutrition;
    }

    public void setWeightNutrition(double weightNutrition) {
        this.weightNutrition = weightNutrition;
    }

    public double getWeightPopularity() {
        return weightPopularity;
    }

    public void setWeightPopularity(double weightPopularity) {
        this.weightPopularity = weightPopularity;
    }

    public double getWeightDistance() {
        return weightDistance;
    }

    public void setWeightDistance(double weightDistance) {
        this.weightDistance = weightDistance;
    }
}
