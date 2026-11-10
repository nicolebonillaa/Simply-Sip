import api from "./api";
import type { Location } from "../types";
import { normalizeLocation, type ApiLocation } from "./normalizeApi";

export const getLocations = async (): Promise<Location[]> => {
  const response = await api.get<ApiLocation[]>("/api/locations");
  return response.data.map(normalizeLocation);
};

export const getLocationById = async (
  id: number,
): Promise<Location> => {
  const response = await api.get<ApiLocation>(`/api/locations/${id}`);
  return normalizeLocation(response.data);
};