import React, { createContext, useContext, useMemo, useState } from "react";
import { useColorScheme } from "react-native";

// Vintage Garden palette
export const palette = {
  sand: "#E6D2B9",
  mocha: "#B08F7A",
  chocolate: "#795138",
  espresso: "#5B3113",
  babyPink: "#FFDBDD",
  blush: "#EFBFC2",
  dustyRose: "#CB8D95",
  mauve: "#A7626B",
};

export const lightTheme = {
  background: "#FFFFFF",
  text: palette.espresso,
  subtext: palette.chocolate,
  icon: palette.mauve,
  divider: "rgba(91, 49, 19, 0.12)",
  band: "rgba(176, 143, 122, 0.14)",
  button: palette.mauve,
  buttonText: "#FFFFFF",
  chip: "rgba(176, 143, 122, 0.18)",
  chipActive: palette.mauve,
  chipText: palette.espresso,
  chipActiveText: "#FFFFFF",
  inputBorder: "rgba(121, 81, 56, 0.45)",
  scrim: "rgba(40, 20, 5, 0.28)",
};

export const darkTheme: typeof lightTheme = {
  background: palette.espresso,
  text: palette.sand,
  subtext: "rgba(230, 210, 185, 0.75)",
  icon: palette.blush,
  divider: "rgba(230, 210, 185, 0.16)",
  band: "rgba(255, 255, 255, 0.07)",
  button: palette.blush,
  buttonText: palette.espresso,
  chip: "rgba(230, 210, 185, 0.12)",
  chipActive: palette.blush,
  chipText: palette.sand,
  chipActiveText: palette.espresso,
  inputBorder: "rgba(230, 210, 185, 0.4)",
  scrim: "rgba(0, 0, 0, 0.4)",
};

export type Theme = typeof lightTheme;
export type ThemeMode = "system" | "light" | "dark";

interface ThemeContextValue {
  theme: Theme;
  isDark: boolean;
  mode: ThemeMode;
  setMode: (mode: ThemeMode) => void;
}

const ThemeContext = createContext<ThemeContextValue>({
  theme: lightTheme,
  isDark: false,
  mode: "system",
  setMode: () => {},
});

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const systemScheme = useColorScheme();
  const [mode, setMode] = useState<ThemeMode>("system");

  const value = useMemo<ThemeContextValue>(() => {
    const isDark = mode === "system" ? systemScheme === "dark" : mode === "dark";
    return { theme: isDark ? darkTheme : lightTheme, isDark, mode, setMode };
  }, [mode, systemScheme]);

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

export const useTheme = () => useContext(ThemeContext);