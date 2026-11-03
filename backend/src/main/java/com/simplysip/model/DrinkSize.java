package com.simplysip.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "drink_sizes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_drink_sizes_drink_size_name",
                columnNames = {"drink_id", "size_name"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class DrinkSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drink_id", nullable = false)
    @JsonIgnore
    private Drink drink;

    @Column(name = "size_name", nullable = false)
    private String sizeName;

    private Double ounces;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @OneToOne(mappedBy = "drinkSize", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private NutritionFacts nutritionFacts;

    public void setNutritionFacts(NutritionFacts nutritionFacts) {
        if (nutritionFacts == null) {
            if (this.nutritionFacts != null) {
                this.nutritionFacts.setDrinkSize(null);
            }
        } else {
            nutritionFacts.setDrinkSize(this);
        }
        this.nutritionFacts = nutritionFacts;
    }
}
