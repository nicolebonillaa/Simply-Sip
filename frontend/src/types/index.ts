export interface Allergen {
  id: number;
  name: string;
}

export interface Drink {
  id: number;
  name: string;
  category: string;
  tags: string | null;
  location: Location | null;
  nutritionFacts: NutritionFacts | null;
  allergens?: Allergen[] | null;
  createdAt: string;
  updatedAt: string;
}

export interface DrinkParams {
  q?: string;
  maxCaffeine?: number;
  excludeAllergens?: string;
  category?: string;
  locationId?: number;
  maxCalories?: number;
  maxSugar?: number;
  tag?: string;
}

export interface Location {
  id: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  createdAt: string;
  updatedAt: string;
}

export interface NutritionFacts {
  id: number;
  calories: number | null;
  sugar: number | null;
  caffeine: number | null;
}

export interface User {
  id: number;
  name: string;
  email: string;
  createdAt: string;
  updatedAt: string;
  favorites?: Favorite[];
}

export interface UpdateUserRequest {
  name?: string;
  email?: string;
  password?: string;
}

export interface Favorite {
  id: {
    userId: number;
    drinkId: number;
  };
  user: User;
  drink: Drink;
  dateAdded: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export type SortKey =
  | "calories"
  | "sugar"
  | "price"
  | "distance"
  | "rating";

export interface Filters {
  sort: SortKey[];

  vegan: boolean;
  vegetarian: boolean;
  dairyFree: boolean;
  glutenFree: boolean;

  maxCalories: number | null;
  maxSugar: number | null;
  minPrice: number | null;
  maxPrice: number | null;
  maxDistance: number | null;

  allergens: string[];
  milkTypes: string[];
}