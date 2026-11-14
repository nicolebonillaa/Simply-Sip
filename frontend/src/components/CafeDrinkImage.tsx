import React, { useState } from "react";
import { Image, StyleSheet, View, type StyleProp, type ViewStyle } from "react-native";

type Props = {
  uri?: string | null;
  style: StyleProp<ViewStyle>;
  label: string;
  fallback?: React.ReactNode;
  children?: React.ReactNode;
};

export default function CafeDrinkImage({ uri, style, label, fallback, children }: Props) {
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
      ) : fallback}
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  clip: { overflow: "hidden" },
  image: { position: "absolute", top: 0, right: 0, bottom: 0, left: 0 },
});