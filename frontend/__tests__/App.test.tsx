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
jest.mock("../assets/mocha icons/mocha-home-filled.svg", () => {
  const MockSvg = () => null;
  return {
    __esModule: true,
    default: MockSvg,
  };
});

jest.mock("../assets/mocha icons/mocha-home-outline.svg", () => {
  const MockSvg = () => null;
  return {
    __esModule: true,
    default: MockSvg,
  };
});

jest.mock("../assets/mocha icons/mocha-favorites-filled.svg", () => {
  const MockSvg = () => null;
  return {
    __esModule: true,
    default: MockSvg,
  };
});

jest.mock("../assets/mocha icons/mocha-favorites-outline.svg", () => {
  const MockSvg = () => null;
  return {
    __esModule: true,
    default: MockSvg,
  };
});

jest.mock("../assets/mocha icons/mocha-profile-filled.svg", () => {
  const MockSvg = () => null;
  return {
    __esModule: true,
    default: MockSvg,
  };
});

jest.mock("../assets/mocha icons/mocha-profile-outline.svg", () => {
  const MockSvg = () => null;
  return {
    __esModule: true,
    default: MockSvg,
  };
});
import App from "../App";

test("renders correctly", async () => {
  await ReactTestRenderer.act(() => {
    ReactTestRenderer.create(<App />);
  });
});
