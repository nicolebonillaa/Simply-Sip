import React, { useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  Pressable,
  ScrollView,
  Image,
} from "react-native";
import { Feather } from "@expo/vector-icons";
import { useTheme, type Theme } from "../theme/ThemeProvider";

const ProfilePage = () => {
  // Temporary mock authentication state
  const { theme } = useTheme();
  const [isSignedIn, setIsSignedIn] = useState(false);

  const mockUsername = "Alex Smith";

  const styles = createStyles(theme);

  const mockFavorites = [
    {
      id: "1",
      name: "Iced Latte",
      location: "Matador Bookstore Cafe",
    },
    {
      id: "2",
      name: "Matcha Latte",
      location: "Campus Cafe",
    },
    {
      id: "3",
      name: "Strawberry Smoothie",
      location: "Juice Bar",
    },
    {
      id: "4",
      name: "Cold Brew",
      location: "Local Coffee Shop",
    },
  ];

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {/* Settings button */}
      <View style={styles.header}>
        <Pressable
          accessibilityLabel="Toggle mode"
          onPress={() => setIsSignedIn((current) => !current)}
          style={styles.toggleButton}
        >
          <Text style={styles.toggleText}>Toggle Mode</Text>
        </Pressable>

        <Pressable
          accessibilityLabel="Settings"
          onPress={() => console.log("Settings pressed")}
          style={styles.settingsButton}
          hitSlop={20}
        >
          <Feather name="settings" size={24} color={theme.icon} />
        </Pressable>
      </View>

      {/* Profile picture and name */}
      <View style={styles.profileHeader}>
        {isSignedIn ? (
          <>
            <Image
              source={require("../../assets/images/profile-picture.png")}
              style={styles.avatar}
            />

            <Text style={styles.name}>{mockUsername}</Text>
          </>
        ) : (
          /* Guest-only sign-in prompt */
          <View style={styles.accountPrompt}>
            <Text style={styles.promptText}>
              Sign in or create an account to personalize your experience.
            </Text>

            <View style={styles.accountButtons}>
              <Pressable
                style={styles.signUpButton}
                onPress={() => console.log("Sign up pressed")}
              >
                <Text style={styles.signUpText}>Sign Up</Text>
              </Pressable>

              <Pressable
                style={styles.logInButton}
                onPress={() => console.log("Log in pressed")}
              >
                <Text style={styles.logInText}>Log In</Text>
              </Pressable>
            </View>
          </View>
        )}
      </View>

      {/* Favorites */}
      <View style={styles.favoritesSection}>
        <Text style={styles.sectionTitle}>My Favorites</Text>

        {mockFavorites.map((drink) => (
          <View key={drink.id} style={styles.favoriteItem}>
            <Feather name="heart" size={20} color={theme.icon} />

            <View style={styles.favoriteInfo}>
              <Text style={styles.drinkName}>{drink.name}</Text>
              <Text style={styles.location}>{drink.location}</Text>
            </View>
          </View>
        ))}
      </View>
    </ScrollView>
  );
};

const createStyles = (theme: Theme) =>
  StyleSheet.create({
    container: {
      flex: 1,
      backgroundColor: theme.background,
    },
    content: {
      padding: 24,
      paddingTop: 55,
      paddingBottom: 125,
      position: "relative",
    },
    header: {
      flexDirection: "row",
      justifyContent: "space-between",
      alignItems: "center",
      marginBottom: 28,
    },
    title: {
      fontSize: 26,
      fontWeight: "700",
      color: theme.text,
    },
    settingsButton: {
      padding: 8,
    },
    profileHeader: {
      alignItems: "center",
      marginBottom: 32,
    },
    avatar: {
      width: 140,
      height: 140,
      borderRadius: 70,
      borderWidth: 1,
      borderColor: theme.inputBorder,
      marginBottom: 12,
      alignItems: "center",
      justifyContent: "center",
    },
    name: {
      fontSize: 30,
      fontWeight: "600",
      color: theme.text,
    },
    accountPrompt: {
      alignItems: "flex-start",
      gap: 12,
      marginTop: 12,
      width: "100%",
    },
    promptText: {
      color: theme.subtext,
      textAlign: "left",
      fontSize: 20,
    },
    accountButtons: {
      flexDirection: "row",
      gap: 24,
    },
    signUpButton: {
      backgroundColor: theme.background,
      padding: 10,
      paddingHorizontal: 38,
      borderRadius: 8,
      borderWidth: 2.5,
      borderColor: theme.button,
    },
    signUpText: {
      color: theme.button,
      fontWeight: "600",
      fontSize: 18,
    },
    logInButton: {
      backgroundColor: theme.button,
      padding: 10,
      paddingHorizontal: 48,
      borderRadius: 8,
    },
    logInText: {
      color: theme.buttonText,
      fontWeight: "600",
      fontSize: 18,
    },
    favoritesSection: {
      gap: 16,
    },
    sectionTitle: {
      fontSize: 20,
      fontWeight: "600",
      color: theme.text,
    },
    favoriteItem: {
      flexDirection: "row",
      alignItems: "center",
      gap: 12,
      paddingVertical: 12,
      borderBottomWidth: 1,
      borderBottomColor: theme.divider,
    },
    favoriteInfo: {
      flex: 1,
      gap: 4,
    },
    drinkName: {
      fontSize: 16,
      fontWeight: "500",
      color: theme.text,
    },
    location: {
      color: theme.subtext,
    },
    toggleButton: {
      padding: 6,
      borderWidth: 1,
      borderColor: theme.inputBorder,
      borderRadius: 8,
    },
    toggleText: {
      color: theme.text,
    },
  });

export default ProfilePage;
