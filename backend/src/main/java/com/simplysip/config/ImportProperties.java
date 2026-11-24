package com.simplysip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "app.import")
public class ImportProperties {

    private boolean enabled = false;
    /** Classpath folder containing scraped CSV files (no leading slash). */
    private String folder = "import/scraped";
    private Map<String, String> cafeNameMap = defaultCafeMap();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public Map<String, String> getCafeNameMap() {
        return cafeNameMap;
    }

    public void setCafeNameMap(Map<String, String> cafeNameMap) {
        this.cafeNameMap = cafeNameMap;
    }

    public String canonicalCafeName(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return cafeNameMap.getOrDefault(trimmed, cafeNameMap.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(trimmed))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(trimmed));
    }

    private static Map<String, String> defaultCafeMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Qwench Juice Bar", "Qwench");
        map.put("Qwench", "Qwench");
        map.put("It's Boba Time", "It's Boba Time");
        map.put("Its Boba Time", "It's Boba Time");
        map.put("Boba Time", "It's Boba Time");
        return map;
    }
}
