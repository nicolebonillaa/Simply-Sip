package com.simplysip.repository;

import com.simplysip.model.NutritionFacts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionFactsRepository extends JpaRepository<NutritionFacts, Long> {
}
