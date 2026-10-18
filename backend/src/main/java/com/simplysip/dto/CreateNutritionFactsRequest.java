package com.simplysip.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNutritionFactsRequest {

    @NotNull
    private Integer calories;

    @NotNull
    private Double sugar;

    @NotNull
    private Double caffeine;
}
