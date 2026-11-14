import React, { useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator,
  FlatList,
  Image,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { useTheme } from "../theme/ThemeProvider";
import type { Theme } from "../theme/ThemeProvider";
import { drinks, stores, favoriteDrinks } from "../fakeDrinks";
import {useNavigation} from "@react-navigation/native";
import {SafeAreaView} from "react-native-safe-area-context";

export default function FavoritesPage() {
  const navigation = useNavigation();

  return (
    <SafeAreaView style={styles.container}>
    <Text style={styles.sectionTitle}>Coffee</Text>
      <FlatList 
        data={favoriteDrinks.filter(drink => drink.category === "Coffee")}
        keyExtractor={(item) => String(item.id)}
        horizontal
        showsHorizontalScrollIndicator={false}
        renderItem={({item}) => (
          //<Text style={styles.name}>{item.name}</Text>
          <Pressable onPress={() => navigation.navigate("DrinkDetails", {drinkId: item.id})}>
           <View style={styles.card}>
              <View style={styles.imagePlaceholder} />
              <Text style={styles.name}>{item.name}</Text>
            </View>
          </Pressable>
        )}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    //backgroundColor: theme.background,
    paddingTop: 16, 
    paddingHorizontal: 16,
  },
});
