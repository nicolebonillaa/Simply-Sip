package com.simplysip.repository;

import com.simplysip.model.Drink;
import com.simplysip.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DrinkRepository extends JpaRepository<Drink, Long> {

    Optional<Drink> findByLocationAndNameIgnoreCase(Location location, String name);

    @Query("""
            SELECT DISTINCT d FROM Drink d
            LEFT JOIN d.location l
            LEFT JOIN d.sizes s
            LEFT JOIN s.nutritionFacts n
            WHERE (:category IS NULL OR LOWER(d.category) = LOWER(:category))
              AND (:locationId IS NULL OR l.id = :locationId)
              AND (:q IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:maxCalories IS NULL OR EXISTS (
                    SELECT 1 FROM DrinkSize s2
                    JOIN s2.nutritionFacts n2
                    WHERE s2.drink = d AND n2.calories IS NOT NULL AND n2.calories <= :maxCalories
              ))
              AND (:maxSugar IS NULL OR EXISTS (
                    SELECT 1 FROM DrinkSize s3
                    JOIN s3.nutritionFacts n3
                    WHERE s3.drink = d AND n3.sugarsG IS NOT NULL AND n3.sugarsG <= :maxSugar
              ))
              AND (:maxCaffeine IS NULL OR EXISTS (
                    SELECT 1 FROM DrinkSize s4
                    JOIN s4.nutritionFacts n4
                    WHERE s4.drink = d AND n4.caffeineMg IS NOT NULL AND n4.caffeineMg <= :maxCaffeine
              ))
              AND (:minPrice IS NULL OR EXISTS (
                    SELECT 1 FROM DrinkSize s5
                    WHERE s5.drink = d AND s5.price IS NOT NULL AND s5.price >= :minPrice
              ))
              AND (:maxPrice IS NULL OR EXISTS (
                    SELECT 1 FROM DrinkSize s6
                    WHERE s6.drink = d AND s6.price IS NOT NULL AND s6.price <= :maxPrice
              ))
              AND (:applyExclude = false OR (
                    (d.allergensKnown = true OR :includeUnknownAllergens = true)
                    AND d.id NOT IN (
                        SELECT d2.id FROM Drink d2 JOIN d2.allergens a
                        WHERE LOWER(a.name) IN :excludeNames
                    )
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
            @Param("applyExclude") boolean applyExclude,
            @Param("excludeNames") List<String> excludeNames,
            @Param("includeUnknownAllergens") boolean includeUnknownAllergens);
}
