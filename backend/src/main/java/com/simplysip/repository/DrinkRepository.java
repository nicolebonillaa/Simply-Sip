package com.simplysip.repository;

import com.simplysip.model.Drink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DrinkRepository extends JpaRepository<Drink, Long> {

    @Query("""
            SELECT d FROM Drink d
            LEFT JOIN d.nutritionFacts n
            LEFT JOIN d.location l
            WHERE (:category IS NULL OR LOWER(d.category) = LOWER(:category))
              AND (:locationId IS NULL OR l.id = :locationId)
              AND (:q IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:maxCalories IS NULL OR n.calories IS NULL OR n.calories <= :maxCalories)
              AND (:maxSugar IS NULL OR n.sugar IS NULL OR n.sugar <= :maxSugar)
              AND (:maxCaffeine IS NULL OR n.caffeine IS NULL OR n.caffeine <= :maxCaffeine)
              AND (:tag IS NULL OR LOWER(d.tags) LIKE LOWER(CONCAT('%', :tag, '%')))
              AND (:applyExclude = false OR d.id NOT IN (
                    SELECT d2.id FROM Drink d2 JOIN d2.allergens a
                    WHERE LOWER(a.name) IN :excludeNames
              ))
            ORDER BY d.name ASC
            """)
    List<Drink> search(
            @Param("category") String category,
            @Param("locationId") Long locationId,
            @Param("q") String q,
            @Param("maxCalories") Integer maxCalories,
            @Param("maxSugar") Double maxSugar,
            @Param("maxCaffeine") Double maxCaffeine,
            @Param("tag") String tag,
            @Param("applyExclude") boolean applyExclude,
            @Param("excludeNames") List<String> excludeNames);
}
