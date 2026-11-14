import React, { useEffect, useState } from "react";
import {
  ActivityIndicator,
  Alert,
  Image,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { Feather } from "@expo/vector-icons";
import * as ImagePicker from "expo-image-picker";
import { useNavigation } from "@react-navigation/native";
import { useTheme, type Theme } from "../theme/ThemeProvider";
import {
  deleteAccount,
  getAccount,
  updateAccount,
  updatePhoto,
  type AccountProfile,
} from "../services/account";

const USERNAME_PATTERN = /^[A-Za-z0-9_]{3,20}$/;
const DANGER = "#D9534F";

const SIGN_IN_LABELS = {
  password: "Email and password",
  google: "Google",
  apple: "Apple",
} as const;

const errorMessage = (error: unknown) =>
  error instanceof Error
    ? error.message
    : "Something went wrong. Please try again.";

export default function AccountInformation() {
  const navigation = useNavigation();
  const { theme } = useTheme();
  const styles = createStyles(theme);

  const [profile, setProfile] = useState<AccountProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [name, setName] = useState("");
  const [username, setUsername] = useState("");
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    let active = true;

    getAccount()
      .then((account) => {
        if (!active) return;
        setProfile(account);
        setName(account.name);
        setUsername(account.username);
      })
      .catch((error) => {
        if (active) setLoadError(errorMessage(error));
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  const nameError = name.trim().length === 0 ? "Please enter your name." : null;
  const usernameError = USERNAME_PATTERN.test(username.trim())
    ? null
    : "Use 3 to 20 letters, numbers, or underscores.";

  // Only show an error once the person has actually changed that field.
  const showNameError = profile !== null && name !== profile.name && nameError;
  const showUsernameError =
    profile !== null && username !== profile.username && usernameError;

  const dirty =
    profile !== null &&
    (name.trim() !== profile.name || username.trim() !== profile.username);
  const canSave = dirty && !nameError && !usernameError && !saving;

  const handleSave = async () => {
    if (!canSave) return;

    setSaving(true);
    try {
      const updated = await updateAccount({
        name: name.trim(),
        username: username.trim(),
      });
      setProfile(updated);
      setName(updated.name);
      setUsername(updated.username);
      Alert.alert("Saved", "Your account information was updated.");
    } catch (error) {
      Alert.alert("Couldn't save", errorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  const handleChangePhoto = async () => {
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ["images"],
      allowsEditing: true, // shows the crop screen
      aspect: [1, 1],
      quality: 0.8,
    });
    if (result.canceled) return;

    const uri = result.assets[0].uri;

    // Show the new photo right away.
    setProfile((current) =>
      current ? { ...current, photoUri: uri } : current,
    );

    try {
      await updatePhoto(uri); // mock for now, real upload later
    } catch (error) {
      Alert.alert("Couldn't save photo", errorMessage(error));
    }
  };

  const confirmDelete = async () => {
    setDeleting(true);
    try {
      await deleteAccount();
      // Mock only: the real version should also clear the saved login.
      navigation.reset({ index: 0, routes: [{ name: "MainTabs" as never }] });
    } catch (error) {
      Alert.alert("Couldn't delete account", errorMessage(error));
      setDeleting(false);
    }
  };

  const handleDelete = () => {
    Alert.alert(
      "Delete account?",
      "This permanently removes your account, favorites, and preferences. This can't be undone.",
      [
        { text: "Cancel", style: "cancel" },
        { text: "Delete", style: "destructive", onPress: confirmDelete },
      ],
    );
  };

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      keyboardShouldPersistTaps="handled"
    >
      <View style={styles.header}>
        <Pressable
          onPress={() => navigation.goBack()}
          hitSlop={12}
          accessibilityLabel="Back to Settings"
        >
          <Feather name="arrow-left" size={24} color={theme.icon} />
        </Pressable>
        <Text style={styles.title}>Account Information</Text>
      </View>

      {loading && (
        <ActivityIndicator
          style={styles.loading}
          color={theme.icon}
          accessibilityLabel="Loading account information"
        />
      )}

      {!loading && loadError && (
        <Text style={styles.errorText}>{loadError}</Text>
      )}

      {!loading && profile && (
        <>
          {/* Photo */}
          <View style={styles.avatarSection}>
            <View style={styles.avatar}>
              {profile.photoUri ? (
                <Image
                  source={{ uri: profile.photoUri }}
                  style={styles.avatarImage}
                  accessibilityLabel="Your profile photo"
                />
              ) : (
                <Feather name="user" size={44} color={theme.icon} />
              )}
            </View>
            <Pressable
              onPress={handleChangePhoto}
              hitSlop={8}
              accessibilityRole="button"
              accessibilityLabel="Change photo"
            >
              <Text style={styles.linkText}>Change photo</Text>
            </Pressable>
          </View>

          {/* Fields */}
          <View style={styles.form}>
            <View style={styles.inputGroup}>
              <Text style={styles.label}>Full Name</Text>
              <TextInput
                style={styles.input}
                value={name}
                onChangeText={setName}
                placeholder="Enter your full name"
                placeholderTextColor={theme.subtext}
                autoCapitalize="words"
                accessibilityLabel="Full name"
              />
              {!!showNameError && (
                <Text style={styles.errorText}>{showNameError}</Text>
              )}
            </View>

            <View style={styles.inputGroup}>
              <Text style={styles.label}>Username</Text>
              <TextInput
                style={styles.input}
                value={username}
                onChangeText={setUsername}
                placeholder="Choose a username"
                placeholderTextColor={theme.subtext}
                autoCapitalize="none"
                autoCorrect={false}
                accessibilityLabel="Username"
              />
              {showUsernameError ? (
                <Text style={styles.errorText}>{showUsernameError}</Text>
              ) : (
                <Text style={styles.helperText}>
                  Shown on your ratings and reviews instead of your full name.
                </Text>
              )}
            </View>

            <View style={styles.inputGroup}>
              <Text style={styles.label}>Email</Text>
              <View style={styles.readOnlyField}>
                <TextInput
                  style={styles.readOnlyInput}
                  value={profile.email}
                  editable={false}
                  accessibilityLabel="Email, read only"
                />
                <Feather name="lock" size={18} color={theme.subtext} />
              </View>
            </View>
          </View>

          {/* Sign-in method */}
          <View style={styles.methodRow}>
            <Feather name="key" size={22} color={theme.icon} />
            <View style={styles.methodInfo}>
              <Text style={styles.methodLabel}>Sign-in method</Text>
              <Text style={styles.methodValue}>
                {SIGN_IN_LABELS[profile.signInMethod]}
              </Text>
            </View>
          </View>

          {/* Save */}
          <Pressable
            style={[styles.saveButton, !canSave && styles.saveButtonDisabled]}
            onPress={handleSave}
            disabled={!canSave}
            accessibilityRole="button"
            accessibilityState={{ disabled: !canSave }}
          >
            {saving ? (
              <ActivityIndicator color={theme.buttonText} />
            ) : (
              <Text style={styles.saveButtonText}>Save Changes</Text>
            )}
          </Pressable>

          {/* Delete */}
          <View style={styles.deleteSection}>
            <Pressable
              onPress={handleDelete}
              disabled={deleting}
              hitSlop={8}
              accessibilityRole="button"
              accessibilityLabel="Delete account"
            >
              <Text style={styles.deleteText}>
                {deleting ? "Deleting..." : "Delete Account"}
              </Text>
            </Pressable>
            <Text style={styles.helperText}>
              Permanently removes your account, favorites, and preferences.
            </Text>
          </View>
        </>
      )}
    </ScrollView>
  );
}

const createStyles = (theme: Theme) =>
  StyleSheet.create({
    container: {
      flex: 1,
      backgroundColor: theme.background,
    },
    content: {
      padding: 24,
      paddingTop: 55,
      paddingBottom: 60,
    },
    header: {
      flexDirection: "row",
      alignItems: "center",
      gap: 18,
      marginBottom: 24,
    },
    title: {
      fontSize: 26,
      fontWeight: "700",
      color: theme.text,
    },
    loading: {
      marginTop: 40,
    },
    avatarSection: {
      alignItems: "center",
      gap: 12,
      marginBottom: 24,
    },
    avatar: {
      width: 96,
      height: 96,
      borderRadius: 48,
      borderWidth: 1,
      borderColor: theme.inputBorder,
      alignItems: "center",
      justifyContent: "center",
      overflow: "hidden",
    },
    avatarImage: {
      width: 96,
      height: 96,
    },
    linkText: {
      color: theme.button,
      fontSize: 16,
      fontWeight: "600",
    },
    form: {
      gap: 18,
    },
    inputGroup: {
      gap: 8,
    },
    label: {
      fontSize: 16,
      fontWeight: "600",
      color: theme.text,
    },
    input: {
      borderWidth: 1,
      borderColor: theme.inputBorder,
      borderRadius: 8,
      padding: 14,
      fontSize: 16,
      color: theme.text,
      backgroundColor: theme.background,
    },
    readOnlyField: {
      flexDirection: "row",
      alignItems: "center",
      borderWidth: 1,
      borderColor: theme.inputBorder,
      borderRadius: 8,
      paddingHorizontal: 14,
    },
    readOnlyInput: {
      flex: 1,
      paddingVertical: 14,
      fontSize: 16,
      color: theme.subtext,
    },
    helperText: {
      fontSize: 13,
      color: theme.subtext,
    },
    errorText: {
      fontSize: 13,
      color: DANGER,
    },
    methodRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 15,
      marginTop: 24,
      paddingVertical: 16,
      borderTopWidth: StyleSheet.hairlineWidth,
      borderBottomWidth: StyleSheet.hairlineWidth,
      borderColor: theme.inputBorder,
    },
    methodInfo: {
      flex: 1,
      gap: 2,
    },
    methodLabel: {
      fontSize: 13,
      color: theme.subtext,
    },
    methodValue: {
      fontSize: 16,
      fontWeight: "500",
      color: theme.text,
    },
    saveButton: {
      backgroundColor: theme.button,
      paddingVertical: 16,
      borderRadius: 8,
      alignItems: "center",
      marginTop: 28,
    },
    saveButtonDisabled: {
      opacity: 0.45,
    },
    saveButtonText: {
      color: theme.buttonText,
      fontSize: 18,
      fontWeight: "600",
    },
    deleteSection: {
      alignItems: "center",
      gap: 6,
      marginTop: 40,
    },
    deleteText: {
      color: DANGER,
      fontSize: 17,
      fontWeight: "600",
    },
  });