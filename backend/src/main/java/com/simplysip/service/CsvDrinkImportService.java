package com.simplysip.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplysip.config.ImportProperties;
import com.simplysip.model.Allergen;
import com.simplysip.model.Drink;
import com.simplysip.model.DrinkSize;
import com.simplysip.model.Location;
import com.simplysip.model.NutritionFacts;
import com.simplysip.repository.AllergenRepository;
import com.simplysip.repository.DrinkRepository;
import com.simplysip.repository.DrinkSizeRepository;
import com.simplysip.repository.LocationRepository;
import com.simplysip.util.ImageUrlNormalizer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class CsvDrinkImportService {

    private static final Logger log = LoggerFactory.getLogger(CsvDrinkImportService.class);
    private static final String[] CSV_FILES = {
            "data/it-s-boba-time_1_.csv",
            "data/qwench-juice-bar_1_.csv"
    };

    private final ImportProperties importProperties;
    private final LocationRepository locationRepository;
    private final DrinkRepository drinkRepository;
    private final DrinkSizeRepository drinkSizeRepository;
    private final AllergenRepository allergenRepository;
    private final ObjectMapper objectMapper;

    public CsvDrinkImportService(
            ImportProperties importProperties,
            LocationRepository locationRepository,
            DrinkRepository drinkRepository,
            DrinkSizeRepository drinkSizeRepository,
            AllergenRepository allergenRepository,
            ObjectMapper objectMapper) {
        this.importProperties = importProperties;
        this.locationRepository = locationRepository;
        this.drinkRepository = drinkRepository;
        this.drinkSizeRepository = drinkSizeRepository;
        this.allergenRepository = allergenRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ImportSummary importAll() {
        ImportSummary summary = new ImportSummary();
        Map<String, Location> locationsByCafe = loadLocations(summary);
        Map<String, Allergen> allergenCache = new HashMap<>();
        Map<String, String> firstImageByDrinkKey = new HashMap<>();

        for (String path : CSV_FILES) {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                log.warn("CSV not found on classpath: {} — skip", path);
                summary.skippedReasons.merge("missing file: " + path, 1, Integer::sum);
                continue;
            }
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                 CSVParser parser = CSVFormat.DEFAULT.builder()
                         .setHeader()
                         .setSkipHeaderRecord(true)
                         .setIgnoreEmptyLines(true)
                         .setTrim(true)
                         .build()
                         .parse(reader)) {
                Map<String, String> headerAliases = trimHeaderMap(parser.getHeaderMap());
                for (CSVRecord record : parser) {
                    summary.rowsRead++;
                    try {
                        importRow(record, headerAliases, locationsByCafe, allergenCache, firstImageByDrinkKey, summary);
                    } catch (Exception ex) {
                        summary.skipped++;
                        String reason = "row " + record.getRecordNumber() + ": " + ex.getMessage();
                        summary.skippedReasons.merge(reason, 1, Integer::sum);
                        log.warn("Skipping CSV row: {}", reason);
                    }
                }
            } catch (Exception ex) {
                log.error("Failed to read CSV {}", path, ex);
                summary.skippedReasons.merge("failed reading " + path + ": " + ex.getMessage(), 1, Integer::sum);
            }
        }

        log.info(
                "CSV import summary: rowsRead={}, drinksCreated={}, drinksUpdated={}, sizesCreated={}, sizesUpdated={}, locationsCreated={}, skipped={}, imagesSet={}, imagesUpdated={}, imagesMissing={}, invalidUrls={}",
                summary.rowsRead,
                summary.drinksCreated,
                summary.drinksUpdated,
                summary.sizesCreated,
                summary.sizesUpdated,
                summary.locationsCreated,
                summary.skipped,
                summary.imagesSet,
                summary.imagesUpdated,
                summary.imagesMissing,
                summary.invalidUrls);
        summary.skippedReasons.forEach((reason, count) ->
                log.info("  skipped reason ({}x): {}", count, reason));
        return summary;
    }

    private void importRow(
            CSVRecord record,
            Map<String, String> headerAliases,
            Map<String, Location> locationsByCafe,
            Map<String, Allergen> allergenCache,
            Map<String, String> firstImageByDrinkKey,
            ImportSummary summary) {
        String cafeRaw = cleanString(get(record, headerAliases, "cafe"));
        String drinkRaw = cleanString(get(record, headerAliases, "drink"));
        if (drinkRaw == null) {
            throw new IllegalArgumentException("missing drink name");
        }
        String cafe = importProperties.canonicalCafeName(cafeRaw);
        if (cafe == null) {
            throw new IllegalArgumentException("missing cafe");
        }
        Location location = locationsByCafe.get(cafe.toLowerCase(Locale.ROOT));
        if (location == null) {
            throw new IllegalArgumentException("no location entry for cafe '" + cafe + "'");
        }

        String drinkName = titleCaseIfAllLower(drinkRaw);
        String category = cleanString(get(record, headerAliases, "category"));
        String description = cleanString(get(record, headerAliases, "description"));
        String sourceUrl = cleanString(get(record, headerAliases, "url"));
        String sizeRaw = cleanString(get(record, headerAliases, "size"));
        Double ounces = parseDouble(get(record, headerAliases, "ounces"));
        BigDecimal price = parseDecimal(get(record, headerAliases, "price_usd"));

        Integer calories = parseInteger(get(record, headerAliases, "calories"));
        Double totalFatG = parseDouble(get(record, headerAliases, "total_fat_g"));
        Double cholesterolMg = parseDouble(get(record, headerAliases, "cholesterol_mg"));
        Double sodiumMg = parseDouble(get(record, headerAliases, "sodium_mg"));
        Double carbsG = parseDouble(get(record, headerAliases, "carbs_g"));
        Double sugarsG = parseDouble(get(record, headerAliases, "sugars_g"));
        Double proteinG = parseDouble(get(record, headerAliases, "protein_g"));
        Double caffeineMg = parseDouble(get(record, headerAliases, "caffeine_mg"));

        String rawImage = firstNonBlank(
                get(record, headerAliases, "image_url"),
                get(record, headerAliases, "drink_image_url"));
        String normalizedImage = resolveImageUrl(rawImage, summary);

        boolean nutritionEmpty = calories == null && totalFatG == null && cholesterolMg == null
                && sodiumMg == null && carbsG == null && sugarsG == null && proteinG == null && caffeineMg == null;

        String sizeName = sizeRaw;
        if (sizeName == null || nutritionEmpty) {
            if (sizeName == null) {
                sizeName = "Standard";
            }
            log.warn(
                    "Drink '{}' at '{}' has missing size and/or nutrition; using size '{}' with null nutrition as needed",
                    drinkName,
                    cafe,
                    sizeName);
        }

        AllergenParse allergenParse = parseAllergens(get(record, headerAliases, "allergens"), allergenCache);

        Drink drink = drinkRepository.findByLocationAndNameIgnoreCase(location, drinkName).orElse(null);
        boolean createdDrink = drink == null;
        if (drink == null) {
            drink = new Drink();
            drink.setLocation(location);
            drink.setName(drinkName);
            summary.drinksCreated++;
        } else {
            summary.drinksUpdated++;
        }

        if (createdDrink) {
            drink.setCategory(category);
            drink.setDescription(description);
            drink.setSourceUrl(sourceUrl);
        } else {
            if (isBlank(drink.getCategory()) && category != null) {
                drink.setCategory(category);
            }
            if (isBlank(drink.getDescription()) && description != null) {
                drink.setDescription(description);
            }
            if (isBlank(drink.getSourceUrl()) && sourceUrl != null) {
                drink.setSourceUrl(sourceUrl);
            }
        }

        String drinkKey = location.getId() + "|" + drinkName.toLowerCase(Locale.ROOT);
        applyDrinkImage(drink, drinkKey, normalizedImage, firstImageByDrinkKey, summary);

        drink.setAllergensKnown(allergenParse.known);
        drink.setAllergens(new HashSet<>(allergenParse.allergens));
        drink = drinkRepository.save(drink);

        DrinkSize size = drinkSizeRepository.findByDrinkAndSizeNameIgnoreCase(drink, sizeName).orElse(null);
        if (size == null) {
            size = new DrinkSize();
            size.setDrink(drink);
            size.setSizeName(sizeName);
            summary.sizesCreated++;
        } else {
            summary.sizesUpdated++;
        }
        size.setOunces(ounces);
        size.setPrice(price);

        if (nutritionEmpty) {
            size.setNutritionFacts(null);
        } else {
            NutritionFacts facts = size.getNutritionFacts();
            if (facts == null) {
                facts = new NutritionFacts();
            }
            facts.setCalories(calories);
            facts.setTotalFatG(totalFatG);
            facts.setCholesterolMg(cholesterolMg);
            facts.setSodiumMg(sodiumMg);
            facts.setCarbsG(carbsG);
            facts.setSugarsG(sugarsG);
            facts.setProteinG(proteinG);
            facts.setCaffeineMg(caffeineMg);
            size.setNutritionFacts(facts);
        }
        DrinkSize savedSize = drinkSizeRepository.save(size);

        if (createdDrink && drink.getSizes().stream().noneMatch(s ->
                s.getId() != null && s.getId().equals(savedSize.getId()))) {
            drink.addSize(savedSize);
        }
    }

    private String resolveImageUrl(String rawImage, ImportSummary summary) {
        String cleaned = cleanString(rawImage);
        if (cleaned == null) {
            summary.imagesMissing++;
            return null;
        }
        String normalized = ImageUrlNormalizer.normalize(cleaned);
        if (normalized == null) {
            summary.invalidUrls++;
            summary.imagesMissing++;
            return null;
        }
        return normalized;
    }

    private void applyDrinkImage(
            Drink drink,
            String drinkKey,
            String normalizedImage,
            Map<String, String> firstImageByDrinkKey,
            ImportSummary summary) {
        if (normalizedImage == null) {
            return;
        }
        String existingFirst = firstImageByDrinkKey.get(drinkKey);
        if (existingFirst != null) {
            if (!existingFirst.equals(normalizedImage)) {
                log.warn(
                        "Conflicting image URLs for drink key '{}'; keeping first '{}', ignoring '{}'",
                        drinkKey,
                        existingFirst,
                        normalizedImage);
            }
            return;
        }
        firstImageByDrinkKey.put(drinkKey, normalizedImage);

        String previous = drink.getImageUrl();
        if (isBlank(previous)) {
            drink.setImageUrl(normalizedImage);
            summary.imagesSet++;
        } else if (!previous.equals(normalizedImage)) {
            drink.setImageUrl(normalizedImage);
            summary.imagesUpdated++;
        } else {
            drink.setImageUrl(normalizedImage);
        }
    }

    private Map<String, Location> loadLocations(ImportSummary summary) {
        Map<String, Location> byCafe = new LinkedHashMap<>();
        try {
            ClassPathResource resource = new ClassPathResource("data/locations.json");
            List<LocationSeed> seeds = objectMapper.readValue(
                    resource.getInputStream(), new TypeReference<List<LocationSeed>>() {});
            for (LocationSeed seed : seeds) {
                String cafe = seed.cafeName == null ? null : seed.cafeName.trim();
                if (cafe == null || cafe.isBlank()) {
                    continue;
                }
                Location location = locationRepository.findByCafeNameIgnoreCase(cafe).orElse(null);
                if (location == null) {
                    location = new Location();
                    location.setCafeName(cafe);
                    summary.locationsCreated++;
                }
                location.setLocationName(seed.locationName);
                location.setStreetAddress(seed.streetAddress);
                location.setCity(seed.city);
                location.setState(seed.state);
                location.setZip(seed.zip);
                location.setLatitude(seed.latitude);
                location.setLongitude(seed.longitude);
                // Location.imageUrl stays null for now (scraper images attach to drinks only).
                location.setImageUrl(null);
                location = locationRepository.save(location);
                byCafe.put(cafe.toLowerCase(Locale.ROOT), location);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load data/locations.json: " + ex.getMessage(), ex);
        }
        return byCafe;
    }

    private AllergenParse parseAllergens(String raw, Map<String, Allergen> cache) {
        String cleaned = cleanString(raw);
        AllergenParse result = new AllergenParse();
        if (cleaned == null) {
            result.known = false;
            return result;
        }
        if ("none".equalsIgnoreCase(cleaned)) {
            result.known = true;
            return result;
        }
        result.known = true;
        for (String part : cleaned.split(";")) {
            String name = cleanString(part);
            if (name == null) {
                continue;
            }
            name = name.toLowerCase(Locale.ROOT);
            Allergen allergen = cache.get(name);
            if (allergen == null) {
                String finalName = name;
                allergen = allergenRepository.findByNameIgnoreCase(finalName).orElseGet(() -> {
                    Allergen created = new Allergen();
                    created.setName(finalName);
                    return allergenRepository.save(created);
                });
                cache.put(name, allergen);
            }
            result.allergens.add(allergen);
        }
        return result;
    }

    static String cleanString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if ("unknown".equalsIgnoreCase(trimmed) || "n/a".equalsIgnoreCase(trimmed) || "na".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }

    static Integer parseInteger(String raw) {
        Double number = parseDouble(raw);
        return number == null ? null : (int) Math.round(number);
    }

    static Double parseDouble(String raw) {
        String cleaned = cleanString(raw);
        if (cleaned == null) {
            return null;
        }
        String numeric = cleaned.replaceAll("[^0-9.+-]", "");
        if (numeric.isBlank() || "-".equals(numeric) || ".".equals(numeric) || "+".equals(numeric)) {
            return null;
        }
        try {
            return Double.parseDouble(numeric);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    static BigDecimal parseDecimal(String raw) {
        Double number = parseDouble(raw);
        return number == null ? null : BigDecimal.valueOf(number);
    }

    /**
     * Title-case only when the entire name is lowercase. Leave mixed-case brand/acronym names alone.
     */
    static String titleCaseIfAllLower(String name) {
        if (name == null) {
            return null;
        }
        boolean allLower = name.equals(name.toLowerCase(Locale.ROOT))
                && name.chars().anyMatch(Character::isLetter);
        if (!allLower) {
            return name;
        }
        StringBuilder out = new StringBuilder(name.length());
        boolean capNext = true;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetter(c)) {
                out.append(capNext ? Character.toUpperCase(c) : c);
                capNext = false;
            } else {
                out.append(c);
                capNext = !Character.isDigit(c);
            }
        }
        return out.toString();
    }

    private static Map<String, String> trimHeaderMap(Map<String, Integer> headerMap) {
        Map<String, String> aliases = new HashMap<>();
        if (headerMap == null) {
            return aliases;
        }
        for (String header : headerMap.keySet()) {
            if (header == null) {
                continue;
            }
            aliases.put(header.trim().toLowerCase(Locale.ROOT), header);
        }
        return aliases;
    }

    private static String get(CSVRecord record, Map<String, String> headerAliases, String logicalHeader) {
        String actual = headerAliases.get(logicalHeader.toLowerCase(Locale.ROOT));
        if (actual == null) {
            return null;
        }
        if (!record.isMapped(actual)) {
            return null;
        }
        String value = record.get(actual);
        return value == null ? null : value.trim();
    }

    private static String firstNonBlank(String first, String second) {
        String a = cleanString(first);
        if (a != null) {
            return a;
        }
        return cleanString(second);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static class ImportSummary {
        public int rowsRead;
        public int drinksCreated;
        public int drinksUpdated;
        public int sizesCreated;
        public int sizesUpdated;
        public int locationsCreated;
        public int skipped;
        public int imagesSet;
        public int imagesUpdated;
        public int imagesMissing;
        public int invalidUrls;
        public final Map<String, Integer> skippedReasons = new LinkedHashMap<>();
    }

    private static class AllergenParse {
        boolean known;
        final Set<Allergen> allergens = new HashSet<>();
    }

    public static class LocationSeed {
        public String cafeName;
        public String locationName;
        public String streetAddress;
        public String city;
        public String state;
        public String zip;
        public Double latitude;
        public Double longitude;
        public String imageUrl;
    }
}
