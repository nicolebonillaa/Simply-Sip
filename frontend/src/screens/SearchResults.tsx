import React, { useEffect, useMemo, useState } from "react";
import {
  FlatList,
  Image,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useNavigation, useRoute } from "@react-navigation/native";
import { Ionicons } from "@expo/vector-icons";

import MapPreview from "../components/MapPreview";
import FilterModal from "./FilterModal";
import { getNearbySpots, MOCK_SPOTS } from "../data/mapData";
import { Theme, useTheme } from "../theme/ThemeProvider";
import type { Filters } from "../types";
import {
  DEFAULT_FILTERS,
  applyFilters,
  countActiveFilters,
  describeFilters,
} from "../utils/filters";

export default function SearchResults() {
  const navigation = useNavigation<any>();
  const route = useRoute<any>();
  const { theme } = useTheme();

  const styles = useMemo(() => makeStyles(theme), [theme]);

  const [query, setQuery] = useState(route.params?.query ?? "");
  const [openNow, setOpenNow] = useState(false);
  const [filterVisible, setFilterVisible] = useState(false);
  const [saved, setSaved] = useState<Set<string>>(new Set());

  const [filters, setFilters] = useState<Filters>(
    route.params?.filters ?? DEFAULT_FILTERS,
  );

  const [appliedFilters, setAppliedFilters] = useState<Filters>(
    route.params?.filters ?? DEFAULT_FILTERS,
  );

  // Updates the screen when HomePage sends a new search.
  useEffect(() => {
    if (typeof route.params?.query === "string") {
      setQuery(route.params.query);
    }

    if (route.params?.filters) {
      setFilters(route.params.filters);
      setAppliedFilters(route.params.filters);
    }
  }, [route.params]);

  const hasPending = useMemo(
    () => JSON.stringify(filters) !== JSON.stringify(appliedFilters),
    [filters, appliedFilters],
  );

  const activeCount = countActiveFilters(filters);

  const appliedTags = useMemo(
    () => describeFilters(appliedFilters),
    [appliedFilters],
  );

  const runSearch = () => {
    setAppliedFilters(filters);
  };

  const nearbySpots = useMemo(() => {
    let spots = getNearbySpots(MOCK_SPOTS);

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
  }, [query, openNow, appliedFilters]);

  const toggleSaved = (id: string) => {
    setSaved((previous) => {
      const next = new Set(previous);

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
            style={[styles.searchButton, hasPending && styles.searchButtonHot]}
            onPress={runSearch}
            accessibilityLabel="Search with filters"
          >
            <Ionicons
              name="search"
              size={18}
              color={hasPending ? theme.buttonText : theme.subtext}
            />
          </Pressable>

          <TextInput
            value={query}
            onChangeText={setQuery}
            onSubmitEditing={runSearch}
            placeholder="Search drink spots"
            placeholderTextColor={theme.subtext}
            style={styles.searchInput}
            returnKeyType="search"
          />

          <Pressable
            style={styles.filterIconButton}
            onPress={() => setFilterVisible(true)}
            accessibilityLabel="Open filters"
          >
            <Ionicons name="options-outline" size={22} color={theme.icon} />

            {activeCount > 0 && (
              <View style={styles.badge}>
                <Text style={styles.badgeText}>{activeCount}</Text>
              </View>
            )}
          </Pressable>
        </View>
      </View>

      <View style={styles.chipRow}>
        <View style={[styles.chip, styles.chipActive]}>
          <Text style={[styles.chipText, styles.chipTextActive]}>
            Nearest
          </Text>
        </View>

        <Pressable
          style={[styles.chip, openNow && styles.chipActive]}
          onPress={() => setOpenNow((value) => !value)}
        >
          <Text style={[styles.chipText, openNow && styles.chipTextActive]}>
            Open now
          </Text>
        </Pressable>
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

      <MapPreview
        spots={nearbySpots}
        onPress={() =>
          navigation.navigate("MapScreen", {
            spots: nearbySpots,
          })
        }
      />

      <View style={styles.headingRow}>
        <Text style={styles.heading}>Nearby drink spots</Text>

        <Text style={styles.count}>
          {nearbySpots.length}{" "}
          {nearbySpots.length === 1 ? "result" : "results"}
        </Text>
      </View>
    </View>
  );

  return (
    <SafeAreaView style={styles.container} edges={["top"]}>
      <FlatList
        data={nearbySpots}
        keyExtractor={(item) => String(item.id)}
        ListHeaderComponent={header}
        ListEmptyComponent={
          <Text style={styles.empty}>No drink spots match your search.</Text>
        }
        contentContainerStyle={styles.listContent}
        keyboardShouldPersistTaps="handled"
        renderItem={({ item }) => {
          const spot = item as any;
          const id = String(item.id);
          const isSaved = saved.has(id);

          const details = [
            spot.category,
            spot.distance ?? "Within 2 miles of CSUN",
          ]
            .filter(Boolean)
            .join("  ·  ");

          return (
            <Pressable style={styles.card}>
              <View style={styles.photo}>
                {spot.image ? (
                  <Image
                    source={{ uri: spot.image }}
                    style={styles.photoImage}
                  />
                ) : (
                  <Ionicons
                    name="cafe-outline"
                    size={34}
                    color={theme.subtext}
                  />
                )}

                <Pressable
                  style={styles.saveButton}
                  onPress={() => toggleSaved(id)}
                  accessibilityLabel="Save to favorites"
                >
                  <Ionicons
                    name={isSaved ? "bookmark" : "bookmark-outline"}
                    size={18}
                    color={theme.icon}
                  />
                </Pressable>
              </View>

              <Text style={styles.name}>{item.name}</Text>

              <View style={styles.metaRow}>
                {spot.rating != null && (
                  <>
                    <Ionicons name="star" size={13} color={theme.subtext} />

                    <Text style={styles.meta}>
                      {spot.rating}
                      {spot.reviewCount != null
                        ? ` (${spot.reviewCount})`
                        : ""}
                      {"  ·  "}
                    </Text>
                  </>
                )}

                <Text style={styles.meta}>{details}</Text>
              </View>
            </Pressable>
          );
        }}
      />

      <FilterModal
        visible={filterVisible}
        onClose={() => setFilterVisible(false)}
        onApply={(selectedFilters) => {
          setFilters(selectedFilters);
          setFilterVisible(false);
        }}
      />
    </SafeAreaView>
  );
}

const makeStyles = (t: Theme) =>
  StyleSheet.create({
    container: {
      flex: 1,
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