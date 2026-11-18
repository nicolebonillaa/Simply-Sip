import React from "react";
import { render } from "@testing-library/react-native";
import { useNavigation, useRoute } from "@react-navigation/native";
import { getDrinkById } from "../src/services/drinks";
import DrinkDetails from "../src/screens/DrinkDetails";

jest.mock("@react-navigation/native", () => ({
  useRoute: jest.fn(),
  useNavigation: jest.fn(),
}));
jest.mock("../src/services/drinks", () => ({ getDrinkById: jest.fn() }));
jest.mock("../src/auth/AuthContext", () => ({
  useAuth: () => ({ status: "signedOut", token: null }),
}));
jest.mock("../src/favorites/FavoritesContext", () => ({
  useFavorites: () => ({ drinks: [], pendingDrinkIds: [], toggleDrink: jest.fn() }),
}));
jest.mock("../src/components/CafeDrinkImage", () => () => null);
jest.mock("../src/theme/ThemeProvider", () => ({
  useTheme: () => ({
    theme: { background: "#fff", text: "#000", subtext: "#555", icon: "#a00", band: "#eee" },
  }),
}));
jest.mock("@expo/vector-icons", () => ({ Ionicons: () => null }));

const mockGetDrinkById = getDrinkById as jest.Mock;
const mockUseRoute = useRoute as jest.Mock;
const mockUseNavigation = useNavigation as jest.Mock;

const drinkWithNutrition = (calories: number | null, sugar: number | null) => ({
  id: 101,
  name: "Iced Coffee",
  category: "Coffee",
  location: null,
  imageUrl: null,
  allergens: [],
  sizes: [{
    id: 1,
    sizeName: "Medium",
    ounces: 16,
    price: 4.5,
    nutritionFacts: { id: 1, calories, sugar, caffeine: 60 },
  }],
});

async function renderScreen(drinkId: number | string) {
  mockUseRoute.mockReturnValue({ params: { drinkId } });
  mockUseNavigation.mockReturnValue({ goBack: jest.fn() });
  return await render(<DrinkDetails />);
}

describe("DrinkDetails screen", () => {
  beforeEach(() => jest.clearAllMocks());

  it("loads a drink by its numeric API ID", async () => {
    mockGetDrinkById.mockResolvedValue(drinkWithNutrition(150, 20));
    const screen = await renderScreen(101);
    expect(await screen.findByText("Iced Coffee")).toBeTruthy();
    expect(mockGetDrinkById).toHaveBeenCalledWith(101);
  });

  it("shows not found for an invalid ID without calling the API", async () => {
    const screen = await renderScreen("101");
    expect(await screen.findByText("Drink not found.")).toBeTruthy();
    expect(mockGetDrinkById).not.toHaveBeenCalled();
  });

  it("shows not found when the API returns 404", async () => {
    mockGetDrinkById.mockRejectedValue({ isAxiosError: true, response: { status: 404 } });
    const screen = await renderScreen(999);
    expect(await screen.findByText("Drink not found.")).toBeTruthy();
  });

  it("shows nutrition from the selected size", async () => {
    mockGetDrinkById.mockResolvedValue(drinkWithNutrition(150, 20));
    const screen = await renderScreen(101);
    expect(await screen.findByText("150 cal")).toBeTruthy();
    expect(screen.getByText("20 g")).toBeTruthy();
  });

  it("shows missing values instead of negative nutrition", async () => {
    mockGetDrinkById.mockResolvedValue(drinkWithNutrition(null, -10));
    const screen = await renderScreen(101);
    expect(await screen.findByText("Iced Coffee")).toBeTruthy();
    expect(screen.getAllByText("Not provided")).toHaveLength(2);
    expect(screen.queryByText("-10 g")).toBeNull();
  });
});