import api from "./api";
import type { Allergen, Drink, DrinkParams } from "../types";

export const getDrinks = async (
  params?: DrinkParams,
): Promise<Drink[]> => {
  const response = await api.get<Drink[]>("/api/drinks", { params });
  return response.data;
};

export const getAllergens = async (): Promise<Allergen[]> => {
  const response = await api.get<Allergen[]>("/api/allergens");
  return response.data;
};