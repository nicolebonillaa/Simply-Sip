package com.simplysip.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplysip.config.GeocodeProperties;
import com.simplysip.model.Location;
import com.simplysip.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NominatimGeocodeService {

    private static final Logger log = LoggerFactory.getLogger(NominatimGeocodeService.class);
    private static final String NOMINATIM_SEARCH = "https://nominatim.openstreetmap.org/search";

    private final LocationRepository locationRepository;
    private final GeocodeProperties geocodeProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /** In-memory cache: normalized address → [lat, lng] or empty array if not found. */
    private final Map<String, double[]> cache = new HashMap<>();

    private long lastRequestAtMs = 0L;

    public NominatimGeocodeService(
            LocationRepository locationRepository,
            GeocodeProperties geocodeProperties,
            ObjectMapper objectMapper) {
        this.locationRepository = locationRepository;
        this.geocodeProperties = geocodeProperties;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public GeocodeSummary fillMissingCoordinates() {
        GeocodeSummary summary = new GeocodeSummary();
        List<Location> locations = locationRepository.findAll();
        for (Location location : locations) {
            if (location.getLatitude() != null && location.getLongitude() != null) {
                summary.skippedAlreadyFilled++;
                continue;
            }
            String address = formatAddress(location);
            if (address == null) {
                summary.skippedNoAddress++;
                log.warn("Geocode skip '{}': missing street/city address", location.getCafeName());
                continue;
            }
            try {
                double[] coords = lookup(address);
                if (coords == null) {
                    summary.notFound++;
                    log.warn("Geocode not found for '{}': {}", location.getCafeName(), address);
                    continue;
                }
                // Never overwrite coordinates already filled in (re-check after lookup).
                if (location.getLatitude() != null && location.getLongitude() != null) {
                    summary.skippedAlreadyFilled++;
                    continue;
                }
                location.setLatitude(coords[0]);
                location.setLongitude(coords[1]);
                locationRepository.save(location);
                summary.updated++;
                String mapUrl = String.format(
                        "https://www.openstreetmap.org/?mlat=%s&mlon=%s#map=18/%s/%s",
                        coords[0], coords[1], coords[0], coords[1]);
                log.info(
                        "Geocoded '{}' -> lat={}, lng={} | verify: {}",
                        location.getCafeName(),
                        coords[0],
                        coords[1],
                        mapUrl);
            } catch (Exception ex) {
                summary.errors++;
                log.error("Geocode failed for '{}': {}", location.getCafeName(), ex.getMessage());
            }
        }
        log.info(
                "Geocode summary: updated={}, skippedAlreadyFilled={}, skippedNoAddress={}, notFound={}, errors={}",
                summary.updated,
                summary.skippedAlreadyFilled,
                summary.skippedNoAddress,
                summary.notFound,
                summary.errors);
        return summary;
    }

    private double[] lookup(String address) throws Exception {
        String cacheKey = address.toLowerCase();
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        throttle();
        URI uri = UriComponentsBuilder.fromUriString(NOMINATIM_SEARCH)
                .queryParam("q", address)
                .queryParam("format", "json")
                .queryParam("limit", "1")
                .build()
                .encode()
                .toUri();
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", geocodeProperties.getUserAgent())
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        lastRequestAtMs = System.currentTimeMillis();
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Nominatim HTTP " + response.statusCode());
        }
        JsonNode root = objectMapper.readTree(response.body());
        if (!root.isArray() || root.isEmpty()) {
            cache.put(cacheKey, null);
            return null;
        }
        JsonNode first = root.get(0);
        double lat = first.get("lat").asDouble();
        double lng = first.get("lon").asDouble();
        double[] coords = new double[] {lat, lng};
        cache.put(cacheKey, coords);
        return coords;
    }

    private void throttle() throws InterruptedException {
        long minInterval = Math.max(1000L, geocodeProperties.getMinIntervalMs());
        long elapsed = System.currentTimeMillis() - lastRequestAtMs;
        if (lastRequestAtMs > 0 && elapsed < minInterval) {
            Thread.sleep(minInterval - elapsed);
        }
    }

    private static String formatAddress(Location location) {
        if (location.getStreetAddress() == null || location.getStreetAddress().isBlank()) {
            return null;
        }
        StringBuilder sb = new StringBuilder(location.getStreetAddress().trim());
        if (location.getCity() != null && !location.getCity().isBlank()) {
            sb.append(", ").append(location.getCity().trim());
        }
        if (location.getState() != null && !location.getState().isBlank()) {
            sb.append(", ").append(location.getState().trim());
        }
        if (location.getZip() != null && !location.getZip().isBlank()) {
            sb.append(' ').append(location.getZip().trim());
        }
        return sb.toString();
    }

    public static class GeocodeSummary {
        public int updated;
        public int skippedAlreadyFilled;
        public int skippedNoAddress;
        public int notFound;
        public int errors;
    }
}
