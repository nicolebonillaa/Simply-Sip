package com.simplysip.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cafe_favorites")
@Getter
@Setter
@NoArgsConstructor
public class CafeFavorite {
    @EmbeddedId
    private CafeFavoriteId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @MapsId("locationId")
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @CreationTimestamp
    @Column(name = "date_added", updatable = false)
    private LocalDateTime dateAdded;
}
