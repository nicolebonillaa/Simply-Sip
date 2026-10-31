import { create } from "axios";

<<<<<<< HEAD
const api = axios.create({
  baseURL: process.env.EXPO_PUBLIC_API_URL ?? "http://localhost:8080",
=======
const api = create({
  baseURL: "http://localhost:8080",
  timeout: 15000,
>>>>>>> origin/main
});

export default api;