import React, { useState } from "react";
import { View, StyleSheet, Button } from "react-native";
import FilterModal from "./FilterModal";
import type { Filters as FilterOptions } from "../types";

export default function HomePage() {
  const [filtersVisible, setFiltersVisible] = useState(false);

  const handleApplyFilters = (filters: FilterOptions) => {
    console.log(filters);
    setFiltersVisible(false);
  };

  return (
    <View style={styles.container}>
      <View style={styles.filterButton}>
        <Button title="Open Filters" onPress={() => setFiltersVisible(true)} />
      </View>

      <FilterModal
        visible={filtersVisible}
        onClose={() => setFiltersVisible(false)}
        onApply={handleApplyFilters}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  filterButton: {
    alignSelf: "center",
    marginTop: "25%",
  },
});
