/**
 * Simply Sip App
 *
 * @format
 */

import React from "react";
import RootNavigator from "./src/navigation/RootNavigator";
import { ThemeProvider } from "./src/theme/ThemeProvider";
import { AuthProvider } from "./src/auth/AuthContext";
import { FavoritesProvider } from "./src/favorites/FavoritesContext";

function App(): React.JSX.Element {
  return (
    <AuthProvider>
      <FavoritesProvider>
        <ThemeProvider>
          <RootNavigator />
        </ThemeProvider>
      </FavoritesProvider>
    </AuthProvider>
  );
}

export default App;
