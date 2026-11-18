package com.simplysip.repository;

import com.simplysip.model.CafeFavorite;
import com.simplysip.model.CafeFavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CafeFavoriteRepository extends JpaRepository<CafeFavorite, CafeFavoriteId> {
    List<CafeFavorite> findByUser_Id(Long userId);
}
