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
              AND (:minPrice IS NULL OR d.price IS NULL OR d.price >= :minPrice)
              AND (:maxPrice IS NULL OR d.price IS NULL OR d.price <= :maxPrice)
              AND (:tag IS NULL OR LOWER(d.tags) LIKE LOWER(CONCAT('%', :tag, '%')))
              AND (:applyExclude = false OR d.id NOT IN (
                    SELECT d2.id FROM Drink d2 JOIN d2.allergens a
                    WHERE LOWER(a.name) IN :excludeNames
              ))
              AND (:applyMilk = false OR (
                    (:milk0 IS NOT NULL AND LOWER(CONCAT(',', REPLACE(COALESCE(d.milkTypes, ''), ' ', ''), ','))
                        LIKE LOWER(CONCAT('%,', :milk0, ',%')))
                 OR (:milk1 IS NOT NULL AND LOWER(CONCAT(',', REPLACE(COALESCE(d.milkTypes, ''), ' ', ''), ','))
                        LIKE LOWER(CONCAT('%,', :milk1, ',%')))
                 OR (:milk2 IS NOT NULL AND LOWER(CONCAT(',', REPLACE(COALESCE(d.milkTypes, ''), ' ', ''), ','))
                        LIKE LOWER(CONCAT('%,', :milk2, ',%')))
                 OR (:milk3 IS NOT NULL AND LOWER(CONCAT(',', REPLACE(COALESCE(d.milkTypes, ''), ' ', ''), ','))
                        LIKE LOWER(CONCAT('%,', :milk3, ',%')))
                 OR (:milk4 IS NOT NULL AND LOWER(CONCAT(',', REPLACE(COALESCE(d.milkTypes, ''), ' ', ''), ','))
                        LIKE LOWER(CONCAT('%,', :milk4, ',%')))
                 OR (:milk5 IS NOT NULL AND LOWER(CONCAT(',', REPLACE(COALESCE(d.milkTypes, ''), ' ', ''), ','))
                        LIKE LOWER(CONCAT('%,', :milk5, ',%')))
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
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("tag") String tag,
            @Param("applyExclude") boolean applyExclude,
            @Param("excludeNames") List<String> excludeNames,
            @Param("applyMilk") boolean applyMilk,
            @Param("milk0") String milk0,
            @Param("milk1") String milk1,
            @Param("milk2") String milk2,
            @Param("milk3") String milk3,
            @Param("milk4") String milk4,
            @Param("milk5") String milk5);
}
