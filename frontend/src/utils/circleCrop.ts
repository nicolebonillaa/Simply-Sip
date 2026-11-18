// Pure math for the circular photo cropper (no React, easy to test).
//
// Coordinates: the crop circle has diameter `diameter` (screen px). The image is
// drawn at `scale` (screen px per source px), centered on the circle, then moved
// by (tx, ty). The image must always fully cover the circle.

export type Transform = { scale: number; tx: number; ty: number };

export type CropRect = {
  originX: number;
  originY: number;
  width: number;
  height: number;
};

export const MAX_ZOOM = 4; // times the "just covers the circle" scale

export const coverScale = (
  imageWidth: number,
  imageHeight: number,
  diameter: number,
) => diameter / Math.min(imageWidth, imageHeight);

const clamp = (value: number, min: number, max: number) =>
  Math.min(max, Math.max(min, value));

/** Keeps the zoom in range and the image covering the whole circle. */
export function clampTransform(
  next: Transform,
  imageWidth: number,
  imageHeight: number,
  diameter: number,
): Transform {
  const min = coverScale(imageWidth, imageHeight, diameter);
  const scale = clamp(next.scale, min, min * MAX_ZOOM);
  const maxX = Math.max(0, (imageWidth * scale - diameter) / 2);
  const maxY = Math.max(0, (imageHeight * scale - diameter) / 2);
  return {
    scale,
    tx: clamp(next.tx, -maxX, maxX),
    ty: clamp(next.ty, -maxY, maxY),
  };
}

/** The square (in source-image pixels) that sits inside the circle. */
export function cropRectFor(
  transform: Transform,
  imageWidth: number,
  imageHeight: number,
  diameter: number,
): CropRect {
  const { scale, tx, ty } = transform;
  const left = (imageWidth * scale - diameter) / 2 - tx;
  const top = (imageHeight * scale - diameter) / 2 - ty;
  const size = diameter / scale;

  const width = Math.min(Math.round(size), imageWidth, imageHeight);
  return {
    originX: clamp(Math.round(left / scale), 0, imageWidth - width),
    originY: clamp(Math.round(top / scale), 0, imageHeight - width),
    width,
    height: width,
  };
}