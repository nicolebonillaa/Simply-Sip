import React from "react";
import {
  View,
  Text,
  StyleSheet,
  Pressable,
  ScrollView,
  Image,
  ActivityIndicator,
} from "react-native";
import { Feather } from "@expo/vector-icons";
import { useTheme, type Theme } from "../theme/ThemeProvider";
import { useNavigation } from "@react-navigation/native";

import { useAuth } from "../auth/AuthContext";

const ProfilePage = () => {
  const navigation = useNavigation();

  const { theme } = useTheme();
  const styles = createStyles(theme);

  const { status, user } = useAuth();
  const isSignedIn = status === "signedIn" && !!user;

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
          accessibilityLabel="Settings"
          onPress={() => navigation.navigate("Settings" as never)}
          style={styles.settingsButton}
          hitSlop={20}
        >
          <Feather name="settings" size={24} color={theme.icon} />
        </Pressable>
      </View>

      {/* Profile picture and name */}
      <View style={styles.profileHeader}>
        {status === "loading" ? (
          <ActivityIndicator color={theme.icon} />
        ) : isSignedIn ? (
          <>
            <Image
              source={require("../../assets/images/profile-picture.png")}
              style={styles.avatar}
            />

            <Text style={styles.name}>{user?.name}</Text>
          </>
        ) : (
          /* Guest-only sign-in prompt */
          <View style={styles.accountPrompt}>
            <Text style={styles.promptText}>
              Create an account or log in to save your favorite drinks and
              access them anytime.
            </Text>

            <View style={styles.accountButtons}>
              <Pressable
                style={styles.createAccountButton}
                onPress={() => navigation.navigate("Register" as never)}
              >
                <Text style={styles.createAccountText}>Create Account</Text>
              </Pressable>

              <Pressable
                style={styles.logInButton}
                onPress={() => navigation.navigate("Login" as never)}
              >
                <Text style={styles.logInText}>Log In</Text>
              </Pressable>
            </View>
          </View>
        )}
      </View>
      {isSignedIn && (
        <View style={styles.favoritesSection}>
          {/* Favorites */}
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
      )}
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
      justifyContent: "flex-end",
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
      alignItems: "center",
      gap: 12,
      marginTop: 12,
      width: "100%",
    },
    promptText: {
      color: theme.subtext,
      textAlign: "center",
      fontSize: 20,
      lineHeight: 30,
    },
    accountButtons: {
      flexDirection: "column",
      gap: 12,
      width: "100%",
    },
    createAccountButton: {
      backgroundColor: theme.background,
      paddingVertical: 16,
      width: "100%",
      alignItems: "center",
      borderRadius: 8,
      borderWidth: 2.5,
      borderColor: theme.button,
    },
    createAccountText: {
      color: theme.button,
      fontWeight: "600",
      fontSize: 20,
    },
    logInButton: {
      backgroundColor: theme.button,
      paddingVertical: 16,
      width: "100%",
      alignItems: "center",
      borderRadius: 8,
    },
    logInText: {
      color: theme.buttonText,
      fontWeight: "600",
      fontSize: 20,
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
  });

export default ProfilePage;
