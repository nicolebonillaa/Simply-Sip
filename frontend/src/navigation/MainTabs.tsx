import React, { useEffect, useState } from "react";
import { Animated, Platform, Pressable, StyleSheet, View } from "react-native";
import {
  createBottomTabNavigator,
  BottomTabBarProps,
} from "@react-navigation/bottom-tabs";
import { BlurView } from "expo-blur";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import HomeStack from "./HomeStack";
import FavoritesPage from "../screens/FavoritesPage";
import ProfilePage from "../screens/ProfilePage";
import { useTheme } from "../theme/ThemeProvider";

import HomeFilled from "../../assets/mocha icons/mocha-home-filled.svg";
import HomeOutline from "../../assets/mocha icons/mocha-home-outline.svg";
import FavoritesFilled from "../../assets/mocha icons/mocha-favorites-filled.svg";
import FavoritesOutline from "../../assets/mocha icons/mocha-favorites-outline.svg";
import ProfileFilled from "../../assets/mocha icons/mocha-profile-filled.svg";
import ProfileOutline from "../../assets/mocha icons/mocha-profile-outline.svg";
import PinkHomeFilled from "../../assets/pink icons/pink-home-filled.svg";
import PinkHomeOutline from "../../assets/pink icons/pink-home-outline.svg";
import PinkFavoritesFilled from "../../assets/pink icons/pink-favorites-filled.svg";
import PinkFavoritesOutline from "../../assets/pink icons/pink-favorites-outline.svg";
import PinkProfileFilled from "../../assets/pink icons/pink-profile-filled.svg";
import PinkProfileOutline from "../../assets/pink icons/pink-profile-outline.svg";
import SandHomeFilled from "../../assets/sand icons/sand-home-filled.svg";
import SandHomeOutline from "../../assets/sand icons/sand-home-outline.svg";
import SandFavoritesFilled from "../../assets/sand icons/sand-favorites-filled.svg";
import SandFavoritesOutline from "../../assets/sand icons/sand-favorites-outline.svg";
import SandProfileFilled from "../../assets/sand icons/sand-profile-filled.svg";
import SandProfileOutline from "../../assets/sand icons/sand-profile-outline.svg";

const Tab = createBottomTabNavigator();

const ICON_SIZE = 30; // use ~44 if you're still on the uncropped icon files
const BAR_HEIGHT = 68;
const PAD = 6;
const LENS_HEIGHT = 52;

const ICONS = {
  mocha: {
    Home: { filled: HomeFilled, outline: HomeOutline },
    Favorites: { filled: FavoritesFilled, outline: FavoritesOutline },
    Profile: { filled: ProfileFilled, outline: ProfileOutline },
  },
  pink: {
    Home: { filled: PinkHomeFilled, outline: PinkHomeOutline },
    Favorites: { filled: PinkFavoritesFilled, outline: PinkFavoritesOutline },
    Profile: { filled: PinkProfileFilled, outline: PinkProfileOutline },
  },
  sand: {
    Home: { filled: SandHomeFilled, outline: SandHomeOutline },
    Favorites: { filled: SandFavoritesFilled, outline: SandFavoritesOutline },
    Profile: { filled: SandProfileFilled, outline: SandProfileOutline },
  },
} as const;

function GlassTabBar({ state, navigation }: BottomTabBarProps) {
  const { theme, isDark, lightTabIcons, darkTabIcons } = useTheme();
  const iconSet = ICONS[isDark ? darkTabIcons : lightTabIcons];
  const insets = useSafeAreaInsets();
  const [barWidth, setBarWidth] = useState(0);
  const tabWidth = barWidth > 0 ? (barWidth - PAD * 2) / state.routes.length : 0;
  const [slide] = useState(() => new Animated.Value(0));

  useEffect(() => {
    Animated.spring(slide, {
      toValue: state.index * tabWidth,
      useNativeDriver: true,
      damping: 18,
      stiffness: 180,
      mass: 0.8,
    }).start();
  }, [state.index, tabWidth, slide]);

  return (
    <View
      style={[styles.shadowWrap, { bottom: Math.max(insets.bottom, 12) }]}
      onLayout={(e) => setBarWidth(e.nativeEvent.layout.width)}
    >
      <View style={[styles.clip, Platform.OS === "android" && {
        backgroundColor: isDark ? theme.chip : "rgba(255,255,255,0.9)",
      }]}>
        <BlurView
          intensity={80}
          tint={isDark ? "dark" : "light"}
          style={{ position: "absolute", top: 0, left: 0, right: 0, bottom: 0 }}
        />
        {tabWidth > 0 && (
          <Animated.View
            pointerEvents="none"
            style={[
              styles.lens,
              { backgroundColor: theme.chip, borderColor: theme.divider },
              { width: tabWidth - 8, transform: [{ translateX: slide }] },
            ]}
          />
        )}
      </View>

      <View style={styles.row}>
        {state.routes.map((route, index) => {
          const isFocused = state.index === index;
          const set = iconSet[route.name as keyof typeof iconSet];
          const Icon = isFocused ? set.filled : set.outline;

          const onPress = () => {
            const event = navigation.emit({
              type: "tabPress",
              target: route.key,
              canPreventDefault: true,
            });
            if (!isFocused && !event.defaultPrevented) {
              navigation.navigate(route.name, route.params);
            }
          };

          return (
            <Pressable
              key={route.key}
              onPress={onPress}
              accessibilityRole="button"
              accessibilityLabel={route.name}
              accessibilityState={isFocused ? { selected: true } : {}}
              style={styles.tab}
            >
              <Icon width={ICON_SIZE} height={ICON_SIZE} />
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

export default function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={{ headerShown: false }}
      tabBar={(props) => <GlassTabBar {...props} />}
    >
      <Tab.Screen name="Home" component={HomeStack} />
      <Tab.Screen name="Favorites" component={FavoritesPage} />
      <Tab.Screen name="Profile" component={ProfilePage} />
    </Tab.Navigator>
  );
}

const styles = StyleSheet.create({
  shadowWrap: {
    position: "absolute",
    left: 16,
    right: 16,
    height: BAR_HEIGHT,
    borderRadius: BAR_HEIGHT / 2,
    shadowColor: "#000",
    shadowOpacity: 0.12,
    shadowRadius: 16,
    shadowOffset: { width: 0, height: 4 },
    elevation: 6,
  },
  clip: {
    position: "absolute",
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    borderRadius: BAR_HEIGHT / 2,
    overflow: "hidden",
    // Android blur can look flat, so a soft frosted fill is added
    backgroundColor:
    Platform.OS === "android" ? "rgba(255,255,255,0.9)" : "transparent",
  },
  lens: {
    position: "absolute",
    left: PAD + 4,
    top: (BAR_HEIGHT - LENS_HEIGHT) / 2,
    height: LENS_HEIGHT,
    borderRadius: LENS_HEIGHT / 2,
    backgroundColor: "rgba(0,0,0,0.06)",
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.8)",
  },
  row: {
    flex: 1,
    flexDirection: "row",
    paddingHorizontal: PAD,
  },
  tab: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
  },
});
