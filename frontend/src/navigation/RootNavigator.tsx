import React from "react";
import {
  DarkTheme,
  DefaultTheme,
  NavigationContainer,
} from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";

import { useTheme } from "../theme/ThemeProvider";
import MainTabs from "./MainTabs";
import DrinkDetails from "../screens/DrinkDetails";
import CafeDetails from "../screens/CafeDetails";
import MapScreen from "../screens/MapScreen";
import RegisterPage from "../screens/RegisterPage";
import LoginPage from "../screens/LoginPage";
import SettingsPage from "../screens/SettingsPage";

const RootStack = createNativeStackNavigator();

export default function RootNavigator() {
  const { theme, isDark } = useTheme();
  const navigationTheme = isDark ? DarkTheme : DefaultTheme;

  return (
    <NavigationContainer
      theme={{
        ...navigationTheme,
        colors: {
          ...navigationTheme.colors,
          background: theme.background,
          card: theme.background,
          text: theme.text,
          primary: theme.button,
          border: theme.divider,
        },
      }}
    >
      <RootStack.Navigator screenOptions={{ headerShown: false }}>
        <RootStack.Screen name="MainTabs" component={MainTabs} />
        <RootStack.Screen name="DrinkDetails" component={DrinkDetails} />
        <RootStack.Screen name="CafeDetails" component={CafeDetails} />
        <RootStack.Screen name="CafeMap" component={MapScreen} />
        <RootStack.Screen name="Register" component={RegisterPage} />
        <RootStack.Screen name="Login" component={LoginPage} />
        <RootStack.Screen name="Settings" component={SettingsPage} />
      </RootStack.Navigator>
    </NavigationContainer>
  );
}
