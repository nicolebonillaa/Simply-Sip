import React from "react";
import { Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { Feather } from "@expo/vector-icons";
import { useNavigation } from "@react-navigation/native";
import { useTheme, type Theme } from "../theme/ThemeProvider";

export default function AppearancePage() {
  const navigation = useNavigation();
  const {
    theme,
    mode,
    setMode,
    isDark,
    lightTabIcons,
    setLightTabIcons,
    darkTabIcons,
    setDarkTabIcons,
  } = useTheme();
  const styles = createStyles(theme);

  const RadioOption = ({
    label,
    selected,
    onPress,
  }: {
    label: string;
    selected: boolean;
    onPress: () => void;
  }) => (
    <Pressable
      style={styles.choice}
      onPress={onPress}
      accessibilityRole="radio"
      accessibilityState={{ checked: selected }}
      accessibilityLabel={label}
    >
      <View style={[styles.radio, selected && styles.radioSelected]}>
        {selected && <View style={styles.radioCenter} />}
      </View>
      <Text style={styles.choiceText}>{label}</Text>
    </Pressable>
  );

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.header}>
        <Pressable
          onPress={() => navigation.goBack()}
          hitSlop={12}
          accessibilityLabel="Back to Settings"
        >
          <Feather name="arrow-left" size={24} color={theme.icon} />
        </Pressable>
        <Text style={styles.title}>Appearance</Text>
      </View>

      <Text style={styles.heading}>Display mode</Text>
      <RadioOption
        label="Use device setting"
        selected={mode === "system"}
        onPress={() => setMode("system")}
      />
      <RadioOption
        label="Light"
        selected={mode === "light"}
        onPress={() => setMode("light")}
      />
      <RadioOption
        label="Dark"
        selected={mode === "dark"}
        onPress={() => setMode("dark")}
      />

      <Text style={[styles.heading, styles.secondHeading]}>Tab icon color</Text>
      <Text style={styles.description}>
        {isDark ? "Dark mode" : "Light mode"} icons
      </Text>
      {isDark ? (
        <>
          <RadioOption
            label="Sand"
            selected={darkTabIcons === "sand"}
            onPress={() => setDarkTabIcons("sand")}
          />
          <RadioOption
            label="Pink"
            selected={darkTabIcons === "pink"}
            onPress={() => setDarkTabIcons("pink")}
          />
        </>
      ) : (
        <>
          <RadioOption
            label="Mocha"
            selected={lightTabIcons === "mocha"}
            onPress={() => setLightTabIcons("mocha")}
          />
          <RadioOption
            label="Pink"
            selected={lightTabIcons === "pink"}
            onPress={() => setLightTabIcons("pink")}
          />
        </>
      )}

    </ScrollView>
  );
}

const createStyles = (theme: Theme) =>
  StyleSheet.create({
    container: { flex: 1, backgroundColor: theme.background },
    content: { paddingHorizontal: 24, paddingTop: 55, paddingBottom: 50 },
    header: { flexDirection: "row", alignItems: "center", gap: 18, marginBottom: 42 },
    title: { fontSize: 26, fontWeight: "700", color: theme.text },
    heading: { fontSize: 20, fontWeight: "700", color: theme.text, marginBottom: 13 },
    secondHeading: { marginTop: 36 },
    description: { color: theme.subtext, fontSize: 14, marginBottom: 8 },
    choice: { flexDirection: "row", alignItems: "center", minHeight: 54, gap: 14 },
    choiceText: { fontSize: 16, color: theme.text },
    radio: {
      width: 22,
      height: 22,
      borderRadius: 11,
      borderWidth: 2,
      borderColor: theme.subtext,
      alignItems: "center",
      justifyContent: "center",
    },
    radioSelected: { borderColor: theme.icon },
    radioCenter: { width: 10, height: 10, borderRadius: 5, backgroundColor: theme.icon },
  });