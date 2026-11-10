import type { Drink, DrinkSize, Location, NutritionFacts } from "../types";

export interface ApiLocation {
  id: number;
  cafeName: string | null;
  locationName: string | null;
  streetAddress: string | null;
  city: string | null;
  state: string | null;
  zip: string | null;
  latitude: number | null;
  longitude: number | null;
  createdAt: string;
  updatedAt: string;
}

interface ApiNutritionFacts {
  id: number;
  calories: number | null;
  sugarsG: number | null;
  caffeineMg: number | null;
  totalFatG: number | null;
  cholesterolMg: number | null;
  sodiumMg: number | null;
  carbsG: number | null;
  proteinG: number | null;
}

interface ApiDrinkSize {
  id: number;
  sizeName: string;
  ounces: number | null;
  price: number | null;
  nutritionFacts: ApiNutritionFacts | null;
}

export interface ApiDrink {
  id: number;
  name: string;
  category: string;
  description: string | null;
  sourceUrl: string | null;
  allergensKnown: boolean;
  allergens: string[];
  location: ApiLocation | null;
  sizes: ApiDrinkSize[];
  createdAt: string;
  updatedAt: string;
}

const label = (value: string | null | undefined) => value?.trim() || "";

export function normalizeLocation(location: ApiLocation): Location {
  const cafeName = label(location.cafeName);
  const branchName = label(location.locationName);

  return {
    ...location,
    name: cafeName
      ? branchName && branchName.toLowerCase() !== cafeName.toLowerCase()
        ? `${cafeName} - ${branchName}`
        : cafeName
      : "Café unavailable",
    address: [
      location.streetAddress,
      location.city,
      location.state,
      location.zip,
    ]
      .map(label)
      .filter(Boolean)
      .join(", "),
    latitude: location.latitude as number,
    longitude: location.longitude as number,
  };
}

function normalizeNutrition(
  facts: ApiNutritionFacts | null,
): NutritionFacts | null {
  if (!facts) return null;

  return {
    id: facts.id,
    calories: facts.calories,
    sugar: facts.sugarsG,
    caffeine: facts.caffeineMg,
    totalFatG: facts.totalFatG,
    cholesterolMg: facts.cholesterolMg,
    sodiumMg: facts.sodiumMg,
    carbsG: facts.carbsG,
    proteinG: facts.proteinG,
  };
}

export function normalizeDrink(drink: ApiDrink): Drink {
  const sizes: DrinkSize[] = (drink.sizes ?? []).map((size) => ({
    id: size.id,
    sizeName: size.sizeName,
    ounces: size.ounces,
    price: size.price,
    nutritionFacts: normalizeNutrition(size.nutritionFacts),
  }));

  const defaultSize = sizes[0];

  return {
    id: drink.id,
    name: drink.name,
    category: drink.category,
    tags: null,
    price: defaultSize?.price ?? null,
    milkTypes: [],
    allergensKnown: drink.allergensKnown,
    allergens: drink.allergens ?? [],
    location: drink.location ? normalizeLocation(drink.location) : null,
    sizes,
    nutritionFacts: defaultSize?.nutritionFacts ?? null,
    createdAt: drink.createdAt,
    updatedAt: drink.updatedAt,
  };
}