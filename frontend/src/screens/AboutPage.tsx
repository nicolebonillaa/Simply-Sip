import React from "react";
import { Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { Feather } from "@expo/vector-icons";
import { useNavigation } from "@react-navigation/native";
import Svg, { Circle, Path } from "react-native-svg";
import { useTheme, type Theme } from "../theme/ThemeProvider";

type FeatureIcon = "sliders" | "map-pin" | "heart" | "star" | "sparkles";

function MockupIcon({ name, color, size = 26 }: { name: FeatureIcon | "coffee"; color: string; size?: number }) {
  const paths: Record<FeatureIcon | "coffee", string[]> = {
    coffee: ["M5 8h12v6a5 5 0 0 1-5 5h-2a5 5 0 0 1-5-5V8Z", "M17 10h1.5a2.5 2.5 0 0 1 0 5H17", "M9 3v2M13 3v2"],
    sliders: ["M21 4h-7M10 4H3M21 12h-9M8 12H3M21 20h-5M12 20H3M14 2v4M8 10v4M16 18v4"],
    "map-pin": ["M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 0 1 16 0Z"],
    heart: ["M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1-1.1a5.5 5.5 0 0 0-7.8 7.8l1 1.1L12 21l7.8-7.5 1-1.1a5.5 5.5 0 0 0 0-7.8Z"],
    star: ["m12 2 3.1 6.3 6.9 1-5 4.9 1.2 6.8L12 17.8 5.8 21l1.2-6.8-5-4.9 6.9-1L12 2Z"],
    sparkles: ["M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8L12 3Z", "M19 17v4M17 19h4"],
  };

  return (
    <Svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth={1.7} strokeLinecap="round" strokeLinejoin="round">
      {paths[name].map((d, index) => <Path key={index} d={d} />)}
      {name === "map-pin" && <Circle cx={12} cy={10} r={3} />}
    </Svg>
  );
}

const features: { icon: FeatureIcon; title: string; description: string }[] = [
  {
    icon: "sliders",
    title: "Filter by what matters",
    description: "Dietary restrictions, allergies, calories, sugar, and ingredients",
  },
  {
    icon: "map-pin",
    title: "Find drinks nearby",
    description: "Cafés and drinks within about 2 miles of CSUN, shown on a map",
  },
  {
    icon: "heart",
    title: "Save your favorites",
    description: "Keep your go-to drinks and spots in one place",
  },
  {
    icon: "star",
    title: "Ratings and reviews",
    description: "See what other students think and share your own",
  },
  {
    icon: "sparkles",
    title: "Suggestions for you",
    description: "Suggestions based on the drinks and cafés you favorite",
  },
];

export default function AboutPage() {
  const navigation = useNavigation();
  const { theme } = useTheme();
  const styles = createStyles(theme);

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.header}>
        <Pressable
          onPress={() => navigation.goBack()}
          accessibilityLabel="Back to Settings"
          hitSlop={12}
          style={styles.backButton}
        >
          <Feather name="arrow-left" size={26} color={theme.icon} />
        </Pressable>
        <Text style={styles.headerTitle}>About Simply Sip</Text>
      </View>

      <View style={styles.hero}>
        <View style={styles.heroIcon}>
          <MockupIcon name="coffee" size={34} color={theme.buttonText} />
        </View>
        <Text style={styles.appTitle}>Simply Sip</Text>
        <Text style={styles.intro}>
          Find drinks around CSUN that fit your diet, nutrition goals, and taste.
        </Text>
      </View>

      <View style={styles.whySection}>
        <Text style={styles.sectionTitle}>Why we built it</Text>
        <Text style={styles.bodyText}>
          It's hard to know what's in a drink, how much sugar it has, or whether
          it works with an allergy or dietary restriction. Simply Sip puts that
          information in one place so you can choose with confidence.
        </Text>
      </View>

      <Text style={styles.featuresTitle}>What you can do</Text>
      <View style={styles.features}>
        {features.map((feature) => (
          <View key={feature.title} style={styles.featureRow}>
            <MockupIcon name={feature.icon} color={theme.icon} />
            <View style={styles.featureText}>
              <Text style={styles.featureTitle}>{feature.title}</Text>
              <Text style={styles.featureDescription}>{feature.description}</Text>
            </View>
          </View>
        ))}
      </View>

      <View style={styles.sourceSection}>
        <Text style={styles.sectionTitle}>Where our info comes from</Text>
        <Text style={styles.bodyText}>
          Drink, nutrition, and location details come from publicly available
          menus and campus dining sources, and are updated by our team. Menus
          change, so details may occasionally be out of date.
        </Text>
        <View style={styles.allergyNote}>
          <Feather name="info" size={24} color={theme.icon} />
          <Text style={styles.allergyText}>
            <Text style={styles.bold}>Have an allergy?</Text> Always confirm with
            the café staff before ordering. Simply Sip is a guide, not a
            guarantee of safety.
          </Text>
        </View>
      </View>

      <Text style={styles.footer}>
        Simply Sip is an independent student project built by CSUN students. It
        is not affiliated with or endorsed by CSUN, Starbucks, Freudian Sip, or
        any café listed in the app.
      </Text>
    </ScrollView>
  );
}

const createStyles = (theme: Theme) =>
  StyleSheet.create({
    container: { flex: 1, backgroundColor: theme.background },
    content: { paddingTop: 55, paddingBottom: 48 },
    header: {
      flexDirection: "row",
      alignItems: "center",
      gap: 14,
      paddingHorizontal: 24,
    },
    backButton: { width: 30, alignItems: "flex-start" },
    headerTitle: { fontSize: 26, fontWeight: "700", color: theme.text },
    hero: { alignItems: "flex-start", gap: 14, paddingHorizontal: 24, paddingTop: 28, paddingBottom: 8 },
    heroIcon: {
      width: 64,
      height: 64,
      borderRadius: 18,
      backgroundColor: theme.button,
      alignItems: "center",
      justifyContent: "center",
    },
    appTitle: { fontSize: 30, fontWeight: "700", color: theme.text },
    intro: { fontSize: 17, lineHeight: 25, color: theme.subtext },
    whySection: { paddingHorizontal: 24, paddingTop: 28 },
    sectionTitle: { fontSize: 20, fontWeight: "700", color: theme.text, marginBottom: 10 },
    bodyText: { fontSize: 16, lineHeight: 25, color: theme.subtext },
    featuresTitle: { fontSize: 20, fontWeight: "700", color: theme.text, marginHorizontal: 24, marginTop: 32, marginBottom: 4 },
    features: { marginHorizontal: 24 },
    featureRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 16,
      paddingVertical: 16,
      borderBottomWidth: StyleSheet.hairlineWidth,
      borderBottomColor: theme.divider,
    },
    featureText: { flex: 1 },
    featureTitle: { fontSize: 17, color: theme.text },
    featureDescription: { fontSize: 14, lineHeight: 20, color: theme.subtext, marginTop: 2 },
    sourceSection: { paddingHorizontal: 24, paddingTop: 32 },
    allergyNote: {
      flexDirection: "row",
      alignItems: "flex-start",
      gap: 12,
      marginTop: 16,
      padding: 16,
      borderRadius: 14,
      backgroundColor: theme.chip,
    },
    allergyText: { flex: 1, fontSize: 15, lineHeight: 23, color: theme.text },
    bold: { fontWeight: "700" },
    footer: { marginHorizontal: 24, marginTop: 28, fontSize: 13, lineHeight: 20, color: theme.subtext },
  });