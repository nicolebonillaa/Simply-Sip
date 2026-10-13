package simplysip;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * General drink-menu scraper. You give it a cafe name and a menu URL; it finds the drinks,
 * groups them by category, and lists sizes, prices, nutrition facts and allergens.
 *
 *   Interactive:  mvn -q compile exec:java            (it asks for the name and URL)
 *   With options: mvn -q compile exec:java "-Dexec.args=\"Cafe Name\" https://cafe.com/menu 5"
 *                 (the optional last number = only look up the first N drinks; good for testing)

 * Jsoup reads the page's plain HTML. If that has no drinks (many menus are built by JavaScript), the
 * scraper automatically retries with a real browser (Playwright, see BrowserLoader.java). If even that
 * finds nothing, it tells you why.
 */
public class Main {

    /** put YOUR real contact info here. Websites see it, so the owner can contact you instead of blocking you. */
    static final String USER_AGENT = "pdanielle2023@gmail.com";
    static final long DELAY_MS = 1000;

    /** visit each drink's own page (if it has one) to collect sizes, nutrition and allergens. */
    static final boolean FOLLOW_DRINK_LINKS = true;

    /** stop if the site's robots.txt asks bots to stay away from the menu path. */
    static final boolean RESPECT_ROBOTS_TXT = true;

    /**
     * Playwright activation:
     * plain Jsoup first; if it finds no drinks, retry with the browser
     */
    enum BrowserMode { NEVER, AUTO, ALWAYS }
    static final BrowserMode BROWSER_MODE = BrowserMode.AUTO;

    private static boolean usingBrowser = false;
    private static BrowserLoader browser = null;

    public static void main(String[] args) throws Exception {
        try {
            run(args);
        } finally {
            if (browser != null) browser.close();                // always shut the browser down
        }
    }

    static void run(String[] args) throws Exception {
        String cafe, url;
        int limit = Integer.MAX_VALUE;
        if (args.length >= 2) {
            cafe = args[0]; url = args[1];
            if (args.length > 2) limit = Integer.parseInt(args[2]);
        } else {
            Scanner in = new Scanner(System.in);
            System.out.print("Cafe name: ");
            cafe = in.nextLine().trim();
            System.out.print("Menu URL (copy it from your browser): ");
            url = in.nextLine().trim();
            System.out.print("Only look up the first N drinks? (press Enter for all): ");
            String n = in.nextLine().trim();
            if (!n.isEmpty()) limit = Integer.parseInt(n);
        }

        if (RESPECT_ROBOTS_TXT && !robotsAllows(url)) {
            System.out.println("\nSTOPPED: this site's robots.txt asks bots not to visit that page, so the scraper won't.");
            System.out.println("(To override on purpose, set RESPECT_ROBOTS_TXT = false, but get the owner's permission first.)");
            return;
        }

        // ---- 1-3. download the page, find the drinks, keep only drinks ----
        usingBrowser = (BROWSER_MODE == BrowserMode.ALWAYS);
        Collected r;
        try {
            r = collect(url);
        } catch (HttpStatusException e) {
            System.out.println("\nCOULD NOT LOAD THE PAGE: the site answered with HTTP " + e.getStatusCode() + ".");
            if (e.getStatusCode() == 403 || e.getStatusCode() == 429)
                System.out.println("The site is blocking automated visitors (or you're going too fast). Try again later or use a different source.");
            return;
        } catch (IOException e) {
            System.out.println("\nCOULD NOT LOAD THE PAGE: " + e.getMessage()
                    + "\nCheck the URL and your internet connection" + (usingBrowser ? " (and that Playwright's browser installed, see README)." : "."));
            return;
        }

        // Nothing in the plain HTML? Many menus are built by JavaScript, so retry with a real browser.
        if (r.found().isEmpty() && r.skipped().isEmpty() && BROWSER_MODE == BrowserMode.AUTO && !usingBrowser) {
            String why = MenuFinder.javaScriptReason(r.doc());
            System.out.println("\nNo drinks in the page's plain HTML" + (why != null ? " (" + why + ")" : "") + ".");
            System.out.println("Retrying with a real browser (Playwright) that runs the page's JavaScript.");
            System.out.println("(The first time, it downloads Chromium, about 150 MB. This can take a minute.)");
            usingBrowser = true;
            try {
                r = collect(url);
            } catch (IOException e) {
                System.out.println("\nCOULD NOT USE THE BROWSER: " + e.getMessage());
                System.out.println("Playwright needs an internet connection the first time (to download Chromium). See the README.");
                return;
            }
        }
        Document doc = r.doc();
        Map<String, Drink> found = r.found();
        List<Drink> skipped = r.skipped();
        String strategy = r.strategy();

        // ----nothing left? explain why ----
        if (found.isEmpty()) {
            String js = MenuFinder.javaScriptReason(doc);
            System.out.println();
            if (!skipped.isEmpty()) {
                System.out.println("FOUND " + skipped.size() + " ITEMS, BUT NONE LOOKED LIKE DRINKS.");
                printSkipped(skipped);
            } else if (usingBrowser) {
                System.out.println("COULD NOT FIND DRINKS, EVEN WITH A REAL BROWSER.");
                System.out.println("The page was opened in Chromium and its JavaScript ran, but no drink menu was recognized.");
                System.out.println("Common reasons:");
                System.out.println("  1. The menu only appears after a click (a category tab, a \"Load more\" button, or choosing a pickup location).");
                System.out.println("     This scraper scrolls the page but does not click anything.");
                System.out.println("  2. The menu is behind a login or ordering flow, or it is an image or a PDF.");
                System.out.println("  3. The site blocks automated browsers. This scraper does not try to get around that.");
                System.out.println("  4. The layout is one the menu finder doesn't recognize.");
                System.out.println("Try a more specific menu URL (e.g. a single category page). To watch what the browser sees,");
                System.out.println("set HEADLESS = false in BrowserLoader.java.");
                System.out.println("The browser also saved what it saw in the output/ folder: debug-page-1.html (the rendered page)");
                System.out.println("and debug-json-*.json (data the page downloaded). Search them for a drink name to see where the menu is.");
            } else if (js != null) {
                System.out.println("COULD NOT SCRAPE: this menu is loaded with JavaScript.");
                System.out.println("Why I think so: " + js + ".");
                System.out.println("Jsoup only reads the HTML the server sends, and here the drinks are added later by JavaScript.");
                System.out.println("What you can try:");
                System.out.println("  1. In Chrome press F12 > Network > Fetch/XHR, reload the menu, and look for a request that returns the menu as JSON.");
                System.out.println("     If you find one, paste that URL here instead (Jsoup can download JSON with .ignoreContentType(true)).");
                System.out.println("  2. Look for the same menu in a plain-HTML place: the cafe's own site, a PDF menu, or a nutrition page.");
                System.out.println("  3. Set BROWSER_MODE to AUTO or ALWAYS in Main.java so the scraper uses a real browser (Playwright) that runs the JavaScript.");
            } else {
                System.out.println("COULD NOT FIND DRINKS: the page loaded and does not look JavaScript-built, but I couldn't recognize a menu layout.");
                System.out.println("Check that the URL is the actual menu page (not the home page) and that the drink names appear after Ctrl+U (view source).");
                System.out.println("If they do, this site needs custom selectors; send me the HTML of one drink.");
            }
            return;
        }

        // ---- trim for testing, then look up each drink's details ----
        List<Drink> drinks = new ArrayList<>(found.values());
        if (drinks.size() > limit) drinks = new ArrayList<>(drinks.subList(0, limit));
        if (FOLLOW_DRINK_LINKS) {
            String host = URI.create(url).getHost();
            int n = 0;
            for (Drink d : drinks) {
                n++;
                if (d.url.isEmpty() || d.hasNutrition() || !host.equals(URI.create(d.url).getHost())) continue;
                System.out.printf("Reading details %d/%d: %s%n", n, drinks.size(), d.name);
                try { Extractors.mergeDetail(fetch(d.url).body(), d); }
                catch (IOException e) { System.out.println("  skipped: " + e.getMessage()); }
                Thread.sleep(DELAY_MS);
            }
        }

        // ---- print grouped by category, and save a CSV ----
        Map<String, List<Drink>> byCategory = new LinkedHashMap<>();
        for (Drink d : drinks) byCategory.computeIfAbsent(d.category, k -> new ArrayList<>()).add(d);
        if (!skipped.isEmpty()) printSkipped(skipped);
        System.out.printf("%n%s - %d drinks in %d categories (found using: %s)%n", cafe, drinks.size(), byCategory.size(), strategy + (usingBrowser ? ", page rendered in a browser" : ""));
        for (Map.Entry<String, List<Drink>> e : byCategory.entrySet()) {
            System.out.printf("%n==================== %s (%d) ====================%n", e.getKey(), e.getValue().size());
            for (Drink d : e.getValue()) print(d);
        }
        Path file = Path.of("output", cafe.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "") + ".csv");
        writeCsv(cafe, drinks, file);
        System.out.println("\nSaved " + file);
    }

    /** What one scrape pass produced. */
    record Collected(Document doc, Map<String, Drink> found, List<Drink> skipped, String strategy) {}

    /** Downloads the menu page, finds category pages and drinks, and splits them into kept drinks and skipped items. */
    static Collected collect(String url) throws IOException, InterruptedException {
        Document doc = fetch(url);
        Map<String, Drink> found = new LinkedHashMap<>();
        String strategy = "none";
        Map<String, String> categoryPages = MenuFinder.categoryLinks(doc);
        if (!categoryPages.isEmpty()) System.out.println("Found " + categoryPages.size() + " category pages.");
        for (Map.Entry<String, String> c : categoryPages.entrySet()) {
            String label = MenuFinder.cleanCategory(c.getValue());
            if (DrinkFilter.categoryKind(label) == DrinkFilter.Kind.FOOD) { System.out.println("  skipping food category: " + label); continue; }
            try {
                MenuFinder.Found f = MenuFinder.find(fetch(c.getKey()));
                if (!f.drinks().isEmpty()) strategy = f.strategy();
                for (Drink d : f.drinks()) { d.category = label; add(found, d); }
                System.out.printf("  %s: %d items%n", label, f.drinks().size());
            } catch (IOException e) {
                System.out.println("  could not load category " + label + ": " + e.getMessage());
            }
            Thread.sleep(DELAY_MS);
        }
        MenuFinder.Found base = MenuFinder.find(doc);
        if (!base.drinks().isEmpty() && strategy.equals("none")) strategy = base.strategy();
        for (Drink d : base.drinks()) add(found, d);

        // Nothing in the rendered HTML? Ordering sites (Square, Toast, ...) often load the menu as JSON, so look there.
        if (found.isEmpty() && usingBrowser && browser != null) {
            List<Drink> fromJson = MenuFinder.fromJson(browser.capturedJson());
            if (!fromJson.isEmpty()) {
                strategy = "JSON the page downloaded";
                System.out.println("No menu in the rendered HTML, but found " + fromJson.size() + " items in JSON the page downloaded.");
                for (Drink d : fromJson) add(found, d);
            }
        }

        // keep only drinks: drop food and alcohol using the word lists in DrinkFilter.java
        List<Drink> skipped = new ArrayList<>();
        found.values().removeIf(d -> { if (DrinkFilter.keep(d)) return false; skipped.add(d); return true; });
        return new Collected(doc, found, skipped, strategy);
    }

    /** Adds a drink unless we already have it and file it under its real category. */
    static void add(Map<String, Drink> found, Drink d) {
        String key = d.name.toLowerCase(Locale.ROOT);
        Drink old = found.get(key);
        if (old == null) found.put(key, d);
        else if (DrinkFilter.isGenericCategory(old.category) && !DrinkFilter.isGenericCategory(d.category)) old.category = d.category;
    }

    /** Shows what was dropped so you can spot mistakes and edit the word lists in DrinkFilter.java. */
    static void printSkipped(List<Drink> skipped) {
        Map<String, List<String>> byCategory = new LinkedHashMap<>();
        for (Drink d : skipped) byCategory.computeIfAbsent(d.category, k -> new ArrayList<>()).add(d.name);
        System.out.printf("%nSkipped %d items that don't look like non-alcoholic drinks:%n", skipped.size());
        for (Map.Entry<String, List<String>> e : byCategory.entrySet()) {
            if (DrinkFilter.categoryKind(e.getKey()) == DrinkFilter.Kind.FOOD)
                System.out.printf("  %s: %d items (food category)%n", e.getKey(), e.getValue().size());
            else                                                  // mixed/unknown category: list the names so you can check them
                System.out.printf("  %s: %s%n", e.getKey(), String.join(", ", e.getValue()));
        }
        System.out.println("  (Something wrong? Add words to the lists at the top of DrinkFilter.java.)");
    }

    /** Prints one drink: description, allergens, then each size with its price and nutrition. */
    static void print(Drink d) {
        System.out.println("\n" + d.name);
        if (!d.description.isEmpty()) System.out.println("  Description: " + d.description);
        System.out.println("  Allergens:   " + (d.allergens.isEmpty() ? "none listed" : String.join(", ", d.allergens)));
        System.out.println("  Sizes:");
        if (d.sizes.isEmpty()) System.out.println("    not found on the menu");
        for (Drink.Size s : d.sizes) {
            String size = s.ounces().isEmpty() ? s.label() : s.label() + " (" + s.ounces() + " oz)";
            String price = s.price().isEmpty() ? "not listed" : "$" + s.price();
            System.out.printf("    %-22s price: %-11s %s%n", size, price, nutritionText(s.nutrition()));
        }
    }

    static String nutritionText(Map<String, String> nut) {
        if (nut.isEmpty()) return "nutrition: not listed";
        List<String> parts = new ArrayList<>();
        for (Extractors.Nutrient n : Extractors.NUTRIENTS) {
            String v = nut.get(n.label());
            if (v == null) continue;
            parts.add(n.label().equals("Calories") ? v + " cal" : n.label() + " " + v + n.unit());
        }
        return String.join(" | ", parts);
    }

    /** Downloads a page: with the real browser when usingBrowser is on, otherwise with plain Jsoup. */
    static Document fetch(String url) throws IOException {
        if (usingBrowser) {
            if (browser == null) browser = new BrowserLoader();
            return browser.load(url);
        }
        return Jsoup.connect(url).userAgent(USER_AGENT).timeout(15_000).get();
    }

    /** Very simple robots.txt check: only reads the "User-agent: *" Disallow lines. */
    static boolean robotsAllows(String url) {
        try {
            URI u = URI.create(url);
            String robots = Jsoup.connect(u.getScheme() + "://" + u.getHost() + "/robots.txt")
                    .userAgent(USER_AGENT).ignoreContentType(true).timeout(10_000).execute().body();
            String path = (u.getPath() == null || u.getPath().isEmpty()) ? "/" : u.getPath();
            boolean applies = false;
            for (String line : robots.split("\\R")) {
                line = line.replaceAll("#.*", "").trim();
                String lower = line.toLowerCase(Locale.ROOT);
                if (lower.startsWith("user-agent:")) applies = line.substring(11).trim().equals("*");
                else if (applies && lower.startsWith("disallow:")) {
                    String rule = line.substring(9).trim();
                    if (!rule.isEmpty() && path.startsWith(rule)) return false;
                }
            }
        } catch (Exception e) { /* no robots.txt, or it couldn't be read: assume allowed */ }
        return true;
    }

    static void writeCsv(String cafe, List<Drink> drinks, Path file) throws IOException {
        Files.createDirectories(file.getParent());
        StringBuilder sb = new StringBuilder("cafe,category,drink,size,ounces,price_usd");
        for (Extractors.Nutrient n : Extractors.NUTRIENTS) sb.append(',').append(n.column());
        sb.append(",allergens,description,url\n");
        for (Drink d : drinks) {
            List<Drink.Size> sizes = d.sizes.isEmpty()
                    ? List.of(new Drink.Size("", "", "", new LinkedHashMap<>())) : d.sizes;
            for (Drink.Size s : sizes) {
                List<String> f = new ArrayList<>(List.of(cafe, d.category, d.name, s.label(), s.ounces(), s.price()));
                for (Extractors.Nutrient n : Extractors.NUTRIENTS) f.add(s.nutrition().getOrDefault(n.label(), ""));
                f.addAll(List.of(String.join("; ", d.allergens), d.description, d.url));
                for (int i = 0; i < f.size(); i++)
                    sb.append(i > 0 ? "," : "").append('"').append(f.get(i).replace("\"", "\"\"")).append('"');
                sb.append('\n');
            }
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }
}