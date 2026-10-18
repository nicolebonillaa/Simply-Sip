export interface Drink {
  id: number;
  name: string;
  category: string;
  tags: string;
  location: Location;
  nutritionFacts: NutritionFacts;
  createdAt: string;
  updatedAt: string;
}

export interface DrinkParams {
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
  calories: number;
  sugar: number;
  caffeine: number;
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

export type SortKey = "calories" | "sugar" | "price" | "distance" | "rating";
 
export interface Filters {
  // "Featured" sort options that are ticked (applied in a fixed order)
  sort: SortKey[];
 
  // Dietary needs
  vegan: boolean;
  vegetarian: boolean;
  dairyFree: boolean;
  glutenFree: boolean;
 
  // Sliders. null = "Any" (slider left at its far end, no limit)
  maxCalories: number | null;
  maxSugar: number | null;
  minPrice: number | null;
  maxPrice: number | null;
  maxDistance: number | null;
 
  // Multi-selects
  allergens: string[]; // allergens to avoid
  milkTypes: string[];
}