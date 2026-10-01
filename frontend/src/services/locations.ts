import api from "./api";
import { Location } from "../types";

export const getLocations = async (): Promise<Location[]> => {
  const response = await api.get("/api/locations");
  return response.data;
};
