import React from "react";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import HomePage from "../screens/HomePage";
import SearchResults from "../screens/SearchResults";
import MapScreen from "../screens/MapScreen";

const Stack = createNativeStackNavigator();

export default function HomeStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="HomeMain" component={HomePage} />
      <Stack.Screen name="SearchResults" component={SearchResults} />
      <Stack.Screen name="MapScreen" component={MapScreen} />
    </Stack.Navigator>
  );
}
