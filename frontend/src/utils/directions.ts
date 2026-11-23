import { Alert, Linking, Platform } from "react-native";

type DirectionsTarget = {
    name?: string | null;
    address?: string | null;
    latitude?: number | null;
    longitude?: number | null;
};

export async function openDirections(place: DirectionsTarget | null | undefined) {
    const hasCoords =
        place != null &&
        Number.isFinite(place.latitude) &&
        Number.isFinite(place.longitude);

    if (!place || !hasCoords) {
        Alert.alert(
            "Directions unavailable",
            "We don't have a location for this café yet."
        );
        return;
    }

    const coords = `${place.latitude},${place.longitude}`;
    const label = place.name ? encodeURIComponent(place.name) : null;

    const url =
        Platform.OS === "ios"
            ? `http://maps.apple.com/?daddr=${coords}${label ? `&q=${label}` : ""}`
            : `https://www.google.com/maps/dir/?api=1&destination=${
                  place.name && place.address
                      ? encodeURIComponent(`${place.name}, ${place.address}`)
                      : coords
              }`;

    try {
        await Linking.openURL(url);
    } catch {
        Alert.alert("Directions unavailable", "Please try again later.");
    }
}