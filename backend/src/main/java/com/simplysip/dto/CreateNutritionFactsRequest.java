package com.simplysip.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNutritionFactsRequest {

    @NotNull
    private Long drinkSizeId;

    private Integer calories;
    private Double totalFatG;
    private Double cholesterolMg;
    private Double sodiumMg;
    private Double carbsG;
    private Double sugarsG;
    private Double proteinG;
    private Double caffeineMg;
}
