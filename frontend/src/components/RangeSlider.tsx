import React, { useRef, useState } from "react";
import { LayoutChangeEvent, PanResponder, StyleSheet, View } from "react-native";

const THUMB = 28;
const HEIGHT = 52;

interface Props {
  min: number;
  max: number;
  step: number;
  values: number[];
  onChange: (values: number[]) => void;
  trackColor: string;
  fillColor: string;
  thumbColor: string;
  thumbBorderColor: string;
  labels?: string[]; // accessibility label per handle
}

interface Latest {
  values: number[];
  width: number;
  min: number;
  max: number;
  step: number;
  onChange: (values: number[]) => void;
}

const snap = (v: number, min: number, step: number) =>
  Number((Math.round((v - min) / step) * step + min).toFixed(4));

function setThumb(L: Latest, index: number, raw: number) {
  const lo = index === 1 ? L.values[0] : L.min;
  const hi = index === 0 && L.values.length > 1 ? L.values[1] : L.max;
  const v = Math.min(Math.max(snap(raw, L.min, L.step), lo), hi);
  if (v !== L.values[index]) {
    const next = [...L.values];
    next[index] = v;
    L.onChange(next);
  }
}

interface ThumbProps {
  index: number;
  latest: React.MutableRefObject<Latest>;
  left: number;
  zIndex: number;
  color: string;
  borderColor: string;
  label?: string;
}

function Thumb({ index, latest, left, zIndex, color, borderColor, label }: ThumbProps) {
  const startValue = useRef(0);

  const pan = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => true,
      onPanResponderTerminationRequest: () => false, // don't let the ScrollView steal the drag
      onPanResponderGrant: () => {
        startValue.current = latest.current.values[index];
      },
      onPanResponderMove: (_, gesture) => {
        const L = latest.current;
        const usable = L.width - THUMB;
        if (usable <= 0) return;
        setThumb(L, index, startValue.current + (gesture.dx / usable) * (L.max - L.min));
      },
    }),
  ).current;

  const L = latest.current;

  return (
    <View
      {...pan.panHandlers}
      accessible
      accessibilityRole="adjustable"
      accessibilityLabel={label}
      accessibilityValue={{ text: String(L.values[index]) }}
      accessibilityActions={[{ name: "increment" }, { name: "decrement" }]}
      onAccessibilityAction={(e) => {
        const cur = latest.current;
        const dir = e.nativeEvent.actionName === "increment" ? 1 : -1;
        setThumb(cur, index, cur.values[index] + dir * cur.step);
      }}
      hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
      style={[
        styles.thumb,
        { left, zIndex, backgroundColor: color, borderColor },
      ]}
    />
  );
}

export default function RangeSlider({
  min,
  max,
  step,
  values,
  onChange,
  trackColor,
  fillColor,
  thumbColor,
  thumbBorderColor,
  labels,
}: Props) {
  const [width, setWidth] = useState(0);

  const latest = useRef<Latest>({ values, width, min, max, step, onChange });
  latest.current = { values, width, min, max, step, onChange };

  const usable = Math.max(width - THUMB, 1);
  const offset = (v: number) => ((v - min) / (max - min)) * usable;

  const isRange = values.length > 1;
  const fillStart = isRange ? offset(values[0]) + THUMB / 2 : THUMB / 2;
  const fillEnd = offset(values[values.length - 1]) + THUMB / 2;

  const onLayout = (e: LayoutChangeEvent) => setWidth(e.nativeEvent.layout.width);

  return (
    <View style={styles.container} onLayout={onLayout}>
      <View
        style={[
          styles.track,
          { backgroundColor: trackColor, left: THUMB / 2, right: THUMB / 2 },
        ]}
      />
      <View
        style={[
          styles.fill,
          { backgroundColor: fillColor, left: fillStart, width: Math.max(fillEnd - fillStart, 0) },
        ]}
      />

      {width > 0 &&
        values.map((value, index) => {
          // If both handles overlap near the right end, keep the left one on top so it can still be dragged back.
          const lowOnTop = isRange && index === 0 && values[0] > min + (max - min) / 2;
          return (
            <Thumb
              key={index}
              index={index}
              latest={latest}
              left={offset(value)}
              zIndex={lowOnTop ? 3 : 2}
              color={thumbColor}
              borderColor={thumbBorderColor}
              label={labels?.[index]}
            />
          );
        })}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    height: HEIGHT,
    justifyContent: "center",
  },
  track: {
    position: "absolute",
    height: 6,
    borderRadius: 3,
    top: (HEIGHT - 6) / 2,
  },
  fill: {
    position: "absolute",
    height: 6,
    borderRadius: 3,
    top: (HEIGHT - 6) / 2,
  },
  thumb: {
    position: "absolute",
    top: (HEIGHT - THUMB) / 2,
    width: THUMB,
    height: THUMB,
    borderRadius: THUMB / 2,
    borderWidth: 4,
    shadowColor: "#000",
    shadowOpacity: 0.25,
    shadowRadius: 4,
    shadowOffset: { width: 0, height: 2 },
    elevation: 3,
  },
});