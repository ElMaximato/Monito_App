"""
Genera las capas de imagen de las escenas animadas del hub y del perfil.

Entradas : assets/hub/hub_background.jpg, assets/sprites/astro_idle.png
Salidas  : android/mobile/src/main/res/drawable-nodpi/
             hub_earth.png          Tierra recortada (círculo con alpha)
             hub_front.png          rocas lunares + astronauta sentado (sin cielo, sin ojos)
             profile_window.png     pared de la nave + ventana redonda (agujero transparente)
             profile_earth.png      Tierra a la resolución "pixel" de la escena del perfil
             profile_robot_back.png astronauta de espaldas (derivado del sprite de frente)

Requiere: pip install pillow numpy scipy
Uso     : python tools/scenes/build_scene_assets.py   (desde la raíz del proyecto)
"""
import math
import random
from pathlib import Path

import numpy as np
from PIL import Image
from scipy import ndimage as ndi

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "android/mobile/src/main/res/drawable-nodpi"
OUT.mkdir(parents=True, exist_ok=True)

# ---------------------------------------------------------------- constantes compartidas con Kotlin
SKY = (17, 24, 42)
EARTH_CX, EARTH_CY, EARTH_R = 517, 254, 226          # en la imagen original 1024x1024
FRONT_TOP = 500                                       # y donde empieza hub_front.png
VISOR = (45, 63, 87)
EYES = [(450, 628, 494, 668), (541, 628, 585, 668)]   # cajas (x0, y0, x1, y1) a borrar
PROFILE_W, PROFILE_H = 120, 270
WIN_CX, WIN_CY = 60, 84
WIN_HOLE = 40
SPRITE_UNIT = 5.45                                    # tamaño de "pixel" del sprite original


def hub_layers():
    src = Image.open(ROOT / "assets/hub/hub_background.jpg").convert("RGB")
    im = np.array(src).astype(int)

    # Tierra: círculo
    yy, xx = np.mgrid[0:1024, 0:1024]
    circle = (xx - EARTH_CX) ** 2 + (yy - EARTH_CY) ** 2 <= EARTH_R ** 2
    x0, y0 = EARTH_CX - EARTH_R, EARTH_CY - EARTH_R
    earth = np.dstack([im, circle.astype(int) * 255]).astype(np.uint8)
    earth_img = Image.fromarray(earth, "RGBA").crop((x0, y0, x0 + 2 * EARTH_R, y0 + 2 * EARTH_R))
    earth_img.save(OUT / "hub_earth.png")

    # Primer plano: todo lo que no es cielo y toca el suelo (rocas + astronauta)
    nonsky = np.abs(im - np.array(SKY)).sum(2) > 40
    lab, n = ndi.label(nonsky)
    bottom_labels = set(np.unique(lab[-1, :])) - {0}
    mask = np.isin(lab, list(bottom_labels))
    mask = ndi.binary_fill_holes(mask)

    rgb = im.copy()
    for (ex0, ey0, ex1, ey1) in EYES:  # borra los ojos: se dibujan animados en Kotlin
        rgb[ey0:ey1, ex0:ex1] = VISOR
    front = np.dstack([rgb, mask.astype(int) * 255]).astype(np.uint8)
    Image.fromarray(front, "RGBA").crop((0, FRONT_TOP, 1024, 1024)).save(OUT / "hub_front.png")


def robot_back():
    im = Image.open(ROOT / "assets/sprites/astro_idle.png").convert("RGBA")
    a = np.array(im).astype(int)
    a[:, :, 3] = np.where(a[:, :, 3] > 128, 255, 0)
    rgb = a[:, :, :3]
    alpha = a[:, :, 3] > 0

    cream = np.array([244, 236, 218])
    tan = np.array([214, 196, 176])
    white = np.array([252, 250, 244])

    # visor (oscuro y rodeado por el casco) -> casco liso
    dark = (rgb.sum(2) < 330) & alpha
    lab, _ = ndi.label(dark)
    visor_label = lab[300, 197]
    visor = ndi.binary_fill_holes(lab == visor_label)
    visor = ndi.binary_dilation(visor, iterations=4)
    ys, xs = np.where(visor)
    cx, cy = xs.mean(), ys.mean()
    for y, x in zip(ys, xs):
        shade = (x - cx) * 0.7 + (y - cy)
        c = tan if shade > 62 else (white if shade < -58 else cream)
        rgb[y, x] = c
    for sx in (148, 247):                         # costuras del casco
        for y in range(int(ys.min()), int(ys.max())):
            if visor[y, sx]:
                rgb[y, sx - 1:sx + 2] = tan

    # panel del pecho -> mochila de oxígeno
    for y in range(386, 470):
        for x in range(128, 268):
            if alpha[y, x]:
                edge = x in (128, 129, 130, 266, 267, 268) or y in (386, 387, 388, 468, 469, 470)
                rgb[y, x] = (20, 19, 31) if edge else (74, 90, 120)
    rgb[392:404, 134:262] = (104, 122, 154)      # brillo superior de la mochila
    rgb[418:446, 150:178] = (246, 142, 45)       # válvula naranja
    rgb[418:446, 210:238] = (46, 230, 208)       # indicador cian
    rgb[452:460, 150:238] = (40, 48, 72)

    out = np.dstack([rgb, a[:, :, 3]]).astype(np.uint8)
    img = Image.fromarray(out, "RGBA")
    small = img.resize((round(390 / SPRITE_UNIT), round(629 / SPRITE_UNIT)), Image.NEAREST)
    small = small.crop(small.getbbox())
    small.save(OUT / "profile_robot_back.png")
    return small


def profile_earth():
    e = Image.open(OUT / "hub_earth.png").convert("RGBA")
    e.resize((72, 72), Image.LANCZOS).save(OUT / "profile_earth.png")


def lerp(c1, c2, t):
    return tuple(int(round(c1[i] + (c2[i] - c1[i]) * t)) for i in range(3))


def profile_window():
    rnd = random.Random(7)
    W, H = PROFILE_W, PROFILE_H
    img = np.zeros((H, W, 4), dtype=np.uint8)
    wall, wall_dark, wall_light = (27, 32, 52), (20, 23, 40), (38, 45, 70)
    seam = (14, 16, 28)
    orange = (246, 142, 45)

    for y in range(H):
        for x in range(W):
            c = wall
            if rnd.random() < 0.05:
                c = wall_light
            elif rnd.random() < 0.05:
                c = wall_dark
            img[y, x] = (*c, 255)

    def rect(x0, y0, x1, y1, c):
        img[y0:y1, x0:x1] = (*c, 255)

    # techo + lámpara
    rect(0, 0, W, 12, (16, 18, 32))
    rect(0, 12, W, 13, seam)
    rect(24, 4, 96, 7, (255, 226, 170))
    rect(28, 3, 92, 4, (120, 100, 80))
    # costuras de paneles
    for y in (150, 200, 232):
        rect(0, y, W, y + 1, seam)
        rect(0, y + 1, W, y + 2, wall_light)
    for x in (30, 90):
        rect(x, 150, x + 1, H, seam)
    # conductos laterales
    for x0 in (0, 113):
        rect(x0, 13, x0 + 7, 150, (22, 26, 44))
        rect(x0 + 2, 13, x0 + 3, 150, (60, 70, 98))
        rect(x0 + 6 if x0 == 0 else x0, 13, x0 + 7 if x0 == 0 else x0 + 1, 150, seam)
        for y in range(40, 150, 46):
            rect(x0, y, x0 + 7, y + 2, orange)
    # consola bajo la ventana
    rect(0, 140, W, 150, (36, 42, 68))
    rect(0, 140, W, 141, (70, 84, 120))
    for i, x in enumerate((14, 22, 98, 106)):
        rect(x, 144, x + 3, 147, (60, 70, 98))
    rect(48, 144, 72, 147, (24, 28, 46))
    for i, c in enumerate(((246, 142, 45), (46, 230, 208), (232, 130, 155))):
        rect(51 + i * 7, 145, 54 + i * 7, 146, c)
    # suelo
    rect(0, 205, W, H, (32, 38, 62))
    rect(0, 205, W, 206, (70, 84, 120))
    for y in (222, 240, 258):
        rect(0, y, W, y + 1, seam)

    # ventana redonda
    light = np.array((-0.6, -0.8))
    R_out = 54
    for y in range(H):
        for x in range(W):
            dx, dy = x + 0.5 - WIN_CX, y + 0.5 - WIN_CY
            r = math.hypot(dx, dy)
            if r > R_out:
                continue
            ang = (dx * light[0] + dy * light[1]) / max(r, 1e-6)
            level = min(3, int((ang + 1) / 2 * 4))
            if r <= WIN_HOLE - 1:
                img[y, x] = (0, 0, 0, 0)
            elif r <= WIN_HOLE:
                img[y, x] = (6, 8, 20, 150)                       # sombra interior del cristal
            elif r <= 42:
                img[y, x] = (*seam, 255)
            elif r <= 45:
                img[y, x] = (*[(54, 62, 90), (72, 84, 116), (96, 112, 146), (130, 148, 182)][level], 255)
            elif r <= 48:
                img[y, x] = (*[(40, 46, 70), (58, 68, 98), (80, 94, 128), (108, 124, 158)][level], 255)
            elif r <= 49:
                img[y, x] = (*seam, 255)
            elif r <= 53:
                img[y, x] = (*[(30, 36, 58), (38, 45, 70), (46, 55, 84), (56, 66, 98)][level], 255)
            else:
                img[y, x] = (*seam, 255)
    # tornillos
    for k in range(8):
        a = math.radians(22.5 + 45 * k)
        bx = int(round(WIN_CX + 51 * math.cos(a))) - 1
        by = int(round(WIN_CY + 51 * math.sin(a))) - 1
        base = orange if k % 2 == 0 else (84, 96, 130)
        rect(bx, by, bx + 3, by + 3, base)
        rect(bx, by, bx + 1, by + 1, lerp(base, (255, 255, 255), 0.5))
        rect(bx + 2, by + 2, bx + 3, by + 3, lerp(base, (0, 0, 0), 0.45))

    Image.fromarray(img, "RGBA").save(OUT / "profile_window.png")


def preview(small_robot):
    """Composición estática para revisar a ojo (no se usa en la app)."""
    W, H = PROFILE_W, PROFILE_H
    canvas = Image.new("RGBA", (W, H), (6, 8, 20, 255))
    rnd = random.Random(3)
    for _ in range(60):
        canvas.putpixel((rnd.randrange(W), rnd.randrange(H)), (230, 240, 255, 255))
    earth = Image.open(OUT / "profile_earth.png")
    canvas.alpha_composite(earth, (WIN_CX - 36, WIN_CY - 40))
    canvas.alpha_composite(Image.open(OUT / "profile_window.png"))
    canvas.alpha_composite(small_robot, (WIN_CX - small_robot.width // 2, 92))
    canvas.resize((W * 6, H * 6), Image.NEAREST).save(ROOT / "tools/scenes/preview_profile.png")


if __name__ == "__main__":
    hub_layers()
    robot = robot_back()
    profile_earth()
    profile_window()
    preview(robot)
    print("robot:", robot.size)
