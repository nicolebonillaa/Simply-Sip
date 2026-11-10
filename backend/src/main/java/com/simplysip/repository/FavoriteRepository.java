package com.simplysip.repository;

import com.simplysip.model.Favorite;
import com.simplysip.model.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    List<Favorite> findByUser_Id(Long userId);

    @Query("SELECT f.drink.id, COUNT(f) FROM Favorite f GROUP BY f.drink.id")
    List<Object[]> countGroupedByDrinkId();
}
