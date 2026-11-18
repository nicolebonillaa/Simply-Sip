import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from "react";
import { isAxiosError } from "axios";
import { useAuth } from "../auth/AuthContext";
import type { Favorite } from "../types";
import {
  addCurrentUserCafeFavorite,
  addCurrentUserFavorite,
  deleteCurrentUserCafeFavorite,
  deleteCurrentUserFavorite,
  getCurrentUserCafeFavorites,
  getCurrentUserFavorites,
  type CafeFavorite,
} from "../services/favorites";

type FavoritesValue = {
  drinks: Favorite[];
  cafes: CafeFavorite[];
  loading: boolean;
  error: string | null;
  pendingDrinkIds: number[];
  pendingCafeIds: number[];
  refresh: () => Promise<void>;
  toggleDrink: (id: number) => Promise<void>;
  toggleCafe: (id: number) => Promise<void>;
};

const FavoritesContext = createContext<FavoritesValue | null>(null);

function failureLabel(name: string, reason: unknown): string {
  if (isAxiosError(reason)) {
    return `${name} (${reason.response?.status ?? reason.code ?? "network error"})`;
  }
  return `${name} (invalid response)`;
}

export function useFavorites(): FavoritesValue {
  const value = useContext(FavoritesContext);
  if (!value) throw new Error("useFavorites must be used within FavoritesProvider");
  return value;
}

export function FavoritesProvider({ children }: { children: React.ReactNode }) {
  const { token, status } = useAuth();
  const [drinks, setDrinks] = useState<Favorite[]>([]);
  const [cafes, setCafes] = useState<CafeFavorite[]>([]);
  const [loading, setLoading] = useState(status === "loading");
  const [error, setError] = useState<string | null>(null);
  const [pendingDrinkIds, setPendingDrinkIds] = useState<number[]>([]);
  const [pendingCafeIds, setPendingCafeIds] = useState<number[]>([]);
  const pendingDrinks = useRef(new Set<number>());
  const pendingCafes = useRef(new Set<number>());
  const version = useRef(0);
  const currentToken = useRef(token);
  currentToken.current = token;

  const refresh = useCallback(async () => {
    if (!token) return;
    const request = ++version.current;
    setLoading(true);
    setError(null);
    try {
      const [drinkResult, cafeResult] = await Promise.allSettled([
        getCurrentUserFavorites(token),
        getCurrentUserCafeFavorites(token),
      ]);
      if (request === version.current && currentToken.current === token) {
        if (drinkResult.status === "fulfilled") setDrinks(drinkResult.value);
        if (cafeResult.status === "fulfilled") setCafes(cafeResult.value);
        const failures = [
          drinkResult.status === "rejected" ? failureLabel("drinks", drinkResult.reason) : null,
          cafeResult.status === "rejected" ? failureLabel("cafés", cafeResult.reason) : null,
        ].filter(Boolean);
        if (failures.length) setError(`Unable to load ${failures.join(" and ")}. Please try again.`);
      }
    } finally {
      if (request === version.current && currentToken.current === token) setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    if (status === "signedIn" && token) {
      void refresh();
    } else {
      ++version.current;
      setDrinks([]);
      setCafes([]);
      setError(null);
      pendingDrinks.current.clear();
      pendingCafes.current.clear();
      setPendingDrinkIds([]);
      setPendingCafeIds([]);
      setLoading(status === "loading");
    }
    return () => { ++version.current; };
  }, [status, token, refresh]);

  const toggleDrink = useCallback(async (id: number) => {
    if (!token) throw new Error("Log in to save drinks.");
    if (pendingDrinks.current.has(id)) return;
    pendingDrinks.current.add(id);
    setPendingDrinkIds([...pendingDrinks.current]);
    setError(null);
    try {
      if (drinks.some((item) => item.drink?.id === id)) {
        await deleteCurrentUserFavorite(token, id);
        if (currentToken.current === token) setDrinks((items) => items.filter((item) => item.drink?.id !== id));
      } else {
        const created = await addCurrentUserFavorite(token, id);
        if (currentToken.current === token) setDrinks((items) => [...items.filter((item) => item.drink?.id !== id), created]);
      }
      if (currentToken.current === token) {
        ++version.current;
        setLoading(false);
      }
    } catch (cause) {
      if (currentToken.current === token) setError("Unable to update drink favorite. Please try again.");
      throw cause;
    } finally {
      pendingDrinks.current.delete(id);
      setPendingDrinkIds([...pendingDrinks.current]);
    }
  }, [token, drinks]);

  const toggleCafe = useCallback(async (id: number) => {
    if (!token) throw new Error("Log in to save cafés.");
    if (pendingCafes.current.has(id)) return;
    pendingCafes.current.add(id);
    setPendingCafeIds([...pendingCafes.current]);
    setError(null);
    try {
      if (cafes.some((item) => item.locationId === id)) {
        await deleteCurrentUserCafeFavorite(token, id);
        if (currentToken.current === token) setCafes((items) => items.filter((item) => item.locationId !== id));
      } else {
        const created = await addCurrentUserCafeFavorite(token, id);
        if (currentToken.current === token) setCafes((items) => [...items.filter((item) => item.locationId !== id), created]);
      }
      if (currentToken.current === token) {
        ++version.current;
        setLoading(false);
      }
    } catch (cause) {
      if (currentToken.current === token) setError("Unable to update café favorite. Please try again.");
      throw cause;
    } finally {
      pendingCafes.current.delete(id);
      setPendingCafeIds([...pendingCafes.current]);
    }
  }, [token, cafes]);

  return (
    <FavoritesContext.Provider value={{
      drinks, cafes, loading, error, pendingDrinkIds, pendingCafeIds,
      refresh, toggleDrink, toggleCafe,
    }}>
      {children}
    </FavoritesContext.Provider>
  );
}