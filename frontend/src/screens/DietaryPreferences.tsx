import React, { useEffect, useState } from "react";
import {
  ActivityIndicator,
  Alert,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { Feather } from "@expo/vector-icons";
import { useNavigation } from "@react-navigation/native";

import { useAuth } from "../auth/AuthContext";
import { getAllergens } from "../services/drinks";
import {
  getSavedPreferences,
  updateSavedPreferences,
  type SavedPreferences,
} from "../services/preferences";
import { useTheme, type Theme } from "../theme/ThemeProvider";

const DIETARY_OPTIONS = ["Vegan", "Vegetarian", "Dairy-free", "Gluten-free"];

const capitalize = (value: string) =>
  value.charAt(0).toUpperCase() + value.slice(1);

export default function DietaryPreferences() {
  const navigation = useNavigation();
  const { theme } = useTheme();
  const { token } = useAuth();
  const styles = createStyles(theme);

  const [dietary, setDietary] = useState<string[]>([]);
  const [excluded, setExcluded] = useState<string[]>([]);
  const [allergenOptions, setAllergenOptions] = useState<string[]>([]);

  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(false);
  const [retry, setRetry] = useState(0);
  const [saving, setSaving] = useState(false);
  const [initial, setInitial] = useState<SavedPreferences | null>(null);

  useEffect(() => {
    if (!token) {
      setLoading(false);
      return;
    }

    let active = true;
    setLoading(true);
    setLoadError(false);

    Promise.all([getSavedPreferences(token), getAllergens()])
      .then(([saved, allergens]) => {
        if (!active) return;
        setDietary(saved.dietaryPreferences);
        setExcluded(saved.excludedAllergens);
        setInitial(saved);
        setAllergenOptions(allergens.map((item) => item.name));
      })
      .catch(() => {
        if (active) setLoadError(true);
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [token, retry]);

  const toggle = (
    setList: React.Dispatch<React.SetStateAction<string[]>>,
    value: string,
  ) => {
    setList((current) =>
      current.includes(value)
        ? current.filter((item) => item !== value)
        : [...current, value],
    );
  };

  const sameItems = (a: string[], b: string[]) =>
    a.length === b.length && [...a].sort().join("|") === [...b].sort().join("|");

  // Save is only enabled once something differs from what is saved.
  const isDirty =
    !!initial &&
    (!sameItems(dietary, initial.dietaryPreferences) ||
      !sameItems(excluded, initial.excludedAllergens));

  const handleSave = async () => {
    if (!token || saving || !isDirty) return;

    setSaving(true);
    try {
      const payload = {
        dietaryPreferences: dietary,
        excludedAllergens: excluded,
      };
      await updateSavedPreferences(token, payload);
      setInitial(payload);
      Alert.alert("Saved", "Your dietary preferences were saved to your account.");
    } catch {
      Alert.alert(
        "Couldn't save",
        "Unable to save your preferences. Please try again.",
      );
    } finally {
      setSaving(false);
    }
  };

  const renderCheckbox = (label: string, checked: boolean, onPress: () => void) => (
    <Pressable
      key={label}
      style={styles.option}
      onPress={onPress}
      accessibilityRole="checkbox"
      accessibilityState={{ checked }}
      accessibilityLabel={label}
    >
      <Text style={styles.optionText}>{label}</Text>
      <View style={[styles.checkbox, checked && styles.checkboxChecked]}>
        {checked && <Feather name="check" size={15} color={theme.buttonText} />}
      </View>
    </Pressable>
  );

  const renderContent = () => {
    if (loading) {
      return <ActivityIndicator style={styles.loading} color={theme.icon} />;
    }

    if (loadError) {
      return (
        <Pressable
          onPress={() => setRetry((value) => value + 1)}
          accessibilityRole="button"
        >
          <Text style={styles.message}>
            Unable to load your preferences. Tap to retry.
          </Text>
        </Pressable>
      );
    }

    return (
      <>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Dietary needs</Text>
        </View>

        {DIETARY_OPTIONS.map((option) =>
          renderCheckbox(option, dietary.includes(option), () =>
            toggle(setDietary, option),
          ),
        )}

        <View style={[styles.sectionHeader, styles.secondSection]}>
          <Text style={styles.sectionTitle}>Allergens to avoid</Text>
        </View>

        {allergenOptions.length ? (
          allergenOptions.map((allergen) =>
            renderCheckbox(
              capitalize(allergen),
              excluded.includes(allergen),
              () => toggle(setExcluded, allergen),
            ),
          )
        ) : (
          <Text style={styles.message}>No allergen options available.</Text>
        )}
      </>
    );
  };

  return (
    <View style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.header}>
          <Pressable
            onPress={() => navigation.goBack()}
            hitSlop={12}
            accessibilityLabel="Back to Settings"
          >
            <Feather name="arrow-left" size={24} color={theme.icon} />
          </Pressable>
          <Text style={styles.title}>Dietary & Allergens</Text>
        </View>

        <Text style={styles.description}>
          Saved to your account. The Filter button still changes a single search.
        </Text>

        {renderContent()}
      </ScrollView>

      <View style={styles.footer}>
        <Pressable
          style={[
            styles.saveButton,
            (loading || loadError || saving || !isDirty) &&
              styles.saveButtonDisabled,
          ]}
          onPress={handleSave}
          disabled={loading || loadError || saving || !isDirty}
          accessibilityRole="button"
        >
          <Text style={styles.saveText}>
            {saving ? "SAVING…" : "SAVE PREFERENCES"}
          </Text>
        </Pressable>
      </View>
    </View>
  );
}

const createStyles = (theme: Theme) =>
  StyleSheet.create({
    container: { flex: 1, backgroundColor: theme.background },
    content: { paddingHorizontal: 24, paddingTop: 55, paddingBottom: 120 },
    header: {
      flexDirection: "row",
      alignItems: "center",
      gap: 18,
      marginBottom: 16,
    },
    title: { fontSize: 26, fontWeight: "700", color: theme.text },
    description: { fontSize: 14, color: theme.subtext, marginBottom: 8 },
    sectionHeader: {
      flexDirection: "row",
      alignItems: "baseline",
      justifyContent: "space-between",
      paddingTop: 24,
      paddingBottom: 4,
    },
    secondSection: { paddingTop: 32 },
    sectionTitle: { fontSize: 20, fontWeight: "700", color: theme.text },
    option: {
      flexDirection: "row",
      justifyContent: "space-between",
      alignItems: "center",
      paddingVertical: 20,
      borderBottomWidth: StyleSheet.hairlineWidth,
      borderBottomColor: theme.divider,
    },
    optionText: { fontSize: 16, color: theme.text, flexShrink: 1 },
    checkbox: {
      width: 24,
      height: 24,
      borderWidth: 2,
      borderRadius: 6,
      borderColor: theme.subtext,
      alignItems: "center",
      justifyContent: "center",
    },
    checkboxChecked: {
      borderColor: theme.icon,
      backgroundColor: theme.icon,
    },
    loading: { marginTop: 48 },
    message: { fontSize: 16, color: theme.text, paddingVertical: 20 },
    footer: {
      position: "absolute",
      left: 24,
      right: 24,
      bottom: 28,
    },
    saveButton: {
      height: 52,
      borderRadius: 999,
      backgroundColor: theme.button,
      alignItems: "center",
      justifyContent: "center",
      shadowColor: "#281405",
      shadowOffset: { width: 0, height: 4 },
      shadowOpacity: 0.28,
      shadowRadius: 10,
      elevation: 6,
    },
    saveButtonDisabled: { opacity: 0.5 },
    saveText: {
      color: theme.buttonText,
      fontSize: 13,
      fontWeight: "700",
      letterSpacing: 0.6,
    },
  });