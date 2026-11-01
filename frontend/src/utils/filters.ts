import type { DrinkParams, Filters, SortKey } from "../types";

export const FILTER_LIMITS = {
  calories: { min: 0, max: 600, step: 10 },
  sugar: { min: 0, max: 70, step: 1 },
  price: { min: 0, max: 20, step: 1 },
  distance: { min: 0.25, max: 2, step: 0.25 },
} as const;

export const SORT_OPTIONS: {
  key: SortKey;
  label: string;
  short: string;
}[] = [
  {
    key: "calories",
    label: "Calories: low to high",
    short: "Calories",
  },
  {
    key: "sugar",
    label: "Sugar: low to high",
    short: "Sugar",
  },
  {
    key: "distance",
    label: "Distance: nearest first",
    short: "Distance",
  },
];

export const DEFAULT_FILTERS: Filters = {
  sort: [],
  vegan: false,
  vegetarian: false,
  dairyFree: false,
  glutenFree: false,
  maxCalories: null,
  maxSugar: null,
  minPrice: null,
  maxPrice: null,
  maxDistance: null,
  allergens: [],
  milkTypes: [],
};

export const ALLERGEN_OPTIONS = [
  "Peanuts",
  "Tree nuts",
  "Soy",
  "Eggs",
  "Wheat",
];

export const MILK_OPTIONS = [
  "Whole",
  "Skim",
  "Oat",
  "Almond",
  "Soy",
  "Coconut",
];

export function priceLabel(
  min: number | null,
  max: number | null,
): string {
  if (min !== null && max !== null) return `$${min}–$${max}`;
  if (min !== null) return `$${min}+`;
  if (max !== null) return `≤ $${max}`;
  return "";
}

export function countActiveFilters(f: Filters): number {
  return [
    f.sort.length > 0,
    f.vegan || f.vegetarian || f.dairyFree || f.glutenFree,
    f.maxCalories !== null,
    f.maxSugar !== null,
    f.allergens.length > 0,
    f.milkTypes.length > 0,
    f.minPrice !== null || f.maxPrice !== null,
    f.maxDistance !== null,
  ].filter(Boolean).length;
}

export function describeFilters(f: Filters): string[] {
  const tags: string[] = [];

  if (f.sort.length) {
    const names = SORT_OPTIONS
      .filter((option) => f.sort.includes(option.key))
      .map((option) => option.short);

    tags.push(`Sorted: ${names.join(", ")}`);
  }

  if (f.vegan) tags.push("Vegan");
  if (f.vegetarian) tags.push("Vegetarian");
  if (f.dairyFree) tags.push("Dairy-free");
  if (f.glutenFree) tags.push("Gluten-free");

  if (f.maxCalories !== null) {
    tags.push(`≤ ${f.maxCalories} cal`);
  }

  if (f.maxSugar !== null) {
    tags.push(`≤ ${f.maxSugar} g sugar`);
  }

  const price = priceLabel(f.minPrice, f.maxPrice);

  if (price) tags.push(price);

  if (f.maxDistance !== null) {
    tags.push(`≤ ${f.maxDistance} mi`);
  }

  f.allergens.forEach((allergen) => {
    tags.push(`No ${allergen.toLowerCase()}`);
  });

  f.milkTypes.forEach((milk) => {
    tags.push(`${milk} milk`);
  });

  return tags;
}

type AnySpot = Record<string, any>;

const toNumber = (value: unknown): number | null => {
  if (typeof value === "number" && Number.isFinite(value)) {
    return value;
  }

  if (typeof value === "string") {
    const number = parseFloat(value);
    return Number.isFinite(number) ? number : null;
  }

  return null;
};

const normalize = (value: string) =>
  value.trim().toLowerCase().replace(/[\s_-]+/g, "");

const toList = (value: unknown): string[] | null => {
  if (!Array.isArray(value)) return null;

  return value.flatMap((item: unknown) => {
    if (typeof item === "string") {
      return [normalize(item)];
    }

    if (
      item &&
      typeof item === "object" &&
      "name" in item &&
      typeof item.name === "string"
    ) {
      return [normalize(item.name)];
    }

    return [];
  });
};

export interface LatLng {
  latitude: number;
  longitude: number;
}

const milesBetween = (a: LatLng, b: LatLng): number => {
  const rad = (degrees: number) => (degrees * Math.PI) / 180;
  const dLat = rad(b.latitude - a.latitude);
  const dLon = rad(b.longitude - a.longitude);

  const h =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(rad(a.latitude)) *
      Math.cos(rad(b.latitude)) *
      Math.sin(dLon / 2) ** 2;

  return 3958.8 * 2 * Math.asin(Math.sqrt(h));
};

const parseTags = (value: unknown): string[] | null => {
  if (Array.isArray(value)) value = value.join(",");
  if (typeof value !== "string") return null;

  return value
    .split(/[,;|]/)
    .map((tag) => normalize(tag))
    .filter(Boolean);
};

export function getSpotFacts(
  spot: AnySpot,
  userLocation?: LatLng | null,
) {
  const nutrition = spot.nutritionFacts ?? {};
  const location = spot.location ?? spot;

  const hasCoords =
    typeof location?.latitude === "number" &&
    typeof location?.longitude === "number";

  return {
    calories: toNumber(nutrition.calories ?? spot.calories),
    sugar: toNumber(nutrition.sugar ?? spot.sugar),
    price: toNumber(spot.price),
    distance:
      toNumber(spot.distance) ??
      (userLocation && hasCoords
        ? milesBetween(userLocation, location)
        : null),
    rating: toNumber(spot.rating),
    dietary: parseTags(spot.tags ?? spot.dietary),
    allergens: toList(spot.allergens),
    milkTypes: toList(spot.milkTypes),
  };
}

const DIET_KEYS = [
  "vegan",
  "vegetarian",
  "dairyFree",
  "glutenFree",
] as const;

export function applyFilters<T extends AnySpot>(
  spots: T[],
  filters: Filters,
  userLocation?: LatLng | null,
): T[] {
  const filtered = spots.filter((spot) => {
    const facts = getSpotFacts(spot, userLocation);

    if (
      filters.maxCalories !== null &&
      (facts.calories === null ||
        facts.calories > filters.maxCalories)
    ) {
      return false;
    }

    if (
      filters.maxSugar !== null &&
      (facts.sugar === null || facts.sugar > filters.maxSugar)
    ) {
      return false;
    }

    if (
      filters.maxDistance !== null &&
      (facts.distance === null ||
        facts.distance > filters.maxDistance)
    ) {
      return false;
    }

    if (filters.minPrice !== null || filters.maxPrice !== null) {
      if (facts.price === null) return false;

      if (
        filters.minPrice !== null &&
        facts.price < filters.minPrice
      ) {
        return false;
      }

      if (
        filters.maxPrice !== null &&
        facts.price > filters.maxPrice
      ) {
        return false;
      }
    }

    for (const key of DIET_KEYS) {
      if (
        filters[key] &&
        !(facts.dietary ?? []).includes(key.toLowerCase())
      ) {
        return false;
      }
    }

    if (filters.allergens.length) {
      if (facts.allergens === null) return false;

      if (
        filters.allergens.some((allergen) =>
          facts.allergens!.includes(normalize(allergen)),
        )
      ) {
        return false;
      }
    }

    if (filters.milkTypes.length) {
      if (facts.milkTypes === null) return false;

      if (
        !filters.milkTypes.some((milk) =>
          facts.milkTypes!.includes(normalize(milk)),
        )
      ) {
        return false;
      }
    }

    return true;
  });

  if (!filters.sort.length) return filtered;

  const order = SORT_OPTIONS
    .filter((option) => filters.sort.includes(option.key))
    .map((option) => option.key);

  const value = (spot: AnySpot, key: SortKey): number => {
    const facts = getSpotFacts(spot, userLocation);

    const result =
      key === "calories"
        ? facts.calories
        : key === "sugar"
          ? facts.sugar
          : key === "price"
            ? facts.price
            : key === "distance"
              ? facts.distance
              : facts.rating !== null
                ? -facts.rating
                : null;

    return result === null ? Number.POSITIVE_INFINITY : result;
  };

  return filtered.slice().sort((a, b) => {
    for (const key of order) {
      const difference = value(a, key) - value(b, key);

      if (difference !== 0 && !Number.isNaN(difference)) {
        return difference;
      }
    }

    return 0;
  });
}

export function supportedFilters(filters: Filters): Filters {
  return {
    ...filters,
    sort: filters.sort.filter(
      (key) =>
        key === "calories" ||
        key === "sugar" ||
        key === "distance",
    ),
    minPrice: null,
    maxPrice: null,
    milkTypes: [],
  };
}

export function toDrinkParams(
  query: string,
  filters: Filters,
): DrinkParams {
  const dietaryTags = [
    filters.vegan ? "vegan" : null,
    filters.vegetarian ? "vegetarian" : null,
    filters.dairyFree ? "dairy-free" : null,
    filters.glutenFree ? "gluten-free" : null,
  ].filter((tag): tag is string => tag !== null);

  return {
    q: query.trim() || undefined,
    maxCalories: filters.maxCalories ?? undefined,
    maxSugar: filters.maxSugar ?? undefined,
    tag: dietaryTags[0],
    excludeAllergens: filters.allergens.length
      ? filters.allergens.join(",")
      : undefined,
  };
}