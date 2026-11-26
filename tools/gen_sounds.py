# Sintesis suara manik & lonceng (asli, dibuat dari nol; bukan rekaman dari video).
# Karakter mengikuti hasil analisis video: "ting" ~1,18 kHz per manik, lonceng akord ~E6/G#6/B6/E7 tiap 33.
import numpy as np, wave, os
SR = 22050
OUT = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res', 'raw')

def save(name, x):
    x = x / max(1e-9, np.abs(x).max()) * 0.9
    with wave.open(os.path.join(OUT, name), 'wb') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(SR)
        f.writeframes((x * 32767).astype('<i2').tobytes())

def t_(d): return np.arange(int(SR * d)) / SR

def tick():
    t = t_(0.35)
    body = np.sin(2*np.pi*1180*t) * np.exp(-t/0.11) + 0.35*np.sin(2*np.pi*2360*t) * np.exp(-t/0.05)
    rng = np.random.default_rng(1)
    click = rng.standard_normal(len(t)) * np.exp(-t/0.002) * 0.5      # ketukan manik
    att = np.minimum(1, t/0.002)
    return (body*att + click)

def bell(freqs, amps, decay, dur):
    t = t_(dur); x = np.zeros_like(t)
    for f, a in zip(freqs, amps):
        x += a * np.sin(2*np.pi*f*t) * np.exp(-t/decay)
    return x * np.minimum(1, t/0.003)

save('tick.wav', tick())
save('chime.wav', bell([1318, 1660, 1975, 2636], [0.6, 0.7, 0.9, 1.0], 0.45, 1.8))
save('chime_final.wav', bell([1318, 1660, 1975, 2636, 3320, 3951], [0.6, 0.7, 0.9, 1.0, 0.5, 0.4], 0.9, 3.2))
