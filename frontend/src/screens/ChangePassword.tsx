import React, { useEffect, useState } from "react";
import {
  ActivityIndicator,
  Alert,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { Feather } from "@expo/vector-icons";
import { useNavigation } from "@react-navigation/native";
import { useTheme, type Theme } from "../theme/ThemeProvider";
import { useAuth } from "../auth/AuthContext";
import {
  changePassword,
  getAccount,
  type SignInMethod,
} from "../services/account";

const MIN_LENGTH = 8;
const DANGER = "#D9534F";

const errorMessage = (error: unknown) =>
  error instanceof Error
    ? error.message
    : "Something went wrong. Please try again.";

type PasswordFieldProps = {
  label: string;
  value: string;
  onChangeText: (text: string) => void;
  placeholder: string;
  theme: Theme;
  styles: ReturnType<typeof createStyles>;
};

// Defined outside ChangePassword so the keyboard doesn't close while typing.
const PasswordField = ({
  label,
  value,
  onChangeText,
  placeholder,
  theme,
  styles,
}: PasswordFieldProps) => {
  const [visible, setVisible] = useState(false);

  return (
    <View style={styles.inputGroup}>
      <Text style={styles.label}>{label}</Text>
      <View style={styles.passwordContainer}>
        <TextInput
          style={styles.passwordInput}
          value={value}
          onChangeText={onChangeText}
          placeholder={placeholder}
          placeholderTextColor={theme.subtext}
          secureTextEntry={!visible}
          autoCapitalize="none"
          autoCorrect={false}
          accessibilityLabel={label}
        />
        <Pressable
          onPress={() => setVisible((current) => !current)}
          hitSlop={10}
          accessibilityRole="button"
          accessibilityLabel={visible ? "Hide password" : "Show password"}
        >
          <Feather
            name={visible ? "eye-off" : "eye"}
            size={22}
            color={theme.icon}
          />
        </Pressable>
      </View>
    </View>
  );
};

export default function ChangePassword() {
  const navigation = useNavigation();
  const { theme } = useTheme();
  const styles = createStyles(theme);
  const { token } = useAuth();

  const [signInMethod, setSignInMethod] = useState<SignInMethod | null>(null);
  const [loading, setLoading] = useState(true);

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) {
      // Not signed in: show the form anyway, as with a load failure.
      setSignInMethod("password");
      setLoading(false);
      return;
    }
    let active = true;

    getAccount(token)
      .then((account) => {
        if (active) setSignInMethod(account.signInMethod);
      })
      .catch(() => {
        // If the account can't load, show the form anyway.
        if (active) setSignInMethod("password");
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [token]);

  const longEnough = newPassword.length >= MIN_LENGTH;
  const matches = newPassword.length > 0 && newPassword === confirmPassword;
  const canSubmit =
    currentPassword.length > 0 && longEnough && matches && !saving;

  const handleSubmit = async () => {
    if (!canSubmit) return;

    setSaving(true);
    setError(null);
    try {
      await changePassword(currentPassword, newPassword);
      Alert.alert("Password updated", "Your password was changed.", [
        { text: "OK", onPress: () => navigation.goBack() },
      ]);
    } catch (submitError) {
      setError(errorMessage(submitError));
    } finally {
      setSaving(false);
    }
  };

  const Requirement = ({ met, text }: { met: boolean; text: string }) => (
    <View style={styles.requirement}>
      <Feather
        name={met ? "check-circle" : "circle"}
        size={20}
        color={met ? theme.button : theme.subtext}
      />
      <Text style={styles.requirementText}>{text}</Text>
    </View>
  );

  const usesPassword = signInMethod === "password";

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
        <Text style={styles.title}>Change Password</Text>
      </View>

      {loading && (
        <ActivityIndicator style={styles.loading} color={theme.icon} />
      )}

      {!loading && !usesPassword && (
        <Text style={styles.description}>
          Your account signs in with{" "}
          {signInMethod === "apple" ? "Apple" : "Google"}, so there is no
          password to change here.
        </Text>
      )}

      {!loading && usesPassword && (
        <>
          <Text style={styles.description}>
            Enter your current password, then choose a new one.
          </Text>

          <View style={styles.form}>
            <PasswordField
              label="Current Password"
              value={currentPassword}
              onChangeText={setCurrentPassword}
              placeholder="Enter your current password"
              theme={theme}
              styles={styles}
            />
            <PasswordField
              label="New Password"
              value={newPassword}
              onChangeText={setNewPassword}
              placeholder="Create a new password"
              theme={theme}
              styles={styles}
            />
            <PasswordField
              label="Confirm New Password"
              value={confirmPassword}
              onChangeText={setConfirmPassword}
              placeholder="Confirm your new password"
              theme={theme}
              styles={styles}
            />
          </View>

          <View style={styles.requirements}>
            <Requirement
              met={longEnough}
              text={`At least ${MIN_LENGTH} characters`}
            />
            <Requirement met={matches} text="New passwords match" />
          </View>

          {error && <Text style={styles.errorText}>{error}</Text>}

          <Pressable
            style={[
              styles.submitButton,
              !canSubmit && styles.submitButtonDisabled,
            ]}
            onPress={handleSubmit}
            disabled={!canSubmit}
            accessibilityRole="button"
            accessibilityState={{ disabled: !canSubmit }}
          >
            {saving ? (
              <ActivityIndicator color={theme.buttonText} />
            ) : (
              <Text style={styles.submitButtonText}>Update Password</Text>
            )}
          </Pressable>
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
      marginBottom: 20,
    },
    title: {
      fontSize: 26,
      fontWeight: "700",
      color: theme.text,
    },
    loading: {
      marginTop: 40,
    },
    description: {
      fontSize: 16,
      lineHeight: 23,
      color: theme.subtext,
      marginBottom: 24,
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
    passwordContainer: {
      flexDirection: "row",
      alignItems: "center",
      borderWidth: 1,
      borderColor: theme.inputBorder,
      borderRadius: 8,
      paddingHorizontal: 14,
    },
    passwordInput: {
      flex: 1,
      paddingVertical: 14,
      fontSize: 16,
      color: theme.text,
    },
    requirements: {
      gap: 10,
      marginTop: 22,
    },
    requirement: {
      flexDirection: "row",
      alignItems: "center",
      gap: 10,
    },
    requirementText: {
      fontSize: 15,
      color: theme.text,
    },
    errorText: {
      fontSize: 14,
      color: DANGER,
      marginTop: 16,
    },
    submitButton: {
      backgroundColor: theme.button,
      paddingVertical: 16,
      borderRadius: 8,
      alignItems: "center",
      marginTop: 28,
    },
    submitButtonDisabled: {
      opacity: 0.45,
    },
    submitButtonText: {
      color: theme.buttonText,
      fontSize: 18,
      fontWeight: "600",
    },
  });