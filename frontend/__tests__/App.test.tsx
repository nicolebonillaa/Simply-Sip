/**
 * @format
 */

import React from "react";
import ReactTestRenderer from "react-test-renderer";

jest.mock("react-native-maps", () => {
  const React = require("react");
  const { View } = require("react-native");

  const MapView = ({ children }: { children?: React.ReactNode }) => (
    <View>{children}</View>
  );

  const Marker = ({ children }: { children?: React.ReactNode }) => (
    <View>{children}</View>
  );

  const Callout = ({ children }: { children?: React.ReactNode }) => (
    <View>{children}</View>
  );

  return {
    __esModule: true,
    default: MapView,
    Marker,
    Callout,
  };
});

import App from "../App";

test("renders correctly", async () => {
  await ReactTestRenderer.act(() => {
    ReactTestRenderer.create(<App />);
  });
});
