package com.simplysip.dto;

import com.simplysip.model.Favorite;
import com.simplysip.model.FavoriteId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FavoriteResponse {

    private final FavoriteId id;
    private final UserResponse user;
    private final DrinkResponse drink;
    private final LocalDateTime dateAdded;

    public FavoriteResponse(Favorite favorite) {
        this.id = favorite.getId();
        this.user = favorite.getUser() == null ? null : new UserResponse(favorite.getUser());
        this.drink = favorite.getDrink() == null ? null : new DrinkResponse(favorite.getDrink());
        this.dateAdded = favorite.getDateAdded();
    }
}
