package com.simplysip.dto;

import com.simplysip.model.CafeFavorite;
import com.simplysip.model.Location;

import java.time.LocalDateTime;

public record CafeFavoriteResponse(Long locationId, Location location, LocalDateTime dateAdded) {
    public CafeFavoriteResponse(CafeFavorite favorite) {
        this(favorite.getLocation().getId(), favorite.getLocation(), favorite.getDateAdded());
    }
}
