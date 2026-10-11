import type { Filters, SortKey } from "../types";

// Slider ranges. Dragging a slider to its far end means "Any" (stored as null).
export const FILTER_LIMITS = {
  calories: { min: 0, max: 600, step: 10 },
  sugar: { min: 0, max: 70, step: 1 },
  price: { min: 0, max: 20, step: 1 },
  distance: { min: 0.25, max: 2, step: 0.25 }, // app only shows spots within 2 miles
} as const;

export const SORT_OPTIONS: { key: SortKey; label: string; short: string }[] = [
  { key: "calories", label: "Calories: low to high", short: "Calories" },
  { key: "sugar", label: "Sugar: low to high", short: "Sugar" },
  { key: "price", label: "Price: low to high", short: "Price" },
  { key: "distance", label: "Distance: nearest first", short: "Distance" },
  { key: "rating", label: "Rating: high to low", short: "Rating" },
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

export const ALLERGEN_OPTIONS = ["Peanuts", "Tree nuts", "Soy", "Eggs", "Wheat"];
export const MILK_OPTIONS = ["Whole", "Skim", "Oat", "Almond", "Soy", "Coconut"];

export function priceLabel(min: number | null, max: number | null): string {
  if (min !== null && max !== null) return `$${min}–$${max}`;
  if (min !== null) return `$${min}+`;
  if (max !== null) return `≤ $${max}`;
  return "";
}

/** How many filter categories are active (shown as the badge on the options icon). */
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

/** Short labels for the tags under the search bar. */
export function describeFilters(f: Filters): string[] {
  const tags: string[] = [];
  if (f.sort.length) {
    const names = SORT_OPTIONS.filter((o) => f.sort.includes(o.key)).map((o) => o.short);
    tags.push(`Sorted: ${names.join(", ")}`);
  }
  if (f.vegan) tags.push("Vegan");
  if (f.vegetarian) tags.push("Vegetarian");
  if (f.dairyFree) tags.push("Dairy-free");
  if (f.glutenFree) tags.push("Gluten-free");
  if (f.maxCalories !== null) tags.push(`≤ ${f.maxCalories} cal`);
  if (f.maxSugar !== null) tags.push(`≤ ${f.maxSugar} g sugar`);
  const price = priceLabel(f.minPrice, f.maxPrice);
  if (price) tags.push(price);
  if (f.maxDistance !== null) tags.push(`≤ ${f.maxDistance} mi`);
  f.allergens.forEach((a) => tags.push(`No ${a.toLowerCase()}`));
  f.milkTypes.forEach((m) => tags.push(`${m} milk`));
  return tags;
}

type AnySpot = Record<string, any>;

const toNumber = (v: unknown): number | null => {
  if (typeof v === "number" && Number.isFinite(v)) return v;
  if (typeof v === "string") {
    const n = parseFloat(v);
    return Number.isFinite(n) ? n : null;
  }
  return null;
};

const toList = (v: unknown): string[] | null =>
  Array.isArray(v) ? v.map(String) : null;

export interface LatLng {
  latitude: number;
  longitude: number;
}

// Straight-line distance in miles (haversine).
const milesBetween = (a: LatLng, b: LatLng): number => {
  const rad = (d: number) => (d * Math.PI) / 180;
  const dLat = rad(b.latitude - a.latitude);
  const dLon = rad(b.longitude - a.longitude);
  const h =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(rad(a.latitude)) * Math.cos(rad(b.latitude)) * Math.sin(dLon / 2) ** 2;
  return 3958.8 * 2 * Math.asin(Math.sqrt(h));
};

// "Dairy-free, Vegan" -> ["dairyfree", "vegan"]
const parseTags = (v: unknown): string[] | null => {
  if (Array.isArray(v)) v = v.join(",");
  if (typeof v !== "string") return null;
  return v
    .split(/[,;|]/)
    .map((s) => s.trim().toLowerCase().replace(/[\s_-]+/g, ""))
    .filter(Boolean);
};

export function getSpotFacts(spot: AnySpot, userLocation?: LatLng | null) {
  const nutrition = spot.nutritionFacts ?? {};
  const loc = spot.location ?? spot;
  const hasCoords = typeof loc?.latitude === "number" && typeof loc?.longitude === "number";

  return {
    calories: toNumber(nutrition.calories ?? spot.calories),
    sugar: toNumber(nutrition.sugar ?? spot.sugar),
    price: toNumber(spot.price),
    distance:
      toNumber(spot.distance) ??
      (userLocation && hasCoords ? milesBetween(userLocation, loc) : null),
    rating: toNumber(spot.rating),
    dietary: parseTags(spot.tags ?? spot.dietary),
    allergens: toList(spot.allergens),
    milkTypes: toList(spot.milkTypes),
  };
}

const DIET_KEYS = ["vegan", "vegetarian", "dairyFree", "glutenFree"] as const;

export function applyFilters<T extends AnySpot>(
  spots: T[],
  f: Filters,
  userLocation?: LatLng | null,
): T[] {
  const filtered = spots.filter((spot) => {
    const facts = getSpotFacts(spot, userLocation);

    if (f.maxCalories !== null && (facts.calories === null || facts.calories > f.maxCalories)) return false;
    if (f.maxSugar !== null && (facts.sugar === null || facts.sugar > f.maxSugar)) return false;
    if (f.maxDistance !== null && (facts.distance === null || facts.distance > f.maxDistance)) return false;

    if (f.minPrice !== null || f.maxPrice !== null) {
      if (facts.price === null) return false;
      if (f.minPrice !== null && facts.price < f.minPrice) return false;
      if (f.maxPrice !== null && facts.price > f.maxPrice) return false;
    }

    for (const key of DIET_KEYS) {
      if (f[key] && !(facts.dietary ?? []).includes(key.toLowerCase())) return false;
    }

    if (f.allergens.length) {
      if (facts.allergens === null) return false;
      if (f.allergens.some((a) => facts.allergens!.includes(a))) return false;
    }

    if (f.milkTypes.length) {
      if (facts.milkTypes === null) return false;
      if (!f.milkTypes.some((m) => facts.milkTypes!.includes(m))) return false;
    }

    return true;
  });

  if (!f.sort.length) return filtered;

  // Ticked sorts apply in the list's fixed order; later ones break ties.
  const order = SORT_OPTIONS.filter((o) => f.sort.includes(o.key)).map((o) => o.key);
  const value = (spot: AnySpot, key: SortKey): number => {
    const facts = getSpotFacts(spot, userLocation);
    const v =
      key === "calories" ? facts.calories :
      key === "sugar" ? facts.sugar :
      key === "price" ? facts.price :
      key === "distance" ? facts.distance :
      facts.rating !== null ? -facts.rating : null; // rating: high to low
    return v === null ? Number.POSITIVE_INFINITY : v; // missing data sorts last
  };

  return filtered.slice().sort((a, b) => {
    for (const key of order) {
      const diff = value(a, key) - value(b, key);
      if (diff !== 0 && !Number.isNaN(diff)) return diff;
    }
    return 0;
  });
}