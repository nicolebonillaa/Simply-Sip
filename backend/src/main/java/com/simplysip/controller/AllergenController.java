package com.simplysip.controller;

import com.simplysip.model.Allergen;
import com.simplysip.repository.AllergenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/allergens")
public class AllergenController {

    private final AllergenRepository allergenRepository;

    public AllergenController(AllergenRepository allergenRepository) {
        this.allergenRepository = allergenRepository;
    }

    /**
     * Lists every allergen by name.
     */
    @GetMapping
    public List<Allergen> getAll() {
        return allergenRepository.findAllByOrderByNameAsc();
    }
}
