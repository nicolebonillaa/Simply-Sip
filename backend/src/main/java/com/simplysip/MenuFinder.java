package simplysip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Three strategies:
 *   1. JSON-LD    - menus that embed structured data (schema.org MenuItem) for Google
 *   2. Prices     - a repeating block of HTML where each block contains a $ price
 *   3. Link cards - a group of links that all point to drink pages (e.g. detail.php?prod_id=123)
 * It can also discover category pages.
 */
public final class MenuFinder {

    /** URL parameter names that mean "this link switches category", e.g. category_id, cat, section. */
    static final Pattern CATEGORY_PARAM = Pattern.compile(
            "(?i)^(?:cat|cats|category|categories|category_?id|cat_?id|section|section_?id|collection|collection_?id|type|group)$");
    // NOTE: the whole parameter name must match.

    record Found(List<Drink> drinks, String strategy) {}

    static Found find(Document doc) {
        List<Drink> d = fromJsonLd(doc);
        if (!d.isEmpty()) return new Found(d, "structured data (JSON-LD)");
        d = fromPrices(doc);
        if (!d.isEmpty()) return new Found(d, "repeating menu items with prices");
        d = fromLinkCards(doc);
        if (!d.isEmpty()) return new Found(d, "repeating links to drink pages");
        return new Found(new ArrayList<>(), "none");
    }

    // ------------------------------------------------------------------
    // Strategy 1: JSON-LD
    // ------------------------------------------------------------------
    static List<Drink> fromJsonLd(Document doc) {
        List<Drink> out = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (Element script : doc.select("script[type=application/ld+json]")) {
            try { walk(mapper.readTree(script.data()), "Menu", out); } catch (Exception ignored) { /* not valid JSON: skip */ }
        }
        return out;
    }

    private static void walk(JsonNode n, String category, List<Drink> out) {
        if (n.isArray()) { for (JsonNode c : n) walk(c, category, out); return; }
        if (!n.isObject()) return;
        String type = n.has("@type") ? n.get("@type").toString() : "";
        if (type.contains("MenuItem")) { out.add(toDrink(n, category)); return; }
        if (type.contains("MenuSection") && n.hasNonNull("name")) category = n.get("name").asText();
        Iterator<JsonNode> it = n.elements();
        while (it.hasNext()) walk(it.next(), category, out);
    }

    private static Drink toDrink(JsonNode n, String category) {
        Drink d = new Drink();
        d.category = category;
        d.name = n.path("name").asText("");
        d.description = n.path("description").asText("");
        Map<String, String> nut = new LinkedHashMap<>();
        JsonNode nu = n.path("nutrition");
        String[][] map = {{"calories", "Calories"}, {"fatContent", "Total Fat"}, {"cholesterolContent", "Cholesterol"},
                {"sodiumContent", "Sodium"}, {"carbohydrateContent", "Total Carbohydrate"}, {"sugarContent", "Sugars"},
                {"proteinContent", "Protein"}};
        for (String[] f : map) {
            Matcher m = Pattern.compile("\\d+(?:\\.\\d+)?").matcher(nu.path(f[0]).asText(""));
            if (m.find()) nut.put(f[1], m.group());
        }
        JsonNode offers = n.path("offers");
        List<JsonNode> offerList = new ArrayList<>();
        if (offers.isArray()) offers.forEach(offerList::add); else if (offers.isObject()) offerList.add(offers);
        for (JsonNode o : offerList) {
            String price = o.hasNonNull("price") ? o.get("price").asText() : o.path("lowPrice").asText("");
            d.sizes.add(new Drink.Size(o.path("name").asText("Standard"), "", price, new LinkedHashMap<>()));
        }
        if (d.sizes.isEmpty()) d.sizes.add(new Drink.Size("Standard", "", "", new LinkedHashMap<>()));
        if (d.sizes.size() == 1) d.sizes.set(0, new Drink.Size(d.sizes.get(0).label(), "", d.sizes.get(0).price(), nut));
        return d;
    }

    // ------------------------------------------------------------------
    // Fallback (browser mode only): JSON the page downloaded while it was loading
    // ------------------------------------------------------------------
    /**
     * Many ordering sites (Square, Toast, ...) fetch their menu as JSON and draw it with JavaScript.
     * This looks through those JSON responses for objects that have a "name" and a price field,
     * and files each one under the closest named object above it.
     */
    static List<Drink> fromJson(List<String> jsonBodies) {
        ObjectMapper mapper = new ObjectMapper();
        List<Drink> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String body : jsonBodies) {
            try { walkJson(mapper.readTree(body), "Menu", out, seen); } catch (Exception ignored) { /* not valid JSON: skip */ }
        }
        return out;
    }

    private static void walkJson(JsonNode n, String category, List<Drink> out, Set<String> seen) {
        if (n.isArray()) { for (JsonNode c : n) walkJson(c, category, out, seen); return; }
        if (!n.isObject()) return;
        String name = n.path("name").isTextual() ? n.get("name").asText().trim() : "";
        String price = name.isEmpty() ? "" : findPrice(n);
        if (!name.isEmpty() && !price.isEmpty()) {                      // looks like a menu item
            if (seen.add(name.toLowerCase(Locale.ROOT))) {
                Drink d = new Drink();
                d.category = category;
                d.name = clean(name);
                String desc = n.path("description").isTextual() ? n.get("description").asText() : "";
                d.description = desc.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
                d.sizes.add(new Drink.Size("Standard", "", price, new LinkedHashMap<>()));
                out.add(d);
            }
            return;
        }
        String next = name.isEmpty() ? category : name;                 // a named object without a price may be a category
        Iterator<JsonNode> it = n.elements();
        while (it.hasNext()) walkJson(it.next(), next, out, seen);
    }

    /** Returns a price like "5.50" from a field whose name contains "price" or "cost", or "" if there isn't one. */
    private static String findPrice(JsonNode n) {
        Iterator<Map.Entry<String, JsonNode>> it = n.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> e = it.next();
            String key = e.getKey().toLowerCase(Locale.ROOT);
            if (!key.contains("price") && !key.contains("cost")) continue;
            String p = priceText(e.getValue(), key.contains("cent") || key.contains("money"));
            if (!p.isEmpty()) return p;
        }
        return "";
    }

    /** Turns 5.5, "5.50", "$5.50" or {"amount":550,"currency":"USD"} into "5.50". Whole numbers in money objects are cents. */
    private static String priceText(JsonNode v, boolean inCents) {
        if (v.isNumber()) {
            java.math.BigDecimal b = v.decimalValue();
            if (inCents && v.isIntegralNumber()) b = b.movePointLeft(2);
            return b.signum() > 0 ? b.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "";
        }
        if (v.isTextual()) {
            Matcher m = Pattern.compile("\\d+(?:\\.\\d+)?").matcher(v.asText());
            if (!m.find()) return "";
            java.math.BigDecimal b = new java.math.BigDecimal(m.group());
            return b.signum() > 0 ? b.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "";
        }
        if (v.isObject()) {
            boolean money = inCents || v.has("currency") || v.has("currency_code") || v.has("currencyCode");
            for (String f : new String[]{"formatted", "display", "value", "amount", "cents", "regular", "low"}) {
                if (v.has(f)) {
                    String p = priceText(v.get(f), money && !f.equals("value") && !f.equals("formatted") && !f.equals("display"));
                    if (!p.isEmpty()) return p;
                }
            }
        }
        return "";
    }

    // ------------------------------------------------------------------
    // Strategy 2: repeating blocks that contain a price
    // ------------------------------------------------------------------
    static List<Drink> fromPrices(Document doc) {
        Map<Element, Boolean> chosen = new IdentityHashMap<>();
        List<Element> items = new ArrayList<>();
        for (Element e : doc.body().getAllElements()) {
            if (!Extractors.PRICE.matcher(e.ownText()).find()) continue;   // only elements that directly hold a "$5.50"
            Element item = itemAbove(e);
            if (item != null && chosen.putIfAbsent(item, true) == null) items.add(item);
        }
        if (items.isEmpty()) return new ArrayList<>();

        // remember document order so each item can be filed under the closest heading above it
        Map<Element, Integer> order = new IdentityHashMap<>();
        int i = 0;
        for (Element e : doc.body().getAllElements()) order.put(e, i++);
        List<Element> headings = new ArrayList<>(doc.body().select("h1, h2, h3, h4, h5, h6"));

        List<Drink> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Element item : items) {
            Drink d = new Drink();
            d.name = nameOf(item);
            if (d.name.isEmpty() || !seen.add(d.name.toLowerCase())) continue;
            int idx = order.get(item);
            for (Element h : headings) if (order.get(h) < idx && !h.text().isBlank()) d.category = cleanCategory(h.text());
            Element link = item.selectFirst("a[href]");
            if (link != null && !link.absUrl("href").contains("#")) d.url = link.absUrl("href");
            Extractors.parse(item, d);
            if (d.description.isEmpty()) d.description = fallbackDescription(item, d.name);
            out.add(d);
        }
        return out;
    }

    /** Climb from a price up to the block that repeats (>= 3 similar siblings, each with a price). */
    private static Element itemAbove(Element price) {
        Element a = price;
        for (int depth = 0; depth < 6 && a != null && a.parent() != null; depth++, a = a.parent()) {
            if (!hasNameText(a)) continue;
            int similar = 0;
            for (Element sib : a.parent().children())
                if (sig(sib).equals(sig(a)) && Extractors.PRICE.matcher(sib.text()).find()) similar++;
            if (similar >= 3) return a;
        }
        return null;
    }

    private static String sig(Element e) {
        return e.tagName() + "." + e.classNames().stream().sorted().collect(Collectors.joining("."));
    }

    /** True if the block has real words left after removing prices and size words (i.e. a drink name). */
    private static boolean hasNameText(Element a) {
        String t = Extractors.PRICE.matcher(a.text()).replaceAll(" ");
        t = t.replaceAll("(?i)\\b(" + Extractors.SIZE_WORDS + "|\\d+\\s*oz)\\b", " ");
        return t.replaceAll("[^A-Za-z]", "").length() >= 3;
    }

    private static String nameOf(Element item) {
        Element n = item.selectFirst("h1, h2, h3, h4, h5, h6, [itemprop=name], [class*=name], [class*=title]");
        if (n != null) {
            String t = cutAtPrice(n.text());
            if (t.length() >= 2) return clean(t);
        }
        for (Element e : item.getAllElements()) {
            if (e == item) continue;
            String t = cutAtPrice(e.ownText().trim());
            if (t.length() >= 3) return clean(t);
        }
        return "";
    }

    /** "Americano 12 fl oz $2.89 (each)" -> "Americano" */
    private static String cutAtPrice(String s) {
        return s.replaceFirst("(?i)\\s*(?:\\$|\\d{1,2}\\s*(?:fl\\.?\\s*)?oz\\b).*$", "").trim();
    }

    /** Menus often put the description in a plain paragraph; take the longest text in the item that isn't the name or a price. */
    private static String fallbackDescription(Element item, String name) {
        String best = "";
        for (Element e : item.getAllElements()) {
            String t = e.ownText().trim();
            if (t.length() > best.length() && t.length() >= 25 && !t.contains("$") && !t.startsWith(name)) best = t;
        }
        return best.length() > 500 ? best.substring(0, 500) : best;
    }

    // ------------------------------------------------------------------
    // Strategy 3: a group of links that all look the same (each one a drink page)
    // ------------------------------------------------------------------
    static List<Drink> fromLinkCards(Document doc) {
        String host = host(pageUrl(doc));
        Map<String, Map<String, Element>> groups = new LinkedHashMap<>();
        for (Element a : doc.body().select("a[href]")) {
            if (a.closest("nav, header, footer") != null) continue;        // ignore site navigation
            String href = a.absUrl("href");
            if (href.isEmpty() || href.contains("#") || href.startsWith("mailto:") || href.startsWith("tel:")) continue;
            URI u;
            try { u = new URI(href); } catch (Exception e) { continue; }
            if (u.getHost() == null || !u.getHost().equals(host)) continue;
            String sig = linkSignature(u);
            if (sig == null) continue;
            groups.computeIfAbsent(sig, k -> new LinkedHashMap<>()).putIfAbsent(href, a);
        }
        Map<String, Element> best = new LinkedHashMap<>();
        for (Map<String, Element> g : groups.values()) if (g.size() > best.size()) best = g;
        List<Drink> out = new ArrayList<>();
        if (best.size() < 4) return out;
        Set<String> seen = new HashSet<>();
        for (Map.Entry<String, Element> e : best.entrySet()) {
            Element a = e.getValue();
            String name = clean(a.text());
            if (!name.matches(".*[A-Za-z].*")) {                            // link has only an image: use its alt text
                Element img = a.selectFirst("img[alt]");
                if (img != null) name = clean(img.attr("alt").replaceFirst("^A Photo of an? ", ""));
            }
            if (name.isEmpty() || !seen.add(name.toLowerCase())) continue;
            Drink d = new Drink();
            d.name = name;
            d.url = e.getKey();
            out.add(d);
        }
        return out;
    }

    /** Links that only differ by an id (detail.php?prod_id=1, ?prod_id=2 or /menu/thai-tea, /menu/matcha) share a signature. */
    private static String linkSignature(URI u) {
        String path = u.getPath() == null ? "" : u.getPath();
        String q = u.getQuery();
        if (q != null && !q.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (String kv : q.split("&")) names.add(kv.split("=", 2)[0]);
            Collections.sort(names);
            for (String n : names) if (CATEGORY_PARAM.matcher(n).find()) return null;   // category switchers aren't drinks
            return path + "?" + String.join("&", names);
        }
        if (path.split("/").length < 3) return null;                       // "/about" style top-level links aren't drinks
        return path.replaceAll("[^/]*/?$", "*");
    }

    // ------------------------------------------------------------------
    // Category pages, e.g. menu?category_id=11
    // ------------------------------------------------------------------
    /** Returns url -> category label for links that stay on the same page path but switch a category parameter. */
    static Map<String, String> categoryLinks(Document doc) {
        Map<String, String> out = new LinkedHashMap<>();
        String host = host(pageUrl(doc));
        String path = URI.create(pageUrl(doc)).getPath();
        for (Element a : doc.body().select("a[href]")) {
            String href = a.absUrl("href");
            URI u;
            try { u = new URI(href); } catch (Exception e) { continue; }
            if (u.getHost() == null || !u.getHost().equals(host) || u.getQuery() == null) continue;
            if (!Objects.equals(u.getPath(), path)) continue;
            boolean isCategory = false;
            for (String kv : u.getQuery().split("&")) if (CATEGORY_PARAM.matcher(kv.split("=", 2)[0]).find()) isCategory = true;
            String label = a.text().trim();
            if (isCategory && !label.isEmpty() && label.length() <= 40) out.putIfAbsent(href, label);
        }
        return out.size() >= 2 ? out : new LinkedHashMap<>();
    }

    // ------------------------------------------------------------------
    // "Is this page built by JavaScript?"
    // ------------------------------------------------------------------
    /** Returns a reason if the page looks JavaScript-rendered (drinks not in the HTML), otherwise null. */
    static String javaScriptReason(Document doc) {
        int textLen = doc.body().text().length();
        for (String id : new String[]{"root", "__next", "app", "__nuxt"}) {
            Element r = doc.getElementById(id);
            if (r != null && r.text().length() < 40) return "the page has an empty <div id=\"" + id + "\"> that JavaScript fills in later";
        }
        if (textLen < 300) return "the page contains almost no text (" + textLen + " characters)";
        boolean noscript = doc.select("noscript").text().toLowerCase(Locale.ROOT).contains("javascript");
        if (noscript && textLen < 1500) return "the page asks you to enable JavaScript";
        if (doc.select("script[src]").size() >= 8 && textLen < 1500) return "the page is mostly script files with very little text";
        return null;
    }

    // ------------------------------------------------------------------
    /** The page's own URL. (Pages loaded by the browser are parsed from a string, so we fall back to baseUri.) */
    static String pageUrl(Document doc) {
        String l = doc.location();
        return (l == null || l.isEmpty()) ? doc.baseUri() : l;
    }

    private static String host(String url) {
        try { return URI.create(url).getHost(); } catch (Exception e) { return ""; }
    }

    /** Tidies a drink name: removes badges like "TOP!", and turns "TOPO CHICO WATER" into "Topo Chico Water". */
    static String clean(String raw) {
        String s = raw.replaceAll("(?i)^\\s*top!\\s*", "").replace("Product background", "").replaceAll("\\s+", " ").trim();
        if (s.length() > 2 && s.equals(s.toUpperCase(Locale.ROOT)) && s.matches(".*[A-Z].*")) s = Extractors.titleCase(s);
        return s;
    }

    /** "NEW! Handcrafted Drinks" -> "Handcrafted Drinks" */
    static String cleanCategory(String raw) {
        return clean(raw.replaceAll("(?i)\\b(?:all\\s+)?new!\\s*", ""));
    }
}