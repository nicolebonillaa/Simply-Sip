package com.simplysip.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
