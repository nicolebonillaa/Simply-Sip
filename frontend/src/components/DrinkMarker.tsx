import React from "react";
import { Text, View } from "react-native";
import { Callout, Marker } from "react-native-maps";
import { MapSpot } from "../data/mapData";

type Props = {
  spot: MapSpot;
};

export default function DrinkMarker({ spot }: Props) {
  return (
    <Marker
      coordinate={{
        latitude: spot.latitude,
        longitude: spot.longitude,
      }}
      title={spot.name}
    >
      <Callout>
        <View>
          <Text>{spot.name}</Text>
        </View>
      </Callout>
    </Marker>
  );
}