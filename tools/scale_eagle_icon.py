from pathlib import Path
from PIL import Image

SCALE = 1.10  # aumento leve
ROOT = Path(r"C:/repos/decurion/app/src/main/res")
FILES = sorted(ROOT.glob("mipmap-*/ic_launcher_foreground.webp"))


def scale_centered(img: Image.Image, scale: float) -> Image.Image:
    img = img.convert("RGBA")
    w, h = img.size
    nw = max(1, int(round(w * scale)))
    nh = max(1, int(round(h * scale)))

    scaled = img.resize((nw, nh), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (w, h), (0, 0, 0, 0))

    # Keep centered after scaling.
    left = (w - nw) // 2
    top = (h - nh) // 2
    canvas.alpha_composite(scaled, (left, top))
    return canvas


def main():
    if not FILES:
        print("No icon files found")
        return

    print(f"Scaling {len(FILES)} files by {SCALE:.2f}x")
    for p in FILES:
        im = Image.open(p)
        out = scale_centered(im, SCALE)
        out.save(p, format="WEBP", lossless=True, quality=100)
        print(f"Updated: {p}")


if __name__ == "__main__":
    main()

