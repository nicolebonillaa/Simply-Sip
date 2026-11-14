package com.simplysip.util;

public final class DistanceUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double EARTH_RADIUS_MI = 3958.8;

    private DistanceUtils() {
    }

    public static double kilometers(double lat1, double lng1, double lat2, double lng2) {
        return haversine(lat1, lng1, lat2, lng2, EARTH_RADIUS_KM);
    }

    public static double miles(double lat1, double lng1, double lat2, double lng2) {
        return haversine(lat1, lng1, lat2, lng2, EARTH_RADIUS_MI);
    }

    private static double haversine(
            double lat1,
            double lng1,
            double lat2,
            double lng2,
            double earthRadius) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
