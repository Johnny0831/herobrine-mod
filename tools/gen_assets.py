"""Gera as texturas (O Oco + Ceifador + Alucinação + itens) e o código do modelo do Corrompido a partir de uma única especificação."""
import random, os
from PIL import Image
random.seed(20261114)
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
TEX = ROOT + "/src/main/resources/assets/hollowvigil/textures/entity/"
JAVA = ROOT + "/src/main/java/com/hollowvigil/HollowReaperModel.java"

def clampc(c): return tuple(max(0, min(255, int(x))) for x in c)
def shade(c, f): return clampc([x * f for x in c])
def noise(c, v=5):
    n = random.randint(-v, v); return clampc([x + n for x in c])
def faces(u, v, w, h, d):
    return {'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d), 'right': (u, v + d, d, h),
            'front': (u + d, v + d, w, h), 'left': (u + d + w, v + d, d, h), 'back': (u + 2 * d + w, v + d, w, h)}

class P:
    def __init__(s, w, h):
        s.img = Image.new("RGBA", (w, h), (0, 0, 0, 0)); s.px = s.img.load()
    def put(s, x, y, c, a=255): s.px[x, y] = (c[0], c[1], c[2], a)
    def clear(s, x, y): s.px[x, y] = (0, 0, 0, 0)
    def sides(s, u, v, w, h, d):
        f = faces(u, v, w, h, d); return [f['right'], f['front'], f['left'], f['back']]
    def fbox(s, u, v, w, h, d, c, grad=0.22, var=5):
        for nm, (x0, y0, fw, fh) in faces(u, v, w, h, d).items():
            for x in range(x0, x0 + fw):
                for y in range(y0, y0 + fh):
                    t = (y - y0) / max(1, fh - 1)
                    f = 1.0 - grad * t + (0.07 if nm == 'top' else 0) - (0.10 if nm == 'bottom' else 0) - (0.05 if nm in ('left', 'back') else 0)
                    s.put(x, y, noise(shade(c, f), var))
    def band(s, u, v, w, h, d, r0, r1, c, var=4):
        for (x0, y0, fw, fh) in s.sides(u, v, w, h, d):
            for y in range(y0 + r0, min(y0 + r1 + 1, y0 + fh)):
                for x in range(x0, x0 + fw): s.put(x, y, noise(c, var))
    def ragged(s, u, v, w, h, d, n, p):
        for (x0, y0, fw, fh) in s.sides(u, v, w, h, d):
            for k in range(n):
                y = y0 + fh - 1 - k; prob = p * (n - k) / n
                for x in range(x0, x0 + fw):
                    if random.random() < prob: s.clear(x, y)
    def holes(s, u, v, w, h, d, p):
        for (x0, y0, fw, fh) in s.sides(u, v, w, h, d):
            for x in range(x0, x0 + fw):
                for y in range(y0, y0 + fh):
                    if random.random() < p: s.clear(x, y)
    def veins(s, u, v, w, h, d, count, col=(72, 76, 98)):
        sd = s.sides(u, v, w, h, d)
        for _ in range(count):
            x0, y0, fw, fh = random.choice(sd)
            x = x0 + random.randint(0, fw - 1); y = y0 + random.randint(0, fh - 1)
            for k in range(random.randint(2, 4)):
                if y + k < y0 + fh and s.px[x, y + k][3]: s.put(x, y + k, noise(col, 4))
    def clear_face(s, rect):
        x0, y0, fw, fh = rect
        for x in range(x0, x0 + fw):
            for y in range(y0, y0 + fh): s.clear(x, y)
    def rect(s, x0, y0, x1, y1, c, var=0):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1): s.put(x, y, noise(c, var) if var else c)

WHITE = (248, 248, 255)

# ============================================================ ESPREITADOR (skin 64x64)
S1 = P(64, 64)
SKIN = (172, 176, 186); SKIN_SH = (140, 144, 156)
DARK = (30, 30, 37); SHIRT = (38, 40, 50); PANTS = (32, 32, 40); HAIR = (18, 18, 22); WRAP = (196, 190, 172)

# cabeça base
S1.fbox(0, 0, 8, 8, 8, SKIN, grad=0.18, var=4)
hf = faces(0, 0, 8, 8, 8)
fx, fy, _, _ = hf['front']
# sombra do capuz sobre o rosto
for x in range(8):
    for y in range(8):
        f = 1.0
        if y < 3: f -= 0.14 * (3 - y)
        if x in (0, 7): f -= 0.16
        S1.put(fx + x, fy + y, noise(shade(SKIN, f), 3))
# órbitas fundas + olhos brancos
SOCK = (34, 36, 46)
for (a, b) in ((0, 2), (5, 7)):
    for x in range(a, b + 1):
        for y in range(2, 6): S1.put(fx + x, fy + y, noise(SOCK, 3))
for (a, b) in ((1, 2), (5, 6)):
    for x in range(a, b + 1):
        for y in range(3, 5): S1.put(fx + x, fy + y, WHITE)
for x in (3, 4):                                    # sombra do nariz
    S1.put(fx + x, fy + 5, noise(SKIN_SH, 3)); S1.put(fx + x, fy + 6, noise(shade(SKIN, 0.7), 3))
for x in range(2, 6): S1.put(fx + x, fy + 7, (62, 40, 46))   # boca fina
S1.put(fx + 6, fy + 7, (62, 40, 46)); S1.put(fx + 1, fy + 5, shade(SKIN, 1.08)); S1.put(fx + 6, fy + 5, shade(SKIN, 1.08))
# cabelo (visível sob o capuz)
tx, ty, tw, th = hf['top']; S1.rect(tx, ty, tx + tw - 1, ty + th - 1, HAIR, 3)
for x in range(8):
    S1.put(fx + x, fy, noise(HAIR, 3))
    if random.random() < 0.7: S1.put(fx + x, fy + 1, noise(HAIR, 3))
for nm, rows in (('right', 3), ('left', 3), ('back', 7)):
    x0, y0, fw, fh = hf[nm]
    for x in range(x0, x0 + fw):
        for y in range(y0, y0 + rows):
            if y - y0 < rows - 1 or random.random() < 0.6: S1.put(x, y, noise(HAIR, 3))

# capuz (camada hat)
S1.fbox(32, 0, 8, 8, 8, (26, 26, 33), grad=0.15, var=4)
hh = faces(32, 0, 8, 8, 8)
S1.clear_face(hh['bottom'])
x0, y0, fw, fh = hh['front']
for x in range(1, 7):
    for y in range(2, 8): S1.clear(x0 + x, y0 + y)
for x in range(8):
    S1.put(x0 + x, y0 + 0, noise((20, 20, 26), 3)); S1.put(x0 + x, y0 + 1, noise((22, 22, 28), 3))
for y in range(8):
    S1.put(x0, y0 + y, noise((24, 24, 30), 3)); S1.put(x0 + 7, y0 + y, noise((24, 24, 30), 3))
S1.ragged(32, 0, 8, 8, 8, 2, 0.6)

# tronco: camisa rasgada + casaco esfarrapado
S1.fbox(16, 16, 8, 12, 4, SHIRT, grad=0.2, var=4)
bf = faces(16, 16, 8, 12, 4)['front']
for x in (3, 4):
    for y in range(0, 3): S1.put(bf[0] + x, bf[1] + y, noise(SKIN_SH, 4))      # gola aberta mostrando a pele
for x in range(8): S1.put(bf[0] + x, bf[1] + 9, noise((70, 58, 44), 3))         # cinto
S1.fbox(16, 32, 8, 12, 4, (28, 28, 35), grad=0.25, var=4)
jf_ = faces(16, 32, 8, 12, 4)
S1.clear_face(jf_['top']); S1.clear_face(jf_['bottom'])
fx0, fy0, fw0, fh0 = jf_['front']
for y in range(2, 12):
    for x in (3, 4):
        if random.random() < 0.8: S1.clear(fx0 + x, fy0 + y)                   # casaco aberto
S1.ragged(16, 32, 8, 12, 4, 4, 0.8); S1.holes(16, 32, 8, 12, 4, 0.04)

# braços (direito 40,16 / esquerdo 32,48): pele pálida com ataduras + mangas rasgadas
for (bu, bv, su, sv) in ((40, 16, 40, 32), (32, 48, 48, 48)):
    S1.fbox(bu, bv, 4, 12, 4, SKIN, grad=0.2, var=4)
    S1.veins(bu, bv, 4, 12, 4, 7)
    for r in range(7, 12):
        if r % 2 == 1: S1.band(bu, bv, 4, 12, 4, r, r, WRAP, 5)
    S1.fbox(su, sv, 4, 12, 4, (32, 32, 39), grad=0.2, var=4)
    S1.clear_face(faces(su, sv, 4, 12, 4)['bottom'])
    S1.band(su, sv, 4, 12, 4, 8, 11, (0, 0, 0), 0)
    for (x0, y0, fw, fh) in S1.sides(su, sv, 4, 12, 4):
        for y in range(y0 + 8, y0 + fh):
            for x in range(x0, x0 + fw): S1.clear(x, y)
    S1.ragged(su, sv, 4, 12, 4, 3, 0.6) if False else None
    for (x0, y0, fw, fh) in S1.sides(su, sv, 4, 12, 4):
        for k in range(3):
            y = y0 + 7 - k
            for x in range(x0, x0 + fw):
                if random.random() < 0.55 * (3 - k) / 3: S1.clear(x, y)

# pernas (direita 0,16 / esquerda 16,48): pele nua pálida + calça rasgada
for (bu, bv, pu, pv) in ((0, 16, 0, 32), (16, 48, 0, 48)):
    S1.fbox(bu, bv, 4, 12, 4, SKIN_SH, grad=0.22, var=4)
    S1.veins(bu, bv, 4, 12, 4, 6)
    S1.band(bu, bv, 4, 12, 4, 0, 5, PANTS, 4)
    S1.fbox(pu, pv, 4, 12, 4, (30, 30, 38), grad=0.2, var=4)
    pf = faces(pu, pv, 4, 12, 4)
    S1.clear_face(pf['bottom'])
    for (x0, y0, fw, fh) in S1.sides(pu, pv, 4, 12, 4):
        for y in range(y0 + 9, y0 + fh):
            for x in range(x0, x0 + fw): S1.clear(x, y)
        for k in range(3):
            y = y0 + 8 - k
            for x in range(x0, x0 + fw):
                if random.random() < 0.6 * (3 - k) / 3: S1.clear(x, y)
S1.img.save(TEX + "hollow.png")

E1 = P(64, 64)
for (a, b) in ((1, 2), (5, 6)):
    for x in range(a, b + 1):
        for y in range(3, 5): E1.put(fx + x, fy + y, WHITE)
E1.img.save(TEX + "hollow_eyes.png")

# ============================================================ CORROMPIDO (spec compartilhada)
S = []
def part(name, parent, kind, box, pose=(0, 0, 0), rot=(0, 0, 0)):
    S.append(dict(name=name, parent=parent, kind=kind, box=box, pose=pose, rot=rot))

part("body", None, "flesh", (-3, -14, -2, 6, 14, 4), (0, 2, 0))
part("ribcage", "body", "ribs", (-4, -12, -3, 8, 9, 1))
for i, y in enumerate((-3, -7, -11)):
    part(f"spine{i}", "body", "bone", (-0.5, -1, 0, 1, 3, 2), (0, y, 2))
part("neck", "body", "neck", (-1.5, -7, -1.5, 3, 7, 3), (0, -14, 0))
part("head", "neck", "head", (-3.5, -9, -4, 7, 9, 8), (0, -7, 0))
part("jaw", "head", "jaw", (-3, 0, -7, 6, 3, 7), (0, -1.5, 3.5))
part("thorn_a_l", "head", "dark", (-0.5, -6, -0.5, 1, 6, 1), (2.2, -8.5, 1), (-0.3, 0, 0.45))
part("thorn_a_r", "head", "dark", (-0.5, -6, -0.5, 1, 6, 1), (-2.2, -8.5, 1), (-0.3, 0, -0.45))
part("thorn_b_l", "head", "dark", (-0.5, -4, -0.5, 1, 4, 1), (1.2, -9, -1.5), (-0.1, 0, 0.2))
part("thorn_b_r", "head", "dark", (-0.5, -4, -0.5, 1, 4, 1), (-1.2, -9, -1.5), (-0.1, 0, -0.2))
for s_, sx in (("r", -1), ("l", 1)):
    part(f"arm_u_{s_}", "body", "flesh", (-1, 0, -1, 2, 12, 2), (sx * 4.5, -12, 0))
    part(f"arm_f_{s_}", f"arm_u_{s_}", "forearm", (-1, 0, -1, 2, 15, 2), (0, 12, 0))
    for i, (cx, cz, rz) in enumerate(((-0.8, 0, -0.15), (0.8, 0, 0.15), (0, -0.9, 0))):
        part(f"claw_{s_}{i}", f"arm_f_{s_}", "claw", (-0.5, 0, -0.5, 1, 5, 1), (cx, 15, cz), (0.1 if i == 2 else 0, 0, rz))
    part(f"ex_u_{s_}", "body", "flesh", (-0.5, 0, -0.5, 1, 9, 1), (sx * 3, -7, -1.5))
    part(f"ex_f_{s_}", f"ex_u_{s_}", "flesh", (-0.5, 0, -0.5, 1, 11, 1), (0, 9, 0))
    part(f"thigh_{s_}", None, "leg", (-1.5, 0, -1.5, 3, 12, 3), (sx * 2.5, 2, 0))
    part(f"shin_{s_}", f"thigh_{s_}", "leg", (-1, 0, -1, 2, 12, 2), (0, 12, 0))
    part(f"foot_{s_}", f"shin_{s_}", "foot", (-1.5, -1, -5, 3, 2, 6), (0, 12, 0))
part("pelvis", None, "cloth", (-3.5, -2, -2, 7, 4, 4), (0, 2, 0))
part("cloth_f", "body", "cloth", (-1.5, 0, -0.5, 3, 10, 1), (0, 0, -2.5))
part("cloth_bl", "body", "cloth", (-1, 0, -0.5, 2, 9, 1), (-2, 0, 2.5))
part("cloth_br", "body", "cloth", (-1, 0, -0.5, 2, 9, 1), (2, 0, 2.5))

# empacotamento de UV (prateleiras)
items = sorted(S, key=lambda p: -(p['box'][5] + p['box'][4]))
x = y = shelf = 0
for p in items:
    w, h, d = [int(t) for t in p['box'][3:6]]
    fw, fh = 2 * d + 2 * w + 1, d + h + 1
    if x + fw > 128: x = 0; y += shelf; shelf = 0
    p['uv'] = (x, y); x += fw; shelf = max(shelf, fh)
assert y + shelf <= 128, "sheet cheia"

PAL = {'flesh': (150, 152, 160), 'forearm': (138, 140, 148), 'leg': (126, 128, 136), 'foot': (58, 56, 64),
       'bone': (210, 202, 184), 'dark': (24, 24, 30), 'cloth': (42, 40, 50), 'claw': (216, 208, 190),
       'ribs': (210, 202, 184), 'head': (162, 164, 172), 'jaw': (132, 134, 142), 'neck': (140, 142, 150)}
C = P(128, 128); E = P(128, 128)
BLOOD = (78, 12, 16)
for p in S:
    u, v = p['uv']; w, h, d = [int(t) for t in p['box'][3:6]]; k = p['kind']
    C.fbox(u, v, w, h, d, PAL[k], grad=0.25, var=6)
    f = faces(u, v, w, h, d)
    if k in ('flesh', 'forearm', 'leg', 'neck'): C.veins(u, v, w, h, d, max(2, (w * h) // 3))
    if k == 'forearm':
        for r in range(5, h, 5): C.band(u, v, w, h, d, r, r + 1, (30, 28, 36), 3)     # faixas
    if k == 'cloth': C.ragged(u, v, w, h, d, 3, 0.75)
    if k == 'claw':
        for (x0, y0, fw, fh) in C.sides(u, v, w, h, d):
            for xx in range(x0, x0 + fw): C.put(xx, y0 + fh - 1, (40, 36, 40))
    if k == 'neck':
        for r in (1, 4): C.band(u, v, w, h, d, r, r, (34, 32, 40), 2)
    if p['name'] == 'body':
        fx_, fy_, fw_, fh_ = f['front']
        for yy in range(fh_):
            C.put(fx_ + 2, fy_ + yy, noise((20, 18, 24), 2))                      # costura central
            if yy % 2 == 0:
                C.put(fx_ + 1, fy_ + yy, (86, 88, 98)); C.put(fx_ + 3, fy_ + yy, (86, 88, 98))
        for _ in range(7):
            for face in (f['front'], f['back']):
                wx = face[0] + random.randint(0, face[2] - 2); wy = face[1] + random.randint(0, face[3] - 2)
                C.rect(wx, wy, wx + 1, wy + 1, BLOOD, 6)
    if k == 'ribs':
        fx_, fy_, fw_, fh_ = f['front']
        C.rect(fx_, fy_, fx_ + fw_ - 1, fy_ + fh_ - 1, (22, 12, 14), 2)
        for yy in range(0, fh_ - 1, 2):
            C.rect(fx_, fy_ + yy, fx_ + fw_ - 1, fy_ + yy, noise((214, 206, 188), 6))
        C.rect(fx_ + 3, fy_, fx_ + 4, fy_ + fh_ - 1, (226, 218, 200), 4)         # esterno
    if p['name'] == 'jaw':
        fx_, fy_, fw_, fh_ = f['front']
        C.rect(fx_, fy_, fx_ + fw_ - 1, fy_ + fh_ - 1, (10, 8, 10))
        for xx in range(fw_):
            if xx % 2 == 0: C.put(fx_ + xx, fy_, WHITE)
        tx_, ty_, tw_, th_ = f['top']; C.rect(tx_, ty_, tx_ + tw_ - 1, ty_ + th_ - 1, (88, 10, 14), 6)
    if p['name'] == 'head':
        fx_, fy_, fw_, fh_ = f['front']
        for (a, b) in ((0, 2), (4, 6)):
            for xx in range(a, b + 1):
                for yy in range(2, 6): C.put(fx_ + xx, fy_ + yy, (4, 4, 6))
        for yy in (5, 6): C.put(fx_ + 3, fy_ + yy, (30, 30, 38))
        for xx in range(fw_):
            C.put(fx_ + xx, fy_ + 8, WHITE if xx % 2 == 0 else (10, 8, 10))     # dentes superiores
        for (sx_, sy_) in ((0, 6), (1, 7), (6, 6), (5, 7), (0, 7), (6, 7)): C.put(fx_ + sx_, fy_ + sy_, (28, 26, 34))
        for (x0, y0, fw2, fh2) in (f['left'], f['right']):
            for xx in range(x0 + 2, x0 + fw2 - 1):
                for yy in range(y0 + 1, y0 + 4): C.put(xx, yy, noise(shade(PAL['head'], 0.55), 4))  # têmporas fundas
        tx_, ty_, tw_, th_ = f['top']
        for _ in range(5):
            cx, cy = tx_ + random.randint(0, tw_ - 1), ty_ + random.randint(0, th_ - 1)
            for q in range(random.randint(2, 4)):
                if cx + q < tx_ + tw_: C.put(cx + q, cy, (34, 32, 40))
        bx, by, bw, bh = f['bottom']; C.rect(bx, by, bx + bw - 1, by + bh - 1, (88, 10, 14), 6)
        # olhos brilhantes (camada emissiva)
        ef = fx_, fy_
        for (a, b) in ((1, 2), (4, 5)):
            for xx in range(a, b + 1):
                for yy in range(3, 5): E.put(ef[0] + xx, ef[1] + yy, WHITE)
        for (ex_, ey_) in ((1, 1), (3, 0), (5, 1)): E.put(ef[0] + ex_, ef[1] + ey_, WHITE)
C.img.save(TEX + "hollow_reaper.png")
E.img.save(TEX + "hollow_reaper_eyes.png")

# ============================================================ CÓDIGO JAVA DO MODELO
def jf(v):
    s = repr(float(v))
    if s.endswith('.0'): s = s[:-2]
    return s + 'F'
fields = "    private final ModelPart " + ", ".join(p['name'] for p in S) + ";"
ctor, layer = [], []
for p in S:
    n, par = p['name'], p['parent']
    ctor.append(f'        this.{n} = {"root" if par is None else "this." + par}.getChild("{n}");')
    x_, y_, z_, w_, h_, d_ = p['box']; u_, v_ = p['uv']
    px_, py_, pz_ = p['pose']; rx, ry, rz = p['rot']
    pv = "root" if par is None else "p_" + par
    layer.append(f'        PartDefinition p_{n} = {pv}.addOrReplaceChild("{n}", CubeListBuilder.create().texOffs({u_}, {v_})'
                 f'.addBox({jf(x_)}, {jf(y_)}, {jf(z_)}, {jf(w_)}, {jf(h_)}, {jf(d_)}), '
                 f'PartPose.offsetAndRotation({jf(px_)}, {jf(py_)}, {jf(pz_)}, {jf(rx)}, {jf(ry)}, {jf(rz)}));')

TEMPLATE = open(os.path.join(os.path.dirname(__file__), "model_template.java.txt")).read()
out = TEMPLATE.replace("//@@FIELDS@@", fields).replace("//@@CTOR@@", "\n".join(ctor)).replace("//@@LAYER@@", "\n".join(layer))
open(JAVA, "w").write(out)
print("partes:", len(S))


# ============================================================ ALUCINAÇÃO (silhueta semitransparente)
H = P(64, 64)
for (u, v, w, h, d) in ((0, 0, 8, 8, 8), (16, 16, 8, 12, 4), (40, 16, 4, 12, 4), (32, 48, 4, 12, 4), (0, 16, 4, 12, 4), (16, 48, 4, 12, 4)):
    H.fbox(u, v, w, h, d, (10, 10, 18), grad=0.35, var=5)
    H.ragged(u, v, w, h, d, 3, 0.5)
for x in range(64):
    for y in range(64):
        r_, g_, b_, a_ = H.px[x, y]
        if a_: H.px[x, y] = (r_, g_, b_, 205)
H.img.save(TEX + "hallucination.png")
HE = P(64, 64)
for (a, b) in ((1, 2), (5, 6)):
    for x in range(a, b + 1):
        for y in range(3, 5): HE.put(8 + x, 8 + y, WHITE)
HE.img.save(TEX + "hallucination_eyes.png")

# ============================================================ ITENS 16x16 (arte original)
ITEMS = ROOT + "/src/main/resources/assets/hollowvigil/textures/item/"
os.makedirs(ITEMS, exist_ok=True)

def from_art(rows, palette, size=16):
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0)); pp = im.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in palette: pp[x, y] = palette[ch] + (255,)
    return im

charm = ["................", "......XXXX......", ".....X....X.....", ".....X....X.....", "....RRRRRRRR....",
         "...RRDDDDDDRR...", "..RRDDDWWDDDRR..", "..RDDDWWWWDDDR..", "..RDDDWWWWDDDR..", "..RRDDDWWDDDRR..",
         "...RRDDDDDDRR...", "....RRRRRRRR....", ".....R.RR.R.....", "......R..R......", "................", "................"]
from_art(charm, {'X': (120, 108, 92), 'R': (206, 198, 176), 'D': (22, 22, 30), 'W': (250, 250, 255)}).save(ITEMS + "sanity_charm.png")

heart_rows = ["...XXXX...XXXX..", "..XXXXXX.XXXXXX.", ".XXXXXXXXXXXXXXX", ".XXXXXXXXXXXXXXX", ".XXXXXXXXXXXXXXX",
              "..XXXXXXXXXXXXX.", "...XXXXXXXXXXX..", "....XXXXXXXXX...", ".....XXXXXXX....", "......XXXXX.....",
              ".......XXX......", "........X......."]
heart = ["................"] * 2 + heart_rows
heart += ["................"] * (16 - len(heart))
hi = from_art(heart, {'X': (58, 10, 20)})
hp = hi.load()
for y in range(16):
    for x in range(16):
        if hp[x, y][3]:
            f = 1.0 + 0.5 * max(0, (7 - x - (y - 2) * 0.5)) / 7 - 0.02 * (y - 2)
            hp[x, y] = tuple(max(0, min(255, int(c * f))) for c in hp[x, y][:3]) + (255,)
            if x in (1, 14) or y in (4, 13): hp[x, y] = (30, 4, 10, 255)
for (x, y) in ((8, 4), (7, 5), (8, 6), (7, 7), (8, 8), (8, 9)): hp[x, y] = (240, 232, 236, 255)
for (x, y) in ((9, 5), (6, 6), (9, 7), (7, 8)): hp[x, y] = (150, 40, 60, 255)
hi.save(ITEMS + "hollow_heart.png")


print("itens ok")
