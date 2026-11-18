import React, { useMemo, useRef, useState } from "react";
import {
  ActivityIndicator,
  Image,
  Modal,
  PanResponder,
  Pressable,
  StyleSheet,
  Text,
  useWindowDimensions,
  View,
  type GestureResponderEvent,
  type LayoutChangeEvent,
} from "react-native";
import Svg, { Circle, Path } from "react-native-svg";
import { ImageManipulator, SaveFormat } from "expo-image-manipulator";
import {
  clampTransform,
  coverScale,
  cropRectFor,
  type Transform,
} from "../utils/circleCrop";

type Props = {
  uri: string;
  imageWidth: number;
  imageHeight: number;
  /** Called with the cropped, square image file. */
  onDone: (croppedUri: string) => void | Promise<void>;
  onCancel: () => void;
};

const OUTPUT_SIZE = 512; // saved photo is OUTPUT_SIZE x OUTPUT_SIZE

type GestureStart = {
  touches: number;
  transform: Transform;
  distance: number;
  midX: number;
  midY: number;
};

const readTouches = (event: GestureResponderEvent) => {
  const touches = event.nativeEvent.touches;
  const count = touches.length;
  const midX = touches.reduce((sum, t) => sum + t.pageX, 0) / count;
  const midY = touches.reduce((sum, t) => sum + t.pageY, 0) / count;
  const distance =
    count >= 2
      ? Math.hypot(
          touches[0].pageX - touches[1].pageX,
          touches[0].pageY - touches[1].pageY,
        )
      : 0;
  return { count, midX, midY, distance };
};

/**
 * Full-screen "Move and Scale" picker with a circular mask.
 * Drag to move the photo, pinch to zoom, then tap Choose.
 */
export default function CircleCropModal({
  uri,
  imageWidth,
  imageHeight,
  onDone,
  onCancel,
}: Props) {
  const { width: screenWidth } = useWindowDimensions();
  const diameter = Math.min(screenWidth - 48, 340);

  const [stage, setStage] = useState({ width: 0, height: 0 });
  const [saving, setSaving] = useState(false);

  const initial: Transform = {
    scale: coverScale(imageWidth, imageHeight, diameter),
    tx: 0,
    ty: 0,
  };
  const [transform, setTransformState] = useState<Transform>(initial);
  const transformRef = useRef<Transform>(initial);
  const gestureStart = useRef<GestureStart | null>(null);

  const setTransform = (next: Transform) => {
    const clamped = clampTransform(next, imageWidth, imageHeight, diameter);
    transformRef.current = clamped;
    setTransformState(clamped);
  };

  const panResponder = useMemo(
    () =>
      PanResponder.create({
        onStartShouldSetPanResponder: () => true,
        onMoveShouldSetPanResponder: () => true,
        onPanResponderGrant: () => {
          gestureStart.current = null;
        },
        onPanResponderMove: (event) => {
          const { count, midX, midY, distance } = readTouches(event);

          // A finger was added or lifted: restart the gesture from here.
          if (!gestureStart.current || gestureStart.current.touches !== count) {
            gestureStart.current = {
              touches: count,
              transform: transformRef.current,
              distance,
              midX,
              midY,
            };
            return;
          }

          const start = gestureStart.current;
          const zoom =
            count >= 2 && start.distance > 0 ? distance / start.distance : 1;

          setTransform({
            scale: start.transform.scale * zoom,
            tx: start.transform.tx + (midX - start.midX),
            ty: start.transform.ty + (midY - start.midY),
          });
        },
        onPanResponderRelease: () => {
          gestureStart.current = null;
        },
        onPanResponderTerminate: () => {
          gestureStart.current = null;
        },
      }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [imageWidth, imageHeight, diameter],
  );

  const handleChoose = async () => {
    if (saving) return;
    setSaving(true);
    try {
      const rect = cropRectFor(
        transformRef.current,
        imageWidth,
        imageHeight,
        diameter,
      );
      const context = ImageManipulator.manipulate(uri);
      context.crop(rect);
      context.resize({ width: OUTPUT_SIZE, height: OUTPUT_SIZE });
      const rendered = await context.renderAsync();
      const saved = await rendered.saveAsync({
        format: SaveFormat.JPEG,
        compress: 0.85,
      });
      await onDone(saved.uri);
    } finally {
      setSaving(false);
    }
  };

  const onStageLayout = (event: LayoutChangeEvent) => {
    const { width, height } = event.nativeEvent.layout;
    setStage({ width, height });
  };

  const cx = stage.width / 2;
  const cy = stage.height / 2;
  const r = diameter / 2;
  // Dim everything except the circle (even-odd fill leaves the circle clear).
  const maskPath =
    `M0 0H${stage.width}V${stage.height}H0Z ` +
    `M${cx - r} ${cy}a${r} ${r} 0 1 0 ${diameter} 0a${r} ${r} 0 1 0 ${-diameter} 0Z`;

  return (
    <Modal
      visible
      animationType="slide"
      presentationStyle="fullScreen"
      statusBarTranslucent
      onRequestClose={onCancel}
    >
      <View style={styles.screen}>
        <Text style={styles.title}>Move and Scale</Text>

        <View
          style={styles.stage}
          onLayout={onStageLayout}
          {...panResponder.panHandlers}
        >
          <Image
            source={{ uri }}
            resizeMode="stretch"
            style={{
              width: imageWidth * transform.scale,
              height: imageHeight * transform.scale,
              transform: [{ translateX: transform.tx }, { translateY: transform.ty }],
            }}
            accessibilityLabel="Photo to crop"
          />

          {stage.width > 0 && (
            <Svg
              style={StyleSheet.absoluteFill}
              width={stage.width}
              height={stage.height}
              pointerEvents="none"
            >
              <Path d={maskPath} fill="rgba(0,0,0,0.65)" fillRule="evenodd" />
              <Circle
                cx={cx}
                cy={cy}
                r={r}
                stroke="rgba(255,255,255,0.9)"
                strokeWidth={1.5}
                fill="none"
              />
            </Svg>
          )}
        </View>

        <View style={styles.footer}>
          <Pressable
            onPress={onCancel}
            disabled={saving}
            hitSlop={12}
            accessibilityRole="button"
            accessibilityLabel="Cancel"
          >
            <Text style={styles.footerText}>Cancel</Text>
          </Pressable>

          <Pressable
            onPress={handleChoose}
            disabled={saving}
            hitSlop={12}
            accessibilityRole="button"
            accessibilityLabel="Choose photo"
          >
            {saving ? (
              <ActivityIndicator color="#fff" />
            ) : (
              <Text style={[styles.footerText, styles.chooseText]}>Choose</Text>
            )}
          </Pressable>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: "#000",
  },
  title: {
    color: "#fff",
    fontSize: 17,
    fontWeight: "600",
    textAlign: "center",
    paddingTop: 64,
    paddingBottom: 16,
  },
  stage: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    overflow: "hidden",
  },
  footer: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: 24,
    paddingTop: 18,
    paddingBottom: 40,
    backgroundColor: "#111",
  },
  footerText: {
    color: "#fff",
    fontSize: 18,
  },
  chooseText: {
    fontWeight: "600",
  },
});