package com.simplysip.service;

import com.simplysip.dto.CafeFavoriteResponse;
import com.simplysip.exception.ConflictException;
import com.simplysip.exception.NotFoundException;
import com.simplysip.model.CafeFavorite;
import com.simplysip.model.CafeFavoriteId;
import com.simplysip.model.Location;
import com.simplysip.model.User;
import com.simplysip.repository.CafeFavoriteRepository;
import com.simplysip.repository.LocationRepository;
import com.simplysip.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CafeFavoriteService {
    private final CafeFavoriteRepository favorites;
    private final UserRepository users;
    private final LocationRepository locations;

    public CafeFavoriteService(CafeFavoriteRepository favorites, UserRepository users, LocationRepository locations) {
        this.favorites = favorites;
        this.users = users;
        this.locations = locations;
    }

    public List<CafeFavoriteResponse> list(Long userId) {
        return favorites.findByUser_Id(userId).stream().map(CafeFavoriteResponse::new).toList();
    }

    public CafeFavoriteResponse save(Long userId, Long locationId) {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Location location = locations.findById(locationId)
                .orElseThrow(() -> new NotFoundException("Café not found: " + locationId));
        CafeFavoriteId id = new CafeFavoriteId(userId, locationId);
        if (favorites.existsById(id)) throw new ConflictException("Café already saved: " + locationId);
        CafeFavorite favorite = new CafeFavorite();
        favorite.setId(id);
        favorite.setUser(user);
        favorite.setLocation(location);
        return new CafeFavoriteResponse(favorites.save(favorite));
    }

    public void remove(Long userId, Long locationId) {
        CafeFavoriteId id = new CafeFavoriteId(userId, locationId);
        if (!favorites.existsById(id)) throw new NotFoundException("Café favorite not found: " + locationId);
        favorites.deleteById(id);
    }
}
