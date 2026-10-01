import api from "./api";
import { Drink, DrinkParams } from "../types";

export const getDrinks = async (params?: DrinkParams): Promise<Drink[]> => {
  const response = await api.get("/api/drinks", { params });
  return response.data;
};
