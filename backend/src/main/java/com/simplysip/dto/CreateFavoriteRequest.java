package com.simplysip.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFavoriteRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long drinkId;
}
