package com.simplysip.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Normalizes drink image URLs from scraper CSV rows: path-only percent-encoding
 * without double-encoding existing %XX sequences.
 */
public final class ImageUrlNormalizer {

    private static final Logger log = LoggerFactory.getLogger(ImageUrlNormalizer.class);

    private ImageUrlNormalizer() {
    }

    /**
     * @return normalized URL, or null if blank/unknown/N/A or still invalid after encoding
     */
    public static String normalize(String raw) {
        String cleaned = clean(raw);
        if (cleaned == null) {
            return null;
        }

        try {
            String normalized = encodeUrlPath(cleaned);
            if (!isAcceptableUrl(normalized)) {
                log.warn("Invalid image URL after encoding: {}", raw);
                return null;
            }
            if (normalized.toLowerCase(Locale.ROOT).startsWith("http://")) {
                log.warn("Non-HTTPS image URL: {}", normalized);
            }
            return normalized;
        } catch (Exception ex) {
            log.warn("Invalid image URL ({}): {}", ex.getMessage(), raw);
            return null;
        }
    }

    static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if ("unknown".equalsIgnoreCase(trimmed)
                || "n/a".equalsIgnoreCase(trimmed)
                || "na".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }

    private static boolean isAcceptableUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.startsWith("https://") || lower.startsWith("http://")) {
            return value.length() > "https://".length();
        }
        // Relative site paths (e.g. /images/boba.jpg)
        return value.startsWith("/");
    }

    /**
     * Percent-encode illegal path characters only. Leaves scheme, host, and '/' alone.
     * Does not re-encode existing %XX sequences.
     */
    static String encodeUrlPath(String url) {
        int schemeIdx = url.indexOf("://");
        if (schemeIdx < 0) {
            return encodePathAndRest(url);
        }
        int authorityStart = schemeIdx + 3;
        int pathStart = indexOfPathStart(url, authorityStart);
        if (pathStart < 0) {
            return url;
        }
        String origin = url.substring(0, pathStart);
        return origin + encodePathAndRest(url.substring(pathStart));
    }

    private static int indexOfPathStart(String url, int authorityStart) {
        for (int i = authorityStart; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == '/') {
                return i;
            }
            if (c == '?' || c == '#') {
                return i;
            }
        }
        return -1;
    }

    private static String encodePathAndRest(String pathAndRest) {
        int queryIdx = indexOfUnencoded(pathAndRest, '?');
        int fragIdx = indexOfUnencoded(pathAndRest, '#');
        int end = pathAndRest.length();
        if (queryIdx >= 0) {
            end = queryIdx;
        }
        if (fragIdx >= 0 && fragIdx < end) {
            end = fragIdx;
        }
        String path = pathAndRest.substring(0, end);
        String suffix = pathAndRest.substring(end);
        return encodePathPreservingSlashes(path) + suffix;
    }

    private static int indexOfUnencoded(String s, char target) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == target) {
                return i;
            }
        }
        return -1;
    }

    static String encodePathPreservingSlashes(String path) {
        StringBuilder out = new StringBuilder(path.length() + 8);
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '/') {
                out.append(c);
            } else if (c == '%' && i + 2 < path.length()
                    && isHex(path.charAt(i + 1))
                    && isHex(path.charAt(i + 2))) {
                out.append(c).append(path.charAt(i + 1)).append(path.charAt(i + 2));
                i += 2;
            } else if (c == ' ') {
                out.append("%20");
            } else if (c == ':') {
                out.append("%3A");
            } else if (needsEncoding(c)) {
                out.append(String.format("%%%02X", (int) c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static boolean needsEncoding(char c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
            return false;
        }
        return "-._~!$'()*+,;=:@".indexOf(c) < 0;
    }

    private static boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}
