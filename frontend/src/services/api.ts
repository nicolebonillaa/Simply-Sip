import { create } from "axios";

const api = create({
  baseURL: "http://localhost:8080",
  timeout: 15000,
});

export default api;