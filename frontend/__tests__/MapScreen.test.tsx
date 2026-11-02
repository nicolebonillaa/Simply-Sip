import React from "react";
import { act, fireEvent, render, waitFor } from "@testing-library/react-native";
import * as Location from "expo-location";
import MapScreen from "../src/screens/MapScreen";
import { CSUN, getNearbySpots } from "../src/data/mapData";
import { getLocations } from "../src/services/locations";

jest.mock("react-native-maps", () => {
  // Jest needs these imports inside the mock factory.
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const React = require("react");
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const { View } = require("react-native");
  const MapView = React.forwardRef(({ children }: { children?: React.ReactNode }, ref: React.Ref<unknown>) => (
    <View ref={ref} testID="map">{children}</View>
  ));
  MapView.displayName = "MockMapView";
  function Marker({ children }: { children?: React.ReactNode }) {
    return <View>{children}</View>;
  }
  function Callout({ children }: { children?: React.ReactNode }) {
    return <View>{children}</View>;
  }
  return {
    __esModule: true,
    default: MapView,
    Marker,
    Callout,
  };
});

jest.mock("@react-navigation/native", () => ({
  useNavigation: () => ({ goBack: jest.fn() }),
  useRoute: () => ({ params: {} }),
}));

jest.mock("react-native-safe-area-context", () => ({
  useSafeAreaInsets: () => ({ top: 0, bottom: 0, left: 0, right: 0 }),
}));

jest.mock("expo-location", () => ({
  requestForegroundPermissionsAsync: jest.fn().mockResolvedValue({
    status: "denied",
    canAskAgain: false,
  }),
  getCurrentPositionAsync: jest.fn(),
  Accuracy: { Balanced: 3 },
}));

jest.mock("../src/services/locations", () => ({
  getLocations: jest.fn().mockResolvedValue([
    { id: 1, name: "Campus Cafe", latitude: 34.241, longitude: -118.529 },
    { id: 2, name: "Faraway Cafe", latitude: 34.35, longitude: -118.529 },
  ]),
}));

test("keeps the map usable after location denial and shows only nearby backend cafés", async () => {
  const screen = await render(<MapScreen />);

  await waitFor(() => {
    expect(screen.getByText("Location denied. Map centered on CSUN.")).toBeTruthy();
    expect(screen.getByText("Campus Cafe")).toBeTruthy();
  });

  expect(screen.getByTestId("map")).toBeTruthy();
  expect(screen.queryByText("Faraway Cafe")).toBeNull();
});

test("keeps cafés at the two-mile boundary and excludes those beyond it", () => {
  const near = { id: 1, name: "Inside", latitude: CSUN.latitude + 0.0286, longitude: CSUN.longitude };
  const outside = { id: 2, name: "Outside", latitude: CSUN.latitude + 0.0294, longitude: CSUN.longitude };
  expect(getNearbySpots([near, outside]).map((spot) => spot.name)).toEqual(["Inside"]);
});

test("shows the campus map if location permission is granted but position is unavailable", async () => {
  jest.mocked(Location.requestForegroundPermissionsAsync).mockResolvedValueOnce({
    status: "granted",
    canAskAgain: true,
  } as any);
  jest.mocked(Location.getCurrentPositionAsync).mockRejectedValueOnce(
    new Error("LocationUnavailable"),
  );

  const screen = await render(<MapScreen />);

  await waitFor(() => {
    expect(screen.getByText("Could not find your position. Map centered on CSUN.")).toBeTruthy();
    expect(screen.getByText("Campus Cafe")).toBeTruthy();
  });
  expect(screen.getByTestId("map")).toBeTruthy();
});

test("shows an API error and recovers when locations are retried", async () => {
  jest.mocked(getLocations).mockRejectedValueOnce(new Error("Offline"));
  const screen = await render(<MapScreen />);

  await waitFor(() => expect(screen.getByText("Could not load café locations.")).toBeTruthy());
  expect(screen.getByTestId("map")).toBeTruthy();

  await act(async () => {
    fireEvent.press(screen.getByText("Retry locations"));
  });
  await waitFor(() => expect(screen.getByText("Campus Cafe")).toBeTruthy());
  expect(screen.queryByText("Could not load café locations.")).toBeNull();
});