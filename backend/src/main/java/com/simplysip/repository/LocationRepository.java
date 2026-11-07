package com.simplysip.repository;

import com.simplysip.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByCafeNameIgnoreCase(String cafeName);
}
