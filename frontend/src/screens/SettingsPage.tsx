import React from "react";
import { View, Text, StyleSheet, Pressable, ScrollView } from "react-native";
import { Feather } from "@expo/vector-icons";
import { useNavigation } from "@react-navigation/native";
import { useTheme, type Theme } from "../theme/ThemeProvider";
import { PRIVACY_URL, TERMS_URL } from "../constants/legal";
import * as WebBrowser from "expo-web-browser";
import { useAuth } from "../auth/AuthContext";

type SettingItemProps = {
  icon: React.ComponentProps<typeof Feather>["name"];
  title: string;
  subtitle?: string;
  onPress: () => void;
  danger?: boolean;
};

const SettingItem = ({
  icon,
  title,
  subtitle,
  onPress,
  danger = false,
}: SettingItemProps) => {
  const { theme } = useTheme();
  const styles = createStyles(theme);

  return (
    <Pressable style={styles.settingItem} onPress={onPress}>
      <Feather name={icon} size={21} color={danger ? "#D9534F" : theme.icon} />

      <View style={styles.settingInfo}>
        <Text style={[styles.settingTitle, danger && styles.dangerText]}>
          {title}
        </Text>

        {subtitle && <Text style={styles.settingSubtitle}>{subtitle}</Text>}
      </View>

      <Feather name="chevron-right" size={20} color={theme.subtext} />
    </Pressable>
  );
};

const SectionTitle = ({ title }: { title: string }) => {
  const { theme } = useTheme();
  const styles = createStyles(theme);

  return <Text style={styles.sectionTitle}>{title}</Text>;
};

const SettingsPage = () => {
  const { theme, mode, isDark, lightTabIcons, darkTabIcons } = useTheme();
  const navigation = useNavigation();

  const styles = createStyles(theme);

  const { status, signOut } = useAuth();
  const isSignedIn = status === "signedIn";

  const openLink = (url: string) =>
    WebBrowser.openBrowserAsync(url, {
      toolbarColor: theme.background,
      controlsColor: theme.icon,
    });

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {/* Header */}
      <View style={styles.header}>
        <Pressable
          onPress={() => navigation.goBack()}
          style={styles.backButton}
          hitSlop={12}
        >
          <Feather name="arrow-left" size={24} color={theme.icon} />
        </Pressable>

        <Text style={styles.headerTitle}>Settings</Text>
      </View>

      {/* Preferences */}
      <View style={styles.section}>
        <SectionTitle title="Preferences" />

        <SettingItem
          icon="moon"
          title="Appearance"
          subtitle={`${mode === "system" ? "Device setting" : isDark ? "Dark" : "Light"} · ${isDark ? darkTabIcons : lightTabIcons} icons`}
          onPress={() => navigation.navigate("Appearance" as never)}
        />

        {isSignedIn && (
          <SettingItem
            icon="bell"
            title="Notifications"
            subtitle="Manage notification preferences"
            onPress={() => console.log("Notifications pressed")}
          />
        )}
      </View>

      {/* Account */}
      <View style={styles.section}>
        <SectionTitle title="Account" />

        {isSignedIn ? (
          <>
            <SettingItem
              icon="user"
              title="Account Information"
              subtitle="Manage your personal information"
              onPress={() => console.log("Account information pressed")}
            />

            <SettingItem
              icon="lock"
              title="Change Password"
              onPress={() => console.log("Change password pressed")}
            />

            <SettingItem
              icon="log-out"
              title="Log Out"
              onPress={signOut}
              danger
            />
          </>
        ) : (
          <>
            <SettingItem
              icon="user-plus"
              title="Create Account"
              subtitle="Save your favorite drinks"
              onPress={() => navigation.navigate("Register" as never)}
            />

            <SettingItem
              icon="log-in"
              title="Log In"
              onPress={() => navigation.navigate("Login" as never)}
            />
          </>
        )}
      </View>

      {/* About */}
      <View style={styles.section}>
        <SectionTitle title="About" />

        <SettingItem
          icon="info"
          title="About Simply Sip"
          onPress={() => navigation.navigate("About" as never)}
        />

        <SettingItem
          icon="shield"
          title="Privacy Policy"
          onPress={() => openLink(PRIVACY_URL)}
        />

        <SettingItem
          icon="file-text"
          title="Terms of Service"
          onPress={() => openLink(TERMS_URL)}
        />
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
      flexGrow: 1,
      padding: 24,
      paddingTop: 55,
    },
    header: {
      flexDirection: "row",
      alignItems: "center",
      marginBottom: 32,
    },
    backButton: {
      marginRight: 18,
    },
    headerTitle: {
      fontSize: 26,
      fontWeight: "700",
      color: theme.text,
    },
    section: {
      marginBottom: 30,
    },
    sectionTitle: {
      fontSize: 18,
      fontWeight: "700",
      color: theme.text,
      marginBottom: 12,
    },
    settingItem: {
      flexDirection: "row",
      alignItems: "center",
      paddingVertical: 17,
      borderBottomWidth: StyleSheet.hairlineWidth,
      borderBottomColor: theme.inputBorder,
      gap: 15,
    },
    settingInfo: {
      flex: 1,
      gap: 4,
    },
    settingTitle: {
      fontSize: 16,
      fontWeight: "500",
      color: theme.text,
    },
    settingSubtitle: {
      fontSize: 13,
      color: theme.subtext,
    },
    dangerText: {
      color: "#D9534F",
    },
    version: {
      textAlign: "center",
      color: theme.subtext,
      fontSize: 13,
      marginTop: 10,
      marginBottom: 20,
    },
  });

export default SettingsPage;
