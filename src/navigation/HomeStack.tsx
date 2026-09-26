import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import HomePage from '../screens/HomePage';
// import DrinkDetails from '../screens/DrinkDetails'; // add once this screen exists

const Stack = createNativeStackNavigator();

export default function HomeStack() {
  return (
    <Stack.Navigator
      screenOptions={{
        headerShown: false,
      }}>
      <Stack.Screen
        name="HomeMain"
        component={HomePage}
      />

      {/* Add DrinkDetails later */}
    </Stack.Navigator>
  );
}