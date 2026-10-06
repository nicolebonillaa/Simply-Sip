package com.simplysip.service;

import com.simplysip.exception.ConflictException;
import com.simplysip.exception.NotFoundException;
import com.simplysip.model.Drink;
import com.simplysip.model.Favorite;
import com.simplysip.model.FavoriteId;
import com.simplysip.model.User;
import com.simplysip.repository.DrinkRepository;
import com.simplysip.repository.FavoriteRepository;
import com.simplysip.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final DrinkRepository drinkRepository;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            UserRepository userRepository,
            DrinkRepository drinkRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.drinkRepository = drinkRepository;
    }

    public List<Favorite> findAll() {
        return favoriteRepository.findAll();
    }

    public List<Favorite> findByUserId(Long userId) {
        return favoriteRepository.findByUser_Id(userId);
    }

    public Optional<Favorite> findById(Long userId, Long drinkId) {
        return favoriteRepository.findById(new FavoriteId(userId, drinkId));
    }

    public Favorite create(Long userId, Long drinkId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Drink drink = drinkRepository.findById(drinkId)
                .orElseThrow(() -> new NotFoundException("Drink not found: " + drinkId));

        FavoriteId id = new FavoriteId(userId, drinkId);
        if (favoriteRepository.existsById(id)) {
            throw new ConflictException("Favorite already exists for user " + userId + " and drink " + drinkId);
        }

        Favorite favorite = new Favorite();
        favorite.setId(id);
        favorite.setUser(user);
        favorite.setDrink(drink);
        return favoriteRepository.save(favorite);
    }

    public Optional<Favorite> update(Long userId, Long drinkId, Favorite ignored) {
        return favoriteRepository.findById(new FavoriteId(userId, drinkId));
    }

    public boolean delete(Long userId, Long drinkId) {
        FavoriteId id = new FavoriteId(userId, drinkId);
        if (!favoriteRepository.existsById(id)) {
            return false;
        }
        favoriteRepository.deleteById(id);
        return true;
    }
}
