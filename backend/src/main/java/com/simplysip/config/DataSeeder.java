package com.simplysip.config;

import com.simplysip.model.Drink;
import com.simplysip.model.Favorite;
import com.simplysip.model.FavoriteId;
import com.simplysip.model.Location;
import com.simplysip.model.NutritionFacts;
import com.simplysip.model.User;
import com.simplysip.repository.DrinkRepository;
import com.simplysip.repository.FavoriteRepository;
import com.simplysip.repository.LocationRepository;
import com.simplysip.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final DrinkRepository drinkRepository;
    private final FavoriteRepository favoriteRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            LocationRepository locationRepository,
            DrinkRepository drinkRepository,
            FavoriteRepository favoriteRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.drinkRepository = drinkRepository;
        this.favoriteRepository = favoriteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        User alice = ensureUser("Alice Matador", "alice@csun.edu", "password123");
        User bob = ensureUser("Bob Sundial", "bob@csun.edu", "password123");
        User cara = ensureUser("Cara Orange", "cara@csun.edu", "password123");

        if (drinkRepository.count() > 0) {
            drinkRepository.findAll().forEach((drink) -> {
                if (drink.getTags() == null || drink.getTags().isBlank()) {
                    if ("Coffee".equalsIgnoreCase(drink.getCategory())) {
                        drink.setTags("dairy");
                    } else if ("Tea".equalsIgnoreCase(drink.getCategory())) {
                        drink.setTags("dairy,gluten-free");
                    } else if ("Smoothie".equalsIgnoreCase(drink.getCategory())) {
                        drink.setTags("vegan,dairy-free,gluten-free");
                    }
                    drinkRepository.save(drink);
                }
            });
            return;
        }

        Location matador = location("Matador Bookstore Cafe", "18111 Nordhoff St, Northridge, CA 91330", 34.2410, -118.5290);
        Location starbucks = location("Starbucks - Reseda", "9301 Reseda Blvd, Northridge, CA 91324", 34.2405, -118.5360);
        Location juiceIt = location("Juice It Up!", "9301 Tampa Ave, Northridge, CA 91324", 34.2388, -118.5535);

        Drink icedLatte = drink("Iced Latte", "Coffee", "dairy", matador, 120, 10.0, 150.0);
        Drink matcha = drink("Matcha Green Tea Latte", "Tea", "dairy,gluten-free", starbucks, 240, 32.0, 80.0);
        Drink smoothie = drink("Berry Blast Smoothie", "Smoothie", "vegan,dairy-free,gluten-free", juiceIt, 290, 45.0, 0.0);

        favorite(alice, icedLatte);
        favorite(bob, matcha);
        favorite(cara, smoothie);
    }

    private User ensureUser(String name, String email, String rawPassword) {
        return userRepository.findByEmail(email).map(existing -> {
            existing.setPassword(passwordEncoder.encode(rawPassword));
            existing.setName(name);
            return userRepository.save(existing);
        }).orElseGet(() -> {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword));
            return userRepository.save(user);
        });
    }

    private Location location(String name, String address, double lat, double lng) {
        Location location = new Location();
        location.setName(name);
        location.setAddress(address);
        location.setLatitude(lat);
        location.setLongitude(lng);
        return locationRepository.save(location);
    }

    private Drink drink(
            String name,
            String category,
            String tags,
            Location location,
            int calories,
            double sugar,
            double caffeine) {
        Drink drink = new Drink();
        drink.setName(name);
        drink.setCategory(category);
        drink.setTags(tags);
        drink.setLocation(location);
        NutritionFacts facts = new NutritionFacts();
        facts.setCalories(calories);
        facts.setSugar(sugar);
        facts.setCaffeine(caffeine);
        drink.setNutritionFacts(facts);
        return drinkRepository.save(drink);
    }

    private void favorite(User user, Drink drink) {
        FavoriteId id = new FavoriteId(user.getId(), drink.getId());
        if (favoriteRepository.existsById(id)) {
            return;
        }
        Favorite favorite = new Favorite();
        favorite.setId(id);
        favorite.setUser(user);
        favorite.setDrink(drink);
        favoriteRepository.save(favorite);
    }
}
