import React, {
  createContext,
  useContext,
  useEffect,
  useRef,
  useState,
  useCallback,
} from "react";
import * as SecureStore from "expo-secure-store";
import api from "../services/api";
const TOKEN_KEY = "auth_token";
const USER_KEY = "auth_user";

export type AuthUser = { userId: number; name: string; email: string };
type Status = "loading" | "signedOut" | "signedIn";

type AuthContextValue = {
  status: Status;
  user: AuthUser | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside <AuthProvider>");
  return ctx;
};

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [status, setStatus] = useState<Status>("loading");
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const tokenRef = useRef<string | null>(null);

  // Restore saved session on app start
  useEffect(() => {
    (async () => {
      try {
        const savedToken = await SecureStore.getItemAsync(TOKEN_KEY);
        const savedUser = await SecureStore.getItemAsync(USER_KEY);
        if (savedToken && savedUser) {
          tokenRef.current = savedToken;
          setToken(savedToken);
          setUser(JSON.parse(savedUser));
          setStatus("signedIn");
          return;
        }
      } catch {
        // fall through to signed out
      }
      setStatus("signedOut");
    })();
  }, []);

  const login = async (email: string, password: string) => {
    let data: any;
    try {
      const res = await api.post("/api/auth/login", {
        email: email.trim(),
        password,
      });
      data = res.data;
    } catch (e: any) {
      if (e.response) {
        throw new Error(
          e.response.data?.message ?? "Something went wrong. Please try again.",
        );
      }
      throw new Error(
        "Can't reach the server. Check your connection and try again.",
      );
    }

    const nextUser: AuthUser = {
      userId: data.userId,
      name: data.name,
      email: data.email,
    };
    await SecureStore.setItemAsync(TOKEN_KEY, data.token);
    await SecureStore.setItemAsync(USER_KEY, JSON.stringify(nextUser));
    tokenRef.current = data.token;
    setToken(data.token);
    setUser(nextUser);
    setStatus("signedIn");
  };

  const signOut = useCallback(async () => {
    tokenRef.current = null;
    setToken(null);
    setUser(null);
    setStatus("signedOut");
    await SecureStore.deleteItemAsync(TOKEN_KEY);
    await SecureStore.deleteItemAsync(USER_KEY);
  }, []);

  return (
    <AuthContext.Provider value={{ status, user, token, login, signOut }}>
      {children}
    </AuthContext.Provider>
  );
}
