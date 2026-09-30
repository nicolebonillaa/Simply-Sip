package com.simplysip.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drinks")
@Getter
@Setter
@NoArgsConstructor
public class Drink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String category;

    private String tags;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Location location;

    @OneToOne(mappedBy = "drink", cascade = CascadeType.ALL, orphanRemoval = true)
    private NutritionFacts nutritionFacts;

    @OneToMany(mappedBy = "drink", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<Favorite> favorites = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void setNutritionFacts(NutritionFacts nutritionFacts) {
        if (nutritionFacts == null) {
            if (this.nutritionFacts != null) {
                this.nutritionFacts.setDrink(null);
            }
        } else {
            nutritionFacts.setDrink(this);
        }
        this.nutritionFacts = nutritionFacts;
    }
}
