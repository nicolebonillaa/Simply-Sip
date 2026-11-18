package com.simplysip.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(
        name = "locations",
        uniqueConstraints = @UniqueConstraint(name = "uk_locations_cafe_name", columnNames = "cafe_name")
)
@Getter
@Setter
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cafe_name", nullable = false)
    private String cafeName;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "street_address")
    private String streetAddress;

    private String city;

    private String state;

    private String zip;

    private Double latitude;

    private Double longitude;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "location", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<CafeFavorite> cafeFavorites = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
