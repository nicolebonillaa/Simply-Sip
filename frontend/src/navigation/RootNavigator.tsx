import React from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import MainTabs from "./MainTabs";
import Filters from "../screens/Filters";

const RootStack = createNativeStackNavigator();

export default function RootNavigator() {
  return (
    <NavigationContainer>
      <RootStack.Navigator screenOptions={{ headerShown: false }}>
        <RootStack.Screen name="MainTabs" component={MainTabs} />
        <RootStack.Screen
          name="Filters"
          component={Filters}
          options={{ presentation: "modal", headerShown: false }}
        />
      </RootStack.Navigator>
    </NavigationContainer>
  );
}
