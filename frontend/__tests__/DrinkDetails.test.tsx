
import React from "react";
import { render } from "@testing-library/react-native";
import { useRoute, useNavigation } from "@react-navigation/native";

import DrinkDetails from "../src/screens/DrinkDetails";

jest.mock("@react-navigation/native", () => ({
  useRoute: jest.fn(),
  useNavigation: jest.fn(),
}));

jest.mock("../src/fakeDrinks", () => ({
  drinks: [
    {
      id: "101",
      name: "Iced Coffee",
      calories: 150,
      sugar: 20,
      allergens: "Milk",
    },
    {
      id: "103",
      name: "Nutrition Test Drink",
      calories: null,
      sugar: -10,
      allergens: "None",
    },
  ],
  stores: [],
}));

jest.mock("../src/theme/ThemeProvider", () => ({
  useTheme: () => ({
    theme: { text: "#000000" },
  }),
}));

jest.mock("@expo/vector-icons", () => ({
  Ionicons: () => null,
}));

const mockUseRoute = useRoute as jest.Mock;
const mockUseNavigation = useNavigation as jest.Mock;
const mockGoBack = jest.fn();

async function renderScreen(drinkId: string | number | null) {
  mockUseRoute.mockReturnValue({
    params: { drinkId },
  });

  mockUseNavigation.mockReturnValue({
    goBack: mockGoBack,
  });

  return await render(<DrinkDetails />);
}

describe("DrinkDetails screen", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("findDrinkById(drinkId)", () => {
    it("TC01: finds a drink when numeric and string IDs match", async () => {
      const { getByText } = await renderScreen(101);

      expect(getByText("Iced Coffee")).toBeTruthy();
    });

    it("TC02: displays error when drink ID does not exist", async () => {
      const { getByText } = await renderScreen(999);

      expect(getByText("Drink not Found.")).toBeTruthy();

      expect(
        getByText("Back to Home Screen")
      ).toBeTruthy();
    });
  });

  describe("displayNutrition(drink)", () => {
    it("TC03: displays valid calories and sugar", async () => {
      const { getByText } = await renderScreen("101");

      expect(getByText("Calories: 150 cal")).toBeTruthy();
      expect(getByText("Sugar: 20 g")).toBeTruthy();
    });
    it("TC04: handles missing and negative nutrition values", async () => {
    const { getByText, queryByText } = await renderScreen("103");

    expect(
      getByText("Calories: Not Provided cal")
    ).toBeTruthy();

    expect(
      getByText("Sugar: Not Provided g")
    ).toBeTruthy();

    expect(
      queryByText("Sugar: -10 g")
    ).toBeNull();
  });
   
  });
});
