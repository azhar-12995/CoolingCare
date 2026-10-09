"""Builds icons.json (name -> 24px SVG) from lucide-react (ISC) + custom brand glyphs."""
import re, json, os
SRC = r'C:/Users/azharsaddique/Projects/dinehub-admin/node_modules/lucide-react/dist/esm/icons'
NAMES = """map-pin chevron-down chevron-up chevron-left chevron-right bell bell-ring arrow-left arrow-right house wrench calendar
calendar-days calendar-check calendar-clock calendar-x calendar-plus headset phone phone-call tag circle-check check check-check clock settings pencil
info camera image images image-plus x plus minus snowflake shield-check shield user user-round languages globe star share-2
receipt refresh-cw rotate-ccw triangle-alert circle-alert circle-help trash-2 truck fan air-vent refrigerator droplets sparkles
hammer gauge layout-grid copy message-square-text message-circle lock mail clipboard-list list-checks navigation badge-check
thermometer-snowflake spray-can file-text building-2 circle-x circle-dot timer history map ban smartphone send award zap wind
cog heart-handshake signal wifi battery-full search sliders-horizontal square-pen eye external-link circle-user banknote
thumbs-up scroll-text clock-3 store briefcase package settings-2 map-pinned house-plus user-check notebook-pen wallet
calendar-sync bell-off circle-plus layers palette type component route git-branch workflow monitor-smartphone mouse-pointer-click
arrow-down ellipsis-vertical circle star-off hand-coins
layout-dashboard users trending-up eye-off log-out image-up user-x inbox list-filter shield-user key-round circle-pause grip-vertical""".split()
ALIAS = {'circle-help': 'circle-question-mark'}
ATTR = re.compile(r'(\w[\w-]*):\s*"([^"]*)"')
def svg_of(name):
    p = os.path.join(SRC, ALIAS.get(name, name) + '.mjs')
    js = open(p, encoding='utf8').read()
    r = re.search(r"export \{ default \} from './([\w-]+)\.mjs'", js)
    if r: js = open(os.path.join(SRC, r.group(1) + '.mjs'), encoding='utf8').read()
    m = re.search(r'__iconNode\s*=\s*(\[.*?\]);\s*\n', js, re.S) or re.search(r'createLucideIcon\([^,]+,\s*(\[.*\])\s*\)', js, re.S)
    body = m.group(1)
    els = []
    for tag, attrs in re.findall(r'\[\s*"(\w+)",\s*\{([^}]*)\}\s*\]', body):
        a = {k: v for k, v in ATTR.findall(attrs) if k != 'key'}
        els.append('<%s %s/>' % (tag, ' '.join('%s="%s"' % (k, v) for k, v in a.items())))
    return ('<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#0A66C8" '
            'stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' + ''.join(els) + '</svg>')
out, missing = {}, []
for n in NAMES:
    try: out[n] = svg_of(n)
    except Exception as e: missing.append(n)
# custom brand glyph: WhatsApp-style chat bubble + filled handset (drawn for this app)
out['whatsapp'] = ('<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#0A66C8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">'
  '<path d="M3.6 20.4l1.3-4.2A8.6 8.6 0 1 1 8 19.1z"/>'
  '<path fill="#0A66C8" stroke="none" d="M9.3 7.7c.3-.1.6 0 .7.3l.8 1.9c.1.3 0 .6-.2.8l-.6.6c.6 1.2 1.6 2.2 2.8 2.8l.6-.6c.2-.2.5-.3.8-.2l1.9.8c.3.1.4.4.3.7l-.3 1.2c-.1.4-.5.6-.9.6-3.9-.3-7-3.4-7.3-7.3 0-.4.2-.8.6-.9z"/></svg>')
json.dump(out, open('icons.json', 'w'), indent=0)
print(len(out), 'icons; missing:', missing)
