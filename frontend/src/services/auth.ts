import api from "./api";
import { LoginRequest, RegisterRequest } from "../types";

export const login = async (data: LoginRequest) => {
  const response = await api.post("/api/auth/login", data);
  return response.data;
};

export const register = async (data: RegisterRequest) => {
  const response = await api.post("/api/auth/register", data);
  return response.data;
};
