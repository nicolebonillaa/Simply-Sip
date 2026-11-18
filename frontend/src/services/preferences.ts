import api from "./api";

export interface SavedPreferences {
  dietaryPreferences: string[];
  excludedAllergens: string[];
}

export const getSavedPreferences = async (token: string): Promise<SavedPreferences> => {
  const response = await api.get<SavedPreferences>("/api/users/me/preferences", {
    headers: { Authorization: `Bearer ${token}` },
  });
  return response.data;
};

export const updateSavedPreferences = async (
  token: string,
  preferences: SavedPreferences,
): Promise<SavedPreferences> => {
  const response = await api.put<SavedPreferences>(
    "/api/users/me/preferences",
    preferences,
    { headers: { Authorization: `Bearer ${token}` } },
  );
  return response.data;
};
