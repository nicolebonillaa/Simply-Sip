package com.simplysip.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DistanceUtilsTest {

    private static final double KM_TO_MILES = 0.621371;

    @ParameterizedTest(name = "lat1={0}, lng1={1}, lat2={2}, lng2={3} -> {4}")
    @DisplayName("Invalid coordinates throw IllegalArgumentException with expected message")
    @CsvSource({
            "91, 0, 0, 0, Latitude must be between -90 and 90",
            "-91, 0, 0, 0, Latitude must be between -90 and 90",
            "0, 181, 0, 0, Longitude must be between -180 and 180",
            "0, -181, 0, 0, Longitude must be between -180 and 180",
            "NaN, 0, 0, 0, Coordinates must not be NaN"
    })
    void invalidCoordinatesThrowIllegalArgumentException(
            double lat1,
            double lng1,
            double lat2,
            double lng2,
            String expectedMessage) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> DistanceUtils.kilometers(lat1, lng1, lat2, lng2));
        assertEquals(expectedMessage, ex.getMessage());
    }

    @ParameterizedTest(name = "({0}, {1}) to ({2}, {3})")
    @DisplayName("Identical points yield distance of zero kilometers")
    @CsvSource({
            "34.2381, -118.5301, 34.2381, -118.5301",
            "0, 0, 0, 0",
            "90, 0, 90, 0"
    })
    void identicalPointsYieldZeroDistance(double lat1, double lng1, double lat2, double lng2) {
        assertEquals(0.0, DistanceUtils.kilometers(lat1, lng1, lat2, lng2), 1e-9);
        assertEquals(0.0, DistanceUtils.miles(lat1, lng1, lat2, lng2), 1e-9);
    }

    @ParameterizedTest(name = "({0}, {1}) to ({2}, {3}) ≈ {4} km")
    @DisplayName("Known distances are within 0.5 percent of expected kilometers")
    @CsvSource({
            "0, 0, 0, 1, 111.19",
            "34.0522, -118.2437, 40.7128, -74.0060, 3936",
            "51.5074, -0.1278, 48.8566, 2.3522, 344"
    })
    void knownDistancesWithinTolerance(
            double lat1,
            double lng1,
            double lat2,
            double lng2,
            double expectedKm) {
        double actual = DistanceUtils.kilometers(lat1, lng1, lat2, lng2);
        assertEquals(expectedKm, actual, expectedKm * 0.005);
    }

    @ParameterizedTest(name = "({0}, {1}) <-> ({2}, {3})")
    @DisplayName("Distance is symmetric and miles equal kilometers times 0.621371")
    @CsvSource({
            "0, 0, 0, 1",
            "34.0522, -118.2437, 40.7128, -74.0060",
            "51.5074, -0.1278, 48.8566, 2.3522"
    })
    void symmetryAndUnitConversion(double lat1, double lng1, double lat2, double lng2) {
        double kmAb = DistanceUtils.kilometers(lat1, lng1, lat2, lng2);
        double kmBa = DistanceUtils.kilometers(lat2, lng2, lat1, lng1);
        double miAb = DistanceUtils.miles(lat1, lng1, lat2, lng2);
        double miBa = DistanceUtils.miles(lat2, lng2, lat1, lng1);

        assertEquals(kmAb, kmBa, 1e-9);
        assertEquals(miAb, miBa, 1e-9);
        assertEquals(kmAb * KM_TO_MILES, miAb, Math.abs(kmAb * KM_TO_MILES) * 0.0001);
    }
}
