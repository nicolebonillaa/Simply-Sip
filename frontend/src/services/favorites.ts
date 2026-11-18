import api from "./api";
import type { Favorite, Location } from "../types";
import { normalizeDrink, normalizeLocation, type ApiDrink, type ApiLocation } from "./normalizeApi";

type ApiFavorite = Omit<Favorite, "drink"> & { drink: ApiDrink };

export const getCurrentUserFavorites = async (
  token: string,
): Promise<Favorite[]> => {
  const response = await api.get<ApiFavorite[]>(`/api/favorites`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.data.map((favorite) => ({
    ...favorite,
    drink: normalizeDrink(favorite.drink),
  }));
};

export const addCurrentUserFavorite = async (
  token: string,
  drinkId: number,
): Promise<Favorite> => {
  const response = await api.post<ApiFavorite>(
    `/api/favorites`,
    {
      drinkId,
    },
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
  );

  return { ...response.data, drink: normalizeDrink(response.data.drink) };
};

export const deleteCurrentUserFavorite = async (
  token: string,
  drinkId: number,
): Promise<void> => {
  await api.delete(`/api/favorites/drinks/${drinkId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

export interface CafeFavorite {
  locationId: number;
  location: Location;
  dateAdded: string;
}

type ApiCafeFavorite = Omit<CafeFavorite, "location"> & { location: ApiLocation };

export const getCurrentUserCafeFavorites = async (
  token: string,
): Promise<CafeFavorite[]> => {
  const response = await api.get<ApiCafeFavorite[]>("/api/cafe-favorites", {
    headers: { Authorization: `Bearer ${token}` },
  });
  return response.data.map((favorite) => ({
    ...favorite,
    location: normalizeLocation(favorite.location),
  }));
};

export const addCurrentUserCafeFavorite = async (
  token: string,
  locationId: number,
): Promise<CafeFavorite> => {
  const response = await api.post<ApiCafeFavorite>(
    "/api/cafe-favorites",
    { locationId },
    { headers: { Authorization: `Bearer ${token}` } },
  );
  return { ...response.data, location: normalizeLocation(response.data.location) };
};

export const deleteCurrentUserCafeFavorite = async (
  token: string,
  locationId: number,
): Promise<void> => {
  await api.delete(`/api/cafe-favorites/${locationId}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
};