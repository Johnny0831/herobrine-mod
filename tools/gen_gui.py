"""Atlas da HUD (barras de Sanidade/Honra, ícones, selos) + texturas dos itens novos + HudAtlas.java."""
import os, random, math
from PIL import Image, ImageDraw
random.seed(424242)
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
TEXG = ROOT + "/src/main/resources/assets/hollowvigil/textures/gui/"
TEXI = ROOT + "/src/main/resources/assets/hollowvigil/textures/item/"
JAVA = ROOT + "/src/main/java/com/hollowvigil/HudAtlas.java"
os.makedirs(TEXG, exist_ok=True); os.makedirs(TEXI, exist_ok=True)

def clamp(v): return max(0, min(255, int(v)))
def lerp(a, b, t): return tuple(clamp(a[i] + (b[i] - a[i]) * t) for i in range(3))
def grad(stops, t):
    for i in range(len(stops) - 1):
        (p0, c0), (p1, c1) = stops[i], stops[i + 1]
        if t <= p1: return lerp(c0, c1, (t - p0) / max(1e-6, p1 - p0))
    return stops[-1][1]

A = Image.new("RGBA", (256, 128), (0, 0, 0, 0)); ap = A.load()
def put(x, y, c, a=255): ap[x, y] = (c[0], c[1], c[2], a)

# ---- moldura 108x16 (fenda interna 100x10 em (4,3))
FRAME = (0, 0, 108, 16)
for x in range(108):
    for y in range(16):
        edge = min(x, y, 107 - x, 15 - y)
        if edge == 0: c = (6, 6, 10)
        elif edge == 1: c = (88, 90, 112) if (y <= 1 or x <= 1) else (30, 30, 42)
        elif edge == 2: c = (52, 54, 70)
        else: c = (16, 16, 24)
        put(x, y, c)
for y in range(3, 13):                                  # fenda
    for x in range(4, 104):
        d = 1.0 - 0.35 * (1 if y == 3 else 0.6 if y == 4 else 0)
        put(x, y, tuple(clamp(v * d) for v in (18, 18, 28)))
for (x, y) in ((2, 2), (105, 2), (2, 13), (105, 13)): put(x, y, (170, 172, 190))   # rebites
for frac in (0.25, 0.5, 0.75):                                                      # marcas de escala
    x = 4 + int(100 * frac)
    for y in (13, 14): put(x, y, (120, 122, 146))

# ---- barras de preenchimento 100x10
def fill_region(y0, stops, wave=0.0, stripe=True):
    for x in range(100):
        base = grad(stops, x / 99.0)
        for y in range(10):
            f = 1.0
            if y == 0: f = 1.35
            elif y == 1: f = 1.18
            elif y >= 8: f = 0.72 if y == 8 else 0.5
            if stripe and ((x + y * 2) // 3) % 2 == 0: f *= 0.93
            n = random.randint(-5, 5)
            c = tuple(clamp(ch * f + n) for ch in base)
            put(x, y0 + y, c)
TEAL, VIOLET, CRIM, HONOR, MISSION = 16, 26, 36, 46, 56
fill_region(TEAL, [(0, (70, 215, 215)), (1, (40, 140, 200))])
fill_region(VIOLET, [(0, (170, 110, 225)), (1, (105, 60, 175))])
fill_region(CRIM, [(0, (225, 60, 55)), (1, (130, 20, 35))])
fill_region(HONOR, [(0, (140, 20, 25)), (0.25, (200, 70, 45)), (0.5, (205, 205, 210)), (0.78, (240, 205, 85)), (1, (255, 225, 80))])
fill_region(MISSION, [(0, (190, 30, 28)), (1, (255, 130, 45))])

# ---- brilho diagonal 40x10 (animado no cliente)
SHEEN = (0, 66, 40, 10)
for x in range(40):
    for y in range(10):
        a = max(0.0, 1.0 - abs(x - y * 2 - 14) / 9.0)
        put(x, 66 + y, (255, 255, 255), int(110 * a))

# ---- soquete de ícone 20x20 em (112,0)
SOCKET = (112, 0, 20, 20)
for x in range(20):
    for y in range(20):
        dx, dy = x - 9.5, y - 9.5
        r = max(abs(dx), abs(dy))
        if abs(dx) > 8.5 and abs(dy) > 8.5: continue
        edge = min(x, y, 19 - x, 19 - y)
        c = (6, 6, 10) if edge == 0 else ((92, 94, 118) if edge == 1 and (x < 10 or y < 10) else (40, 40, 54) if edge == 1 else (20, 20, 30))
        put(112 + x, y, c)

# ---- ícones 16x16 em y=80
ICON_Y = 80
def icon(idx, drawfn):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im); drawfn(d, im)
    A.alpha_composite(im, (idx * 16, ICON_Y))
def eye(d, im, iris, crack=False):
    d.polygon([(1, 8), (4, 5), (8, 4), (12, 5), (15, 8), (12, 11), (8, 12), (4, 11)], fill=(222, 226, 236, 255), outline=(40, 40, 56, 255))
    d.ellipse((5, 5, 11, 11), fill=iris + (255,)); d.ellipse((7, 7, 9, 9), fill=(5, 5, 10, 255)); d.point((6, 6), fill=(255, 255, 255, 255))
    if crack:
        for (a, b) in (((2, 7), (5, 8)), ((14, 7), (11, 8)), ((8, 4), (7, 6)), ((9, 12), (10, 10))): d.line([a, b], fill=(150, 20, 30, 255))
icon(0, lambda d, im: eye(d, im, (60, 175, 210)))
icon(1, lambda d, im: eye(d, im, (205, 45, 55), True))
def scales(d, im):
    c = (214, 214, 226, 255); d.line([(8, 3), (8, 13)], fill=c); d.line([(3, 5), (13, 5)], fill=c); d.line([(5, 13), (11, 13)], fill=c)
    d.line([(3, 5), (3, 9)], fill=c); d.line([(13, 5), (13, 9)], fill=c)
    d.ellipse((1, 9, 5, 11), fill=c); d.ellipse((11, 9, 15, 11), fill=c); d.point((8, 2), fill=(255, 220, 100, 255))
icon(2, scales)
def skull(d, im):
    c = (228, 222, 206, 255); d.ellipse((3, 2, 12, 10), fill=c); d.rectangle((5, 9, 10, 13), fill=c)
    d.rectangle((5, 6, 6, 8), fill=(10, 8, 12, 255)); d.rectangle((9, 6, 10, 8), fill=(10, 8, 12, 255)); d.point((7, 9), fill=(10, 8, 12, 255)); d.point((8, 9), fill=(10, 8, 12, 255))
    for x in (6, 8, 10): d.point((x, 12), fill=(20, 16, 22, 255))
icon(3, skull)
def halo(d, im):
    g = (255, 220, 85, 255); d.ellipse((2, 2, 13, 6), outline=g, width=2)
    d.polygon([(8, 7), (9, 10), (12, 11), (9, 12), (8, 15), (7, 12), (4, 11), (7, 10)], fill=(255, 245, 190, 255))
icon(4, halo)
def knife(d, im):
    d.line([(4, 11), (13, 2)], fill=(214, 218, 232, 255), width=2); d.line([(5, 12), (13, 4)], fill=(120, 124, 146, 255))
    d.line([(2, 8), (7, 13)], fill=(150, 20, 30, 255), width=2); d.line([(3, 12), (1, 14)], fill=(30, 22, 26, 255), width=2); d.point((14, 1), fill=(255, 255, 255, 255))
icon(5, knife)
def wings(d, im):
    g = (250, 245, 225, 255); o = (190, 160, 70, 255)
    d.polygon([(7, 8), (1, 3), (2, 8), (1, 11), (4, 11), (7, 12)], fill=g, outline=o)
    d.polygon([(9, 8), (15, 3), (14, 8), (15, 11), (12, 11), (9, 12)], fill=g, outline=o)
    for (a, b) in (((3, 5), (6, 8)), ((3, 8), (6, 9)), ((13, 5), (10, 8)), ((13, 8), (10, 9))): d.line([a, b], fill=o)
    d.rectangle((7, 6, 8, 13), fill=(255, 220, 85, 255))
icon(6, wings)

# ---- selo de modo 96x14 em (0,98)
BADGE = (0, 98, 96, 14)
for x in range(96):
    for y in range(14):
        edge = min(x, y, 95 - x, 13 - y)
        if (x < 2 or x > 93) and (y < 2 or y > 11): continue
        c = (6, 6, 10) if edge == 0 else (74, 76, 98) if edge == 1 and (y <= 1 or x <= 1) else (30, 30, 42) if edge == 1 else (18, 18, 28)
        put(x, 98 + y, c)

A.save(TEXG + "hud.png")

with open(JAVA, "w") as f:
    f.write("""package com.hollowvigil;

/** GERADO por tools/gen_gui.py - coordenadas do atlas textures/gui/hud.png (256x128). */
public final class HudAtlas {
    private HudAtlas() {}

    public static final int TEX_W = 256, TEX_H = 128;
    public static final int FRAME_W = 108, FRAME_H = 16;      // moldura em (0,0)
    public static final int SOCKET_U = 112, SOCKET_V = 0, SOCKET_SIZE = 20;
    public static final int FILL_W = 100, FILL_H = 10;
    public static final int FILL_TEAL_V = %d, FILL_VIOLET_V = %d, FILL_CRIMSON_V = %d, FILL_HONOR_V = %d, FILL_MISSION_V = %d;
    public static final int SHEEN_V = 66, SHEEN_W = 40;
    public static final int ICON_V = %d;
    public static final int ICON_EYE = 0, ICON_EYE_LOW = 1, ICON_SCALES = 2, ICON_SKULL = 3, ICON_HALO = 4, ICON_KNIFE = 5, ICON_WINGS = 6;
    public static final int BADGE_V = 98, BADGE_W = 96, BADGE_H = 14;
}
""" % (TEAL, VIOLET, CRIM, HONOR, MISSION, ICON_Y))

# ================= itens 16x16
def art(rows, pal):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); p = im.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in pal: p[x, y] = pal[ch] + (255,)
    return im

# faca do assassino (lâmina escura com fio vermelho)
kn = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(kn)
d.line([(4, 11), (14, 1)], fill=(42, 44, 56, 255), width=3)          # lâmina
d.line([(5, 11), (14, 2)], fill=(150, 154, 176, 255), width=1)        # brilho do corte
d.line([(3, 11), (13, 1)], fill=(190, 25, 35, 255), width=1)          # fio sangrento
d.line([(2, 8), (8, 14)], fill=(120, 18, 28, 255), width=2)           # guarda
d.line([(4, 12), (1, 15)], fill=(24, 20, 24, 255), width=2)           # cabo
d.point([(3, 13), (2, 14)], fill=(160, 30, 40, 255)); d.point([(15, 0), (14, 1)], fill=(255, 235, 235, 255))
kn.save(TEXI + "killer_knife.png")

# lanterna oca (gaiola escura + chama branco-azulada)
lan = ["................", ".......XX.......", "......X..X......", ".....XXXXXX.....", "....XCCCCCCX....", "...XCGGGGGGCX...",
       "...XCGWWWWGCX...", "...XCGWWWWGCX...", "...XCGWWWWGCX...", "...XCGGWWGGCX...", "...XCGGGGGGCX...", "....XCCCCCCX....",
       ".....XXXXXX.....", "......X..X......", "................", "................"]
li = art(lan, {'X': (34, 34, 46), 'C': (92, 96, 120), 'G': (120, 210, 230), 'W': (250, 252, 255)})
lp_ = li.load()
for y in range(16):
    for x in range(16):
        if lp_[x, y][3] and lan[y][x] == 'G':
            t = (y - 5) / 6.0; lp_[x, y] = (clamp(110 + 60 * (1 - t)), clamp(200 + 40 * (1 - t)), 235, 255)
li.save(TEXI + "hollow_lantern.png")

# remove textura antiga da adaga
for old in ("hollow_dagger.png",):
    if os.path.exists(TEXI + old): os.remove(TEXI + old)
print("hud.png e itens ok")
