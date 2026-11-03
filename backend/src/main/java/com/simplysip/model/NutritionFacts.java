package com.simplysip.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nutrition_facts")
@Getter
@Setter
@NoArgsConstructor
public class NutritionFacts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drink_size_id", unique = true)
    @JsonIgnore
    private DrinkSize drinkSize;

    private Integer calories;

    @Column(name = "total_fat_g")
    private Double totalFatG;

    @Column(name = "cholesterol_mg")
    private Double cholesterolMg;

    @Column(name = "sodium_mg")
    private Double sodiumMg;

    @Column(name = "carbs_g")
    private Double carbsG;

    @Column(name = "sugars_g")
    private Double sugarsG;

    @Column(name = "protein_g")
    private Double proteinG;

    @Column(name = "caffeine_mg")
    private Double caffeineMg;
}
