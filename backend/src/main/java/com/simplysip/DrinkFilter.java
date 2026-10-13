package simplysip;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Decides what is a drink and what is food by using the word lists below that you can edit.
 * How a drink is judged:
 *   1. CATEGORY  - "Whole Cakes" has a food word -> FOOD.  "Hot Coffee" has a drink word -> DRINK.
 *                  A category with neither (or both) is UNKNOWN.
 *   2. ITEM NAME - the LAST word usually says what it is: "Pumpkin Pie Latte" is a latte (drink),
 *                  "Coffee Cake" is a cake (food). If the last word isn't in either list, it looks
 *                  for any drink or food word in the name.
 *   3. Then:  DRINK category  -> keep everything except items that are clearly food
 *             FOOD category   -> drop everything except items that are clearly drinks
 *             UNKNOWN         -> keep only items that are clearly drinks
 *   4. Alcohol is dropped since this project is non-alcoholic drinks only.
 *
 * Every dropped item is printed at the end, so you can see mistakes and fix these lists.
 * Note: Words must be spelled out in each form you want ("cake" AND "cakes"); matching is whole-word, any case.
 */
public final class DrinkFilter {

    // ---------------------------------------------------------------------------------------
    // Add words here when the scraper keeps a food item or drops a real drink.
    // ---------------------------------------------------------------------------------------

    /** Words that mean "this is a drink". Multi-word phrases are fine. */
    static final List<String> DRINK_WORDS = List.of(
            "coffee", "coffees", "espresso", "latte", "lattes", "cappuccino", "americano", "mocha", "macchiato",
            "cortado", "cortadito", "cold brew", "brew", "brewed", "tea", "teas", "chai", "matcha", "boba",
            "bubble tea", "milk tea", "lemonade", "lemonades", "juice", "juices", "smoothie", "smoothies",
            "shake", "shakes", "milkshake", "milkshakes", "frappe", "frappes", "frappuccino", "slush", "slushes",
            "slushie", "slushies", "soda", "sodas", "cola", "kola", "malta", "water", "waters", "sparkling",
            "kombucha", "cocoa", "hot chocolate", "horchata", "mojito", "agua fresca", "refresher", "refreshers",
            "cider", "drink", "drinks", "beverage", "beverages", "punch", "cooler", "coolers", "float",
            "freeze", "blended", "frosty", "yerba mate", "beer",
            // added for coffee-shop menus (signature drinks often end in one of these)
            "flat white", "breve", "chiller", "chillers", "milk", "protein", "coke", "fizz", "shaken", "dirty soda");

    /** Words that mean "this is food / not a drink". */
    static final List<String> FOOD_WORDS = List.of(
            "cake", "cakes", "cheesecake", "cheesecakes", "cupcake", "cupcakes", "cookie", "cookies", "muffin",
            "muffins", "croissant", "croissants", "sandwich", "sandwiches", "torta", "tortas", "wrap", "wraps",
            "salad", "salads", "soup", "soups", "bread", "breads", "loaf", "baguette", "baguettes", "batard",
            "roll", "rolls", "bun", "buns", "pastry", "pastries", "tart", "tarts", "tartlet", "tartlets", "pie",
            "pies", "eclair", "eclairs", "\u00e9clair", "\u00e9clairs", "empanada", "empanadas", "croquette",
            "croquettes", "tamale", "tamales", "fries", "chips", "plato", "platos", "breakfast", "brunch",
            "lunch", "dinner", "entree", "entrees", "appetizer", "appetizers", "dessert", "desserts", "sweets",
            "savories", "savory", "pizza", "burger", "burgers", "taco", "tacos", "burrito", "burritos", "bowl",
            "bowls", "panini", "bagel", "bagels", "donut", "donuts", "doughnut", "doughnuts", "scone", "scones",
            "strudel", "strudels", "flan", "tiramisu", "mousse", "brownie", "brownies", "gelato", "ice cream",
            "sundae", "waffle", "waffles", "pancake", "pancakes", "toast", "omelette", "omelet", "quesadilla",
            "quesadillas", "nachos", "sides", "slice", "slices", "shaved ice", "acai", "topping", "toppings",
            "add-on", "add-ons", "gift", "gifts", "gift card", "merch", "merchandise", "salsa", "sauce", "sauces");

    /** Category names that mean "a mixed list", like FEATURED ITEMS. Duplicates in these get filed under the real category. */
    static final List<String> GENERIC_CATEGORY_WORDS = List.of(
            "featured", "new", "popular", "favorites", "favourites", "top", "specials", "special", "seasonal",
            "limited", "recommended", "trending", "best sellers", "bestsellers", "menu", "all", "items");

    /** Alcohol words. An item is dropped if it has one of these (unless it also has a NOT_ALCOHOLIC phrase). */
    static final List<String> ALCOHOL_WORDS = List.of(
            "beer", "beers", "wine", "wines", "sake", "soju", "whiskey", "whisky", "vodka", "rum", "tequila", "gin",
            "bourbon", "brandy", "champagne", "prosecco", "mimosa", "margarita", "margaritas", "cocktail",
            "cocktails", "spiked", "hard seltzer", "sangria", "liqueur", "bloody mary");

    /** If an alcohol word appears WITH one of these, the drink is fine (root beer, "Non-alcoholic", ...). */
    static final List<String> NOT_ALCOHOLIC_PHRASES = List.of(
            "non-alcoholic", "non alcoholic", "nonalcoholic", "alcohol-free", "alcohol free", "virgin", "mocktail",
            "zero proof", "root beer", "ginger beer", "iron beer", "birch beer",
            // "Butter Beer" and descriptions like "rum flavored syrup" are not alcohol
            "butter beer", "butterbeer", "flavor", "flavored", "flavour", "flavoured", "syrup");

    /**
     * what to do with an item in an UNKNOWN category when its name has no drink or food word.
     * true  = keep it (right for drink-only menus full of signature names like "Waikiki" or "The Legend").
     * false = drop it (safer for mixed food + drink menus). Items with a food word are dropped either way.
     */
    static final boolean KEEP_UNKNOWN_ITEMS = true;

    // ---------------------------------------------------------------------------------------
    // The logic Part, don't edit this part
    // ---------------------------------------------------------------------------------------

    enum Kind { DRINK, FOOD, UNKNOWN }

    private static final Pattern DRINK_RE = compile(DRINK_WORDS);
    private static final Pattern FOOD_RE = compile(FOOD_WORDS);
    private static final Pattern GENERIC_RE = compile(GENERIC_CATEGORY_WORDS);
    private static final Pattern ALCOHOL_RE = compile(ALCOHOL_WORDS);
    private static final Pattern NOT_ALCOHOLIC_RE = compile(NOT_ALCOHOLIC_PHRASES);

    /** Builds one whole-word, case-insensitive pattern from a word list. */
    private static Pattern compile(List<String> words) {
        String body = words.stream()
                .map(w -> Arrays.stream(w.split(" ")).map(Pattern::quote).collect(Collectors.joining("\\s+")))
                .collect(Collectors.joining("|"));
        return Pattern.compile("(?iU)(?<![\\p{L}\\p{N}])(?:" + body + ")(?![\\p{L}\\p{N}])");
    }

    static Kind categoryKind(String category) {
        boolean drink = DRINK_RE.matcher(category).find();
        boolean food = FOOD_RE.matcher(category).find();
        return drink && !food ? Kind.DRINK : food && !drink ? Kind.FOOD : Kind.UNKNOWN;
    }

    /** A mixed list like "FEATURED ITEMS" or "Menu" (not a real category such as "Hot Coffee"). */
    static boolean isGenericCategory(String category) {
        return category.isBlank() || (categoryKind(category) == Kind.UNKNOWN && GENERIC_RE.matcher(category).find());
    }

    static Kind itemKind(String name) {
        String last = lastWord(name);
        if (!last.isEmpty()) {
            if (DRINK_RE.matcher(last).find()) return Kind.DRINK;   // "Pumpkin Pie Latte" -> latte
            if (FOOD_RE.matcher(last).find()) return Kind.FOOD;     // "Cinnamon Coffee Cake" -> cake
        }
        boolean drink = DRINK_RE.matcher(name).find();
        boolean food = FOOD_RE.matcher(name).find();
        return drink && !food ? Kind.DRINK : food && !drink ? Kind.FOOD : Kind.UNKNOWN;
    }

    /** Last word of the name, ignoring anything in (parentheses) and symbols like the registered mark. */
    private static String lastWord(String name) {
        String n = name.replaceAll("\\([^)]*\\)", " ").replaceAll("[^\\p{L}\\s'-]", " ").trim();
        if (n.isEmpty()) return "";
        String[] parts = n.split("\\s+");
        return parts[parts.length - 1].toLowerCase(Locale.ROOT);
    }

    static boolean isAlcoholic(Drink d) {
        String text = d.name + " " + d.description;
        return ALCOHOL_RE.matcher(text).find() && !NOT_ALCOHOLIC_RE.matcher(text).find();
    }

    /** True if this looks like a non-alcoholic drink. */
    static boolean keep(Drink d) {
        Kind category = categoryKind(d.category);
        Kind item = itemKind(d.name);
        boolean drink = switch (category) {
            case DRINK -> item != Kind.FOOD;
            case FOOD -> item == Kind.DRINK;
            default -> item == Kind.DRINK || (item == Kind.UNKNOWN && KEEP_UNKNOWN_ITEMS);
        };
        return drink && !isAlcoholic(d);
    }
}