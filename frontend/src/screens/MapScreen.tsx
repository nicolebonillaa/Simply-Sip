import React, { useEffect, useRef, useState } from "react";
import {
  ActivityIndicator,
  Button,
  Linking,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import * as Location from "expo-location";
import MapView from "react-native-maps";
import { useNavigation, useRoute } from "@react-navigation/native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import DrinkMarker from "../components/DrinkMarker";
import { getLocations } from "../services/locations";
import { CSUN_REGION, getNearbySpots } from "../data/mapData";
import type { MapSpot } from "../data/mapData";

type PermissionState = "checking" | "granted" | "denied" | "unavailable";

function CloseButton() {
  const navigation = useNavigation<any>();
  const insets = useSafeAreaInsets();

  return (
    <Pressable
      style={[styles.closeButton, { top: insets.top + 8 }]}
      onPress={() => navigation.goBack()}
      accessibilityRole="button"
      accessibilityLabel="Close map"
      hitSlop={8}
    >
      {/* swap for your close icon asset */}
      <Text style={styles.closeText}>✕</Text>
    </Pressable>
  );
}

export default function MapScreen() {
  const route = useRoute<any>();
  const insets = useSafeAreaInsets();
  const mapRef = useRef<MapView>(null);
  const [permission, setPermission] =
    useState<PermissionState>("checking");
  const [canAskAgain, setCanAskAgain] = useState(true);
  const [locationUnavailable, setLocationUnavailable] = useState(false);
  // Search Results passes its applied pins; a direct visit loads locations from the API.
  const passedSpots: MapSpot[] | undefined = Array.isArray(route.params?.spots)
    ? route.params.spots
    : undefined;
  const [apiSpots, setApiSpots] = useState<MapSpot[]>([]);
  const [spotsLoading, setSpotsLoading] = useState(passedSpots === undefined);
  const [spotsError, setSpotsError] = useState(false);
  const [retry, setRetry] = useState(0);
  const nearbySpots = passedSpots ?? apiSpots;

  useEffect(() => {
    if (passedSpots !== undefined) return;

    let active = true;
    void getLocations()
      .then((locations) => {
        if (!active) return;
        const spots: MapSpot[] = locations.map((location) => ({
          id: location.id,
          name: location.name,
          latitude: location.latitude,
          longitude: location.longitude,
        }));
        setApiSpots(getNearbySpots(spots));
      })
      .catch(() => {
        if (active) setSpotsError(true);
      })
      .finally(() => {
        if (active) setSpotsLoading(false);
      });

    return () => {
      active = false;
    };
  }, [passedSpots, retry]);

  const requestLocation = async () => {
    try {
      const result = await Location.requestForegroundPermissionsAsync();
      setCanAskAgain(result.canAskAgain);

      if (result.status !== "granted") {
        setPermission("denied");
        return;
      }

      setPermission("granted");
      try {
        const currentLocation = await Location.getCurrentPositionAsync({
          accuracy: Location.Accuracy.Balanced,
        });
        mapRef.current?.animateToRegion(
          {
            latitude: currentLocation.coords.latitude,
            longitude: currentLocation.coords.longitude,
            latitudeDelta: CSUN_REGION.latitudeDelta,
            longitudeDelta: CSUN_REGION.longitudeDelta,
          },
          500,
        );
      } catch {
        setLocationUnavailable(true);
      }
    } catch {
      setPermission("unavailable");
    }
  };

  const retryLocation = () => {
    setPermission("checking");
    setLocationUnavailable(false);
    void requestLocation();
  };

  const retrySpots = () => {
    setSpotsLoading(true);
    setSpotsError(false);
    setApiSpots([]);
    setRetry((value) => value + 1);
  };

  useEffect(() => {
    void Promise.resolve().then(requestLocation);
  }, []);

  const showNotice =
    permission !== "granted" ||
    locationUnavailable ||
    spotsLoading ||
    spotsError ||
    nearbySpots.length === 0;

  return (
    <View style={styles.screen}>
      <MapView
        ref={mapRef}
        style={styles.map}
        initialRegion={CSUN_REGION}
        showsUserLocation={permission === "granted"}
        showsMyLocationButton={permission === "granted"}
      >
        {nearbySpots.map((spot) => (
          <DrinkMarker key={spot.id} spot={spot} />
        ))}
      </MapView>

      {showNotice && (
        <View style={[styles.notice, { top: insets.top + 64 }]}>
          {permission === "checking" && (
            <ActivityIndicator accessibilityLabel="Checking location permission" />
          )}
          {permission === "denied" && (
            <>
              <Text style={styles.message}>Location denied. Map centered on CSUN.</Text>
              <Button
                title={canAskAgain ? "Try Again" : "Open Settings"}
                onPress={canAskAgain ? retryLocation : () => { void Linking.openSettings(); }}
              />
            </>
          )}
          {permission === "unavailable" && (
            <>
              <Text style={styles.message}>Location unavailable. Map centered on CSUN.</Text>
              <Button title="Try Again" onPress={retryLocation} />
            </>
          )}
          {permission === "granted" && locationUnavailable && (
            <>
              <Text style={styles.message}>Could not find your position. Map centered on CSUN.</Text>
              <Button title="Try Again" onPress={retryLocation} />
            </>
          )}
          {spotsLoading && <ActivityIndicator accessibilityLabel="Loading café locations" />}
          {spotsError && (
            <>
              <Text style={styles.message}>Could not load café locations.</Text>
              <Button title="Retry locations" onPress={retrySpots} />
            </>
          )}
          {!spotsLoading && !spotsError && nearbySpots.length === 0 && (
            <Text style={styles.message}>No nearby cafés match these results.</Text>
          )}
        </View>
      )}
      <CloseButton />
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
  },
  map: {
    flex: 1,
  },
  notice: {
    position: "absolute",
    left: 16,
    right: 72,
    alignItems: "flex-start",
    padding: 10,
    borderRadius: 10,
    backgroundColor: "white",
  },
  message: {
    marginBottom: 4,
  },
  closeButton: {
    position: "absolute",
    right: 16,
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: "#FFFFFF",
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: "rgba(0, 0, 0, 0.25)",
    alignItems: "center",
    justifyContent: "center",
    shadowColor: "#000",
    shadowOpacity: 0.15,
    shadowRadius: 6,
    shadowOffset: { width: 0, height: 2 },
  },
  closeText: {
    fontSize: 18,
    fontWeight: "600",
    color: "#111",
  },
});