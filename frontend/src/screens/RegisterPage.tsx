import React, { useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  Pressable,
  ScrollView,
  TextInput,
} from "react-native";
import { Feather, FontAwesome5 } from "@expo/vector-icons";
import { useTheme, type Theme } from "../theme/ThemeProvider";
import { useNavigation } from "@react-navigation/native";
import GoogleLogo from "../components/GoogleLogo";

const RegisterPage = () => {
  const navigation = useNavigation();

  const { theme } = useTheme();
  const styles = createStyles(theme);

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      keyboardShouldPersistTaps="handled"
    >
      <Pressable onPress={() => navigation.goBack()}>
        <Feather name="arrow-left" size={24} color={theme.icon} />
      </Pressable>

      <View style={styles.formContent}>
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.title}>Create Account</Text>
          <Text style={styles.subtitle}>
            Sign up to save your favorite drinks!
          </Text>
        </View>
        {/* Registration Form */}
        <View style={styles.form}>
          <View style={styles.inputGroup}>
            <Text style={styles.label}>Full Name</Text>
            <TextInput
              style={styles.input}
              placeholder="Enter your full name"
              placeholderTextColor={theme.subtext}
              value={fullName}
              onChangeText={setFullName}
              autoCapitalize="words"
            />
          </View>
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
          </View>
          <View style={styles.inputGroup}>
            <Text style={styles.label}>Password</Text>
            <View style={styles.passwordContainer}>
              <TextInput
                style={styles.passwordInput}
                placeholder="Create a password"
                placeholderTextColor={theme.subtext}
                value={password}
                onChangeText={setPassword}
                secureTextEntry={!showPassword}
                autoCapitalize="none"
              />
              <Pressable
                onPress={() => setShowPassword(!showPassword)}
                hitSlop={10}
              >
                <Feather
                  name={showPassword ? "eye-off" : "eye"}
                  size={20}
                  color={theme.icon}
                />
              </Pressable>
            </View>
          </View>
          <View style={styles.inputGroup}>
            <Text style={styles.label}>Confirm Password</Text>
            <View style={styles.passwordContainer}>
              <TextInput
                style={styles.passwordInput}
                placeholder="Confirm your password"
                placeholderTextColor={theme.subtext}
                value={confirmPassword}
                onChangeText={setConfirmPassword}
                secureTextEntry={!showConfirmPassword}
                autoCapitalize="none"
              />
              <Pressable
                onPress={() => setShowConfirmPassword(!showConfirmPassword)}
                hitSlop={10}
              >
                <Feather
                  name={showConfirmPassword ? "eye-off" : "eye"}
                  size={20}
                  color={theme.icon}
                />
              </Pressable>
            </View>
          </View>
          {/* Create Account Button */}
          <Pressable
            style={styles.createAccountButton}
            onPress={() => console.log("Create Account pressed")}
          >
            <Text style={styles.createAccountText}>Create Account</Text>
          </Pressable>
        </View>
        {/* Divider */}
        <View style={styles.dividerContainer}>
          <View style={styles.divider} />
          <Text style={styles.orText}>OR</Text>
          <View style={styles.divider} />
        </View>
        {/* Social Registration */}
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
            <FontAwesome5 name="apple" size={22} color="black" />
            <Text style={styles.socialButtonText}>Continue with Apple</Text>
          </Pressable>
        </View>
        {/* Login Link */}
        <View style={styles.loginContainer}>
          <Text style={styles.loginText}>Already have an account?</Text>
          <Pressable onPress={() => navigation.navigate("Login" as never)}>
            <Text style={styles.loginLink}>Log In</Text>
          </Pressable>
        </View>
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
      paddingBottom: 100,
    },
    formContent: {
      marginTop: 35,
    },
    header: {
      alignItems: "flex-start",
      marginBottom: 32,
      gap: 8,
    },
    title: {
      fontSize: 28,
      fontWeight: "700",
      color: theme.text,
    },
    subtitle: {
      fontSize: 16,
      color: theme.subtext,
      textAlign: "center",
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
    createAccountButton: {
      backgroundColor: theme.button,
      paddingVertical: 16,
      borderRadius: 8,
      alignItems: "center",
      marginTop: 8,
    },
    createAccountText: {
      color: theme.buttonText,
      fontSize: 18,
      fontWeight: "600",
    },
    dividerContainer: {
      flexDirection: "row",
      alignItems: "center",
      gap: 12,
      marginVertical: 24,
    },
    divider: {
      flex: 1,
      height: 1,
      backgroundColor: theme.divider,
    },
    orText: {
      color: theme.subtext,
      fontSize: 14,
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
    },
    socialButtonText: {
      color: theme.text,
      fontSize: 16,
      fontWeight: "500",
    },
    loginContainer: {
      flexDirection: "row",
      justifyContent: "center",
      alignItems: "center",
      gap: 5,
      marginTop: 28,
    },
    loginText: {
      color: theme.subtext,
      fontSize: 15,
    },
    loginLink: {
      color: theme.button,
      fontSize: 15,
      fontWeight: "700",
    },
  });

export default RegisterPage;
