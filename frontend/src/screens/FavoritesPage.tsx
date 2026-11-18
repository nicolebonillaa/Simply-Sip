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
import {favoriteDrinks as initialFavorites } from "../fakeDrinks";
import {useNavigation} from "@react-navigation/native";
import {SafeAreaView} from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";


export default function FavoritesPage() {
  const navigation = useNavigation();
  const theme = useTheme();
  const[favoriteDrinks, setFavoriteDrinks] = useState(initialFavorites);

  const toggleFavorite = (drink: any) =>
  {
    setFavoriteDrinks(favoriteDrinks.filter((d) => d.id !== drink.id));
  }

  const coffeeDrinks = favoriteDrinks.filter(drink => drink.category === "Coffee");
  const teaDrinks = favoriteDrinks.filter(drink => drink.category === "Tea");
  const bobaDrinks = favoriteDrinks.filter(drink => drink.category === "Boba");
  const juiceDrinks = favoriteDrinks.filter(drink => drink.category === "Juice");

  return (
    <SafeAreaView style={[styles.container, {backgroundColor: theme.background}]}>
      <ScrollView showsVerticalScrollIndicator={false}>
      <Text style={[styles.favTitle, {color: theme.text}]}>Favorites</Text>
      {coffeeDrinks.length > 0 && (
        <>
         
    <Text style={[styles.sectionTitle, {color: theme.text}]}>Coffee</Text>
      <FlatList 
        data={coffeeDrinks}
        keyExtractor={(item) => String(item.id)}
        horizontal
        showsHorizontalScrollIndicator={false}
        renderItem={({item}) => (
          <Pressable onPress={() => navigation.navigate("DrinkDetails", {drinkId: item.id})}>
           <View style={styles.card}>
            <View style={styles.imageContainer}>
              <View style={styles.imagePlaceholder} />
              <Pressable
                style={styles.favButton}
                onPress={() => toggleFavorite(item)}>
                >
              <Ionicons
                name="heart"
                size={18}
                color= "#EF4444"
                />
                </Pressable>
                </View>
              <Text style={[styles.name, {color: theme.text}]}>{item.name}</Text>
            </View>
          </Pressable>
      )}
      />
      </>
    )}
      {teaDrinks.length > 0 && (
        <>
      <Text style={[styles.sectionTitle, {color: theme.text}]}>Tea</Text>
      <FlatList 
        data={teaDrinks}
        keyExtractor={(item) => String(item.id)}
        horizontal
        showsHorizontalScrollIndicator={false}
        renderItem={({item}) => (
          <Pressable onPress={() => navigation.navigate("DrinkDetails", {drinkId: item.id})}>
           <View style={styles.card}>
              <View style={styles.imageContainer}>
              <View style={styles.imagePlaceholder} />
              <Pressable
                style={styles.favButton}
                onPress={() => toggleFavorite(item)}>
              <Ionicons
                name= "heart"
                size={18}
                color= "#EF4444"
                />
                </Pressable>
                </View>
              <Text style={[styles.name, {color: theme.text}]}>{item.name}</Text>
            </View>
          </Pressable>
         
      )}
      />
     </>
    )}
     
      {bobaDrinks.length > 0 && (
        <>
      <Text style={[styles.sectionTitle, {color: theme.text}]}>Boba</Text>
      <FlatList 
        data={bobaDrinks}
        keyExtractor={(item) => String(item.id)}
        horizontal
        showsHorizontalScrollIndicator={false}
        renderItem={({item}) => (
          <Pressable onPress={() => navigation.navigate("DrinkDetails", {drinkId: item.id})}>
           <View style={styles.card}>
            <View style={styles.imageContainer}>
              <View style={styles.imagePlaceholder} />
               <Pressable
                style={styles.favButton}
                onPress={() => toggleFavorite(item)}>
              <Ionicons
                name="heart"
                size={18}
                color="#EF4444"
                />
                </Pressable>
                </View>
              <Text style={[styles.name, {color: theme.text}]}>{item.name}</Text>
            </View>
          </Pressable>
        )}
        
        />
      </>
      )}
      
      {juiceDrinks.length > 0 && (
        <>
       <Text style={[styles.sectionTitle, {color: theme.text}]}>Juice</Text>
      <FlatList 
        data={juiceDrinks}
        keyExtractor={(item) => String(item.id)}
        horizontal
        showsHorizontalScrollIndicator={false}
        renderItem={({item}) => (
          <Pressable onPress={() => navigation.navigate("DrinkDetails", {drinkId: item.id})}>
           <View style={styles.card}>
            <View style={styles.imageContainer}>
              <View style={styles.imagePlaceholder} />
               <Pressable
                style={styles.favButton}
                onPress={() => toggleFavorite(item)}>
              <Ionicons
                name= "heart"
                size={18}
                color="#EF4444"
                />
                </Pressable>
                </View>
              <Text style={[styles.name, {color: theme.text}]}>{item.name}</Text>
            </View>
          </Pressable>
        )}
        />
        </>
      )}
    
      </ScrollView>
    </SafeAreaView>
  );
}


const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 16, 
    paddingHorizontal: 16,
  },
  favTitle:
  {
    fontSize: 30,
    fontWeight: "800",
    marginBottom: 8,
  },
  sectionTitle:
  {
    fontSize: 20,
    fontWeight: "700",
    marginTop: 16,
    marginBottom: 10,
  },
  card:
  {
    width: 120,
    marginRight: 10,
  },
  imagePlaceholder:
  {
    width: "100%",
    height: 100,
    borderRadius: 12,
    backgroundColor: "#E5E7EB",
    marginBottom: 6,
  },
  imageContainer:
  {
    position: "relative",
    width: "100%",
    height: 100,
    borderRadius: 12,
    backgroundColor: "#E5E7EB",
  },
  name:
  {
    fontSize: 12, 
    fontWeight: "500",
  },
  favButton:
  {
    position: "absolute",
    top: 3,
    right: 8,
    padding: 6,
    borderRadius: 20,
  },

});
