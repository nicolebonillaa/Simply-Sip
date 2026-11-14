package com.simplysip.repository;

import com.simplysip.model.DrinkSize;
import com.simplysip.model.NutritionFacts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutritionFactsRepository extends JpaRepository<NutritionFacts, Long> {
    Optional<NutritionFacts> findByDrinkSize(DrinkSize drinkSize);

    Optional<NutritionFacts> findByDrinkSize_Id(Long drinkSizeId);
}
