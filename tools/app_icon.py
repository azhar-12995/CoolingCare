"""Renders the app icon (blue gradient + white Lucide snowflake) for iOS (1024) and the Play Store (512)."""
import json, os, re
from PIL import Image, ImageDraw
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
svg = json.load(open(os.path.join(ROOT, 'tools/figma/icons.json'), encoding='utf8'))['snowflake']

def polylines(d):
    toks = re.findall(r'[MmLlHhVvZz]|-?\d*\.?\d+', d); i = 0; cmd = None; x = y = 0; cur = []; out = []
    while i < len(toks):
        t = toks[i]
        if re.match(r'[A-Za-z]', t): cmd = t; i += 1
        if cmd in 'Zz': cur and out.append(cur + [cur[0]]); cur = []; continue
        if cmd in 'Mm':
            dx, dy = float(toks[i]), float(toks[i + 1]); i += 2
            x, y = (x + dx, y + dy) if cmd == 'm' else (dx, dy)
            if cur: out.append(cur)
            cur = [(x, y)]; cmd = 'l' if cmd == 'm' else 'L'; continue
        if cmd in 'Ll': dx, dy = float(toks[i]), float(toks[i + 1]); i += 2; x, y = (x + dx, y + dy) if cmd == 'l' else (dx, dy)
        elif cmd in 'Hh': v = float(toks[i]); i += 1; x = x + v if cmd == 'h' else v
        elif cmd in 'Vv': v = float(toks[i]); i += 1; y = y + v if cmd == 'v' else v
        cur.append((x, y))
    if cur: out.append(cur)
    return out

def render(size, scale=4, glyph=0.6, stroke=1.7):
    S = size * scale; im = Image.new('RGB', (S, S))
    a, b = (0x0A, 0x66, 0xC8), (0x0B, 0x3D, 0x91)
    px = im.load()
    for yy in range(S):
        for xx in range(0, S, 1):
            t = (xx + yy) / (2 * S); px[xx, yy] = tuple(int(a[k] + (b[k] - a[k]) * t) for k in range(3))
    dr = ImageDraw.Draw(im); k = S * glyph / 24; off = (S - 24 * k) / 2; w = max(1, int(stroke * k))
    for d in re.findall(r'd="([^"]+)"', svg):
        for line in polylines(d):
            pts = [(off + x * k, off + y * k) for x, y in line]
            dr.line(pts, fill='white', width=w, joint='curve')
            for p in (pts[0], pts[-1]): dr.ellipse([p[0] - w / 2, p[1] - w / 2, p[0] + w / 2, p[1] + w / 2], fill='white')
    return im.resize((size, size), Image.LANCZOS)

ios = os.path.join(ROOT, 'iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png')
render(1024).save(ios)
os.makedirs(os.path.join(ROOT, 'store'), exist_ok=True)
render(512).save(os.path.join(ROOT, 'store/play_icon_512.png'))
print('icons written')
