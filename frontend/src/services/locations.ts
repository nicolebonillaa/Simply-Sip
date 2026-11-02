import api from "./api";
import { Location } from "../types";

export const getLocations = async (): Promise<Location[]> => {
  const response = await api.get("/api/locations");
  return response.data;
};

export const getLocationById = async (id: number): Promise<Location> => {
  const response = await api.get<Location>(`/api/locations/${id}`);
  return response.data;
}
