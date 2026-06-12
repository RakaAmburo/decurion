from pathlib import Path
from PIL import Image
import numpy as np

# Center based on dark pixels (the black eagle), fallback to alpha if needed.

ROOT = Path(r"C:/repos/decurion/app/src/main/res")
ICON_FILES = sorted(ROOT.glob("mipmap-*/ic_launcher_foreground.webp"))


def build_masks(arr: np.ndarray):
    rgb = arr[:, :, :3]
    alpha = arr[:, :, 3]
    alpha_mask = alpha > 0

    # "Black eagle" detection: dark pixels with visible alpha.
    dark_mask = (
        (rgb[:, :, 0] < 70)
        & (rgb[:, :, 1] < 70)
        & (rgb[:, :, 2] < 70)
        & alpha_mask
    )

    if dark_mask.any():
        return dark_mask, "dark"
    return alpha_mask, "alpha"


def bbox_center(mask: np.ndarray):
    ys, xs = np.where(mask)
    x0, x1 = xs.min(), xs.max()
    y0, y1 = ys.min(), ys.max()
    return (x0 + x1) / 2.0, (y0 + y1) / 2.0, (x0, y0, x1, y1)


def shift_rgba(arr: np.ndarray, dx: int, dy: int):
    h, w, _ = arr.shape
    out = np.zeros_like(arr)

    src_x0 = max(0, -dx)
    src_x1 = min(w, w - dx) if dx >= 0 else w
    dst_x0 = max(0, dx)
    dst_x1 = min(w, w + dx) if dx < 0 else w

    src_y0 = max(0, -dy)
    src_y1 = min(h, h - dy) if dy >= 0 else h
    dst_y0 = max(0, dy)
    dst_y1 = min(h, h + dy) if dy < 0 else h

    if src_x0 < src_x1 and src_y0 < src_y1 and dst_x0 < dst_x1 and dst_y0 < dst_y1:
        out[dst_y0:dst_y1, dst_x0:dst_x1] = arr[src_y0:src_y1, src_x0:src_x1]

    return out


def process(path: Path):
    img = Image.open(path).convert("RGBA")
    arr = np.array(img)
    h, w, _ = arr.shape

    mask, basis = build_masks(arr)
    cx, cy, before_bbox = bbox_center(mask)

    target_x = (w - 1) / 2.0
    target_y = (h - 1) / 2.0
    dx = int(round(target_x - cx))
    dy = int(round(target_y - cy))

    shifted = shift_rgba(arr, dx, dy)
    out_img = Image.fromarray(shifted, mode="RGBA")
    out_img.save(path, format="WEBP", lossless=True, quality=100)

    arr2 = np.array(Image.open(path).convert("RGBA"))
    mask2, basis2 = build_masks(arr2)
    cx2, cy2, after_bbox = bbox_center(mask2)

    return {
        "path": str(path),
        "basis_before": basis,
        "basis_after": basis2,
        "shift": (dx, dy),
        "before_center": (cx, cy),
        "after_center": (cx2, cy2),
        "target_center": (target_x, target_y),
        "before_bbox": before_bbox,
        "after_bbox": after_bbox,
    }


def main():
    if not ICON_FILES:
        print("No foreground icon files found.")
        return

    print(f"Found {len(ICON_FILES)} files")
    for p in ICON_FILES:
        result = process(p)
        print("---")
        print(result["path"])
        print(
            f"basis={result['basis_before']} shift(dx,dy)={result['shift']} "
            f"center {result['before_center']} -> {result['after_center']} "
            f"target={result['target_center']}"
        )


if __name__ == "__main__":
    main()

