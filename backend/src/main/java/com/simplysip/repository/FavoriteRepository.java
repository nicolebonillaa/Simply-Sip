package com.simplysip.repository;

import com.simplysip.model.Favorite;
import com.simplysip.model.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    List<Favorite> findByUser_Id(Long userId);
}
