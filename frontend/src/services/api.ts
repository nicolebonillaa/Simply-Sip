import { create } from "axios";

const api = create({
  baseURL:
    process.env.EXPO_PUBLIC_API_URL ??
    "http://localhost:8080",
  timeout: 15000,
});

export default api;
