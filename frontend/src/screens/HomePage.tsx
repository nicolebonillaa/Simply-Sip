import React, { useState } from "react";
import { Button, StyleSheet, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useNavigation } from "@react-navigation/native";
import FilterModal from "./FilterModal";

export default function HomePage() {
  const navigation = useNavigation<any>();
  const [filterVisible, setFilterVisible] = useState(false);

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.buttons}>
        <Button
          title="Test Search Results"
          onPress={() => navigation.navigate("SearchResults")}
        />

        <Button
          title="Test Full Map"
          onPress={() => navigation.navigate("MapScreen")}
        />

        <Button
          title="Test Filters"
          onPress={() => setFilterVisible(true)}
        />
      </View>

      <FilterModal
        visible={filterVisible}
        onClose={() => setFilterVisible(false)}
        onApply={(selectedFilters) => {
          console.log("Selected filters:", selectedFilters);
          setFilterVisible(false);
        }}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  buttons: {
    gap: 12,
    padding: 20,
  },
});