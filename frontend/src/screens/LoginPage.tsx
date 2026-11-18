import React, { useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  Pressable,
  ScrollView,
  TextInput,
  ActivityIndicator,
  Alert,
} from "react-native";
import { Feather, FontAwesome5 } from "@expo/vector-icons";
import { useTheme, type Theme } from "../theme/ThemeProvider";
import { useNavigation } from "@react-navigation/native";
import GoogleLogo from "../components/GoogleLogo";
import { useAuth } from "../auth/AuthContext";

const LoginPage = () => {
  const navigation = useNavigation();

  const { theme } = useTheme();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const styles = createStyles(theme);

  const { login } = useAuth();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{
    email?: string;
    password?: string;
  }>({});

  const handleLogin = async () => {
    if (submitting) return;

    const errors: { email?: string; password?: string } = {};
    if (!/\S+@\S+\.\S+/.test(email.trim()))
      errors.email = "Enter a valid email address.";
    if (!password) errors.password = "Enter your password.";
    setFieldErrors(errors);
    setError(null);
    if (Object.keys(errors).length) return;

    setSubmitting(true);
    try {
      await login(email, password);
      Alert.alert("Logged in", email); // TEMPORARY, remove later
      navigation.goBack();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      keyboardShouldPersistTaps="handled"
    >
      {/* Back button */}
      <Pressable
        onPress={() => navigation.goBack()}
        style={styles.backButton}
        hitSlop={12}
      >
        <Feather name="arrow-left" size={24} color={theme.icon} />
      </Pressable>

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.title}>Welcome Back</Text>
        <Text style={styles.subtitle}>Log in to your Simply Sip account.</Text>
      </View>

      {/* Login form */}
      <View style={styles.form}>
        <View style={styles.inputGroup}>
          <Text style={styles.label}>Email</Text>
          <TextInput
            style={styles.input}
            placeholder="Enter your email"
            placeholderTextColor={theme.subtext}
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
            autoCorrect={false}
          />
          {fieldErrors.email && (
            <Text style={styles.errorText}>{fieldErrors.email}</Text>
          )}
        </View>

        <View style={styles.inputGroup}>
          <View style={styles.passwordHeader}>
            <Text style={styles.label}>Password</Text>
            <Pressable onPress={() => console.log("Forgot password pressed")}>
              <Text style={styles.forgotPassword}>Forgot Password?</Text>
            </Pressable>
          </View>

          <View style={styles.passwordContainer}>
            <TextInput
              style={styles.passwordInput}
              placeholder="Enter your password"
              placeholderTextColor={theme.subtext}
              value={password}
              onChangeText={setPassword}
              secureTextEntry={!showPassword}
              autoCapitalize="none"
              autoCorrect={false}
            />
            <Pressable
              onPress={() => setShowPassword((current) => !current)}
              hitSlop={10}
            >
              <Feather
                name={showPassword ? "eye-off" : "eye"}
                size={20}
                color={theme.icon}
              />
            </Pressable>
          </View>
          {fieldErrors.password && (
            <Text style={styles.errorText}>{fieldErrors.password}</Text>
          )}
        </View>
        {error && <Text style={styles.errorText}>{error}</Text>}
        <Pressable
          style={[styles.loginButton, submitting && { opacity: 0.6 }]}
          onPress={handleLogin}
          disabled={submitting}
        >
          {submitting ? (
            <ActivityIndicator color={theme.buttonText} />
          ) : (
            <Text style={styles.loginButtonText}>Log In</Text>
          )}
        </Pressable>
      </View>

      {/* Divider */}
      <View style={styles.dividerContainer}>
        <View style={styles.divider} />
        <Text style={styles.dividerText}>OR</Text>
        <View style={styles.divider} />
      </View>

      {/* Social login buttons */}
      <View style={styles.socialButtons}>
        <Pressable
          style={styles.socialButton}
          onPress={() => console.log("Google pressed")}
        >
          <GoogleLogo />
          <Text style={styles.socialButtonText}>Continue with Google</Text>
        </Pressable>

        <Pressable
          style={styles.socialButton}
          onPress={() => console.log("Apple pressed")}
        >
          <FontAwesome5 name="apple" size={22} color={theme.text} />
          <Text style={styles.socialButtonText}>Continue with Apple</Text>
        </Pressable>
      </View>

      {/* Sign up link */}
      <View style={styles.signupContainer}>
        <Text style={styles.signupText}>Don't have an account? </Text>
        <Pressable onPress={() => navigation.navigate("Register" as never)}>
          <Text style={styles.signupLink}>Sign Up</Text>
        </Pressable>
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
    backButton: {
      alignSelf: "flex-start",
      marginBottom: 28,
    },
    header: {
      marginBottom: 32,
    },
    title: {
      color: theme.text,
      fontSize: 30,
      fontWeight: "700",
      marginBottom: 8,
    },
    subtitle: {
      color: theme.subtext,
      fontSize: 16,
    },
    form: {
      gap: 22,
    },
    inputGroup: {
      gap: 8,
    },
    label: {
      color: theme.text,
      fontSize: 16,
      fontWeight: "600",
    },
    input: {
      borderWidth: 1,
      borderColor: theme.inputBorder,
      borderRadius: 8,
      padding: 14,
      fontSize: 16,
      color: theme.text,
    },
    passwordHeader: {
      flexDirection: "row",
      justifyContent: "space-between",
      alignItems: "center",
    },
    forgotPassword: {
      color: theme.button,
      fontSize: 14,
      fontWeight: "500",
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
    loginButton: {
      backgroundColor: theme.button,
      paddingVertical: 16,
      alignItems: "center",
      borderRadius: 8,
      marginTop: 4,
    },
    loginButtonText: {
      color: theme.buttonText,
      fontSize: 18,
      fontWeight: "600",
    },
    dividerContainer: {
      flexDirection: "row",
      alignItems: "center",
      gap: 12,
      marginVertical: 28,
    },
    divider: {
      flex: 1,
      height: 1,
      backgroundColor: theme.divider,
    },
    dividerText: {
      color: theme.subtext,
      fontSize: 13,
    },
    socialButtons: {
      gap: 12,
    },
    socialButton: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "center",
      gap: 12,
      paddingVertical: 14,
      borderWidth: 1,
      borderColor: theme.inputBorder,
      borderRadius: 8,
      backgroundColor: theme.background,
    },
    socialButtonText: {
      color: theme.text,
      fontSize: 16,
      fontWeight: "500",
    },
    signupContainer: {
      flexDirection: "row",
      justifyContent: "center",
      marginTop: 28,
      marginBottom: 20,
    },
    signupText: {
      color: theme.subtext,
      fontSize: 15,
    },
    signupLink: {
      color: theme.button,
      fontSize: 15,
      fontWeight: "600",
    },
    errorText: { color: "#D32F2F", fontSize: 14 },
  });

export default LoginPage;
