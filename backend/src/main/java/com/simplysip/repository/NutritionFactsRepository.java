package com.simplysip.repository;

import com.simplysip.model.NutritionFacts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutritionFactsRepository extends JpaRepository<NutritionFacts, Long> {
    Optional<NutritionFacts> findByDrink_Id(Long drinkId);
}
