import api from "./api";
import { Favorite } from "../types";

export const getCurrentUserFavorites = async (
  token: string,
): Promise<Favorite[]> => {
  const response = await api.get(`/api/favorites`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.data;
};

export const addCurrentUserFavorite = async (
  token: string,
  drinkId: number,
): Promise<Favorite> => {
  const response = await api.post(
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

  return response.data;
};

export const deleteCurrentUserFavorite = async (
  token: string,
  userId: number,
  drinkId: number,
): Promise<void> => {
  await api.delete(`/api/favorites/${userId}/${drinkId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};
