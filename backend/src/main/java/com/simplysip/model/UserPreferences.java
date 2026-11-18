package com.simplysip.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "user_preferences")
@Getter @Setter @NoArgsConstructor
public class UserPreferences {
    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ElementCollection
    @CollectionTable(name = "user_dietary_preferences", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "preference", nullable = false)
    private Set<String> dietaryPreferences = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_excluded_allergens", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "allergen", nullable = false)
    private Set<String> excludedAllergens = new LinkedHashSet<>();
}
