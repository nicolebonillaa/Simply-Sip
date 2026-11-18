package com.simplysip.repository;

import com.simplysip.model.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
}
