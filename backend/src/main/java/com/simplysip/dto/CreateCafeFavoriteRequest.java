package com.simplysip.dto;

import jakarta.validation.constraints.NotNull;

public record CreateCafeFavoriteRequest(@NotNull Long locationId) {
}
