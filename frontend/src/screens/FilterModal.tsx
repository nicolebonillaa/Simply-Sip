import React, { useRef, useState } from "react";
import {
  Modal,
  View,
  Text,
  StyleSheet,
  Pressable,
  ScrollView,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import RangeSlider from "../components/RangeSlider";
import { useTheme } from "../theme/ThemeProvider";
import type { Filters, SortKey } from "../types";
import {
  ALLERGEN_OPTIONS,
  DEFAULT_FILTERS,
  FILTER_LIMITS,
  MILK_OPTIONS,
  SORT_OPTIONS,
  priceLabel,
} from "../utils/filters";

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

type Screen =
  | "main"
  | "featured"
  | "dietary"
  | "calories"
  | "sugar"
  | "allergens"
  | "milk"
  | "price"
  | "distance";

const TITLES: Record<Screen, string> = {
  main: "",
  featured: "Featured",
  dietary: "Dietary needs",
  calories: "Calories",
  sugar: "Sugar",
  allergens: "Allergens",
  milk: "Milk",
  price: "Price range",
  distance: "Distance",
};

const L = FILTER_LIMITS;

function Checkbox({ label, checked, onPress }: CheckboxProps) {
  const { theme: t } = useTheme();

  return (
    <Pressable
      style={[styles.option, { borderBottomColor: t.divider }]}
      onPress={onPress}
    >
      <Text style={[styles.optionText, { color: t.text }]}>{label}</Text>

      <View
        style={[
          styles.checkbox,
          {
            borderColor: checked ? t.icon : t.subtext,
            backgroundColor: checked ? t.icon : "transparent",
          },
        ]}
      >
        {checked && (
          <Text style={[styles.checkmark, { color: t.buttonText }]}>✔</Text>
        )}
      </View>
    </Pressable>
  );
}

// One row on the main list: label on the left, optional value + chevron on the right
function NavRow({
  label,
  value,
  onPress,
}: {
  label: string;
  value?: string;
  onPress: () => void;
}) {
  const { theme: t } = useTheme();

  return (
    <Pressable
      style={[styles.option, { borderBottomColor: t.divider }]}
      onPress={onPress}
    >
      <Text style={[styles.optionText, { color: t.text }]}>{label}</Text>

      <View style={styles.optionRight}>
        {!!value && (
          <Text
            style={[styles.optionValue, { color: t.subtext }]}
            numberOfLines={1}
          >
            {value}
          </Text>
        )}
        <Ionicons name="chevron-forward" size={22} color={t.icon} />
      </View>
    </Pressable>
  );
}

export default function FilterModal({
  visible,
  onClose,
  onApply,
}: FiltersProps) {
  const { theme: t } = useTheme();

  const [screen, setScreen] = useState<Screen>("main");

  const [sort, setSort] = useState<SortKey[]>([]);

  const [vegan, setVegan] = useState(false);
  const [vegetarian, setVegetarian] = useState(false);
  const [dairyFree, setDairyFree] = useState(false);
  const [glutenFree, setGlutenFree] = useState(false);

  // Sliders hold a number; the far end of each slider means "Any" (saved as null)
  const [maxCalories, setMaxCalories] = useState<number>(L.calories.max);
  const [maxSugar, setMaxSugar] = useState<number>(L.sugar.max);
  const [minPrice, setMinPrice] = useState<number>(L.price.min);
  const [maxPrice, setMaxPrice] = useState<number>(L.price.max);
  const [maxDistance, setMaxDistance] = useState<number>(L.distance.max);

  const [allergens, setAllergens] = useState<string[]>([]);
  const [milkTypes, setMilkTypes] = useState<string[]>([]);

  // Last applied filters, so closing without DONE can put everything back
  const savedFilters = useRef<Filters>(DEFAULT_FILTERS);

  const toggleAllergen = (allergen: string) => {
    setAllergens((current) =>
      current.includes(allergen)
        ? current.filter((item) => item !== allergen)
        : [...current, allergen],
    );
  };

  const toggleMilk = (milk: string) => {
    setMilkTypes((current) =>
      current.includes(milk)
        ? current.filter((item) => item !== milk)
        : [...current, milk],
    );
  };

  const toggleSort = (key: SortKey) => {
    setSort((current) =>
      current.includes(key)
        ? current.filter((item) => item !== key)
        : [...current, key],
    );
  };

  const loadFilters = (f: Filters) => {
    setSort(f.sort);
    setVegan(f.vegan);
    setVegetarian(f.vegetarian);
    setDairyFree(f.dairyFree);
    setGlutenFree(f.glutenFree);
    setMaxCalories(f.maxCalories ?? L.calories.max);
    setMaxSugar(f.maxSugar ?? L.sugar.max);
    setMinPrice(f.minPrice ?? L.price.min);
    setMaxPrice(f.maxPrice ?? L.price.max);
    setMaxDistance(f.maxDistance ?? L.distance.max);
    setAllergens(f.allergens);
    setMilkTypes(f.milkTypes);
  };

  const handleReset = () => loadFilters(DEFAULT_FILTERS);

  // DONE
  const handleApply = () => {
    const filters: Filters = {
      sort,
      vegan,
      vegetarian,
      dairyFree,
      glutenFree,
      maxCalories: maxCalories >= L.calories.max ? null : maxCalories,
      maxSugar: maxSugar >= L.sugar.max ? null : maxSugar,
      minPrice: minPrice <= L.price.min ? null : minPrice,
      maxPrice: maxPrice >= L.price.max ? null : maxPrice,
      maxDistance: maxDistance >= L.distance.max ? null : maxDistance,
      allergens,
      milkTypes,
    };

    savedFilters.current = filters;
    setScreen("main");
    onApply(filters);
  };

  // Android back button / swipe down: step back, or close without applying
  const handleRequestClose = () => {
    if (screen !== "main") {
      setScreen("main");
      return;
    }

    loadFilters(savedFilters.current);
    onClose();
  };

  const sliderColors = {
    trackColor: t.chip,
    fillColor: t.icon,
    thumbColor: t.icon,
    thumbBorderColor: t.background,
  };

  const dietCount = [vegan, vegetarian, dairyFree, glutenFree].filter(Boolean).length;
  const priceActive = minPrice > L.price.min || maxPrice < L.price.max;

  const priceReadout = !priceActive
    ? "Any price"
    : minPrice > L.price.min && maxPrice < L.price.max
      ? `$${minPrice}–$${maxPrice}`
      : minPrice > L.price.min
        ? `$${minPrice}+`
        : `Up to $${maxPrice}`;

  const renderSlider = (
    readout: string,
    endMin: string,
    endMax: string,
    slider: React.ReactNode,
  ) => (
    <View style={styles.sliderPad}>
      <Text style={[styles.readout, { color: t.text }]}>{readout}</Text>
      {slider}
      <View style={styles.ends}>
        <Text style={[styles.endText, { color: t.subtext }]}>{endMin}</Text>
        <Text style={[styles.endText, { color: t.subtext }]}>{endMax}</Text>
      </View>
    </View>
  );

  const renderMain = () => (
    <>
      <View style={[styles.sectionHeader, { backgroundColor: t.band }]}>
        <Text style={[styles.sectionTitle, { color: t.text }]}>Sort by</Text>
      </View>

      <NavRow
        label="Featured"
        value={sort.length ? `${sort.length} selected` : undefined}
        onPress={() => setScreen("featured")}
      />

      <View style={[styles.sectionHeader, { backgroundColor: t.band }]}>
        <Text style={[styles.sectionTitle, { color: t.text }]}>Filter by</Text>
      </View>

      <NavRow
        label="Dietary needs"
        value={dietCount ? `${dietCount} selected` : undefined}
        onPress={() => setScreen("dietary")}
      />
      <NavRow
        label="Calories"
        value={maxCalories < L.calories.max ? `≤ ${maxCalories}` : undefined}
        onPress={() => setScreen("calories")}
      />
      <NavRow
        label="Sugar"
        value={maxSugar < L.sugar.max ? `≤ ${maxSugar} g` : undefined}
        onPress={() => setScreen("sugar")}
      />
      <NavRow
        label="Allergens"
        value={allergens.length ? `${allergens.length} avoided` : undefined}
        onPress={() => setScreen("allergens")}
      />
      <NavRow
        label="Milk"
        value={milkTypes.length ? milkTypes.join(", ") : undefined}
        onPress={() => setScreen("milk")}
      />
      <NavRow
        label="Price range"
        value={
          priceActive
            ? priceLabel(
                minPrice > L.price.min ? minPrice : null,
                maxPrice < L.price.max ? maxPrice : null,
              )
            : undefined
        }
        onPress={() => setScreen("price")}
      />
      <NavRow
        label="Distance"
        value={maxDistance < L.distance.max ? `≤ ${maxDistance} mi` : undefined}
        onPress={() => setScreen("distance")}
      />
    </>
  );

  const renderSub = () => {
    switch (screen) {
      case "featured":
        return SORT_OPTIONS.map((option) => (
          <Checkbox
            key={option.key}
            label={option.label}
            checked={sort.includes(option.key)}
            onPress={() => toggleSort(option.key)}
          />
        ));

      case "dietary":
        return (
          <>
            <Checkbox label="Vegan" checked={vegan} onPress={() => setVegan(!vegan)} />
            <Checkbox label="Vegetarian" checked={vegetarian} onPress={() => setVegetarian(!vegetarian)} />
            <Checkbox label="Dairy-free" checked={dairyFree} onPress={() => setDairyFree(!dairyFree)} />
            <Checkbox label="Gluten-free" checked={glutenFree} onPress={() => setGlutenFree(!glutenFree)} />
          </>
        );

      case "allergens":
        return ALLERGEN_OPTIONS.map((allergen) => (
          <Checkbox
            key={allergen}
            label={allergen}
            checked={allergens.includes(allergen)}
            onPress={() => toggleAllergen(allergen)}
          />
        ));

      case "milk":
        return MILK_OPTIONS.map((milk) => (
          <Checkbox
            key={milk}
            label={milk}
            checked={milkTypes.includes(milk)}
            onPress={() => toggleMilk(milk)}
          />
        ));

      case "calories":
        return renderSlider(
          maxCalories >= L.calories.max ? "Any" : `Up to ${maxCalories} cal`,
          "0",
          "600+",
          <RangeSlider
            {...sliderColors}
            {...L.calories}
            values={[maxCalories]}
            labels={["Maximum calories"]}
            onChange={([v]) => setMaxCalories(v)}
          />,
        );

      case "sugar":
        return renderSlider(
          maxSugar >= L.sugar.max ? "Any" : `Up to ${maxSugar} g`,
          "0 g",
          "70+ g",
          <RangeSlider
            {...sliderColors}
            {...L.sugar}
            values={[maxSugar]}
            labels={["Maximum sugar"]}
            onChange={([v]) => setMaxSugar(v)}
          />,
        );

      case "price":
        return renderSlider(
          priceReadout,
          "$0",
          "$20+",
          <RangeSlider
            {...sliderColors}
            {...L.price}
            values={[minPrice, maxPrice]}
            labels={["Minimum price", "Maximum price"]}
            onChange={([lo, hi]) => {
              setMinPrice(lo);
              setMaxPrice(hi);
            }}
          />,
        );

      case "distance":
        return renderSlider(
          maxDistance >= L.distance.max ? "Any" : `Within ${maxDistance} mi`,
          "0.25 mi",
          "2 mi",
          <RangeSlider
            {...sliderColors}
            {...L.distance}
            values={[maxDistance]}
            labels={["Maximum distance"]}
            onChange={([v]) => setMaxDistance(v)}
          />,
        );

      default:
        return null;
    }
  };

  return (
    <Modal
      visible={visible}
      transparent
      animationType="slide"
      onRequestClose={handleRequestClose}
    >
      <View style={[styles.overlay, { backgroundColor: t.scrim }]}>
        {/* A Modal is its own native view, so the sheet needs the same background token App.tsx uses */}
        <View style={[styles.sheet, { backgroundColor: t.background }]}>
          <View style={styles.header}>
            <View style={styles.headerSide}>
              {screen === "main" ? (
                <Pressable onPress={handleReset} hitSlop={10}>
                  <Text style={[styles.resetText, { color: t.subtext }]}>
                    Reset
                  </Text>
                </Pressable>
              ) : (
                <Pressable
                  onPress={() => setScreen("main")}
                  hitSlop={10}
                  accessibilityLabel="Back"
                >
                  <Ionicons name="chevron-back" size={26} color={t.icon} />
                </Pressable>
              )}
            </View>

            <Text style={[styles.title, { color: t.text }]}>
              {TITLES[screen]}
            </Text>

            <Pressable
              style={[styles.applyButton, { backgroundColor: t.button }]}
              onPress={handleApply}
            >
              <Text style={[styles.applyText, { color: t.buttonText }]}>
                DONE
              </Text>
            </Pressable>
          </View>

          <ScrollView showsVerticalScrollIndicator={false}>
            {screen === "main" ? renderMain() : renderSub()}
            <View style={styles.bottomSpace} />
          </ScrollView>
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
    flex: 1,
    marginTop: 52,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    overflow: "hidden",
  },

  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: 20,
    paddingTop: 22,
    paddingBottom: 10,
    minHeight: 68,
  },

  headerSide: {
    minWidth: 76,
  },

  title: {
    fontSize: 18,
    fontWeight: "700",
  },

  resetText: {
    fontSize: 15,
    fontWeight: "600",
  },

  sectionHeader: {
    paddingHorizontal: 20,
    paddingVertical: 14,
  },

  sectionTitle: {
    fontSize: 20,
    fontWeight: "700",
  },

  option: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: 20,
    paddingVertical: 20,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },

  optionText: {
    fontSize: 16,
    flexShrink: 1,
  },

  optionRight: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    flexShrink: 1,
    marginLeft: 12,
  },

  optionValue: {
    fontSize: 14,
    flexShrink: 1,
    maxWidth: 150,
  },

  checkbox: {
    width: 24,
    height: 24,
    borderWidth: 2,
    borderRadius: 6,
    alignItems: "center",
    justifyContent: "center",
  },

  checkmark: {
    fontSize: 14,
    fontWeight: "bold",
  },

  sliderPad: {
    paddingHorizontal: 20,
    paddingTop: 8,
  },

  readout: {
    fontSize: 30,
    fontWeight: "700",
    paddingTop: 22,
    paddingBottom: 6,
  },

  ends: {
    flexDirection: "row",
    justifyContent: "space-between",
  },

  endText: {
    fontSize: 13,
  },

  applyButton: {
    minWidth: 76,
    height: 40,
    paddingHorizontal: 20,
    borderRadius: 999,
    alignItems: "center",
    justifyContent: "center",
  },

  applyText: {
    fontWeight: "700",
    fontSize: 13,
    letterSpacing: 0.6,
  },

  bottomSpace: {
    height: 48,
  },
});