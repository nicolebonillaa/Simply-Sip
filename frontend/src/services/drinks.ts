import api from "./api";
import type { Allergen, Drink, DrinkParams } from "../types";
import { normalizeDrink, type ApiDrink } from "./normalizeApi";

export const getDrinks = async (
  params?: DrinkParams,
): Promise<Drink[]> => {
  const response = await api.get<ApiDrink[]>("/api/drinks", { params });
  return response.data.map(normalizeDrink);
};

export const getDrinkById = async (id: number): Promise<Drink> => {
  const response = await api.get<ApiDrink>(`/api/drinks/${id}`);
  return normalizeDrink(response.data);
};

export const getAllergens = async (): Promise<Allergen[]> => {
  const response = await api.get<Allergen[]>("/api/allergens");
  return response.data;
};