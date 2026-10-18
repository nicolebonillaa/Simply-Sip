import api from "./api";
import { User, UpdateUserRequest } from "../types";

export const getCurrentUser = async (token: string): Promise<User> => {
  const response = await api.get("/api/users/me", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.data;
};

export const getUser = async (userId: number, token: string): Promise<User> => {
  const response = await api.get(`/api/users/${userId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.data;
};

export const updateUser = async (
  userId: number,
  userData: UpdateUserRequest,
  token: string,
): Promise<User> => {
  const response = await api.put(`/api/users/${userId}`, userData, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.data;
};
