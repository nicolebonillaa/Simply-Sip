import React, { useEffect, useState } from "react";
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
import { useNavigation } from "@react-navigation/native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import DrinkMarker from "../components/DrinkMarker";
import {
  CSUN_REGION,
  getNearbySpots,
  MOCK_SPOTS,
} from "../data/mapData";

type PermissionState = "checking" | "granted" | "denied";

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
  const [permission, setPermission] =
    useState<PermissionState>("checking");

  const [canAskAgain, setCanAskAgain] = useState(true);
  const [region, setRegion] = useState(CSUN_REGION);

  const nearbySpots = getNearbySpots(MOCK_SPOTS);

  const requestLocation = async () => {
    setPermission("checking");

    const result = await Location.requestForegroundPermissionsAsync();

    setCanAskAgain(result.canAskAgain);

    if (result.status !== "granted") {
      setPermission("denied");
      return;
    }

    setPermission("granted");

    const currentLocation = await Location.getCurrentPositionAsync({
      accuracy: Location.Accuracy.Balanced,
    });

    setRegion({
      latitude: currentLocation.coords.latitude,
      longitude: currentLocation.coords.longitude,
      latitudeDelta: 0.02,
      longitudeDelta: 0.02,
    });
  };

  useEffect(() => {
    void requestLocation();
  }, []);

  if (permission === "checking") {
    return (
      <View style={styles.screen}>
        <View style={styles.center}>
          <ActivityIndicator size="large" />
          <Text>Requesting location permission...</Text>
        </View>
        <CloseButton />
      </View>
    );
  }

  if (permission === "denied") {
    return (
      <View style={styles.screen}>
        <View style={styles.center}>
          <Text style={styles.message}>
            Location permission is needed to center the map on you.
          </Text>

          {canAskAgain ? (
            <Button title="Try Again" onPress={requestLocation} />
          ) : (
            <Button
              title="Open Settings"
              onPress={() => {
                void Linking.openSettings();
              }}
            />
          )}
        </View>
        <CloseButton />
      </View>
    );
  }

  return (
    <View style={styles.screen}>
      <MapView
        style={styles.map}
        region={region}
        showsUserLocation
        showsMyLocationButton
      >
        {nearbySpots.map((spot) => (
          <DrinkMarker key={spot.id} spot={spot} />
        ))}
      </MapView>
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
  center: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    padding: 24,
  },
  message: {
    textAlign: "center",
    marginBottom: 16,
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