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
    @JoinColumn(name = "drink_id", unique = true)
    @JsonIgnore
    private Drink drink;

    private Integer calories;

    private Double sugar;

    private Double caffeine;
}
