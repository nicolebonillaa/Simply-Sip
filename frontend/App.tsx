/**
 * Simply Sip App
 *
 * @format
 */

import React from "react";
import RootNavigator from "./src/navigation/RootNavigator";
import { ThemeProvider } from "./src/theme/ThemeProvider";

function App(): React.JSX.Element {
  return (
    <ThemeProvider>
      <RootNavigator />
    </ThemeProvider>
  );
}

export default App;
