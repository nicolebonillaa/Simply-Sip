import React, { useState } from "react";
import {
  Modal,
  View,
  Text,
  StyleSheet,
  Pressable,
  ScrollView,
  TextInput,
} from "react-native";
import type { Filters } from "../types";

interface FiltersProps {
  visible: boolean;
  onClose: () => void;
  onApply: (filters: Filters) => void;
}

interface CheckboxProps {
  label: string;
  checked: boolean;
  onPress: () => void;
}

function Checkbox({ label, checked, onPress }: CheckboxProps) {
  return (
    <Pressable style={styles.option} onPress={onPress}>
      <Text style={styles.optionText}>{label}</Text>

      <View style={styles.checkbox}>
        {checked && <Text style={styles.checkmark}>✔</Text>}
      </View>
    </Pressable>
  );
}

export default function FilterModal({
  visible,
  onClose,
  onApply,
}: FiltersProps) {
  const [vegan, setVegan] = useState(false);
  const [vegetarian, setVegetarian] = useState(false);
  const [dairyFree, setDairyFree] = useState(false);
  const [glutenFree, setGlutenFree] = useState(false);

  const [maxCalories, setMaxCalories] = useState("");
  const [maxSugar, setMaxSugar] = useState("");

  const [allergens, setAllergens] = useState<string[]>([]);

  const toggleAllergen = (allergen: string) => {
    setAllergens((current) =>
      current.includes(allergen)
        ? current.filter((item) => item !== allergen)
        : [...current, allergen],
    );
  };

  const handleApply = () => {
    onApply({
      vegan,
      vegetarian,
      dairyFree,
      glutenFree,
      maxCalories: maxCalories ? Number(maxCalories) : null,
      maxSugar: maxSugar ? Number(maxSugar) : null,
      allergens,
    });
  };

  return (
    <Modal
      visible={visible}
      transparent
      animationType="slide"
      onRequestClose={onClose}
    >
      <View style={styles.overlay}>
        <View style={styles.sheet}>
          <View style={styles.header}>
            <Text style={styles.title}>Filters</Text>

            <Pressable onPress={onClose}>
              <Text style={styles.close}>✕</Text>
            </Pressable>
          </View>

          <ScrollView>
            <Text style={styles.sectionTitle}>Dietary Needs</Text>

            <Checkbox
              label="Vegan"
              checked={vegan}
              onPress={() => setVegan(!vegan)}
            />

            <Checkbox
              label="Vegetarian"
              checked={vegetarian}
              onPress={() => setVegetarian(!vegetarian)}
            />

            <Checkbox
              label="Dairy-free"
              checked={dairyFree}
              onPress={() => setDairyFree(!dairyFree)}
            />

            <Checkbox
              label="Gluten-free"
              checked={glutenFree}
              onPress={() => setGlutenFree(!glutenFree)}
            />

            <Text style={styles.sectionTitle}>Nutrition</Text>

            <TextInput
              style={styles.input}
              placeholder="Maximum calories"
              keyboardType="numeric"
              value={maxCalories}
              onChangeText={setMaxCalories}
            />

            <TextInput
              style={styles.input}
              placeholder="Maximum sugar (g)"
              keyboardType="numeric"
              value={maxSugar}
              onChangeText={setMaxSugar}
            />

            <Text style={styles.sectionTitle}>Allergens</Text>

            {["Peanuts", "Tree nuts", "Soy", "Eggs", "Wheat"].map(
              (allergen) => (
                <Checkbox
                  key={allergen}
                  label={allergen}
                  checked={allergens.includes(allergen)}
                  onPress={() => toggleAllergen(allergen)}
                />
              ),
            )}
          </ScrollView>

          <Pressable style={styles.applyButton} onPress={handleApply}>
            <Text style={styles.applyText}>Apply Filters</Text>
          </Pressable>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    justifyContent: "flex-end",
  },

  sheet: {
    height: "90%",
    backgroundColor: "white",
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 20,
  },

  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 20,
  },

  title: {
    fontSize: 24,
    fontWeight: "700",
  },

  close: {
    fontSize: 22,
  },

  sectionTitle: {
    fontSize: 18,
    fontWeight: "600",
    marginTop: 20,
    marginBottom: 10,
  },

  option: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: "#eee",
  },

  optionText: {
    fontSize: 16,
  },

  checkbox: {
    width: 22,
    height: 22,
    borderWidth: 2,
    borderColor: "#000",
    borderRadius: 4,
    alignItems: "center",
    justifyContent: "center",
  },

  checkmark: {
    color: "#000",
    fontSize: 15,
    fontWeight: "bold",
  },

  input: {
    borderWidth: 1,
    borderColor: "#ccc",
    borderRadius: 8,
    padding: 12,
    marginBottom: 10,
  },

  applyButton: {
    backgroundColor: "#000",
    padding: 16,
    borderRadius: 10,
    alignItems: "center",
    marginTop: 12,
  },

  applyText: {
    color: "white",
    fontWeight: "600",
    fontSize: 16,
  },
});
