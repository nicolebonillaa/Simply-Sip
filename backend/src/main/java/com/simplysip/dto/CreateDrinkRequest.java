package com.simplysip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDrinkRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String category;

    @NotNull
    private Long locationId;
}
