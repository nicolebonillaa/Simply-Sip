package com.simplysip.dto;

import com.simplysip.model.Location;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class LocationResponse {

    private final Long id;
    private final String name;
    private final String address;
    private final String cafeName;
    private final String locationName;
    private final String streetAddress;
    private final String city;
    private final String state;
    private final String zip;
    private final Double latitude;
    private final Double longitude;
    private final String imageUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public LocationResponse(Location location) {
        this.id = location.getId();
        this.cafeName = location.getCafeName();
        this.locationName = location.getLocationName();
        this.streetAddress = location.getStreetAddress();
        this.city = location.getCity();
        this.state = location.getState();
        this.zip = location.getZip();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.imageUrl = location.getImageUrl();
        this.createdAt = location.getCreatedAt();
        this.updatedAt = location.getUpdatedAt();
        this.name = firstNonBlank(location.getCafeName(), location.getLocationName());
        this.address = formatAddress(
                location.getStreetAddress(),
                location.getCity(),
                location.getState(),
                location.getZip());
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return null;
    }

    private static String formatAddress(String street, String city, String state, String zip) {
        List<String> parts = new ArrayList<>();
        if (street != null && !street.isBlank()) {
            parts.add(street.trim());
        }
        StringBuilder cityStateZip = new StringBuilder();
        if (city != null && !city.isBlank()) {
            cityStateZip.append(city.trim());
        }
        if (state != null && !state.isBlank()) {
            if (!cityStateZip.isEmpty()) {
                cityStateZip.append(", ");
            }
            cityStateZip.append(state.trim());
        }
        if (zip != null && !zip.isBlank()) {
            if (!cityStateZip.isEmpty()) {
                cityStateZip.append(' ');
            }
            cityStateZip.append(zip.trim());
        }
        if (!cityStateZip.isEmpty()) {
            parts.add(cityStateZip.toString());
        }
        return parts.isEmpty() ? null : String.join(", ", parts);
    }
}
