package simplysip;

import org.jsoup.nodes.Element;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads sizes, prices, nutrition facts, allergens and descriptions out of the TEXT of a page
 * or menu item. It looks for common wording ("Large 21 oz", "$5.50", "Calories 290",
 * "Allergens: milk") instead of site-specific HTML, so it works on many menus.
 */
public final class Extractors {

    /** size letters some sites use ("R 16 oz") -> full names. */
    static final Map<String, String> SIZE_NAMES =
            Map.of("S", "Small", "M", "Medium", "R", "Regular", "L", "Large", "XL", "Extra Large");

    /** allergen words to look for when a menu writes "Contains: ..." in plain text. More can be added to the list if needed. */
    static final List<String> KNOWN_ALLERGENS = List.of("milk", "dairy", "egg", "soy", "wheat", "gluten",
            "peanut", "tree nut", "almond", "coconut", "sesame", "fish", "shellfish");

    /** nutrients to look for. label = how we print it, regex = how menus write it. */
    record Nutrient(String label, String regex, String unit, String column) {}
    static final List<Nutrient> NUTRIENTS = List.of(
            new Nutrient("Calories", "Calories|Kcal", "", "calories"),
            new Nutrient("Total Fat", "Total Fat|(?<!saturated )(?<!trans )Fat", "g", "total_fat_g"),
            new Nutrient("Cholesterol", "Cholesterol", "mg", "cholesterol_mg"),
            new Nutrient("Sodium", "Sodium", "mg", "sodium_mg"),
            new Nutrient("Total Carbohydrate", "Total Carbohydrates?|Carbohydrates?|Carbs", "g", "carbs_g"),
            new Nutrient("Sugars", "Total Sugars|Sugars?", "g", "sugars_g"),
            new Nutrient("Protein", "Protein", "g", "protein_g"),
            new Nutrient("Caffeine", "Caffeine", "mg", "caffeine_mg"));

    static final String SIZE_WORDS = "extra large|x-large|xl|small|medium|large|regular|mini|tall|grande|venti";
    static final Pattern PRICE = Pattern.compile("\\$\\s*(\\d+(?:\\.\\d{1,2})?)");
    static final Pattern CODE_OZ = Pattern.compile("\\b(XL|L|R|S|M)\\s+(\\d{1,2})\\s*oz\\b");            // "R 16 oz"
    static final String OZ = "\\d{1,2}\\s*(?:fl\\.?\\s*)?oz";                                          // "16 oz" or "12 fl oz"
    static final Pattern WORD_OZ = Pattern.compile("(?i)\\b(" + SIZE_WORDS + ")\\b[^A-Za-z0-9$]{0,4}(\\d{1,2})\\s*(?:fl\\.?\\s*)?oz"); // "Large (21 oz)"
    static final Pattern SIZE_THEN_PRICE = Pattern.compile("(?i)\\b(" + SIZE_WORDS + "|" + OZ + ")\\b[^$\\d]{0,15}\\$\\s*(\\d+(?:\\.\\d{1,2})?)");   // "12 fl oz $4.59"
    static final Pattern PRICE_THEN_SIZE = Pattern.compile("(?i)\\$\\s*(\\d+(?:\\.\\d{1,2})?)[\\s(\\[*/|-]{0,4}(" + SIZE_WORDS + "|" + OZ + ")\\b"); // "$2.79 (12 oz)"
    static final Pattern OZ_ONLY = Pattern.compile("(?i)\\b(\\d{1,2})\\s*(?:fl\\.?\\s*)?oz\\b");
    static final Pattern BLOCK = Pattern.compile("(?i)(?:per serving|nutrition facts?)\\s*\\(([^)]+)\\)");   // "Nutrition Facts per serving (regular)"
    static final Pattern DESCRIPTION = Pattern.compile("(?i)Description\\s+(.*?)\\s+allergens\\b");

    /** Fill a drink's description, allergens and sizes (with prices + nutrition) from this element's text. */
    static void parse(Element scope, Drink d) {
        String text = scope.text();
        if (d.description.isEmpty()) d.description = description(scope, text);
        allergens(scope, d);
        fillSizes(text, d);
    }

    /** Used after visiting a drink's own page: adds new info without losing prices found on the menu page. */
    static void mergeDetail(Element body, Drink d) {
        Drink t = new Drink();
        parse(body, t);
        if (d.description.isEmpty()) d.description = t.description;
        for (String a : t.allergens) if (!d.allergens.contains(a)) d.allergens.add(a);
        if (t.sizes.isEmpty()) return;
        Map<String, String> oldPrice = new HashMap<>();
        for (Drink.Size s : d.sizes) if (!s.price().isEmpty()) oldPrice.put(sizeKey(s.label()), s.price());
        List<Drink.Size> merged = new ArrayList<>();
        for (Drink.Size s : t.sizes) {
            String p = s.price();
            if (p.isEmpty()) p = oldPrice.getOrDefault(sizeKey(s.label()), oldPrice.size() == 1 ? oldPrice.values().iterator().next() : "");
            merged.add(new Drink.Size(s.label(), s.ounces(), p, s.nutrition()));
        }
        d.sizes = merged;
    }

    static String description(Element scope, String text) {
        Matcher m = DESCRIPTION.matcher(text);
        if (m.find() && m.group(1).length() <= 500) return m.group(1).trim();
        Element e = scope.selectFirst("[itemprop=description], [class*=desc]");
        if (e != null) {
            String t = e.text().trim();
            if (t.length() >= 10 && t.length() <= 500) return t;
        }
        return "";
    }

    static void allergens(Element scope, Drink d) {
        // 1. allergen icons: <img alt="Milk"> or a file name like milk.png
        for (Element img : scope.select("img[src*='/icon/'], img[src*='allergen'], [class*='allergen'] img")) {
            String a = img.attr("alt").isBlank() ? img.attr("src").replaceAll(".*/|\\.\\w+$", "") : img.attr("alt");
            a = a.replaceAll("[-_]", " ").trim().toLowerCase();
            if (!a.isEmpty() && !d.allergens.contains(a)) d.allergens.add(a);
        }
        // 2. an element with "allergen" in its class name, e.g. <span class="allergens">Milk, Soy</span>
        for (Element el : scope.select("[class*='allergen']:not(img)")) {
            for (String part : el.text().split("[,;/]")) {
                String a = part.trim().toLowerCase();
                if (a.length() >= 2 && a.length() <= 30 && !d.allergens.contains(a)) d.allergens.add(a);
            }
        }
        // 3. plain text like "Allergens: milk, soy" or "Contains: peanuts" (only known allergen words are kept)
        if (d.allergens.isEmpty()) {
            Matcher m = Pattern.compile("(?i)\\b(?:allergens?|contains)\\b[^.]{0,100}").matcher(scope.text());
            while (m.find()) {
                for (String word : KNOWN_ALLERGENS)
                    if (Pattern.compile("(?i)\\b" + word).matcher(m.group()).find() && !d.allergens.contains(word)) d.allergens.add(word);
            }
        }
    }

    /** Works out the sizes, then attaches a price and nutrition block to each. */
    static void fillSizes(String text, Drink d) {
        int nutStart = text.toLowerCase(Locale.ROOT).indexOf("nutrition fact");
        String head = nutStart > 0 ? text.substring(0, nutStart) : text;   // sizes/prices come BEFORE the nutrition section

        // ---- sizes: {label, ounces} ----
        List<String[]> sizes = new ArrayList<>();
        Matcher m = CODE_OZ.matcher(head);
        while (m.find()) if (Integer.parseInt(m.group(2)) > 0)              // "XL 0 oz" means "this size doesn't exist"
            addSize(sizes, SIZE_NAMES.getOrDefault(m.group(1), m.group(1)), m.group(2));
        if (sizes.isEmpty()) {
            m = WORD_OZ.matcher(head);
            while (m.find()) if (Integer.parseInt(m.group(2)) > 0) addSize(sizes, titleCase(m.group(1)), m.group(2));
        }

        // ---- prices written next to a size, e.g. "Small $4.50": key -> {label, price} ----
        Map<String, String[]> fwd = new LinkedHashMap<>();      // "12 fl oz $4.59"  (size first)
        Map<String, String[]> rev = new LinkedHashMap<>();      // "$2.79 (12 oz)"   (price first)
        m = SIZE_THEN_PRICE.matcher(head);
        while (m.find()) fwd.putIfAbsent(sizeKey(m.group(1)), new String[]{m.group(1).trim(), m.group(2)});
        m = PRICE_THEN_SIZE.matcher(head);
        while (m.find()) rev.putIfAbsent(sizeKey(m.group(2)), new String[]{m.group(2).trim(), m.group(1)});
        Map<String, String[]> pairs = rev.size() > fwd.size() ? rev : fwd;
        if (sizes.isEmpty()) {
            for (String[] p : pairs.values()) {
                Matcher oz = Pattern.compile("(?i)(\\d{1,2})\\s*(?:fl\\.?\\s*)?oz").matcher(p[0]);
                if (oz.find()) addSize(sizes, oz.group(1) + " oz", oz.group(1));
                else addSize(sizes, titleCase(p[0]), "");
            }
        }
        if (sizes.isEmpty() && head.length() < 400) {                        // short menu item that only says "16 oz"
            m = OZ_ONLY.matcher(head);
            while (m.find()) addSize(sizes, m.group(1) + " oz", m.group(1));
        }
        List<String> allPrices = new ArrayList<>();
        m = PRICE.matcher(head);
        while (m.find()) allPrices.add(m.group(1));

        // ---- nutrition blocks (one per size when the site shows several tables) ----
        List<String> labels = new ArrayList<>();
        List<Map<String, String>> blocks = new ArrayList<>();
        List<int[]> spans = new ArrayList<>();
        Matcher bm = BLOCK.matcher(text);
        while (bm.find()) { spans.add(new int[]{bm.start(), bm.end()}); labels.add(bm.group(1).trim()); }
        for (int i = 0; i < spans.size(); i++) {
            int end = (i + 1 < spans.size()) ? spans.get(i + 1)[0] : text.length();
            blocks.add(parseNutrition(text.substring(spans.get(i)[1], end)));
        }
        if (blocks.isEmpty()) {                                              // only one nutrition table on the page
            Map<String, String> single = parseNutrition(text);
            if (!single.isEmpty()) { blocks.add(single); labels.add(""); }
        }

        // ---- combine ----
        List<Drink.Size> out = new ArrayList<>();
        if (sizes.isEmpty()) {
            String price = allPrices.size() == 1 ? allPrices.get(0) : "";
            if (blocks.isEmpty()) {
                if (!price.isEmpty()) out.add(new Drink.Size("Standard", "", price, new LinkedHashMap<>()));
            } else {
                for (int i = 0; i < blocks.size(); i++) {
                    if (blocks.size() > 1 && isAllZero(blocks.get(i))) continue;
                    String label = labels.get(i).isEmpty() ? "Standard" : titleCase(labels.get(i));
                    out.add(new Drink.Size(label, "", blocks.size() == 1 ? price : "", blocks.get(i)));
                }
            }
        } else {
            for (int i = 0; i < sizes.size(); i++) {
                String label = sizes.get(i)[0], oz = sizes.get(i)[1];
                String[] p = pairs.get(sizeKey(label));
                if (p == null) p = pairs.get(oz + "oz");
                String price = p != null ? p[1]
                        : allPrices.size() == sizes.size() ? allPrices.get(i)
                          : allPrices.size() == 1 ? allPrices.get(0) : "";
                Map<String, String> nut = new LinkedHashMap<>();
                int j = -1;
                for (int k = 0; k < labels.size(); k++) if (norm(labels.get(k)).equals(norm(label))) j = k;
                if (j >= 0) nut = blocks.get(j);
                else if (blocks.size() == sizes.size()) nut = blocks.get(i);
                else if (blocks.size() == 1) nut = blocks.get(0);
                out.add(new Drink.Size(label, oz, price, nut));
            }
        }
        d.sizes = out;
    }

    /** Finds "Calories 290", "Total Fat 10g", ... inside a block of text. Returns label -> number. */
    static Map<String, String> parseNutrition(String block) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Nutrient n : NUTRIENTS) {
            Matcher m = Pattern.compile("(?i)\\b(?:" + n.regex() + ")\\b\\s*:?\\s*(\\d+(?:\\.\\d+)?)").matcher(block);
            if (m.find()) out.put(n.label(), m.group(1));
        }
        if (!out.containsKey("Calories")) {                                  // also accept "290 cal" / "290 kcal"
            Matcher m = Pattern.compile("(?i)\\b(\\d{2,4})\\s*(?:kcal|cal(?:ories)?)\\b").matcher(block);
            if (m.find()) out.put("Calories", m.group(1));
        }
        return out;
    }

    static boolean isAllZero(Map<String, String> nut) {
        return nut.isEmpty() || nut.values().stream().allMatch(v -> Double.parseDouble(v) == 0);
    }

    static void addSize(List<String[]> sizes, String label, String oz) {
        for (String[] s : sizes) if (s[0].equalsIgnoreCase(label)) return;
        sizes.add(new String[]{label, oz});
    }

    /** "Extra Large", "x-large" and "XL" all become "xl" so sizes from different places can be matched up. */
    static String norm(String s) {
        String n = s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return (n.equals("xlarge") || n.equals("extralarge")) ? "xl" : n;
    }

    /** Same size, same key: "12 fl oz" and "12 oz" -> "12oz"; "Extra Large" and "XL" -> "xl". */
    static String sizeKey(String raw) {
        Matcher m = OZ_ONLY.matcher(raw);
        return m.find() ? m.group(1) + "oz" : norm(raw);
    }

    /** adjusts titles example: "TOPO CHICO WATER (crystal)" -> "Topo Chico Water (Crystal)" */
    static String titleCase(String s) {
        StringBuilder b = new StringBuilder();
        boolean up = true;
        for (char c : s.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT).toCharArray()) {
            b.append(up && Character.isLetter(c) ? Character.toUpperCase(c) : c);
            up = !(Character.isLetterOrDigit(c) || c == '\'');
        }
        return b.toString();
    }
}