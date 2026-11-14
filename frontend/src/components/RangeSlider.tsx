import React, { useRef, useState } from "react";
import {
  LayoutChangeEvent,
  StyleSheet,
  View,
} from "react-native";

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
  labels?: string[];
}

interface SliderSettings {
  values: number[];
  width: number;
  min: number;
  max: number;
  step: number;
  onChange: (values: number[]) => void;
}

const snap = (
  value: number,
  min: number,
  step: number,
) =>
  Number(
    (
      Math.round((value - min) / step) * step +
      min
    ).toFixed(4),
  );

function setThumb(
  settings: SliderSettings,
  index: number,
  raw: number,
) {
  const lower =
    index === 1
      ? settings.values[0]
      : settings.min;

  const upper =
    index === 0 && settings.values.length > 1
      ? settings.values[1]
      : settings.max;

  const value = Math.min(
    Math.max(
      snap(raw, settings.min, settings.step),
      lower,
    ),
    upper,
  );

  if (value !== settings.values[index]) {
    const next = [...settings.values];
    next[index] = value;
    settings.onChange(next);
  }
}

interface ThumbProps {
  index: number;
  value: number;
  settings: SliderSettings;
  left: number;
  zIndex: number;
  color: string;
  borderColor: string;
  label?: string;
}

function Thumb({
  index,
  value,
  settings,
  left,
  zIndex,
  color,
  borderColor,
  label,
}: ThumbProps) {
  const startValue = useRef(0);
  const startX = useRef(0);

  return (
    <View
      onStartShouldSetResponder={() => true}
      onMoveShouldSetResponder={() => true}
      onResponderTerminationRequest={() => false}
      onResponderGrant={(event) => {
        startValue.current = value;
        startX.current = event.nativeEvent.pageX;
      }}
      onResponderMove={(event) => {
        const usable = settings.width - THUMB;

        if (usable <= 0) return;

        const delta =
          event.nativeEvent.pageX - startX.current;

        const next =
          startValue.current +
          (delta / usable) *
            (settings.max - settings.min);

        setThumb(settings, index, next);
      }}
      accessible
      accessibilityRole="adjustable"
      accessibilityLabel={label}
      accessibilityValue={{
        text: String(value),
      }}
      accessibilityActions={[
        { name: "increment" },
        { name: "decrement" },
      ]}
      onAccessibilityAction={(event) => {
        const direction =
          event.nativeEvent.actionName === "increment"
            ? 1
            : -1;

        setThumb(
          settings,
          index,
          settings.values[index] +
            direction * settings.step,
        );
      }}
      hitSlop={{
        top: 12,
        bottom: 12,
        left: 12,
        right: 12,
      }}
      style={[
        styles.thumb,
        {
          left,
          zIndex,
          backgroundColor: color,
          borderColor,
        },
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

  const usable = Math.max(width - THUMB, 1);

  const offset = (value: number) =>
    ((value - min) / (max - min)) * usable;

  const isRange = values.length > 1;

  const fillStart = isRange
    ? offset(values[0]) + THUMB / 2
    : THUMB / 2;

  const fillEnd =
    offset(values[values.length - 1]) + THUMB / 2;

  const onLayout = (event: LayoutChangeEvent) => {
    setWidth(event.nativeEvent.layout.width);
  };

  return (
    <View
      testID="range-slider"
      style={styles.container}
      onLayout={onLayout}
    >
      <View
        style={[
          styles.track,
          {
            backgroundColor: trackColor,
            left: THUMB / 2,
            right: THUMB / 2,
          },
        ]}
      />

      <View
        style={[
          styles.fill,
          {
            backgroundColor: fillColor,
            left: fillStart,
            width: Math.max(fillEnd - fillStart, 0),
          },
        ]}
      />

      {width > 0 &&
        values.map((value, index) => {
          const lowOnTop =
            isRange &&
            index === 0 &&
            values[0] > min + (max - min) / 2;

          return (
            <Thumb
              key={index}
              index={index}
              value={value}
              settings={{
                values,
                width,
                min,
                max,
                step,
                onChange,
              }}
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
    shadowOffset: {
      width: 0,
      height: 2,
    },
    elevation: 3,
  },
});