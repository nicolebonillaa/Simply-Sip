package com.simplysip.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageUrlNormalizerTest {

    @Test
    @DisplayName("Space in path is percent-encoded to %20")
    void encodesSpaceInPath() {
        String result = ImageUrlNormalizer.normalize(
                "https://cdn.example.com/drinks/classic milk tea.jpg");
        assertEquals("https://cdn.example.com/drinks/classic%20milk%20tea.jpg", result);
    }

    @Test
    @DisplayName("Colon in path is percent-encoded to %3A")
    void encodesColonInPath() {
        String result = ImageUrlNormalizer.normalize(
                "https://cdn.example.com/drinks/taro:slush.jpg");
        assertEquals("https://cdn.example.com/drinks/taro%3Aslush.jpg", result);
    }

    @Test
    @DisplayName("Already-encoded %20 is not double-encoded")
    void doesNotDoubleEncode() {
        String result = ImageUrlNormalizer.normalize(
                "https://cdn.example.com/drinks/brown%20sugar%20milk%20tea.jpg");
        assertEquals("https://cdn.example.com/drinks/brown%20sugar%20milk%20tea.jpg", result);
    }

    @ParameterizedTest
    @DisplayName("unknown and N/A clean to null")
    @ValueSource(strings = {"unknown", "Unknown", "N/A", "n/a", "NA", "na"})
    void unknownAndNaAreNull(String raw) {
        assertNull(ImageUrlNormalizer.normalize(raw));
    }

    @ParameterizedTest
    @DisplayName("Blank values clean to null")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void blankIsNull(String raw) {
        assertNull(ImageUrlNormalizer.normalize(raw));
    }

    @Test
    @DisplayName("HTTPS URL is accepted")
    void httpsAccepted() {
        String result = ImageUrlNormalizer.normalize("https://cdn.example.com/drinks/ok.jpg");
        assertEquals("https://cdn.example.com/drinks/ok.jpg", result);
    }

    @Test
    @DisplayName("HTTP URL is still returned (caller may warn)")
    void httpReturned() {
        String result = ImageUrlNormalizer.normalize("http://cdn.example.com/drinks/ok.jpg");
        assertEquals("http://cdn.example.com/drinks/ok.jpg", result);
        assertTrue(result.startsWith("http://"));
    }

    @ParameterizedTest
    @DisplayName("Malformed URLs become null")
    @CsvSource({
            "https://",
            "not a url",
            "ftp://cdn.example.com/x.jpg"
    })
    void malformedBecomesNull(String raw) {
        assertNull(ImageUrlNormalizer.normalize(raw));
    }

    @Test
    @DisplayName("Relative /images path is accepted")
    void relativePathAccepted() {
        assertEquals("/images/boba.jpg", ImageUrlNormalizer.normalize("/images/boba.jpg"));
        assertNotNull(ImageUrlNormalizer.normalize("/images/with space.jpg"));
        assertEquals("/images/with%20space.jpg", ImageUrlNormalizer.normalize("/images/with space.jpg"));
    }
}
