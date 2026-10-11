export type MapSpot = {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
};

export const CSUN = { // California State University, Northridge location
  latitude: 34.241,
  longitude: -118.529,
};

export const CSUN_REGION = { // Initial region for the map centered around CSUN
  ...CSUN,
  latitudeDelta: 0.02,
  longitudeDelta: 0.02,
};

export const MOCK_SPOTS: MapSpot[] = [ // Mock data for drink spots 
  {
    id: 1,
    name: "Matador Bookstore Cafe",
    latitude: 34.241,
    longitude: -118.529,
  },
  {
    id: 2,
    name: "Starbucks - Reseda",
    latitude: 34.2405,
    longitude: -118.536,
  },
  {
    id: 3,
    name: "Juice It Up!",
    latitude: 34.2388,
    longitude: -118.5535,
  },
];

export function distanceInMiles( //Two mile radius function to find nearby drink spots
  first: { latitude: number; longitude: number },
  second: { latitude: number; longitude: number },
) {
  const earthRadiusMiles = 3958.8;

  const latDifference =
    ((second.latitude - first.latitude) * Math.PI) / 180;

  const lonDifference =
    ((second.longitude - first.longitude) * Math.PI) / 180;

  const firstLatitude = (first.latitude * Math.PI) / 180;
  const secondLatitude = (second.latitude * Math.PI) / 180;

  const haversineValue =
    Math.sin(latDifference / 2) ** 2 +
    Math.cos(firstLatitude) *
      Math.cos(secondLatitude) *
      Math.sin(lonDifference / 2) ** 2;

  const centralAngle = 2 * Math.atan2(
    Math.sqrt(haversineValue),
    Math.sqrt(1 - haversineValue)
  );

  return earthRadiusMiles * centralAngle;
}

export function getNearbySpots(spots: MapSpot[]) {
  return spots.filter((spot) => distanceInMiles(CSUN, spot) <= 2);
}