package com.simplysip.repository;

import com.simplysip.model.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AllergenRepository extends JpaRepository<Allergen, Long> {
    List<Allergen> findAllByOrderByNameAsc();

    Optional<Allergen> findByNameIgnoreCase(String name);
}
