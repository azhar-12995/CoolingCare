"""Cooling Care image pipeline.
src/client_mockup.webp (client's 1536x1024 board) -> upscale.py (Real-ESRGAN x4) -> src/client_mockup_x4.png
This script crops every app image from the x4 board (coords below are in 1x board pixels), fixes the
AI-garbled shirt logo, feathers product tiles so they melt into the tile colour, and writes out/<name>.png.
"""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import numpy as np, cv2, os, json
Image.MAX_IMAGE_PIXELS = None
X4 = Image.open('src/client_mockup_x4.png').convert('RGB')
POP = r'C:/Users/azharsaddique/AndroidStudioProjects/YogaNutrition/composeApp/src/commonMain/composeResources/font/poppins_semibold.ttf'
os.makedirs('out', exist_ok=True)

def crop(b): a, b_, c, d = b; return X4.crop((a * 4, b_ * 4, c * 4, d * 4))

def feather(img, edge=0.16):
    w, h = img.size
    ex, ey = int(w * edge), int(h * edge)
    m = np.ones((h, w), np.float32)
    for i in range(ex): m[:, i] *= i / ex; m[:, w - 1 - i] *= i / ex
    for j in range(ey): m[j, :] *= j / ey; m[h - 1 - j, :] *= j / ey
    m = m ** 0.8
    out = img.convert('RGBA'); out.putalpha(Image.fromarray((m * 255).astype(np.uint8)))
    return out

def relogo(img, box, size, center=None, rot=4, lines=('Cooling', 'Care')):
    """Inpaint white garbled text inside box and write a crisp two-line logo (centre = text centroid if None)."""
    a = np.array(img); x0, y0, x1, y1 = box
    reg = a[y0:y1, x0:x1].astype(int)
    if center is None:
        ys, xs = np.nonzero(reg.sum(2) > 420); center = (x0 + xs.mean(), y0 + ys.mean())
    m = np.zeros(a.shape[:2], np.uint8); m[y0:y1, x0:x1][reg.sum(2) > 330] = 255
    m = cv2.dilate(m, np.ones((5, 5), np.uint8), iterations=2)
    a = cv2.cvtColor(cv2.inpaint(cv2.cvtColor(a, cv2.COLOR_RGB2BGR), m, 9, cv2.INPAINT_TELEA), cv2.COLOR_BGR2RGB)
    out = Image.fromarray(a).convert('RGBA')
    F = ImageFont.truetype(POP, size); W, H = size * 6, int(size * 3.2)
    L = Image.new('RGBA', (W, H), (0, 0, 0, 0)); d = ImageDraw.Draw(L)
    for i, t in enumerate(lines):
        tw = d.textlength(t, font=F); d.text(((W - tw) / 2, size * 0.25 + i * size * 1.2), t, font=F, fill=(255, 255, 255, 235))
    L = L.rotate(rot, resample=Image.BICUBIC).filter(ImageFilter.GaussianBlur(0.4))
    out.alpha_composite(L, (int(center[0] - W / 2), int(center[1] - H / 2)))
    return out.convert('RGB')

SPEC = {
    # category tiles (Home grid) — feathered, shown on tile colour #EEF5FB
    'cat_split_ac':     dict(box=(410, 330, 490, 382), feather=True),
    'cat_refrigerator': dict(box=(498, 322, 582, 381), feather=True),
    'cat_cassette':     dict(box=(588, 335, 672, 378), feather=True),
    'cat_vrf':          dict(box=(410, 408, 492, 456), feather=True),
    'cat_commercial':   dict(box=(498, 408, 582, 458), feather=True),
    'cat_installation': dict(box=(590, 408, 670, 456), feather=True),
    # service thumbnails (Services list)
    'svc_general':  dict(box=(749, 149, 831, 209)),
    'svc_deep':     dict(box=(749, 232, 831, 293)),
    'svc_gas':      dict(box=(749, 316, 831, 378)),
    'svc_install':  dict(box=(749, 407, 831, 470), logo=dict(box=(250, 130, 328, 200), size=11)),
    'svc_annual':   dict(box=(750, 495, 831, 559), logo=dict(box=(250, 120, 324, 200), size=11, center=(296, 128))),
    # hero + detail header
    'hero_technician': dict(box=(553, 150, 683, 257), logo=dict(box=(410, 285, 520, 365), size=24, center=(452, 323))),
    'detail_deep_clean': dict(box=(1086, 89, 1381, 204)),
    # large product shots from the poster (detail headers / welcome)
    'big_split_ac':   dict(box=(30, 325, 295, 465)),
    'big_fridge':     dict(box=(0, 455, 160, 805)),
    'big_cassette':   dict(box=(182, 478, 372, 600)),
    'big_vrf':        dict(box=(160, 588, 332, 800)),
    'welcome_scene':  dict(box=(0, 312, 380, 830)),
    # people
    'tech_portrait':  dict(box=(938, 735, 1028, 831), logo=dict(box=(180, 300, 300, 384), size=17)),
}
meta = {}
for name, s in SPEC.items():
    im = crop(s['box'])
    if 'logo' in s: im = relogo(im, **s['logo'])
    if s.get('feather'): im = feather(im)
    im.save(f'out/{name}.png'); meta[name] = im.size
json.dump(meta, open('out/meta.json', 'w'), indent=1)
print(json.dumps(meta))
