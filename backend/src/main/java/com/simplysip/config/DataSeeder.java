package com.simplysip.config;

import com.simplysip.model.Allergen;
import com.simplysip.model.Drink;
import com.simplysip.model.DrinkSize;
import com.simplysip.model.Favorite;
import com.simplysip.model.FavoriteId;
import com.simplysip.model.Location;
import com.simplysip.model.NutritionFacts;
import com.simplysip.model.User;
import com.simplysip.repository.AllergenRepository;
import com.simplysip.repository.DrinkRepository;
import com.simplysip.repository.FavoriteRepository;
import com.simplysip.repository.LocationRepository;
import com.simplysip.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final DrinkRepository drinkRepository;
    private final FavoriteRepository favoriteRepository;
    private final AllergenRepository allergenRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            LocationRepository locationRepository,
            DrinkRepository drinkRepository,
            FavoriteRepository favoriteRepository,
            AllergenRepository allergenRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.drinkRepository = drinkRepository;
        this.favoriteRepository = favoriteRepository;
        this.allergenRepository = allergenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        User alice = ensureUser("Alice Matador", "alice@csun.edu", "password123");
        User bob = ensureUser("Bob Sundial", "bob@csun.edu", "password123");
        User cara = ensureUser("Cara Orange", "cara@csun.edu", "password123");

        ensureAllergen("Peanuts");
        ensureAllergen("Tree nuts");
        ensureAllergen("Soy");
        ensureAllergen("Eggs");
        ensureAllergen("Wheat");

        backfillDemoImageUrls();

        if (drinkRepository.count() > 0) {
            return;
        }

        Location matador = location(
                "Matador Bookstore Cafe",
                "Matador Bookstore Cafe",
                "18111 Nordhoff St",
                "Northridge",
                "CA",
                "91330",
                34.2410,
                -118.5290,
                "/images/latte.jpg");
        Location starbucks = location(
                "Starbucks - Reseda",
                "Starbucks - Reseda",
                "9301 Reseda Blvd",
                "Northridge",
                "CA",
                "91324",
                34.2405,
                -118.5360,
                null);
        Location juiceIt = location(
                "Juice It Up!",
                "Juice It Up!",
                "9301 Tampa Ave",
                "Northridge",
                "CA",
                "91324",
                34.2388,
                -118.5535,
                null);

        Allergen soy = ensureAllergen("Soy");
        Allergen treeNuts = ensureAllergen("Tree nuts");

        Drink icedLatte = drink(
                "Iced Latte",
                "Coffee",
                "Classic iced espresso with milk.",
                null,
                "/images/latte.jpg",
                true,
                matador,
                Set.of(),
                "Grande",
                16.0,
                "4.50",
                120,
                10.0,
                150.0);
        Drink matcha = drink(
                "Matcha Green Tea Latte",
                "Tea",
                "Sweetened matcha with milk.",
                null,
                null,
                true,
                starbucks,
                Set.of(soy),
                "Grande",
                16.0,
                "5.25",
                240,
                32.0,
                80.0);
        Drink smoothie = drink(
                "Berry Blast Smoothie",
                "Smoothie",
                "Mixed berry smoothie.",
                null,
                null,
                true,
                juiceIt,
                Set.of(treeNuts),
                "Regular",
                20.0,
                "6.50",
                290,
                45.0,
                0.0);

        favorite(alice, icedLatte);
        favorite(bob, matcha);
        favorite(cara, smoothie);
    }

    private void backfillDemoImageUrls() {
        locationRepository.findByCafeNameIgnoreCase("Matador Bookstore Cafe").ifPresent(location -> {
            if (location.getImageUrl() == null || location.getImageUrl().isBlank()) {
                location.setImageUrl("/images/latte.jpg");
                locationRepository.save(location);
            }
        });
        drinkRepository.findAll().stream()
                .filter(drink -> "Iced Latte".equalsIgnoreCase(drink.getName()))
                .filter(drink -> drink.getImageUrl() == null || drink.getImageUrl().isBlank())
                .forEach(drink -> {
                    drink.setImageUrl("/images/latte.jpg");
                    drinkRepository.save(drink);
                });
    }

    private Allergen ensureAllergen(String name) {
        return allergenRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Allergen allergen = new Allergen();
            allergen.setName(name);
            return allergenRepository.save(allergen);
        });
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

    private Location location(
            String cafeName,
            String locationName,
            String street,
            String city,
            String state,
            String zip,
            Double lat,
            Double lng,
            String imageUrl) {
        return locationRepository.findByCafeNameIgnoreCase(cafeName).orElseGet(() -> {
            Location location = new Location();
            location.setCafeName(cafeName);
            location.setLocationName(locationName);
            location.setStreetAddress(street);
            location.setCity(city);
            location.setState(state);
            location.setZip(zip);
            location.setLatitude(lat);
            location.setLongitude(lng);
            location.setImageUrl(imageUrl);
            return locationRepository.save(location);
        });
    }

    private Drink drink(
            String name,
            String category,
            String description,
            String sourceUrl,
            String imageUrl,
            boolean allergensKnown,
            Location location,
            Set<Allergen> allergens,
            String sizeName,
            Double ounces,
            String price,
            Integer calories,
            Double sugarsG,
            Double caffeineMg) {
        Drink drink = new Drink();
        drink.setName(name);
        drink.setCategory(category);
        drink.setDescription(description);
        drink.setSourceUrl(sourceUrl);
        drink.setImageUrl(imageUrl);
        drink.setAllergensKnown(allergensKnown);
        drink.setLocation(location);
        drink.setAllergens(allergens);

        DrinkSize size = new DrinkSize();
        size.setSizeName(sizeName);
        size.setOunces(ounces);
        size.setPrice(new BigDecimal(price));

        NutritionFacts facts = new NutritionFacts();
        facts.setCalories(calories);
        facts.setSugarsG(sugarsG);
        facts.setCaffeineMg(caffeineMg);
        size.setNutritionFacts(facts);

        drink.addSize(size);
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
