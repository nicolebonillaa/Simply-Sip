import React, { useCallback, useMemo } from "react";
import { ActivityIndicator, Alert, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect, useNavigation } from "@react-navigation/native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useAuth } from "../auth/AuthContext";
import CafeDrinkImage from "../components/CafeDrinkImage";
import { useFavorites } from "../favorites/FavoritesContext";
import { useTheme, type Theme } from "../theme/ThemeProvider";
import type { Favorite } from "../types";

export default function FavoritesPage() {
  const navigation = useNavigation<any>();
  const { theme } = useTheme();
  const styles = useMemo(() => createStyles(theme), [theme]);
  const { status, token } = useAuth();
  const { drinks, cafes, loading, error, pendingDrinkIds, pendingCafeIds, refresh, toggleDrink, toggleCafe } = useFavorites();

  useFocusEffect(useCallback(() => {
    if (status === "signedIn" && token) void refresh();
  }, [status, token, refresh]));

  const groups = useMemo(() => {
    const result = new Map<string, Favorite[]>();
    for (const favorite of drinks) {
      if (!favorite.drink) continue;
      const category = favorite.drink.category || "Other drinks";
      result.set(category, [...(result.get(category) ?? []), favorite]);
    }
    return [...result.entries()].sort(([a], [b]) => a.localeCompare(b));
  }, [drinks]);

  const removeDrink = async (id: number) => {
    try { await toggleDrink(id); }
    catch { Alert.alert("Unable to remove drink", "Please try again."); }
  };
  const removeCafe = async (id: number) => {
    try { await toggleCafe(id); }
    catch { Alert.alert("Unable to remove café", "Please try again."); }
  };

  return (
    <SafeAreaView style={styles.screen}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>Favorites</Text>
        {status === "loading" || (status === "signedIn" && loading && !drinks.length && !cafes.length) ? (
          <ActivityIndicator color={theme.icon} accessibilityLabel="Loading favorites" />
        ) : status !== "signedIn" || !token ? (
          <View style={styles.messageArea}>
            <Text style={styles.message}>Log in to save your favorite drinks and cafés.</Text>
            <Pressable onPress={() => navigation.navigate("Login")} accessibilityRole="button">
              <Text style={styles.action}>Log In</Text>
            </Pressable>
            <Pressable onPress={() => navigation.navigate("Register")} accessibilityRole="button">
              <Text style={styles.action}>Create Account</Text>
            </Pressable>
          </View>
        ) : (
          <>
            {error && (
              <View style={styles.messageArea}>
                <Text style={styles.message} accessibilityRole="alert">{error}</Text>
                <Pressable onPress={() => { void refresh(); }} accessibilityRole="button">
                  <Text style={styles.action}>Try again</Text>
                </Pressable>
              </View>
            )}
            {!loading && !error && !drinks.length && !cafes.length && (
              <Text style={styles.message}>No favorites yet. Save a drink or café to see it here.</Text>
            )}
            {groups.map(([category, items]) => (
              <View key={category} style={styles.section}>
                <Text style={styles.heading}>{category}</Text>
                <ScrollView horizontal showsHorizontalScrollIndicator={false}>
                  {items.map(({ drink }) => (
                    <View key={drink.id} style={styles.card}>
                      <Pressable onPress={() => navigation.navigate("DrinkDetails", { drinkId: drink.id })}
                        accessibilityRole="button" accessibilityLabel={`View ${drink.name}`}>
                        <CafeDrinkImage uri={drink.imageUrl} style={styles.photo} label={`${drink.name} photo`} />
                        <Text style={styles.cardTitle}>{drink.name}</Text>
                        <Text style={styles.subtitle}>{drink.location?.name || "Café unavailable"}</Text>
                      </Pressable>
                      <Pressable style={styles.saveButton} onPress={() => { void removeDrink(drink.id); }}
                        disabled={loading || !!error || pendingDrinkIds.includes(drink.id)} accessibilityRole="button"
                        accessibilityLabel={`Remove ${drink.name} from favorites`}>
                        <Ionicons name="heart" color={theme.icon} size={22} />
                      </Pressable>
                    </View>
                  ))}
                </ScrollView>
              </View>
            ))}
            {cafes.length > 0 && (
              <View style={styles.section}>
                <Text style={styles.heading}>Cafés</Text>
                <ScrollView horizontal showsHorizontalScrollIndicator={false}>
                  {cafes.map(({ location }) => (
                    <View key={location.id} style={styles.card}>
                      <Pressable onPress={() => navigation.navigate("CafeDetails", { locationId: location.id })}
                        accessibilityRole="button" accessibilityLabel={`View ${location.name}`}>
                        <CafeDrinkImage uri={location.imageUrl} style={styles.photo} label={`${location.name} photo`} />
                        <Text style={styles.cardTitle}>{location.name}</Text>
                        <Text style={styles.subtitle}>{location.address || "Address unavailable"}</Text>
                      </Pressable>
                      <Pressable style={styles.saveButton} onPress={() => { void removeCafe(location.id); }}
                        disabled={loading || !!error || pendingCafeIds.includes(location.id)} accessibilityRole="button"
                        accessibilityLabel={`Remove ${location.name} from favorites`}>
                        <Ionicons name="heart" color={theme.icon} size={22} />
                      </Pressable>
                    </View>
                  ))}
                </ScrollView>
              </View>
            )}
          </>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const createStyles = (theme: Theme) => StyleSheet.create({
  screen: { flex: 1, backgroundColor: theme.background },
  content: { paddingHorizontal: 20, paddingTop: 18, paddingBottom: 120 },
  title: { color: theme.text, fontSize: 30, fontWeight: "800", marginBottom: 20 },
  section: { marginBottom: 22 },
  heading: { color: theme.text, fontSize: 20, fontWeight: "700", marginBottom: 12 },
  card: { width: 150, marginRight: 16, position: "relative" },
  photo: { width: 150, height: 120, borderRadius: 12, backgroundColor: theme.band },
  cardTitle: { color: theme.text, fontSize: 15, fontWeight: "700", marginTop: 8 },
  subtitle: { color: theme.subtext, fontSize: 13, marginTop: 3 },
  saveButton: { position: "absolute", top: 6, right: 6, backgroundColor: theme.chip,
    borderRadius: 20, padding: 7 },
  messageArea: { paddingVertical: 12, gap: 8 },
  message: { color: theme.subtext, fontSize: 16, lineHeight: 23 },
  action: { color: theme.icon, fontSize: 16, fontWeight: "700", paddingVertical: 8 },
});