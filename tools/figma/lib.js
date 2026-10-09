(async () => {
const Z = {};
Z.fontList = [['Poppins','Regular'],['Poppins','Medium'],['Poppins','SemiBold'],['Poppins','Bold'],['Inter','Regular'],['Inter','Medium'],['Inter','Semi Bold'],['Inter','Bold'],
  ['Noto Nastaliq Urdu','Regular'],['Noto Nastaliq Urdu','Medium'],['Noto Nastaliq Urdu','SemiBold'],['Noto Nastaliq Urdu','Bold']];
await Promise.all(Z.fontList.map(([family, style]) => figma.loadFontAsync({family, style})));
Z.lang = 'en'; Z.rtl = false;
Z.setLang = l => { Z.lang = l; Z.rtl = l === 'ur'; };
Z.L = (en, ur) => (Z.lang === 'ur' && ur != null) ? ur : en;
Z.W = 375; Z.H = 812; Z.G_ = 20; // screen size, gutter
Z.rgb = h => { h = h.replace('#',''); return {r: parseInt(h.slice(0,2),16)/255, g: parseInt(h.slice(2,4),16)/255, b: parseInt(h.slice(4,6),16)/255}; };
const vars = await figma.variables.getLocalVariablesAsync('COLOR');
Z.V = {}; for (const v of vars) Z.V[v.name.replace('color/','')] = v;
Z.val = tok => { const v = Z.V[tok]; if (!v) throw new Error('No color var ' + tok); return v.valuesByMode[Object.keys(v.valuesByMode)[0]]; };
Z.hex = tok => { if (tok.startsWith('#')) return tok; const c = Z.val(tok); return '#' + [c.r, c.g, c.b].map(x => Math.round(x * 255).toString(16).padStart(2, '0')).join(''); };
Z.paint = (tok, op) => {
  if (tok && typeof tok === 'object') return tok;
  if (typeof tok === 'function') return tok();
  if (op != null && op !== 1 && !tok.startsWith('#')) { const val = Z.val(tok); return {type:'SOLID', color:{r:val.r, g:val.g, b:val.b}, opacity: op}; }
  let p;
  if (tok.startsWith('#')) p = {type:'SOLID', color: Z.rgb(tok)};
  else { const val = Z.val(tok); p = figma.variables.setBoundVariableForPaint({type:'SOLID', color:{r:val.r, g:val.g, b:val.b}}, 'color', Z.V[tok]); }
  if (op != null && op !== 1) p = Object.assign({}, p, {opacity: op});
  return p;
};
Z.grad = (stops, dir = 'down') => {
  const T = { down: [[0,1,0],[-1,0,1]], up: [[0,-1,1],[1,0,0]], right: [[1,0,0],[0,1,0]], left: [[-1,0,1],[0,-1,1]], diag: [[0.7,0.7,0],[-0.7,0.7,0.5]] }[dir];
  return { type:'GRADIENT_LINEAR', gradientTransform: T,
    gradientStops: stops.map(([c, pos, a]) => { const k = Z.rgb(Z.hex(c)); return {position: pos, color: {r:k.r, g:k.g, b:k.b, a: a == null ? 1 : a}}; }) };
};
Z.G = {
  brand: () => Z.grad([['#0A66C8', 0], ['#0B3D91', 1]], 'diag'),
  hero: () => Z.grad([['#E3F0FD', 0], ['#F6FAFF', 1]], Z.rtl ? 'left' : 'right'),
  splash: () => Z.grad([['#1E88E5', 0], ['#0A66C8', 0.45], ['#0B2A5B', 1]], 'down')
};
const tss = await figma.getLocalTextStylesAsync();
Z.TS = {}; for (const s of tss) Z.TS[s.name] = s;
const effs = await figma.getLocalEffectStylesAsync();
Z.ES = {}; for (const e of effs) Z.ES[e.name] = e;
Z.H_ = (await figma.clientStorage.getAsync('cc_img')) || {};
Z.cmap = {};
const imap = (await figma.clientStorage.getAsync('cc_icons')) || {};
for (const [k, id] of Object.entries(imap)) Z.cmap['Icon/' + k] = id;
Object.assign(Z.cmap, (await figma.clientStorage.getAsync('cc_comp')) || {});
Z.C = {};
await Promise.all(Object.entries(Z.cmap).map(async ([k, id]) => { const n = await figma.getNodeByIdAsync(id); if (n) Z.C[k] = n; }));
Z._txt = []; Z._fill = []; Z._fx = []; Z._abs = [];
Z.isUr = s => { s = String(s); const a = (s.match(/[؀-ۿ]/g) || []).length, l = (s.match(/[A-Za-z]/g) || []).length; return a > 0 && a >= l; };

// ---------- primitives ----------
// Latin text that contains a few Urdu words: give the Urdu runs the Nastaliq font (Figma has no glyph fallback)
Z._fixMixed = t => { const c = t.characters; const re = /[؀-ۿ][؀-ۿ\s]*/g; let m;
  while ((m = re.exec(c))) { const end = m.index + m[0].trimEnd().length; t.setRangeFontName(m.index, end, {family: 'Noto Nastaliq Urdu', style: 'SemiBold'}); } };
Z.t = (str, style = 'Body/M', color = 'text', o = {}) => {
  const ur = Z.isUr(str);
  const sname = ur && Z.TS['UR/' + style] ? 'UR/' + style : style;
  const st = Z.TS[sname]; if (!st) throw new Error('No text style ' + sname);
  const t = figma.createText();
  t.fontName = st.fontName; t.fontSize = o.size || st.fontSize; t.lineHeight = st.lineHeight; t.letterSpacing = st.letterSpacing;
  t.characters = String(str);
  t.fills = [Z.paint(color, o.cop)];
  t.name = o.name || String(str).slice(0, 28);
  if (o.w) { t.resize(o.w, Math.max(1, t.height)); t.textAutoResize = 'HEIGHT'; }
  let al = o.align;
  if (Z.rtl && !o.ltr) al = (!al || al === 'LEFT') ? 'RIGHT' : (al === 'RIGHT' ? 'LEFT' : al);
  if (al) t.textAlignHorizontal = al;
  if (o.lh) t.lineHeight = {unit:'PIXELS', value: o.lh};
  if (o.upper) t.textCase = 'UPPER';
  if (o.strike) t.textDecoration = 'STRIKETHROUGH';
  if (o.under) t.textDecoration = 'UNDERLINE';
  if (o.op != null) t.opacity = o.op;
  if (o.trunc) { t.textTruncation = 'ENDING'; t.maxLines = o.trunc; }
  if (!o.size && !o.lh) Z._txt.push([t, st.id]);
  if (!ur && /[؀-ۿ]/.test(t.characters)) { Z._mixed = Z._mixed || []; Z._mixed.push(t); if (o.size || o.lh) Z._fixMixed(t); }
  if (o.to) Z.link(t, o.to);
  return t;
};
Z.fw = n => ({n, fw: 1});
Z.fh = n => ({n, fh: 1});
Z.fwh = n => ({n, fw: 1, fh: 1});
Z.abs = (n, x, y, o = {}) => ({n, abs: 1, x, y, cons: o.cons, ltr: o.ltr});
Z.put = (p, k) => {
  if (!k) return null;
  const n = k.n || k;
  p.appendChild(n);
  if (k.n) {
    if (k.abs) { n.layoutPositioning = 'ABSOLUTE'; Z._abs.push([n, p, k.x, k.y, !!k.ltr]); n.x = k.x; n.y = k.y; if (k.cons) n.constraints = k.cons; }
    if (k.fw || k.fh) Z._fill.push([n, !!k.fw, !!k.fh]);
  }
  return n;
};
Z.al = (dir, o = {}, kids = []) => {
  const rtl = Z.rtl && !o.ltr;
  const f = figma.createFrame(); f.layoutMode = dir === 'H' ? 'HORIZONTAL' : 'VERTICAL'; f.primaryAxisSizingMode = 'AUTO'; f.counterAxisSizingMode = 'AUTO';
  f.name = o.name || (dir === 'H' ? 'row' : 'col');
  f.fills = o.fill ? (Array.isArray(o.fill) ? o.fill.map(x => Z.paint(x)) : [Z.paint(o.fill, o.fop)]) : [];
  f.itemSpacing = o.gap == null ? 0 : o.gap;
  const p = o.pad == null ? 0 : o.pad;
  let [pt, pr, pb, pl] = typeof p === 'number' ? [p,p,p,p] : (p.length === 2 ? [p[0],p[1],p[0],p[1]] : p);
  if (rtl) [pr, pl] = [pl, pr];
  f.paddingTop = pt; f.paddingRight = pr; f.paddingBottom = pb; f.paddingLeft = pl;
  if (o.r != null) { if (Array.isArray(o.r)) { let [tl, tr, br, bl] = o.r; if (rtl) [tl, tr, br, bl] = [tr, tl, bl, br]; f.topLeftRadius = tl; f.topRightRadius = tr; f.bottomRightRadius = br; f.bottomLeftRadius = bl; } else f.cornerRadius = o.r; }
  if (o.stroke) { f.strokes = [Z.paint(o.stroke, o.sop)]; f.strokeWeight = o.sw == null ? 1 : o.sw; f.strokeAlign = 'INSIDE'; if (o.dash) f.dashPattern = o.dash; }
  let main = o.main, cross = o.cross;
  if (rtl && dir === 'H') { kids = [...kids].reverse(); main = (!main || main === 'MIN') ? 'MAX' : (main === 'MAX' ? 'MIN' : main); }
  if (rtl && dir === 'V') cross = (!cross || cross === 'MIN') ? 'MAX' : (cross === 'MAX' ? 'MIN' : cross);
  if (main) f.primaryAxisAlignItems = main;
  if (cross) f.counterAxisAlignItems = cross;
  if (o.wrap) { f.layoutWrap = 'WRAP'; f.counterAxisSpacing = o.wgap == null ? f.itemSpacing : o.wgap; }
  if (o.w != null || o.h != null) {
    f.resize(o.w == null ? Math.max(1, f.width) : o.w, o.h == null ? Math.max(1, f.height) : o.h);
    if (o.w == null) f.layoutSizingHorizontal = 'HUG';
    if (o.h == null) f.layoutSizingVertical = 'HUG';
  }
  f.clipsContent = !!o.clip;
  if (o.op != null) f.opacity = o.op;
  if (o.fx) Z._fx.push([f, o.fx]);
  for (const k of kids) Z.put(f, k);
  if (o.to) Z.link(f, o.to);
  return f;
};
Z.row = (o, kids) => Z.al('H', o, kids);
Z.col = (o, kids) => Z.al('V', o, kids);
Z.box = (w, h, o = {}) => {
  const f = figma.createFrame(); f.name = o.name || 'box'; f.resize(w, h);
  f.fills = o.fill ? (Array.isArray(o.fill) ? o.fill.map(x => Z.paint(x)) : [Z.paint(o.fill, o.fop)]) : [];
  if (o.r != null) f.cornerRadius = o.r;
  if (o.stroke) { f.strokes = [Z.paint(o.stroke, o.sop)]; f.strokeWeight = o.sw == null ? 1 : o.sw; f.strokeAlign = 'INSIDE'; if (o.dash) f.dashPattern = o.dash; }
  f.clipsContent = o.clip !== false;
  if (o.fx) Z._fx.push([f, o.fx]);
  for (const k of (o.kids || [])) { const n = k.n || k; f.appendChild(n); if (k.n) { n.x = (Z.rtl && !k.ltr) ? w - (k.x || 0) - n.width : (k.x || 0); n.y = k.y || 0; } }
  if (o.to) Z.link(f, o.to);
  return f;
};
Z.at = (n, x, y, o = {}) => ({n, x, y, ltr: o.ltr});
Z.rect = (w, h, fill = 'primary-soft', r = 0, o = {}) => {
  const n = figma.createRectangle(); n.resize(w, h); n.cornerRadius = r; n.fills = fill ? [Z.paint(fill, o.op)] : []; n.name = o.name || 'rect';
  if (o.stroke) { n.strokes = [Z.paint(o.stroke)]; n.strokeWeight = o.sw || 1; }
  return n;
};
Z.dot = (size, fill, o = {}) => { const e = figma.createEllipse(); e.resize(size, size); e.fills = fill ? [Z.paint(fill, o.op)] : []; e.name = o.name || 'dot'; if (o.stroke) { e.strokes = [Z.paint(o.stroke)]; e.strokeWeight = o.sw || 2; e.strokeAlign = o.salign || 'INSIDE'; } return e; };
Z.imgPaint = (key, mode = 'FILL') => { const h = Z.H_[key]; if (!h) throw new Error('No image ' + key); return {type:'IMAGE', imageHash: h.hash, scaleMode: mode}; };
// photo frame; o.top/o.left (0..1) = focus point, o.zoom >= 1
Z.img = (key, w, h, r = 12, o = {}) => {
  const f = figma.createFrame(); f.name = 'img/' + key; f.resize(w, h); if (Array.isArray(r)) { f.topLeftRadius = r[0]; f.topRightRadius = r[1]; f.bottomRightRadius = r[2]; f.bottomLeftRadius = r[3]; } else f.cornerRadius = r; f.clipsContent = true;
  let fills = [Z.imgPaint(key, o.fit ? 'FIT' : 'FILL')];
  if (o.top != null || o.left != null || o.zoom) { const d = Z.H_[key]; const sc = Math.max(w / d.w, h / d.h) * (o.zoom || 1); const fw = w / (d.w * sc), fh = h / (d.h * sc);
    fills = [{type: 'IMAGE', imageHash: d.hash, scaleMode: 'CROP', imageTransform: [[fw, 0, (1 - fw) * (o.left == null ? 0.5 : o.left)], [0, fh, (1 - fh) * (o.top == null ? 0.5 : o.top)]]}]; }
  if (o.bg) fills.unshift(Z.paint(o.bg));
  if (o.shade) fills.push(Z.grad([['#0B1F45', 0.4, 0], ['#0B1F45', 1, o.shade]]));
  f.fills = fills;
  if (o.stroke) { f.strokes = [Z.paint(o.stroke)]; f.strokeWeight = o.sw || 1; f.strokeAlign = 'INSIDE'; }
  if (o.to) Z.link(f, o.to);
  return f;
};
Z.MIRROR = {'arrow-left': 'arrow-right', 'arrow-right': 'arrow-left', 'chevron-left': 'chevron-right', 'chevron-right': 'chevron-left'};
Z.ic = (name, size = 20, color = 'primary', o = {}) => {
  if (Z.rtl && !o.ltr && Z.MIRROR[name]) name = Z.MIRROR[name];
  const c = Z.C['Icon/' + name]; if (!c) throw new Error('No icon ' + name);
  const i = c.createInstance(); i.name = name;
  if (size !== 24) i.rescale(size / 24);
  if (color !== 'primary' || o.solid) Z.recolor(i, color, o.solid);
  if (o.sw) for (const v of i.findAll(n => 'strokeWeight' in n && n.strokes && n.strokes.length)) v.strokeWeight = o.sw;
  if (o.to) Z.link(i, o.to);
  return i;
};
Z.recolor = (node, color, solid) => {
  const p = Z.paint(color);
  for (const v of node.findAll(n => ['VECTOR','ELLIPSE','LINE','BOOLEAN_OPERATION','POLYGON','STAR','RECTANGLE'].includes(n.type))) {
    if (v.strokes && v.strokes.length) v.strokes = [p];
    if (solid) v.fills = [p]; else if (v.fills && v.fills.length && v.fills[0].type === 'SOLID') v.fills = [p];
  }
};
Z.link = (n, to) => { (n.n || n).setPluginData('to', String(to)); return n; };

// ---------- app chrome ----------
Z.statusBar = (light) => {
  const col = light ? 'on-primary' : 'text';
  return Z.fw(Z.row({name: 'Status Bar', h: 44, pad: [0, 26, 0, 30], main: 'SPACE_BETWEEN', cross: 'CENTER', ltr: true}, [
    Z.t('9:41', 'Label/M', col, {name: 'Time', ltr: true}),
    Z.row({gap: 5, cross: 'CENTER', name: 'Indicators', ltr: true}, [Z.ic('signal', 16, col, {ltr: true}), Z.ic('wifi', 16, col, {ltr: true}), Z.ic('battery-full', 22, col, {ltr: true})])]));
};
Z.homeBar = (light) => Z.fw(Z.row({name: 'Home Indicator', h: 22, main: 'CENTER', cross: 'CENTER', ltr: true}, [Z.rect(134, 5, light ? 'on-primary' : 'text', 3)]));
Z.screen = (id, title, o = {}) => {
  const key = (Z.lang === 'ur' ? 'u' : '') + id;
  let s = Z.reuse && Z.reuse[key];
  if (s) { for (const c of [...s.children]) c.remove(); s.description = ''; } else s = figma.createComponent();
  if (Z.page && s.parent !== Z.page) Z.page.appendChild(s);
  s.name = id + ' · ' + title; s.setPluginData('screen', id); s.setPluginData('lang', Z.lang); s.setPluginData('title', title);
  s.setPluginData('origin', o.origin || 'added'); s.setPluginData('flow', o.flow || '');
  s.layoutMode = 'VERTICAL'; s.resize(Z.W, o.h || Z.H); s.primaryAxisSizingMode = 'FIXED'; s.counterAxisSizingMode = 'FIXED';
  s.itemSpacing = 0; s.clipsContent = true; s.cornerRadius = 36;
  s.fills = o.fill ? (Array.isArray(o.fill) ? o.fill.map(x => Z.paint(x)) : [Z.paint(o.fill)]) : [Z.paint('bg')];
  if (o.desc) s.description = o.desc;
  if (!o.noStatus) Z.put(s, Z.statusBar(o.light));
  return s;
};
Z.iconBtn = (icon = 'arrow-left', style = 'Plain', o = {}) => {
  const fill = {Plain: null, Soft: 'primary-tint', Primary: 'primary', Glass: 'surface', Outline: null, Grey: 'surface-2'}[style];
  const col = o.color || (style === 'Primary' ? 'on-primary' : style === 'Soft' ? 'primary' : 'text');
  const b = Z.row({name: 'btn/' + icon, w: o.size || 40, h: o.size || 40, r: o.r == null ? (o.size || 40) / 2 : o.r, main: 'CENTER', cross: 'CENTER', fill, fop: style === 'Glass' ? 0.94 : 1,
    stroke: style === 'Outline' ? 'border' : null, fx: style === 'Glass' ? 'Shadow/Card' : null, to: o.to}, [Z.ic(icon, o.isz || 22, col)]);
  if (o.badge) { const d = Z.dot(9, 'orange', {stroke: 'surface', sw: 2, salign: 'OUTSIDE'}); Z.put(b, Z.abs(d, (o.size || 40) - 13, 9)); }
  return b;
};
Z.appBar = (title, o = {}) => {
  const kids = [];
  if (o.back !== false) kids.push(Z.iconBtn(o.backIcon || 'arrow-left', o.backStyle || 'Plain', {to: o.backTo || 'back'}));
  const tcol = Z.col({gap: 0, name: 'title', cross: o.center ? 'CENTER' : 'MIN'}, [Z.t(title, o.big ? 'Title/L' : 'Title/M', 'text')].concat(o.sub ? [Z.t(o.sub, 'Body/S', 'text-2')] : []));
  kids.push(Z.fw(tcol));
  for (const a of (o.actions || [])) kids.push(typeof a === 'string' ? Z.iconBtn(a, 'Plain', {color: 'text'}) : a);
  if (o.center && !(o.actions || []).length) kids.push(Z.box(40, 40, {name: 'spacer'}));
  return Z.fw(Z.row({name: 'App Bar', pad: [2, 12, 4, o.back === false ? Z.G_ : 8], gap: 6, cross: 'CENTER', h: o.h || 54}, kids));
};
Z.body = (kids, o = {}) => Z.fwh(Z.col({name: 'Body', pad: o.pad || [6, Z.G_, 16, Z.G_], gap: o.gap == null ? 16 : o.gap, clip: true, fill: o.fill, cross: o.cross, main: o.main}, kids));
Z.footer = (kids, o = {}) => Z.fw(Z.col({name: 'Footer', pad: o.pad || [12, Z.G_, 8, Z.G_], gap: 10, fill: o.fill === undefined ? 'surface' : o.fill, fx: o.fill === null || o.flat ? null : 'Shadow/Nav'}, [
  ...kids.map(k => k && k.n ? k : Z.fw(k)), o.noHome ? null : Z.homeBar()]));
Z.NAV = [['home', 'house', '10'], ['services', 'layout-grid', '20'], ['bookings', 'calendar-days', '50'], ['support', 'headset', '52']];
Z.NAVL = {home: ['Home', 'ہوم'], services: ['Services', 'سروسز'], bookings: ['Bookings', 'بکنگز'], support: ['Support', 'مدد']};
Z.nav = (active = 'home') => {
  const kids = Z.NAV.map(([k, icon, to]) => {
    const on = k === active; const col = on ? 'primary' : 'text-3';
    return Z.col({name: 'nav/' + k, w: 72, gap: 3, cross: 'CENTER', pad: [6, 0, 0, 0], to}, [
      Z.row({w: 52, h: 28, r: 14, fill: on ? 'primary-tint' : null, main: 'CENTER', cross: 'CENTER'}, [Z.ic(icon, 21, col)]),
      Z.t(Z.L(...Z.NAVL[k]), on ? 'Label/S' : 'Caption', col)]);
  });
  return Z.fw(Z.col({name: 'Bottom Nav', fill: 'surface', fx: 'Shadow/Nav'}, [Z.fw(Z.row({pad: [4, 14, 0, 14], main: 'SPACE_BETWEEN', cross: 'MIN'}, kids)), Z.homeBar()]));
};
// buttons: Primary | Secondary | Soft | Light | Ghost | Green | GreenOutline | Orange | Danger | DangerSoft
Z.BTN = {
  Primary: ['primary', null, 'on-primary', 'Shadow/Button'], Secondary: [null, 'primary', 'primary'], Soft: ['primary-tint', null, 'primary'], Light: ['surface', 'border', 'text'],
  Ghost: [null, null, 'primary'], Green: ['green', null, 'on-primary'], GreenOutline: ['surface', 'green', 'green'], Orange: ['orange', null, 'on-primary'],
  Danger: ['danger', null, 'on-primary'], DangerSoft: ['danger-tint', null, 'danger'], White: ['surface', null, 'primary']
};
Z.btn = (label, variant = 'Primary', o = {}) => {
  const [fill, stroke, col, fx] = Z.BTN[variant];
  const kids = [];
  if (o.lead) kids.push(Z.ic(o.lead, o.isz || 20, o.leadColor || col, {solid: o.leadSolid}));
  const tc = [Z.t(label, o.small ? 'Label/M' : 'Button', col)];
  if (o.sub) tc.push(Z.t(o.sub, 'Caption', col, {op: 0.9}));
  kids.push(o.sub ? Z.col({gap: 0}, tc) : tc[0]);
  if (o.arrow) kids.push(Z.ic('arrow-right', 18, col));
  if (o.trail) kids.push(Z.ic(o.trail, 18, col));
  return Z.row({name: 'Button/' + label, h: o.h || (o.small ? 38 : 50), pad: [0, o.px || 20], gap: o.gap || 8, main: o.main || 'CENTER', cross: 'CENTER', r: o.r || 12, fill, stroke, sw: 1.5, fx: o.flat ? null : fx, to: o.to}, kids);
};
Z.chip = (label, on, o = {}) => Z.row({name: 'chip/' + label, h: o.h || 36, pad: [0, o.px || 16], r: 999, main: 'CENTER', cross: 'CENTER', gap: 6, fill: on ? 'primary' : (o.fill || 'primary-tint'),
  stroke: on ? null : (o.stroke || null), fx: on && !o.flat ? 'Shadow/Button' : null, to: o.to}, [o.icon ? Z.ic(o.icon, 15, on ? 'on-primary' : 'primary') : null, Z.t(label, 'Label/M', on ? 'on-primary' : (o.color || 'text'))]);
Z.sec = (title, action = null, o = {}) => Z.fw(Z.row({name: 'Section Header', cross: 'CENTER', gap: 8}, [Z.fw(Z.t(title, o.style || 'Title/M', 'text')), action ? Z.t(action, 'Label/M', 'primary', {to: o.to}) : null]));
Z.TONES = { blue: ['primary-tint', 'primary'], green: ['green-tint', 'green'], orange: ['orange-tint', 'orange'], red: ['danger-tint', 'danger'], amber: ['warning-tint', 'warning'], grey: ['surface-2', 'text-2'], dark: ['primary', 'on-primary'] };
Z.iconTile = (icon, tone = 'blue', size = 40, isz, o = {}) => {
  const [bg, fg] = Z.TONES[tone];
  return Z.row({name: 'icon-tile', w: size, h: size, r: o.r == null ? size / 2 : o.r, fill: o.fill || bg, main: 'CENTER', cross: 'CENTER', to: o.to}, [Z.ic(icon, isz || Math.round(size * 0.5), o.color || fg, {solid: o.solid})]);
};
Z.pill = (text, o = {}) => Z.row({name: 'pill', pad: o.pad || [4, 10], r: o.r || 999, gap: 5, cross: 'CENTER', fill: o.fill || 'primary-tint', stroke: o.stroke, to: o.to}, [
  o.icon ? Z.ic(o.icon, o.isz || 13, o.color || 'primary') : null, Z.t(text, o.style || 'Label/S', o.color || 'primary')]);
Z.card = (kids, o = {}) => Z.col(Object.assign({name: 'card', fill: 'surface', r: 16, pad: 14, gap: 10, stroke: 'border', fx: 'Shadow/Card'}, o), kids);
Z.radio = on => Z.row({name: 'radio', w: 22, h: 22, r: 11, main: 'CENTER', cross: 'CENTER', fill: 'surface', stroke: on ? 'primary' : 'placeholder', sw: on ? 2 : 1.5}, on ? [Z.dot(11, 'primary')] : []);
Z.check = on => Z.row({name: 'check', w: 22, h: 22, r: 6, main: 'CENTER', cross: 'CENTER', fill: on ? 'primary' : 'surface', stroke: on ? null : 'placeholder', sw: 1.5}, on ? [Z.ic('check', 15, 'on-primary', {sw: 3})] : []);
Z.toggle = on => Z.row({name: 'toggle', w: 46, h: 28, r: 14, pad: 3, cross: 'CENTER', main: on ? 'MAX' : 'MIN', fill: on ? 'primary' : 'border', ltr: !Z.rtl ? true : false}, [Z.dot(22, 'surface', {name: 'knob'})]);
Z.divider = (w = 335) => Z.rect(w, 1, 'divider', 0, {name: 'divider'});
Z.field = (label, value, o = {}) => Z.fw(Z.col({name: 'field/' + label, gap: 6}, [
  label ? Z.row({gap: 4, cross: 'CENTER'}, [Z.t(label, 'Label/M', 'text'), o.opt ? Z.t(o.opt, 'Caption', 'text-3') : null]) : null,
  Z.fw(Z.row({name: 'input', h: o.h || 46, r: 12, fill: o.fill || 'surface', stroke: o.error ? 'danger' : o.focus ? 'primary' : 'border', sw: o.focus || o.error ? 1.5 : 1, pad: o.multi ? [11, 14] : [0, 12, 0, 14], gap: 10, cross: o.multi ? 'MIN' : 'CENTER', to: o.to}, [
    o.icon ? Z.ic(o.icon, 18, o.focus ? 'primary' : 'text-2') : null,
    Z.fw(Z.t(value, 'Body/M', o.ph ? 'placeholder' : 'text', {trunc: o.multi ? 3 : 1})),
    o.trail ? (typeof o.trail === 'string' ? Z.ic(o.trail, 18, o.trailColor || 'text-2') : o.trail) : null])),
  o.error ? Z.row({gap: 5, cross: 'CENTER'}, [Z.ic('circle-alert', 14, 'danger'), Z.t(o.error, 'Caption', 'danger')]) : (o.help ? Z.t(o.help, 'Caption', 'text-3') : null)]));
Z.note = (icon, text, tone = 'blue', o = {}) => Z.fw(Z.row({name: 'note', pad: o.pad || [12, 14], gap: 10, r: 14, fill: o.fill || Z.TONES[tone][0], cross: o.cross || 'CENTER', to: o.to, stroke: o.stroke}, [
  Z.ic(icon, 20, Z.TONES[tone][1]), Z.fw(Z.col({gap: 2}, [o.title ? Z.fw(Z.t(o.title, 'Title/S', o.tcolor || 'text')) : null, Z.fw(Z.t(text, o.style || 'Body/S', o.color || (o.title ? 'text-2' : 'text')))])), o.right || null]));
Z.kv = (icon, k, v, o = {}) => Z.fw(Z.row({name: 'kv/' + k, gap: 12, cross: 'CENTER', pad: o.pad || [8, 0], to: o.to}, [
  Z.ic(icon, 20, o.icol || 'text-2'), Z.fw(Z.t(k, o.kstyle || 'Body/M', o.kcol || 'text-2')), v ? Z.t(v, o.vstyle || 'Title/S', o.vcol || 'text') : null, o.chevron ? Z.ic('chevron-right', 18, 'text-3') : null]));
Z.info = (icon, text, o = {}) => Z.fw(Z.row({name: 'info/' + text, gap: 10, cross: 'CENTER', pad: o.pad || [4, 0]}, [Z.ic(icon, 18, o.icol || 'text-2'), Z.fw(Z.t(text, o.style || 'Body/M', o.color || 'text'))]));
Z.listRow = (o) => {
  const kids = [];
  if (o.lead) kids.push(o.lead);
  if (o.icon) kids.push(Z.iconTile(o.icon, o.tone || 'blue', o.isz || 40, null, {r: o.ir == null ? 12 : o.ir}));
  const tc = [Z.fw(Z.t(o.title, o.tstyle || 'Title/S', o.tcol || 'text'))];
  if (o.sub) tc.push(Z.fw(Z.t(o.sub, o.sstyle || 'Body/S', 'text-2')));
  kids.push(Z.fw(Z.col({gap: 2, name: 'text'}, tc)));
  if (o.right) kids.push(typeof o.right === 'string' ? Z.t(o.right, o.rstyle || 'Label/M', o.rcolor || 'text-2') : o.right);
  if (o.chevron) kids.push(Z.ic('chevron-right', 18, o.chevColor || 'text-3'));
  return Z.fw(Z.row({name: 'row/' + o.title, gap: 12, cross: 'CENTER', pad: o.pad || [12, 0], to: o.to, fill: o.fill, r: o.r, stroke: o.stroke, fx: o.fx}, kids));
};
Z.sheet = (kids, o = {}) => Z.col({name: o.name || 'Sheet', w: o.w || Z.W, fill: 'surface', r: o.r || [26, 26, 0, 0], pad: o.pad || [10, Z.G_, 0, Z.G_], gap: o.gap == null ? 14 : o.gap, fx: 'Shadow/Float', cross: 'MIN'}, [
  o.grabber === false ? null : Z.fw(Z.row({main: 'CENTER', name: 'grabber'}, [Z.rect(40, 5, 'border', 3)])), ...kids, o.home === false ? null : Z.homeBar()]);
// overlay a sheet/dialog on top of another screen's instance; call after Z.done
Z.overlay = (s, bgId, panel, o = {}) => { const bg = Z.S[bgId].createInstance(); Z.put(s, Z.abs(bg, 0, 0, {ltr: true})); Z.scrim(s, o.scrim); Z.put(s, Z.abs(panel, 0, 0, {ltr: true})); return panel; };
Z.dock = (panel, where = 'bottom') => { if (where === 'bottom') { panel.x = (Z.W - panel.width) / 2; panel.y = Z.H - panel.height; } else { panel.x = (Z.W - panel.width) / 2; panel.y = (Z.H - panel.height) / 2; } };
Z.scrim = (s, op = 0.45) => { const r = Z.rect(Z.W, s.height, '#0B1F45', 0, {name: 'scrim', op}); Z.put(s, Z.abs(r, 0, 0, {ltr: true})); return r; };
Z.place = (s, x, y) => { s.x = x; s.y = y; return s; };
Z.stepper = (labels, done, o = {}) => { // done = number of completed steps (active = done-1)
  const n = labels.length;
  const dots = [];
  labels.forEach((l, i) => {
    const ok = i < done;
    const dot = ok ? Z.row({w: 26, h: 26, r: 13, fill: o.color || 'primary', main: 'CENTER', cross: 'CENTER'}, [Z.ic('check', 15, 'on-primary', {sw: 3})])
                   : Z.row({w: 26, h: 26, r: 13, fill: 'surface', stroke: 'border', sw: 2, main: 'CENTER', cross: 'CENTER'}, [Z.dot(8, i === done ? (o.color || 'primary') : 'border')]);
    dots.push(Z.fw(Z.col({name: 'step/' + l, gap: 8, cross: 'CENTER'}, [dot, Z.t(l, ok ? 'Label/S' : 'Caption', ok ? 'text' : 'text-3', {align: 'CENTER'})])));
  });
  const node = Z.row({name: 'Stepper', gap: 0, cross: 'MIN'}, dots); const f = Z.fw(node);
  // connector lines drawn after layout in Z.done via _late
  Z._late = Z._late || []; Z._late.push(() => {
    const W = node.width, seg = W / n;
    for (let i = 0; i < n - 1; i++) {
      const vis = Z.rtl ? n - 2 - i : i;
      const ln = Z.rect(seg - 34, 3, i + 1 < done ? (o.color || 'primary') : 'border', 2, {name: 'line'});
      node.insertChild(0, ln); ln.layoutPositioning = 'ABSOLUTE'; ln.x = seg * (vis + 0.5) + 17; ln.y = 11.5;
    }
  });
  return f;
};

// sample booking used across screens
Z.bk = () => { const L = Z.L; return {
  svc: L('AC Deep Cleaning', 'اے سی ڈیپ کلیننگ'), app: L('Split AC', 'اسپلٹ اے سی'), date: L('Thu, 08 Oct 2026', 'جمعرات، 08 اکتوبر 2026'), dateShort: L('Thu, 08 Oct', 'جمعرات، 08 اکتوبر'),
  slot: L('10 AM – 12 PM', 'صبح 10 تا دوپہر 12'), addr: L('House No. 12, Main Street, Pasrur', 'مکان نمبر 12، مین اسٹریٹ، پسرور'), name: L('Ali Raza', 'علی رضا'), phone: '0300 1234567', id: 'CC-1004',
  issue: L('Not cooling well, unusual noise', 'ٹھنڈک کم ہے، عجیب سی آواز آتی ہے'),
  past: L('AC General Service', 'اے سی جنرل سروس'), pastDate: L('12 Sep 2026', '12 ستمبر 2026') }; };
Z.kvFill = (icon, k, v, o = {}) => Z.fw(Z.row({name: 'kv/' + k, gap: 12, cross: 'CENTER', pad: o.pad || [8, 0]}, [Z.ic(icon, 20, 'text-2'), Z.t(k, 'Body/M', 'text-2'), Z.fw(Z.t(v, o.vstyle || 'Body/S', o.vcol || 'text-2', {align: 'RIGHT'}))]));
Z.tabs = (labels, active = 0, o = {}) => Z.fw(Z.row({name: 'Tabs', fill: 'tile', r: 14, pad: 4, gap: 4}, labels.map((l, i) => Z.fw(Z.col({name: 'tab/' + l, h: 40, r: 11, fill: i === active ? 'surface' : null, fx: i === active ? 'Shadow/Card' : null, main: 'CENTER', cross: 'CENTER', to: o.to && o.to[i]},
  [Z.t(l, i === active ? 'Label/M' : 'Label/M', i === active ? 'primary' : 'text-2')])))));
Z.badge = (text, tone = 'green') => Z.pill(text, {fill: Z.TONES[tone][0], color: tone === 'green' ? 'green-text' : Z.TONES[tone][1], icon: tone === 'green' ? 'circle-check' : null, isz: 12, pad: [3, 8]});
Z.thumb = (key, size = 64, o = {}) => Z.img(key, o.w || size, o.h || size, o.r == null ? 12 : o.r, Object.assign(key.startsWith('cat_') ? {fit: true, bg: 'tile'} : {}, o));

// brand mark: gradient squircle + snowflake
Z.logo = (size = 56, o = {}) => {
  const f = Z.row({name: 'Logo Mark', w: size, h: size, r: Math.round(size * 0.3), main: 'CENTER', cross: 'CENTER', fx: o.fx === undefined ? 'Shadow/Button' : o.fx, ltr: true}, [Z.ic('snowflake', Math.round(size * 0.6), o.fg || 'on-primary', {ltr: true, sw: o.sw || 2})]);
  f.fills = o.fill ? [Z.paint(o.fill, o.fop)] : [Z.G.brand()];
  return f;
};
Z.wordmark = (o = {}) => Z.col({name: 'Wordmark', gap: o.gap == null ? 2 : o.gap, cross: o.center ? 'CENTER' : 'MIN', ltr: !!o.center}, [
  Z.t('Cooling Care', o.style || 'Logo/M', o.color || 'navy', {ltr: true, align: o.center ? 'CENTER' : null}),
  o.sub === false ? null : Z.t(Z.L('AC & Refrigeration Services', 'اے سی اور ریفریجریشن سروسز'), o.subStyle || 'Body/S', o.subColor || 'text-2', {align: o.center ? 'CENTER' : null})]);

// ---------- finalize ----------
Z.done = async (roots = []) => {
  for (let i = Z._fill.length - 1; i >= 0; i--) {
    const [n, fw, fh] = Z._fill[i];
    if (fw) { n.layoutSizingHorizontal = 'FILL'; if (n.type === 'TEXT') n.textAutoResize = 'HEIGHT'; }
    if (fh) n.layoutSizingVertical = 'FILL';
  }
  await Promise.all(Z._txt.map(([t, id]) => t.setTextStyleIdAsync(id)));
  for (const t of (Z._mixed || [])) Z._fixMixed(t); Z._mixed = [];
  await Promise.all(Z._fx.map(([f, name]) => Z.ES[name] ? f.setEffectStyleIdAsync(Z.ES[name].id) : null));
  for (const [n, p, x, y, ltr] of Z._abs) { if (Z.rtl && !ltr) n.x = p.width - x - n.width; }
  for (const fn of (Z._late || [])) fn();
  Z._fill = []; Z._txt = []; Z._fx = []; Z._abs = []; Z._late = [];
  return roots.map(r => r.id);
};
return Z;
})()
