"""Play Store feature graphic (1024x500) and framed phone screenshots (1080x1920).
Screenshots: put raw emulator captures (1080 px wide) in tools/store_src/<name>.png and list them in SHOTS."""
import os
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
F = os.path.join(ROOT, 'tools/fonts')
OUT = os.path.join(ROOT, 'store'); os.makedirs(os.path.join(OUT, 'screenshots'), exist_ok=True)
pop = lambda w, s: ImageFont.truetype(os.path.join(F, f'poppins_{w}.ttf'), s)
inter = lambda w, s: ImageFont.truetype(os.path.join(F, f'inter_{w}.ttf'), s)


def gradient(w, h, a, b, diag=True):
    im = Image.new('RGB', (w, h)); px = im.load()
    for y in range(h):
        for x in range(w):
            t = ((x / w) + (y / h)) / 2 if diag else y / h
            px[x, y] = tuple(int(a[k] + (b[k] - a[k]) * t) for k in range(3))
    return im


def rounded(im, r):
    m = Image.new('L', im.size, 0); ImageDraw.Draw(m).rounded_rectangle([0, 0, im.width - 1, im.height - 1], r, fill=255)
    out = im.convert('RGBA'); out.putalpha(m); return out


def shadow(size, r, blur=40, alpha=90):
    s = Image.new('RGBA', (size[0] + blur * 4, size[1] + blur * 4), (0, 0, 0, 0))
    ImageDraw.Draw(s).rounded_rectangle([blur * 2, blur * 2, blur * 2 + size[0], blur * 2 + size[1]], r, fill=(11, 31, 69, alpha))
    return s.filter(ImageFilter.GaussianBlur(blur))


def feature_graphic():
    W, H = 1024, 500
    bg = gradient(W, H, (0x1E, 0x88, 0xE5), (0x0B, 0x2A, 0x5B)).convert('RGBA')
    icon = Image.open(os.path.join(OUT, 'play_icon_512.png')).resize((120, 120), Image.LANCZOS)
    bg.alpha_composite(rounded(icon, 30), (64, 96))
    d = ImageDraw.Draw(bg)
    d.text((64, 236), 'Cooling Care', font=pop('bold', 64), fill='white')
    d.text((66, 322), 'AC & Refrigeration Services · Pasrur', font=inter('medium', 26), fill=(225, 236, 250))
    d.text((66, 372), 'Book in a minute · Confirm on WhatsApp', font=inter('semibold', 24), fill='white')
    hero = Image.open(os.path.join(ROOT, 'tools/images/out/hero_technician.png')).convert('RGBA')
    hero = hero.resize((round(hero.width * 500 / hero.height), 500), Image.LANCZOS)
    hero = hero.crop((hero.width - 470, 0, hero.width, 500))  # keep the technician, leave room for the text
    fade = Image.new('L', hero.size, 255); fp = fade.load()
    for x in range(180):
        for y in range(hero.height): fp[x, y] = int(255 * (x / 180) ** 1.4)
    hero.putalpha(fade)
    bg.alpha_composite(hero, (W - hero.width, 0))
    bg.convert('RGB').save(os.path.join(OUT, 'feature_graphic.png'))


SHOTS = [
    ('home', 'Expert cooling care\nat your doorstep'),
    ('services', '10 services for AC,\nfridge & commercial'),
    ('book', 'Book a visit in\nunder a minute'),
    ('review', 'Your request goes\nstraight to WhatsApp'),
    ('status', 'Track every visit,\nstep by step'),
    ('urdu', 'Fully in English\nand Urdu'),
]


def screenshots():
    src = os.path.join(ROOT, 'tools/store_src')
    for i, (name, cap) in enumerate(SHOTS, 1):
        p = os.path.join(src, name + '.png')
        if not os.path.exists(p): print('skip', name); continue
        W, H = 1080, 1920
        bg = gradient(W, H, (0xEA, 0xF3, 0xFC), (0xD3, 0xE5, 0xF8), diag=False).convert('RGBA')
        d = ImageDraw.Draw(bg)
        y = 90
        for line in cap.split('\n'):
            f = pop('bold', 66)  # PIL here has no complex-script shaping, so captions stay Latin
            w = d.textlength(line, font=f); d.text(((W - w) / 2, y), line, font=f, fill=(11, 31, 69)); y += 86
        shot = Image.open(p).convert('RGB')
        sh = 1440; sw = round(shot.width * sh / shot.height)
        shot = rounded(shot.resize((sw, sh), Image.LANCZOS), 48)
        x0, y0 = (W - sw) // 2, H - sh - 60
        s = shadow((sw, sh), 48); bg.alpha_composite(s, (x0 - 80, y0 - 60))
        frame = Image.new('RGBA', (sw + 24, sh + 24), (0, 0, 0, 0)); ImageDraw.Draw(frame).rounded_rectangle([0, 0, sw + 23, sh + 23], 58, fill=(11, 31, 69, 255))
        bg.alpha_composite(frame, (x0 - 12, y0 - 12)); bg.alpha_composite(shot, (x0, y0))
        bg.convert('RGB').save(os.path.join(OUT, 'screenshots', f'{i:02d}_{name}.png'))
        print('shot', name)


if __name__ == '__main__':
    feature_graphic(); screenshots()
