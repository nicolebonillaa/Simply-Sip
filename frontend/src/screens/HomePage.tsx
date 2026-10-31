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
import { SafeAreaView } from "react-native-safe-area-context";
import { useNavigation } from "@react-navigation/native";
import { Ionicons } from "@expo/vector-icons";

import { getDrinks } from "../services/drinks";
import { getLocations } from "../services/locations";
import FilterModal from "./FilterModal";
import type { Drink, Location, Filters } from "../types";
import {
  DEFAULT_FILTERS,
  countActiveFilters,
} from "../utils/filters";
import { useTheme } from "../theme/ThemeProvider";
import type { Theme } from "../theme/ThemeProvider";

export default function HomePage() {
  const navigation = useNavigation<any>();
  const { theme } = useTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);

  const [search, setSearch] = useState("");
  const [filterVisible, setFilterVisible] = useState(false);
  const [selectedFilters, setSelectedFilters] =
    useState<Filters>(DEFAULT_FILTERS);

  const [drinks, setDrinks] = useState<Drink[]>([]);
  const [stores, setStores] = useState<Location[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    let active = true;

    async function loadHomeData() {
      setLoading(true);
      setError(null);
      setDrinks([]);
      setStores([]);

      try {
        const [drinkData, locationData] = await Promise.all([
          getDrinks(),
          getLocations(),
        ]);

        if (active) {
          setDrinks(drinkData);
          setStores(locationData);
        }
      } catch {
        if (active) {
          setError(
            "Unable to load drinks and cafés. Please try again.",
          );
        }
      } finally {
        if (active) setLoading(false);
      }
    }

    void loadHomeData();

    return () => {
      active = false;
    };
  }, [retry]);

  const activeCount = countActiveFilters(selectedFilters);

  const handleSearch = () => {
    navigation.navigate("SearchResults", {
      query: search,
      filters: selectedFilters,
    });
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView showsVerticalScrollIndicator={false}>
        <View style={styles.welcomeRow}>
          <Image
            style={styles.tinyLogo}
            source={require("../../assets/simply sip logo/app-logo.png")}
          />

          <Text style={styles.welcomeTitle}>
            Welcome, Guest!
          </Text>
        </View>

        <View style={styles.searchRow}>
          <View style={styles.searchBox}>
            <Pressable
              style={styles.searchButton}
              onPress={handleSearch}
              accessibilityLabel="Search"
            >
              <Ionicons
                name="search"
                size={18}
                color={theme.subtext}
              />
            </Pressable>

            <TextInput
              value={search}
              onChangeText={setSearch}
              onSubmitEditing={handleSearch}
              placeholder="Search drinks"
              placeholderTextColor={theme.subtext}
              style={styles.input}
              returnKeyType="search"
            />

            <Pressable
              style={styles.filterButton}
              onPress={() => setFilterVisible(true)}
              accessibilityLabel="Open filters"
            >
              <Ionicons
                name="options-outline"
                size={22}
                color={theme.icon}
              />

              {activeCount > 0 && (
                <View style={styles.badge}>
                  <Text style={styles.badgeText}>
                    {activeCount}
                  </Text>
                </View>
              )}
            </Pressable>
          </View>
        </View>

        {loading && (
          <ActivityIndicator
            color={theme.icon}
            accessibilityLabel="Loading drinks and cafés"
          />
        )}

        {error && (
          <View style={{ paddingHorizontal: 15 }}>
            <Text
              style={styles.place}
              accessibilityRole="alert"
            >
              {error}
            </Text>

            <Pressable
              onPress={() => setRetry((value) => value + 1)}
              accessibilityRole="button"
            >
              <Text style={styles.name}>Try again</Text>
            </Pressable>
          </View>
        )}

        <Text style={styles.popDrinksTitle}>
          Explore Drinks
        </Text>

        <FlatList
          data={drinks}
          ListEmptyComponent={
            !loading && !error ? (
              <Text style={styles.place}>
                No drinks available yet.
              </Text>
            ) : null
          }
          renderItem={({ item }) => (
            <View style={styles.card}>
              <View style={styles.imagePlaceholder} />

              <Text style={styles.name}>{item.name}</Text>

              <Text style={styles.place}>
                {item.location?.name ?? "Café unavailable"}
              </Text>
            </View>
          )}
          keyExtractor={(item) => String(item.id)}
          horizontal
          showsHorizontalScrollIndicator={false}
        />

        <Text style={styles.popStoresTitle}>
          Explore Cafés
        </Text>

        <FlatList
          data={stores}
          ListEmptyComponent={
            !loading && !error ? (
              <Text style={styles.place}>
                No cafés available yet.
              </Text>
            ) : null
          }
          renderItem={({ item }) => (
            <View style={styles.card}>
              <View style={styles.imagePlaceholder} />
              <Text style={styles.name}>{item.name}</Text>
            </View>
          )}
          keyExtractor={(item) => `store-${item.id}`}
          horizontal
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={styles.carouselContainer}
        />
      </ScrollView>

      {filterVisible && (
        <FilterModal
          visible={filterVisible}
          initialFilters={selectedFilters}
          onClose={() => setFilterVisible(false)}
          onApply={(newFilters) => {
            setSelectedFilters(newFilters);
            setFilterVisible(false);
          }}
        />
      )}
    </SafeAreaView>
  );
}

const makeStyles = (theme: Theme) =>
  StyleSheet.create({
    container: {
      flex: 1,
      backgroundColor: theme.background,
      paddingHorizontal: 1,
    },
    welcomeRow: {
      flexDirection: "row",
      alignItems: "center",
      paddingHorizontal: 15,
      marginBottom: 10,
    },
    tinyLogo: {
      width: 70,
      height: 70,
    },
    welcomeTitle: {
      fontSize: 22,
      fontWeight: "bold",
      marginLeft: 15,
      marginBottom: 10,
      marginTop: 15,
      color: theme.text,
      alignSelf: "center",
    },
    searchRow: {
      marginTop: 8,
      marginHorizontal: 12,
      marginBottom: 15,
    },
    searchBox: {
      flexDirection: "row",
      alignItems: "center",
      height: 46,
      paddingLeft: 4,
      paddingRight: 6,
      borderRadius: 23,
      backgroundColor: theme.band,
    },
    searchButton: {
      width: 38,
      height: 38,
      borderRadius: 19,
      alignItems: "center",
      justifyContent: "center",
    },
    input: {
      flex: 1,
      fontSize: 15,
      color: theme.text,
      marginHorizontal: 4,
    },
    filterButton: {
      width: 38,
      height: 38,
      alignItems: "center",
      justifyContent: "center",
    },
    badge: {
      position: "absolute",
      top: 2,
      right: 0,
      minWidth: 16,
      height: 16,
      paddingHorizontal: 4,
      borderRadius: 8,
      backgroundColor: theme.button,
      alignItems: "center",
      justifyContent: "center",
    },
    badgeText: {
      color: theme.buttonText,
      fontSize: 10,
      fontWeight: "700",
    },
    popDrinksTitle: {
      fontSize: 18,
      fontWeight: "bold",
      marginLeft: 15,
      marginBottom: 10,
      marginTop: 15,
      color: theme.text,
    },
    popStoresTitle: {
      fontSize: 18,
      fontWeight: "bold",
      marginLeft: 15,
      marginBottom: 10,
      marginTop: 15,
      color: theme.text,
    },
    card: {
      backgroundColor: theme.background,
      padding: 12,
      marginLeft: 15,
      borderRadius: 12,
      width: 150,
      alignSelf: "flex-start",
    },
    imagePlaceholder: {
      width: "100%",
      height: 100,
      backgroundColor: theme.band,
      borderRadius: 6,
      marginBottom: 8,
    },
    name: {
      fontSize: 16,
      fontWeight: "bold",
      color: theme.text,
    },
    place: {
      fontSize: 14,
      color: theme.subtext,
      marginBottom: 4,
    },
    carouselContainer: {
      paddingRight: 15,
      paddingBottom: 15,
    },
  });