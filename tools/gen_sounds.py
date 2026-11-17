"""Sintetiza os efeitos sonoros originais do Hollow Vigil (mono, 44.1 kHz) e converte para .ogg (Vorbis) com ffmpeg."""
import numpy as np, scipy.signal as sg, scipy.io.wavfile as wf, subprocess, os, json, sys

SR = 44100
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
OUT = ROOT + "/src/main/resources/assets/hollowvigil/sounds/"
os.makedirs(OUT, exist_ok=True)
rng = np.random.default_rng(20261115)

def tt(d): return np.arange(int(d * SR)) / SR
def noise(d): return rng.standard_normal(int(d * SR))
def bp(x, lo, hi, order=2):
    hi = min(hi, SR / 2 - 200); return sg.sosfilt(sg.butter(order, [lo, hi], btype='band', fs=SR, output='sos'), x)
def lp(x, fc, order=2): return sg.sosfilt(sg.butter(order, min(fc, SR / 2 - 200), btype='low', fs=SR, output='sos'), x)
def hp(x, fc, order=2): return sg.sosfilt(sg.butter(order, fc, btype='high', fs=SR, output='sos'), x)
def place(buf, snd, at):
    i = int(at * SR); n = min(len(snd), len(buf) - i)
    if n > 0: buf[i:i + n] += snd[:n]
def swell(n, peak=0.45, power=1.6):
    x = np.linspace(0, 1, n); a = np.where(x < peak, (x / peak) ** power, ((1 - x) / (1 - peak)) ** power); return a
def fade(x, fin=0.01, fout=0.05):
    n = len(x); a = int(fin * SR); b = int(fout * SR)
    if a > 0: x[:a] *= np.linspace(0, 1, a)
    if b > 0: x[-b:] *= np.linspace(1, 0, b)
    return x
def reverb(x, rt=2.0, wet=0.35, damp=5000):
    n = int(rt * SR); t = np.arange(n) / SR
    ir = rng.standard_normal(n) * np.exp(-t * 6.9 / rt)
    ir = lp(ir, damp, 1); ir[:int(0.012 * SR)] *= np.linspace(0, 1, int(0.012 * SR))
    ir /= np.sqrt(np.sum(ir ** 2)) + 1e-9
    y = sg.fftconvolve(x, ir)[:len(x) + n // 2]
    xx = np.concatenate([x, np.zeros(len(y) - len(x))])
    return (1 - wet) * xx + wet * y * 1.2
def norm(x, peak=0.89):
    m = np.max(np.abs(x)) + 1e-9; return x / m * peak
def tail_trim(x, thresh=0.004):
    idx = np.where(np.abs(x) > thresh)[0]
    return x[:idx[-1] + 1] if len(idx) else x
def formant(src, formants):
    y = np.zeros_like(src)
    for f, bw, g in formants: y += g * bp(src, max(60, f - bw), f + bw, 2)
    return y
def softclip(x, k): return np.tanh(k * x) / np.tanh(k)

def save(name, x, peak=0.89):
    x = fade(norm(tail_trim(x), peak), 0.004, 0.06)
    wav = OUT + name + ".wav"
    wf.write(wav, SR, (x * 32767).astype(np.int16))
    subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", wav, "-ac", "1", "-ar", str(SR),
                    "-c:a", "libvorbis", "-q:a", "5", OUT + name + ".ogg"], check=True)
    os.remove(wav)
    print(f"{name}.ogg  {len(x)/SR:4.1f}s")

# ---------------------------------------------------------------- batimento cardíaco
def thump(f0, dur, amp):
    t = tt(dur); f = f0 * (1 + 0.9 * np.exp(-t * 28)); ph = 2 * np.pi * np.cumsum(f) / SR
    return amp * (np.sin(ph) * np.exp(-t * 20) + 0.7 * lp(noise(dur), 170, 2) * np.exp(-t * 55))
def heartbeat(bpm, f0, name):
    period = 60.0 / bpm; buf = np.zeros(int((period + 0.35) * SR))
    place(buf, thump(f0, 0.26, 1.0), 0.0); place(buf, thump(f0 * 0.85, 0.22, 0.68), period * 0.32)
    buf = lp(buf, 260, 2); buf = reverb(buf, 0.5, 0.12, 900); save(name, buf)
heartbeat(58, 60, "heartbeat"); heartbeat(112, 72, "heartbeat_fast")

# ---------------------------------------------------------------- respiração
d = 4.2; buf = np.zeros(int(d * SR))
inh = bp(noise(1.3), 450, 3400) * swell(int(1.3 * SR), 0.7)
inh *= 0.75 + 0.25 * np.sin(2 * np.pi * 33 * tt(1.3))
exh = bp(noise(1.7), 260, 2300) * swell(int(1.7 * SR), 0.3) * 0.8
exh *= 0.8 + 0.2 * np.sin(2 * np.pi * 27 * tt(1.7))
place(buf, inh, 0.2); place(buf, exh, 1.95)
place(buf, 0.04 * np.sin(2 * np.pi * (820 + 60 * np.sin(2 * np.pi * 0.6 * tt(0.9))) * tt(0.9)) * swell(int(0.9 * SR), 0.6), 0.9)
save("breath", reverb(buf, 1.2, 0.25))

# ---------------------------------------------------------------- sussurros (sílabas filtradas, várias vozes)
VOWELS = [(730, 1090), (270, 2290), (300, 870), (530, 1840), (660, 1720), (440, 1020)]
def whisper(dur, seed, voices=3):
    r = np.random.default_rng(seed); out = np.zeros(int(dur * SR))
    for v in range(voices):
        t0 = r.uniform(0.0, 0.18) + v * r.uniform(0.04, 0.14); pos = t0
        while pos < dur - 0.35:
            L = r.uniform(0.09, 0.24); f1, f2 = VOWELS[r.integers(len(VOWELS))]
            n = noise(L + 0.05)
            body = bp(n, f1 - 90, f1 + 90) * 1.0 + bp(n, f2 - 150, f2 + 150) * 0.7
            sib = hp(noise(0.06), 4200) * np.exp(-tt(0.06) * 60) * r.uniform(0.2, 0.8)
            e = swell(len(body), r.uniform(0.25, 0.5), 1.4)
            syl = body * e; syl[:len(sib)] += sib[:len(syl)]
            place(out, syl * r.uniform(0.5, 1.0), pos)
            pos += L + r.uniform(0.03, 0.22) + (0.25 if r.random() < 0.18 else 0)
    return out
for i, (dur, seed) in enumerate(((2.2, 11), (1.8, 23), (2.6, 37)), 1):
    save(f"whisper_{i}", reverb(whisper(dur, seed), 1.8, 0.4, 3500), 0.8)

# ---------------------------------------------------------------- drone
t = tt(7.0); sig = np.zeros_like(t)
for f, a in ((41.2, 1.0), (41.55, 0.75), (61.7, 0.4), (87.3, 0.22), (123.5, 0.1)):
    sig += a * np.sin(2 * np.pi * f * t + 0.8 * np.sin(2 * np.pi * 0.11 * t + f))
sig += 0.35 * lp(noise(7.0), 140, 2) + 0.03 * np.sin(2 * np.pi * 1760 * t) * (0.5 + 0.5 * np.sin(2 * np.pi * 0.7 * t))
sig *= 1 + 0.25 * np.sin(2 * np.pi * 0.37 * t)
sig = fade(sig, 2.2, 2.6)
save("drone", sig, 0.8)

# ---------------------------------------------------------------- stingers
def saw(f, d): return sg.sawtooth(2 * np.pi * f * tt(d))
t = tt(3.2); boom = np.sin(2 * np.pi * np.cumsum(35 + 50 * np.exp(-t * 9)) / SR) * np.exp(-t * 2.4)
burst = lp(noise(3.2), 900, 2) * np.exp(-t * 7)
cl = sum(saw(f, 3.2) * 0.25 for f in (233.1, 246.9, 311.1, 329.6, 415.3)); cl = lp(cl, 2800, 2) * np.exp(-t * 1.5) * (1 - np.exp(-t * 120))
save("stinger_1", reverb(boom * 1.2 + burst * 0.8 + cl * 0.55, 2.6, 0.45, 4500))
t = tt(3.6); buf = np.zeros_like(t)
rev = bp(noise(1.5), 1800, 7500) * np.linspace(0, 1, int(1.5 * SR)) ** 2.4
ris = np.sin(2 * np.pi * np.cumsum(np.linspace(260, 1500, int(1.5 * SR))) / SR) * np.linspace(0, 1, int(1.5 * SR)) ** 2 * 0.3
place(buf, rev * 0.8 + ris, 0.0)
tb = tt(2.1); place(buf, np.sin(2 * np.pi * np.cumsum(30 + 60 * np.exp(-tb * 12)) / SR) * np.exp(-tb * 2.2) * 1.3 + lp(noise(2.1), 700, 2) * np.exp(-tb * 8), 1.5)
save("stinger_2", reverb(buf, 2.4, 0.4, 4000))

# ---------------------------------------------------------------- grito distante (vocal sintetizado)
d = 2.8; t = tt(d); x = np.clip(t / 0.5, 0, 1)
f0 = 380 + 300 * x ** 2 - 120 * np.clip((t - 1.4) / 1.2, 0, 1) + 14 * np.sin(2 * np.pi * 6.2 * t)
ph = 2 * np.pi * np.cumsum(f0) / SR; src = sg.sawtooth(ph) + 0.35 * noise(d) * 0.4
v = formant(src, [(800, 140, 1.0), (1250, 200, 0.7), (2700, 300, 0.35)])
env = swell(len(t), 0.22, 1.2) ** 0.7
v = softclip(v * env * 2.4, 3.0)
save("scream_far", reverb(lp(v, 3600, 2), 3.6, 0.6, 2800), 0.85)

# ---------------------------------------------------------------- arranhado metálico (2 variações)
for i, seed in ((1, 5), (2, 9)):
    r = np.random.default_rng(seed); d = 2.1; t = tt(d); sig = np.zeros_like(t)
    for _ in range(7):
        f = r.uniform(1500, 5200); fm = r.uniform(2, 9); sig += np.sin(2 * np.pi * f * t + r.uniform(1.5, 5) * np.sin(2 * np.pi * fm * t)) * r.uniform(0.2, 0.6)
    am = np.abs(lp(noise(d), 9, 1)); am = am / (am.max() + 1e-9)
    sig = (sig * 0.5 + bp(noise(d), 2500, 8000) * 0.6) * am * swell(len(t), 0.4, 1.0)
    save(f"scrape_{i}", reverb(sig, 1.0, 0.25))

# ---------------------------------------------------------------- estática de rádio
d = 1.0; t = tt(d); g = (np.sin(2 * np.pi * 37 * t) + 0.6 * np.sin(2 * np.pi * 91 * t + 1) > 0.0).astype(float)
n = noise(d) * g; n = np.round(n * 4) / 4
save("static_burst", hp(n, 300) * swell(len(t), 0.1, 1.0) ** 0.5, 0.7)

# ---------------------------------------------------------------- rangido de madeira (2 variações)
for i, seed in ((1, 3), (2, 8)):
    r = np.random.default_rng(seed); d = 1.9; t = tt(d)
    f = np.interp(t, [0, d * 0.4, d * 0.7, d], [70, 105, 85, 130]) * (1 + 0.04 * lp(noise(d), 12, 1) * 8)
    ph = 2 * np.pi * np.cumsum(f) / SR; p = sg.square(ph, duty=0.18) * 0.6 + sg.sawtooth(ph) * 0.4
    y = bp(p, 220, 520) + 0.8 * bp(p, 760, 1100) + 0.3 * bp(p, 1500, 2200)
    j = 0.6 + 0.4 * np.abs(lp(noise(d), 18, 1)) * 6
    save(f"creak_{i}", reverb(y * np.clip(j, 0, 1.4) * swell(len(t), 0.5, 1.1), 0.9, 0.22))

# ---------------------------------------------------------------- batida na madeira (2 variações)
for i, (f1, f2) in ((1, (190, 430)), (2, (165, 380))):
    d = 0.5; t = tt(d)
    y = np.sin(2 * np.pi * f1 * t) * np.exp(-t * 32) + 0.5 * np.sin(2 * np.pi * f2 * t) * np.exp(-t * 48) + lp(noise(d), 1600, 2) * np.exp(-t * 140)
    save(f"knock_{i}", reverb(y, 0.4, 0.15), 0.9)

# ---------------------------------------------------------------- coral angelical (acorde de lá maior)
d = 7.0; t = tt(d); pad = np.zeros_like(t)
def vowel_gain(f): return sum(g * np.exp(-((f - c) / bw) ** 2) for c, bw, g in ((800, 220, 1.0), (1150, 260, 0.8), (2900, 500, 0.4))) + 0.05
for base in (220.0, 277.18, 329.63, 440.0):
    for det in (-0.004, 0.0, 0.004):
        f = base * (1 + det)
        for h in range(1, 12):
            fh = f * h
            if fh > 6000: break
            pad += vowel_gain(fh) / h ** 0.8 * np.sin(2 * np.pi * fh * t + rng.uniform(0, 6.28) + 0.003 * h * np.sin(2 * np.pi * 5.2 * t))
env = swell(len(t), 0.45, 1.5)
pad = pad * env * (1 + 0.08 * np.sin(2 * np.pi * 5.0 * t))
pad += 0.05 * np.sin(2 * np.pi * 3520 * t) * (0.5 + 0.5 * np.sin(2 * np.pi * 0.9 * t)) * env
save("angel_choir", reverb(pad, 3.8, 0.55, 6000), 0.8)

# ---------------------------------------------------------------- sinos
def bell(base, partials, decays, d, amps=None):
    t = tt(d); y = np.zeros_like(t)
    for i, (r, k) in enumerate(zip(partials, decays)):
        a = amps[i] if amps else 1.0 / (1 + i * 0.6)
        y += a * np.sin(2 * np.pi * base * r * t + 0.2 * i) * np.exp(-t * k)
    return y * (1 - np.exp(-t * 400))
save("chime", reverb(bell(880, [1, 2.0, 2.76, 5.4, 8.93], [2.0, 2.6, 3.3, 5.0, 7.0], 3.2), 1.6, 0.3), 0.8)
save("toll", reverb(bell(98, [0.5, 1, 1.19, 1.56, 2.0, 2.74, 4.07], [0.9, 1.1, 1.4, 1.8, 2.3, 3.0, 4.0], 5.0), 2.4, 0.35, 3000), 0.9)

# ---------------------------------------------------------------- raiva do assassino
d = 3.2; t = tt(d); pulse = 0.55 + 0.45 * (np.sin(2 * np.pi * 3.2 * t) > 0)
bass = softclip(sg.sawtooth(2 * np.pi * np.cumsum(55 * (1 + 0.03 * np.sin(2 * np.pi * 0.8 * t))) / SR), 9) * pulse * 0.55
sweep = bp(noise(d), 600, 7000) * np.exp(-t * 1.2) * 0.4
crash = lp(noise(d), 4000, 2) * np.exp(-t * 4.5) * 0.9
sub = np.sin(2 * np.pi * np.cumsum(30 + 40 * np.exp(-t * 6)) / SR) * np.exp(-t * 1.6)
save("rage", reverb(bass * np.exp(-t * 0.6) + sweep + crash + sub, 1.8, 0.35, 4000))

# ---------------------------------------------------------------- respiração de O Oco (chocalho de garganta)
d = 3.4; t = tt(d); r2 = np.random.default_rng(77)
rattle = np.zeros_like(t)
for k in range(5):
    L = r2.uniform(0.25, 0.5); s = bp(noise(L), 220, 800) * swell(int(L * SR), 0.4, 1.3) * (0.7 + 0.3 * np.sin(2 * np.pi * 31 * tt(L)))
    place(rattle, s * r2.uniform(0.5, 1), r2.uniform(0, d - 0.6))
clicks = np.zeros_like(t)
for _ in range(22): clicks[int(r2.uniform(0, d - 0.1) * SR)] = r2.uniform(0.3, 1.0)
clicks = bp(sg.fftconvolve(clicks, np.exp(-tt(0.02) * 250) * np.sin(2 * np.pi * 1700 * tt(0.02)))[:len(t)], 900, 3200) * 0.7
hum = np.sin(2 * np.pi * 88 * t) * (0.5 + 0.5 * np.sin(2 * np.pi * 0.9 * t)) * 0.18
save("hollow_idle", reverb(rattle + clicks + hum, 1.6, 0.3, 3000), 0.8)

# ---------------------------------------------------------------- rugido do Ceifador
d = 3.0; t = tt(d); f = 82 - 38 * np.clip(t / 2.2, 0, 1) + 6 * np.sin(2 * np.pi * 11 * t)
src = sg.sawtooth(2 * np.pi * np.cumsum(f) / SR) + 0.5 * sg.square(2 * np.pi * np.cumsum(f * 0.5) / SR)
g = formant(src, [(480, 100, 1.0), (880, 140, 0.8), (2200, 260, 0.25)]) + 0.5 * lp(noise(d), 500, 2)
g = softclip(g * swell(len(t), 0.18, 1.1) * 1.8, 3.5)
g += 0.6 * np.sin(2 * np.pi * np.cumsum(f * 0.5) / SR) * np.exp(-t * 0.8)
save("reaper_roar", reverb(lp(g, 3800, 2), 2.6, 0.45, 3500), 0.9)

# ---------------------------------------------------------------- jumpscare (screech curto e agressivo)
d = 1.7; t = tt(d)
f = np.interp(t, [0, 0.25, 0.9, d], [900, 2400, 1700, 1200]) + 320 * np.sin(2 * np.pi * 43 * t)
y = softclip(sg.sawtooth(2 * np.pi * np.cumsum(f) / SR) * 0.6 + sg.sawtooth(2 * np.pi * np.cumsum(f * 1.5) / SR) * 0.35, 4.5)
y += hp(noise(d), 2200) * 0.5 + np.sin(2 * np.pi * np.cumsum(48 + 80 * np.exp(-t * 14)) / SR) * np.exp(-t * 3.5) * 1.4
y *= (1 - np.exp(-t * 900)) * np.exp(-t * 1.5)
save("jumpscare", reverb(y, 1.0, 0.25, 5000), 0.95)

# ---------------------------------------------------------------- sounds.json + legendas
events = {
    "heartbeat": ["heartbeat"], "heartbeat_fast": ["heartbeat_fast"], "breath": ["breath"],
    "whisper": ["whisper_1", "whisper_2", "whisper_3"], "drone": ["drone"], "stinger": ["stinger_1", "stinger_2"],
    "scream_far": ["scream_far"], "scrape": ["scrape_1", "scrape_2"], "static_burst": ["static_burst"],
    "creak": ["creak_1", "creak_2"], "knock": ["knock_1", "knock_2"], "angel_choir": ["angel_choir"],
    "chime": ["chime"], "rage": ["rage"], "hollow_idle": ["hollow_idle"], "reaper_roar": ["reaper_roar"],
    "jumpscare": ["jumpscare"], "toll": ["toll"]}
js = {ev: {"subtitle": "subtitles.hollowvigil." + ev, "sounds": [{"name": "hollowvigil:" + s} for s in files]} for ev, files in events.items()}
json.dump(js, open(ROOT + "/src/main/resources/assets/hollowvigil/sounds.json", "w"), indent=2)
json.dump(sorted(events.keys()), open(ROOT + "/tools/_sound_events.json", "w"))
print("eventos:", len(events))
