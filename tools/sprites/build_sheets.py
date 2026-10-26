#!/usr/bin/env python3
"""
Convierte carpetas/zips de frames (frame_001.png, ...) en hojas de sprites para el reloj
y genera el catálogo Kotlin SpriteAnimations.kt.

Uso (desde la raíz del proyecto):
  pip install pillow numpy scipy
  python tools/sprites/build_sheets.py --src "C:/ruta/a/los/zips"

Cada animación se recorta, se escala para que mida lo mismo que los sprites estáticos
(se compara la altura de los audífonos naranjas) y se alinea por las botas.
"""
import argparse, io, json, math, zipfile
from pathlib import Path

import numpy as np
from PIL import Image
from scipy import ndimage as ndi

STAGE_W, STAGE_H = 390, 629          # tamaño de los sprites estáticos
MAX_COLUMNS = 8


def orange_mask(rgba: np.ndarray) -> np.ndarray:
    f = rgba.astype(float) / 255
    r, g, b, a = f[..., 0], f[..., 1], f[..., 2], f[..., 3]
    mx, mn = f[..., :3].max(-1), f[..., :3].min(-1)
    delta = np.maximum(mx - mn, 1e-6)
    hue = np.where(mx == r, ((g - b) / delta) % 6, 9) * 60
    sat = (mx - mn) / np.maximum(mx, 1e-6)
    return (a > 0.5) & (hue > 10) & (hue < 42) & (sat > 0.5) & (mx > 0.6)


def measure(img: Image.Image):
    """(altura de audífono, fila de las botas) o None si no se detecta."""
    rgba = np.asarray(img.convert("RGBA"))
    bbox = img.convert("RGBA").split()[3].getbbox()
    if not bbox:
        return None
    lab, n = ndi.label(orange_mask(rgba))
    top, height = bbox[1], bbox[3] - bbox[1]
    ears, boots = [], []
    for k in range(1, n + 1):
        ys, _ = np.where(lab == k)
        if len(ys) < 300:
            continue
        cy = ys.mean()
        if cy < top + 0.55 * height:
            ears.append((len(ys), ys.max() - ys.min() + 1))
        elif cy > top + 0.65 * height:
            boots.append(ys.max())
    if not ears or not boots:
        return None
    return max(ears)[1], max(boots)


def load_frames(src: Path, zip_name: str):
    matches = sorted(src.glob(f"*{zip_name}"))
    if not matches:
        raise SystemExit(f"No encuentro {zip_name} en {src}")
    with zipfile.ZipFile(matches[0]) as z:
        names = sorted(n for n in z.namelist() if n.lower().endswith(".png"))
        return [Image.open(io.BytesIO(z.read(n))).convert("RGBA") for n in names]


def frame_numbers(spec):
    if not spec:
        return []
    return list(range(spec["from"], spec["to"] + 1, spec.get("step", 1)))


def build(cfg, root: Path, src: Path, res_dir: Path, kt_path: Path, preview_dir: Path):
    ref = Image.open(root / cfg["referenceSprite"]).convert("RGBA")
    ref_ear, ref_ground = measure(ref)
    print(f"referencia: audífono {ref_ear}px, botas en y={ref_ground}")

    entries, previews = {}, {}
    for anim in cfg["animations"]:
        frames = load_frames(src, anim["zip"])
        intro = frame_numbers(anim.get("intro"))
        loop = frame_numbers(anim["loop"])
        chosen = intro + loop
        imgs = [frames[n - 1] for n in chosen]
        canvas_w = imgs[0].width

        # recorte común a todos los cuadros de esta animación
        boxes = [i.split()[3].getbbox() for i in imgs]
        x0 = min(b[0] for b in boxes); y0 = min(b[1] for b in boxes)
        x1 = max(b[2] for b in boxes); y1 = max(b[3] for b in boxes)
        cw, ch = x1 - x0, y1 - y0

        # Escala y suelo: se miden en un cuadro neutro (measureFrame) si se indica, porque efectos
        # como el hielo tapan los audífonos; si no, se toma la mediana de los cuadros elegidos.
        sample = [frames[anim["measureFrame"] - 1]] if "measureFrame" in anim else imgs
        ears, grounds = [], []
        for i in sample:
            m = measure(i)
            if m:
                ears.append(m[0]); grounds.append(m[1])
        ear, ground = float(np.median(ears)), float(np.median(grounds))
        unit = ref_ear / ear * anim.get("scaleAdjust", 1.0)

        cols = min(MAX_COLUMNS, len(imgs))
        rows = math.ceil(len(imgs) / cols)
        sheet = Image.new("RGBA", (cols * cw, rows * ch), (0, 0, 0, 0))
        for idx, i in enumerate(imgs):
            sheet.paste(i.crop((x0, y0, x1, y1)), ((idx % cols) * cw, (idx // cols) * ch))
        out = res_dir / f"anim_{anim['name']}.png"
        sheet.save(out, optimize=True)

        entry = dict(
            sheet=f"anim_{anim['name']}", fw=cw, fh=ch, cols=cols,
            intro=len(intro), loop=len(loop), fps=anim["fps"],
            sx=STAGE_W / 2 + (x0 - canvas_w / 2) * unit,
            sy=ref_ground - (ground - y0) * unit,
            sw=cw * unit, sh=ch * unit,
        )
        entries[anim["state"]] = entry
        previews[anim["state"]] = (sheet, entry)
        print(f"{anim['state']:7s} {len(imgs):3d} cuadros  hoja {sheet.width}x{sheet.height}  "
              f"{out.stat().st_size/1e6:.2f} MB  escala {unit:.2f}  (audífono {ear:.0f}px, botas y={ground:.0f})")

    for v in cfg.get("variants", []):
        e = dict(entries[v["of"]]); e["fps"] = v["fps"]
        entries[v["state"]] = e

    write_kotlin(entries, cfg, kt_path)
    write_previews(previews, entries, cfg, ref, preview_dir)


def write_kotlin(entries, cfg, kt_path: Path):
    names = {a["state"]: a for a in cfg["animations"]}
    lines = [
        "package com.monito.wear.presentation.pet.animation",
        "",
        "import com.monito.shared.state.PetState",
        "import com.monito.wear.R",
        "",
        "/**",
        " * GENERADO por tools/sprites/build_sheets.py a partir de tools/sprites/animations.json.",
        " * No editar a mano: cambia el JSON y vuelve a correr el script.",
        " */",
        "object SpriteAnimations {",
    ]
    order = list(entries)
    for state in order:
        e = entries[state]
        lines += [
            f"    val {state} = SpriteAnimation(",
            f"        sheetRes = R.drawable.{e['sheet']},",
            f"        frameWidth = {e['fw']}, frameHeight = {e['fh']}, columns = {e['cols']},",
            f"        introFrames = {e['intro']}, loopFrames = {e['loop']}, fps = {float(e['fps'])}f,",
            f"        stageX = {e['sx']:.1f}f, stageY = {e['sy']:.1f}f, stageWidth = {e['sw']:.1f}f, stageHeight = {e['sh']:.1f}f,",
            "    )",
            "",
        ]
    lines += ["    /** Animación de un estado, o null si ese estado usa solo su imagen fija. */",
              "    fun forState(state: PetState): SpriteAnimation? = when (state) {"]
    lines += [f"        PetState.{s} -> {s}" for s in order]
    lines += ["        else -> null", "    }", "}", ""]
    kt_path.write_text("\n".join(lines), encoding="utf-8")


def write_previews(previews, entries, cfg, ref, preview_dir: Path):
    preview_dir.mkdir(parents=True, exist_ok=True)
    states = [a["state"] for a in cfg["animations"]] + [v["state"] for v in cfg.get("variants", [])]
    scale = 0.5
    sw, sh = int(STAGE_W * scale), int(STAGE_H * scale)

    def compose(state, t_ns):
        e = entries[state]
        base = previews[state if state in previews else [v for v in cfg["variants"] if v["state"] == state][0]["of"]]
        sheet = base[0]
        tick = int(t_ns * e["fps"] / 1e9)
        idx = tick if tick < e["intro"] else e["intro"] + (tick - e["intro"]) % e["loop"]
        c, r = idx % e["cols"], idx // e["cols"]
        frame = sheet.crop((c * e["fw"], r * e["fh"], (c + 1) * e["fw"], (r + 1) * e["fh"]))
        frame = frame.resize((max(1, round(e["sw"] * scale)), max(1, round(e["sh"] * scale))), Image.NEAREST)
        stage = Image.new("RGBA", (sw, sh), (20, 19, 31, 255))
        stage.alpha_composite(ref.resize((sw, sh), Image.NEAREST), (0, 0)) if state == "__ref__" else None
        stage.alpha_composite(frame, (round(e["sx"] * scale), round(e["sy"] * scale)))
        return stage

    # GIF: todas las animaciones lado a lado, con la imagen fija de referencia a la izquierda
    ref_stage = Image.new("RGBA", (sw, sh), (20, 19, 31, 255))
    ref_stage.alpha_composite(ref.resize((sw, sh), Image.NEAREST))
    frames = []
    for step in range(60):
        t = step * 1e9 / 20  # 20 fps de vista previa, 3 segundos
        row = Image.new("RGBA", (sw * (len(states) + 1), sh), (20, 19, 31, 255))
        row.paste(ref_stage, (0, 0))
        for i, s in enumerate(states):
            row.paste(compose(s, t), (sw * (i + 1), 0))
        frames.append(row.convert("RGB"))
    frames[0].save(preview_dir / "preview.gif", save_all=True, append_images=frames[1:], duration=50, loop=0)
    print("vista previa:", preview_dir / "preview.gif", " columnas:", ["IDLE(fijo)"] + states)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--src", required=True, help="carpeta con los .zip de frames")
    ap.add_argument("--config", default=str(Path(__file__).with_name("animations.json")))
    ap.add_argument("--root", default=str(Path(__file__).resolve().parents[2]))
    a = ap.parse_args()
    root = Path(a.root)
    wear = root / "android/wear/src/main"
    res_dir = wear / "res/drawable-nodpi"
    kt = wear / "kotlin/com/monito/wear/presentation/pet/animation/SpriteAnimations.kt"
    kt.parent.mkdir(parents=True, exist_ok=True)
    build(json.loads(Path(a.config).read_text(encoding="utf-8")), root, Path(a.src), res_dir, kt,
          Path(__file__).with_name("preview"))


if __name__ == "__main__":
    main()
