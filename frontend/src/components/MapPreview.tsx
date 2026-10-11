import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import MapView from "react-native-maps";
import DrinkMarker from "./DrinkMarker";
import { CSUN_REGION, MapSpot } from "../data/mapData";

type Props = {
  spots: MapSpot[];
  onPress: () => void;
};

export default function MapPreview({ spots, onPress }: Props) {
  return (
    <View style={styles.container}>
      <MapView
        style={styles.map}
        initialRegion={CSUN_REGION}
        scrollEnabled={false}
        zoomEnabled={false}
        rotateEnabled={false}
        pitchEnabled={false}
        pointerEvents="none"
      >
        {spots.map((spot) => (
          <DrinkMarker key={spot.id} spot={spot} />
        ))}
      </MapView>

      <Pressable style={styles.overlay} onPress={onPress}>
        <Text style={styles.overlayText}>Tap to open full map</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    height: 220,
    marginVertical: 16,
    overflow: "hidden",
    borderRadius: 12,
  },
  map: {
    flex: 1,
  },
  overlay: {
    position: "absolute",
    right: 12,
    bottom: 12,
    padding: 10,
    borderRadius: 8,
    backgroundColor: "white",
  },
  overlayText: {
    fontWeight: "600",
  },
});