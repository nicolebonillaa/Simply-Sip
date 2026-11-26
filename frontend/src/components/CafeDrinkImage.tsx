import React, { useState } from "react";
import { Image, StyleSheet, View, type StyleProp, type ViewStyle } from "react-native";
import {Ionicons} from "@expo/vector-icons";
import {useTheme} from "../theme/ThemeProvider";

type Props = {
  uri?: string | null;
  style: StyleProp<ViewStyle>;
  label: string;
  fallback?: React.ReactNode;
  iconSize?: number;
  children?: React.ReactNode;
};

export default function CafeDrinkImage({ uri, style, label, fallback, iconSize=28, children }: Props) {
  const {theme} = useTheme();
  const [failedUri, setFailedUri] = useState<string | null>(null);
  const showImage = !!uri && failedUri !== uri;

  return (
    <View style={[style, styles.clip]} accessibilityLabel={showImage ? label : `${label} unavailable`}>
      {showImage ? (
        <Image
          source={{ uri }}
          style={styles.image}
          resizeMode="cover"
          onError={() => setFailedUri(uri ?? null)}
        />
      ) : fallback ? (
        fallback
      ) : (
        <View style={[styles.defaultPlaceholder, {backgroundColor: theme.card}]}>
          <Ionicons name="cafe-outline" size={iconSize} color={theme.subtext} />
          </View>
      )}  
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  clip: { overflow: "hidden" },
  image: { position: "absolute", top: 0, right: 0, bottom: 0, left: 0 },
  defaultPlaceholder:
  {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
});