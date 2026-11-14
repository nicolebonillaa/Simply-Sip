//  DrinkDetails.tsx
//  Created by Faith Hill on 10/22/26.

import React, { useMemo, useState } from "react";
import {FlatList, Image, Pressable, ScrollView, StyleSheet, Text, TextInput, View, Button, Alert } from "react-native";

import {Theme, useTheme} from "../theme/ThemeProvider";
import {useRoute, useNavigation} from "@react-navigation/native";
import { drinks, stores } from "../fakeDrinks";
import {SafeAreaView} from "react-native-safe-area-context";
import {Ionicons} from "@expo/vector-icons";

const formatNutrition = (value: number | null | undefined) =>
{
    if (value == null || !Number.isFinite(value) ||value < 0)
    {
        return "Not Provided";
    }
    return value;
};

export default function DrinkDetails()
{
    const { theme } = useTheme();
    const styles = useMemo(() => makeStyles(theme), [theme]);
    const[isLiked, setIsLiked] = React.useState(false);
    
    const route = useRoute();
    const navigation = useNavigation();
    const {drinkId} = route.params || {};
    
    const pressedDrink = drinks.find(d => String(d.id) === String(drinkId));
    
    if (!pressedDrink)
    {
        return (
                <SafeAreaView style = {styles.safeArea}>
                <View style={styles.container}>
                <Text style = {styles.errorText}>Drink not Found.
                </Text>
                <Button
                title="Back to Home Screen"
                onPress={() => navigation.goBack()}
                />
                </View>
                </SafeAreaView>
                );
        
    }
    return (
            <SafeAreaView style = {styles.safeArea}>
            <ScrollView contentContainerStyle={styles.scrollContainer}>
            <Pressable onPress={() => navigation.goBack()} style = {styles.backButton}>
            <Ionicons name = "arrow-back" size={20} color ="#000"/>
            </Pressable>
            <View style={styles.card}>
            <View style={styles.imagePlaceholder} />
            <View style={styles.titleRow}>
            <Text style={styles.titleText}>{pressedDrink.name}</Text>
            <Pressable onPress={() => setIsLiked(!isLiked)} style={styles.likeButton}>
                <Ionicons
                    name={isLiked ? "heart" : "heart-outline"}
                    size={20}
                    color={isLiked ? "#ef4444" : "#000"}
                />
                </Pressable>
                </View>
            <Text style = {styles.locationText}>Store Location | 0.5 mi </Text>
            <View style={styles.starContainer}>
            <Ionicons name="star" size={20} color="#FFD700" />
            <Ionicons name="star" size={20} color="#FFD700" />
            <Ionicons name="star" size={20} color="#FFD700" />
            <Ionicons name="star" size={20} color="#FFD700" />
            <Ionicons name="star" size={20} color="#FFD700" />
            </View>
            <Text style={styles.nutritionTitle}>Nutrition Facts</Text>
            <View style={styles.nutritionBox}>
            <Text style = {styles.nutritionText}>Calories: {formatNutrition(pressedDrink.calories)} cal</Text>
            <Text style = {styles.nutritionText}>Sugar: {formatNutrition(pressedDrink.sugar)} g</Text>
            </View>
            <Text style={styles.allergensTitle}>Allergens</Text>
            <View style={styles.badgeRow}>
            <View style={styles.badge}>
            <Ionicons name="warning-outline" size={12} color ="#b45309" />
            <Text style={styles.allergenText}> {pressedDrink.allergens} </Text>
            </View>
            </View>
            <Text style = {styles.recipeTitle}>Recipe</Text>
            <Text style = {styles.ingredientsList}>Ingredient 1, Ingredient 2, Ingredient 3</Text>
            <View style={styles.buttonContainer}>
            <Pressable style={styles.getDirButton}
            onPress={() => Alert.alert ("Direction Button Coming Soon")}>
            
        <Text style={styles.getDirButtonText}> Get Directions</Text>
    </Pressable>
                    <Pressable style={styles.viewStoreButton}
            onPress={() => {Alert.alert("View Store Button Coming Soon")}}>
                        <Text style ={styles.viewStoreText}>View Store</Text>
                    </Pressable>
                </View>
            </View>
    </ScrollView>
</SafeAreaView>
            
    );
    
     
}


const makeStyles = (theme: Theme) =>
StyleSheet.create({
    card:
    {
        borderRadius: 12,
        paddingVertical: 18,
        margin: 14,
        
    },
    titleRow:
    {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        width: "100%",
    },
    likeButton:
    {
        padding: 5,
    },
    titleText:
    {
        fontSize: 20,
        fontWeight: "bold",
        color: theme.text,
        flex: 1,
        marginRight: 12,
    },
    errorText: {
        fontSize: 16,
        fontWeight: "bold",
        color: theme.text,
    },
locationText:
    {
        fontSize: 14,
        color: "#666",
    },
starContainer:
    {
        flexDirection: "row",
        alignItems: "center",
        gap: 3,
        marginVertical: 12,
    },
    nutritionTitle: {
        fontSize: 16,
        fontWeight: "bold",
        color: theme.text,
        paddingTop: 16,
    },
nutritionBox:
    {
        backgroundColor: "#e1e4e8",
        borderRadius: 8,
        padding: 12,
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        marginVertical: 10,
    },
nutritionText:
    {
        fontSize: 16,
        fontWeight: "600",
        color: "#333",
    },
allergensTitle:
    {
        fontSize: 16,
        fontWeight: "bold",
        color: theme.text,
        paddingTop: 10,
        
    },
badgeRow:
    {
        flexDirection: "row",
        flexWrap: "wrap",
        gap: 5,
        paddingTop: 5,
    },
badge:
    {
        flexDirection: "row",
        alignItems: "center",
        backgroundColor: "#fde68a",
        paddingHorizontal: 10,
        paddingVertical: 6,
        borderRadius: 14,
        gap: 6,
    },
allergenText:
    {
        fontSize: 14,
        color: "#b45309",
        fontWeight: "600",
    },
recipeTitle:
    {
        fontSize: 16,
        fontWeight: "bold",
        color: theme.text,
        paddingTop: 22,
    },
ingredientsList:
    {
        fontSize: 13,
        color: "#666",
        paddingBottom: 20,
    },
getDirButtonText:
    {
        fontSize: 16,
        fontWeight: "bold",
    },
viewStoreText:
    {
        fontSize: 16,
        fontWeight: "bold",

    },
    getDirButton:
    {
        paddingVertical: 14,
        borderRadius: 24,
        borderWidth: 1,
        alignItems: "center",
        justifyContent: "center",
        marginVertical: 6,
    },
    viewStoreButton:
    {
        paddingVertical: 14,
        borderRadius: 24,
        alignItems: "center",
        justifyContent: "center",
        borderWidth: 1,
        marginVertical: 6,
    },
    
    
    
});
