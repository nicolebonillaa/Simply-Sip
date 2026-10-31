import React, { useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import {
  useNavigation,
  useRoute,
} from "@react-navigation/native";
import { Ionicons } from "@expo/vector-icons";

import MapPreview from "../components/MapPreview";
import FilterModal from "./FilterModal";
<<<<<<< HEAD
import { getNearbySpots } from "../data/mapData";
import { getDrinks } from "../services/drinks";
import { Theme, useTheme } from "../theme/ThemeProvider";
import type { Drink, Filters } from "../types";
=======
import { CSUN, distanceInMiles } from "../data/mapData";
import type { MapSpot } from "../data/mapData";
import { useTheme } from "../theme/ThemeProvider";
import type { Theme } from "../theme/ThemeProvider";
import type { Drink, Filters } from "../types";
import { getDrinks } from "../services/drinks";
>>>>>>> origin/main
import {
  DEFAULT_FILTERS,
  applyFilters,
  countActiveFilters,
  describeFilters,
  supportedFilters,
  toDrinkParams,
} from "../utils/filters";

type DrinkSpot = Drink & { latitude: number; longitude: number };

function toDrinkSpot(drink: Drink): DrinkSpot | null {
  const latitude = drink.location?.latitude;
  const longitude = drink.location?.longitude;
  if (typeof latitude !== "number" || typeof longitude !== "number") {
    return null;
  }
  return { ...drink, latitude, longitude };
}

export default function SearchResults() {
  const route = useRoute<any>();

  return (
    <SearchResultsContent
      key={JSON.stringify(route.params ?? {})}
      initialQuery={route.params?.query ?? ""}
      initialFilters={supportedFilters(
        route.params?.filters ?? DEFAULT_FILTERS,
      )}
    />
  );
}

function SearchResultsContent({
  initialQuery,
  initialFilters,
}: {
  initialQuery: string;
  initialFilters: Filters;
}) {
  const navigation = useNavigation<any>();
  const { theme } = useTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);

  const [query, setQuery] = useState(initialQuery);
  const [filters, setFilters] =
    useState<Filters>(initialFilters);

  const [request, setRequest] = useState(() => ({
    query: initialQuery,
    filters: initialFilters,
  }));

  const [drinks, setDrinks] = useState<Drink[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [retry, setRetry] = useState(0);
  const [filterVisible, setFilterVisible] = useState(false);
<<<<<<< HEAD
  const [saved, setSaved] = useState<Set<string>>(new Set());
  const [drinks, setDrinks] = useState<DrinkSpot[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
=======
  const [saved, setSaved] = useState<Set<number>>(new Set());
>>>>>>> origin/main

  useEffect(() => {
    let active = true;

    async function loadResults() {
      setLoading(true);
      setError(null);
      setDrinks([]);

      try {
        const items = await getDrinks(
          toDrinkParams(request.query, request.filters),
        );

        if (active) setDrinks(items);
      } catch {
        if (active) {
          setError(
            "Unable to load search results. Please try again.",
          );
        }
      } finally {
        if (active) setLoading(false);
      }
    }

    void loadResults();

<<<<<<< HEAD
  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setLoadError(null);
      try {
        const data = await getDrinks();
        if (cancelled) return;
        setDrinks(
          data
            .map(toDrinkSpot)
            .filter((spot): spot is DrinkSpot => spot !== null),
        );
      } catch {
        if (!cancelled) {
          setLoadError("Could not load drinks. Is the API running?");
          setDrinks([]);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const hasPending = useMemo(
    () => JSON.stringify(filters) !== JSON.stringify(appliedFilters),
    [filters, appliedFilters],
  );
=======
    return () => {
      active = false;
    };
  }, [request, retry]);

  const results = useMemo(() => {
    const nearbyDrinks = drinks.filter((drink) => {
      const location = drink.location;

      return (
        location !== null &&
        Number.isFinite(location.latitude) &&
        Number.isFinite(location.longitude) &&
        distanceInMiles(CSUN, location) <= 2
      );
    });

    return applyFilters(nearbyDrinks, request.filters, CSUN);
  }, [drinks, request.filters]);

  const mapSpots = useMemo(() => {
    const locations = new Map<number, MapSpot>();

    for (const drink of results) {
      if (drink.location) {
        const { id, name, latitude, longitude } =
          drink.location;

        locations.set(id, {
          id,
          name,
          latitude,
          longitude,
        });
      }
    }

    return Array.from(locations.values());
  }, [results]);

  const hasPending =
    query.trim() !== request.query.trim() ||
    JSON.stringify(filters) !==
      JSON.stringify(request.filters);
>>>>>>> origin/main

  const activeCount = countActiveFilters(filters);
  const appliedTags = describeFilters(request.filters);

  const runSearch = () => {
    setRequest({
      query: query.trim(),
      filters,
    });
  };

<<<<<<< HEAD
  const nearbySpots = useMemo(() => {
    let spots = getNearbySpots(drinks);

    if (query.trim()) {
      const searchText = query.toLowerCase();

      spots = spots.filter((spot) =>
        spot.name.toLowerCase().includes(searchText),
      );
    }

    if (openNow) {
      spots = spots.filter((spot) => (spot as any).isOpen !== false);
    }

    return applyFilters(spots, appliedFilters);
  }, [drinks, query, openNow, appliedFilters]);

  const toggleSaved = (id: string) => {
    setSaved((previous) => {
      const next = new Set(previous);
=======
  const toggleSaved = (id: number) => {
    setSaved((current) => {
      const next = new Set(current);
>>>>>>> origin/main

      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }

      return next;
    });
  };

  const header = (
    <View>
      <View style={styles.searchRow}>
        <View style={styles.searchBox}>
          <Pressable
            style={[
              styles.searchButton,
              hasPending && styles.searchButtonHot,
            ]}
            onPress={runSearch}
            accessibilityRole="button"
            accessibilityLabel="Search with filters"
          >
            <Ionicons
              name="search"
              size={18}
              color={
                hasPending
                  ? theme.buttonText
                  : theme.subtext
              }
            />
          </Pressable>

          <TextInput
            value={query}
            onChangeText={setQuery}
            onSubmitEditing={runSearch}
            placeholder="Search drinks"
            placeholderTextColor={theme.subtext}
            style={styles.searchInput}
            returnKeyType="search"
          />

          <Pressable
            style={styles.filterIconButton}
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

      <View style={styles.chipRow}>
        <View style={[styles.chip, styles.chipActive]}>
          <Text
            style={[
              styles.chipText,
              styles.chipTextActive,
            ]}
          >
            Within 2 miles of CSUN
          </Text>
        </View>
      </View>

      {appliedTags.length > 0 && (
        <View style={styles.tagRow}>
          {appliedTags.map((tag) => (
            <View key={tag} style={styles.tag}>
              <Text style={styles.tagText}>{tag}</Text>
            </View>
          ))}
        </View>
      )}

      {!loading && !error && mapSpots.length > 0 && (
        <MapPreview
          spots={mapSpots}
          onPress={() =>
            navigation.navigate("MapScreen", {
              spots: mapSpots,
            })
          }
        />
      )}

      <View style={styles.headingRow}>
        <Text style={styles.heading}>
          Drinks near CSUN
        </Text>

        {!loading && !error && (
          <Text style={styles.count}>
            {results.length}{" "}
            {results.length === 1 ? "result" : "results"}
          </Text>
        )}
      </View>
    </View>
  );

  return (
    <SafeAreaView
      style={styles.container}
      edges={["top"]}
    >
      <FlatList
        data={loading || error ? [] : results}
        keyExtractor={(item) => String(item.id)}
        ListHeaderComponent={header}
        ListEmptyComponent={
<<<<<<< HEAD
          <Text style={styles.empty}>
            {loading
              ? "Loading drinks…"
              : loadError
                ? loadError
                : "No drink spots match your search."}
          </Text>
=======
          loading ? (
            <ActivityIndicator
              color={theme.icon}
              accessibilityLabel="Loading search results"
            />
          ) : error ? (
            <View>
              <Text
                style={styles.empty}
                accessibilityRole="alert"
              >
                {error}
              </Text>

              <Pressable
                onPress={() =>
                  setRetry((value) => value + 1)
                }
                accessibilityRole="button"
              >
                <Text style={styles.empty}>
                  Try again
                </Text>
              </Pressable>
            </View>
          ) : (
            <Text style={styles.empty}>
              No drinks match your search. Try changing
              your search or filters.
            </Text>
          )
>>>>>>> origin/main
        }
        contentContainerStyle={styles.listContent}
        keyboardShouldPersistTaps="handled"
        renderItem={({ item }) => {
          const isSaved = saved.has(item.id);
          const nutrition = item.nutritionFacts;

          const distance = item.location
            ? distanceInMiles(CSUN, item.location)
            : null;

          const nutritionText = [
            nutrition?.calories != null
              ? `${nutrition.calories} cal`
              : null,
            nutrition?.sugar != null
              ? `${nutrition.sugar} g sugar`
              : null,
          ]
            .filter(Boolean)
            .join(" · ");

          return (
            <View style={styles.card}>
              <View style={styles.photo}>
                <Ionicons
                  name="cafe-outline"
                  size={34}
                  color={theme.subtext}
                />

                <Pressable
                  style={styles.saveButton}
                  onPress={() => toggleSaved(item.id)}
                  accessibilityRole="button"
                  accessibilityLabel={
                    isSaved
                      ? "Remove temporary bookmark"
                      : "Bookmark for this session"
                  }
                  accessibilityState={{
                    selected: isSaved,
                  }}
                >
                  <Ionicons
                    name={
                      isSaved
                        ? "bookmark"
                        : "bookmark-outline"
                    }
                    size={18}
                    color={theme.icon}
                  />
                </Pressable>
              </View>

              <Text style={styles.name}>
                {item.name}
              </Text>

              <Text style={styles.meta}>
                {item.location?.name ??
                  "Café unavailable"}
              </Text>

              <Text style={styles.meta}>
                {[
                  item.category,
                  distance !== null
                    ? `${distance.toFixed(2)} mi from CSUN`
                    : null,
                ]
                  .filter(Boolean)
                  .join(" · ")}
              </Text>

              <Text style={styles.meta}>
                {nutritionText ||
                  "Nutrition information unavailable"}
              </Text>
            </View>
          );
        }}
      />

      {filterVisible && (
        <FilterModal
          visible={filterVisible}
          initialFilters={filters}
          onClose={() => setFilterVisible(false)}
          onApply={(selectedFilters) => {
            const nextFilters =
              supportedFilters(selectedFilters);

            setFilters(nextFilters);
            setRequest({
              query: query.trim(),
              filters: nextFilters,
            });
            setFilterVisible(false);
          }}
        />
      )}
    </SafeAreaView>
  );
}

const makeStyles = (t: Theme) =>
  StyleSheet.create({
    container: {
      flex: 1,
      backgroundColor: t.background,
    },
    listContent: {
      paddingHorizontal: 16,
      paddingBottom: 120,
    },
    searchRow: {
      marginTop: 8,
    },
    searchBox: {
      flexDirection: "row",
      alignItems: "center",
      height: 46,
      paddingLeft: 4,
      paddingRight: 6,
      borderRadius: 23,
      backgroundColor: t.band,
    },
    searchButton: {
      width: 38,
      height: 38,
      borderRadius: 19,
      alignItems: "center",
      justifyContent: "center",
    },
    searchButtonHot: {
      backgroundColor: t.button,
    },
    searchInput: {
      flex: 1,
      fontSize: 15,
      color: t.text,
      marginHorizontal: 4,
    },
    filterIconButton: {
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
      backgroundColor: t.button,
      alignItems: "center",
      justifyContent: "center",
    },
    badgeText: {
      color: t.buttonText,
      fontSize: 10,
      fontWeight: "700",
    },
    chipRow: {
      flexDirection: "row",
      gap: 8,
      marginTop: 12,
    },
    chip: {
      height: 34,
      paddingHorizontal: 14,
      borderRadius: 17,
      backgroundColor: t.chip,
      alignItems: "center",
      justifyContent: "center",
    },
    chipActive: {
      backgroundColor: t.chipActive,
    },
    chipText: {
      fontSize: 13,
      fontWeight: "500",
      color: t.chipText,
    },
    chipTextActive: {
      color: t.chipActiveText,
    },
    tagRow: {
      flexDirection: "row",
      flexWrap: "wrap",
      gap: 6,
      marginTop: 12,
    },
    tag: {
      paddingVertical: 5,
      paddingHorizontal: 10,
      borderRadius: 999,
      borderWidth: 1,
      borderColor: t.inputBorder,
    },
    tagText: {
      fontSize: 12,
      color: t.text,
    },
    headingRow: {
      flexDirection: "row",
      justifyContent: "space-between",
      alignItems: "baseline",
      marginTop: 18,
      marginBottom: 12,
    },
    heading: {
      fontSize: 20,
      fontWeight: "700",
      color: t.text,
    },
    count: {
      fontSize: 13,
      color: t.subtext,
    },
    card: {
      marginBottom: 20,
    },
    photo: {
      height: 150,
      borderRadius: 14,
      backgroundColor: t.band,
      alignItems: "center",
      justifyContent: "center",
      overflow: "hidden",
    },
    photoImage: {
      width: "100%",
      height: "100%",
    },
    saveButton: {
      position: "absolute",
      top: 10,
      right: 10,
      width: 36,
      height: 36,
      borderRadius: 18,
      backgroundColor: t.background,
      alignItems: "center",
      justifyContent: "center",
    },
    name: {
      fontSize: 17,
      fontWeight: "600",
      marginTop: 10,
      color: t.text,
    },
    metaRow: {
      flexDirection: "row",
      alignItems: "center",
      flexWrap: "wrap",
      gap: 4,
      marginTop: 3,
    },
    meta: {
      fontSize: 13,
      color: t.subtext,
    },
    empty: {
      textAlign: "center",
      color: t.subtext,
      marginTop: 24,
    },
  });