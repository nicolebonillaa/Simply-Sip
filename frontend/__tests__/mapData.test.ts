import {
  CSUN,
  distanceInMiles,
  getNearbySpots,
  type MapSpot,
} from "../src/data/mapData";

describe("distanceInMiles", () => {
  test("returns zero for the same location", () => {
    expect(distanceInMiles(CSUN, CSUN)).toBe(0);
  });

  test("returns a positive distance for different locations", () => {
    const oneDegreeNorth = {
      latitude: CSUN.latitude + 1,
      longitude: CSUN.longitude,
    };

    expect(distanceInMiles(CSUN, oneDegreeNorth)).toBeGreaterThan(69);
    expect(distanceInMiles(CSUN, oneDegreeNorth)).toBeLessThan(70);
  });

  test("returns the same distance in either direction", () => {
    const nearby = { latitude: 34.25, longitude: -118.54 };

    expect(distanceInMiles(CSUN, nearby)).toBeCloseTo(
      distanceInMiles(nearby, CSUN),
      6,
    );
  });
});

describe("getNearbySpots", () => {
  test("includes a café inside the two-mile radius", () => {
    const atCsun: MapSpot = { id: 1, name: "Campus Café", ...CSUN };

    expect(getNearbySpots([atCsun])).toEqual([atCsun]);
  });

  test("excludes a café outside the two-mile radius", () => {
    const farAway: MapSpot = {
      id: 2,
      name: "Far Away Café",
      latitude: CSUN.latitude + 0.1,
      longitude: CSUN.longitude,
    };

    expect(getNearbySpots([farAway])).toEqual([]);
  });

  test("excludes cafés with invalid coordinates", () => {
    const valid: MapSpot = { id: 1, name: "Campus Café", ...CSUN };
    const invalid: MapSpot = {
      id: 2,
      name: "Unknown Location",
      latitude: Number.NaN,
      longitude: CSUN.longitude,
    };

    expect(getNearbySpots([valid, invalid])).toEqual([valid]);
  });
});