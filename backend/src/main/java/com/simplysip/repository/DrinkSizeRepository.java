package com.simplysip.repository;

import com.simplysip.model.Drink;
import com.simplysip.model.DrinkSize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DrinkSizeRepository extends JpaRepository<DrinkSize, Long> {
    Optional<DrinkSize> findByDrinkAndSizeNameIgnoreCase(Drink drink, String sizeName);
}
