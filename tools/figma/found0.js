(async () => {
const log = [];
// ---- pages ----
await figma.loadAllPagesAsync();
const PAGES = ['Cover', 'Overview Board', 'App Flow · English', 'App Flow · اردو', 'Foundations', 'Handoff'];
const first = figma.root.children[0];
if (first.name === 'Page 1') first.name = PAGES[0];
for (const name of PAGES) if (!figma.root.children.find(p => p.name === name)) { const p = figma.createPage(); p.name = name; }
PAGES.forEach((n, i) => { const p = figma.root.children.find(x => x.name === n); figma.root.insertChild(i, p); });
// ---- color variables ----
const C = {
  'primary': '#0A66C8', 'primary-dark': '#0852A3', 'primary-bright': '#1E88E5', 'primary-tint': '#EAF3FC', 'primary-soft': '#D3E5F8', 'navy': '#0B2A5B',
  'text': '#0B1F45', 'text-2': '#4E5566', 'text-3': '#6B7280', 'placeholder': '#8A919C', 'border': '#E2E8F0', 'divider': '#EEF2F6',
  'bg': '#FFFFFF', 'bg-2': '#F4F8FC', 'surface': '#FFFFFF', 'surface-2': '#F1F5F9', 'tile': '#EEF5FB', 'on-primary': '#FFFFFF',
  'green': '#0C8446', 'green-dark': '#0A6B39', 'green-tint': '#E2F4E9', 'green-text': '#1E6B43',
  'orange': '#C9470C', 'orange-tint': '#FDEDDF', 'orange-text': '#B4441C',
  'danger': '#C62828', 'danger-tint': '#FDECEC', 'warning': '#B45309', 'warning-tint': '#FEF3C7', 'board': '#E8F1FB'
};
const rgb = h => ({r: parseInt(h.slice(1,3),16)/255, g: parseInt(h.slice(3,5),16)/255, b: parseInt(h.slice(5,7),16)/255});
let col = (await figma.variables.getLocalVariableCollectionsAsync()).find(c => c.name === 'Color');
if (!col) { col = figma.variables.createVariableCollection('Color'); col.renameMode(col.modes[0].modeId, 'Light'); }
const mode = col.modes[0].modeId;
const existing = await figma.variables.getLocalVariablesAsync('COLOR');
for (const [k, h] of Object.entries(C)) {
  let v = existing.find(x => x.name === 'color/' + k);
  if (!v) v = figma.variables.createVariable('color/' + k, col, 'COLOR');
  v.setValueForMode(mode, rgb(h));
}
log.push('vars ' + Object.keys(C).length);
// ---- text styles ----
const T = {
  'Display/L': ['Poppins', 'Bold', 26, 34], 'Display/M': ['Poppins', 'Bold', 22, 29], 'Title/L': ['Poppins', 'SemiBold', 20, 28], 'Title/M': ['Poppins', 'SemiBold', 17, 24],
  'Title/S': ['Inter', 'Semi Bold', 15, 20], 'Body/L': ['Inter', 'Regular', 15, 22], 'Body/M': ['Inter', 'Regular', 14, 20], 'Body/S': ['Inter', 'Regular', 13, 18],
  'Button': ['Inter', 'Semi Bold', 15, 20], 'Label/M': ['Inter', 'Semi Bold', 13, 18], 'Label/S': ['Inter', 'Semi Bold', 11, 14], 'Caption': ['Inter', 'Medium', 11, 14],
  'Logo/XL': ['Poppins', 'Bold', 34, 40], 'Logo/M': ['Poppins', 'Bold', 20, 26],
  'Board/H1': ['Poppins', 'Bold', 64, 76], 'Board/H2': ['Poppins', 'SemiBold', 36, 44], 'Board/H3': ['Poppins', 'SemiBold', 24, 32], 'Board/Body': ['Inter', 'Regular', 20, 30], 'Board/Label': ['Inter', 'Semi Bold', 16, 20],
  'UR/Display/L': ['Noto Nastaliq Urdu', 'Bold', 22, 46], 'UR/Display/M': ['Noto Nastaliq Urdu', 'Bold', 19, 40], 'UR/Title/L': ['Noto Nastaliq Urdu', 'Bold', 17, 36], 'UR/Title/M': ['Noto Nastaliq Urdu', 'SemiBold', 15, 32],
  'UR/Title/S': ['Noto Nastaliq Urdu', 'SemiBold', 13.5, 29], 'UR/Body/L': ['Noto Nastaliq Urdu', 'Regular', 13.5, 29], 'UR/Body/M': ['Noto Nastaliq Urdu', 'Regular', 12.5, 27], 'UR/Body/S': ['Noto Nastaliq Urdu', 'Regular', 11.5, 25],
  'UR/Button': ['Noto Nastaliq Urdu', 'SemiBold', 13.5, 28], 'UR/Label/M': ['Noto Nastaliq Urdu', 'SemiBold', 12, 25], 'UR/Label/S': ['Noto Nastaliq Urdu', 'Medium', 10.5, 22], 'UR/Caption': ['Noto Nastaliq Urdu', 'Regular', 10.5, 22],
  'UR/Logo/XL': ['Noto Nastaliq Urdu', 'Bold', 28, 56], 'UR/Logo/M': ['Noto Nastaliq Urdu', 'Bold', 17, 36],
  'UR/Board/H1': ['Noto Nastaliq Urdu', 'Bold', 52, 104], 'UR/Board/H2': ['Noto Nastaliq Urdu', 'Bold', 30, 62], 'UR/Board/H3': ['Noto Nastaliq Urdu', 'Bold', 21, 44], 'UR/Board/Body': ['Noto Nastaliq Urdu', 'Regular', 17, 36], 'UR/Board/Label': ['Noto Nastaliq Urdu', 'SemiBold', 14, 30]
};
const fonts = [...new Set(Object.values(T).map(v => v[0] + '|' + v[1]))];
await Promise.all(fonts.map(f => figma.loadFontAsync({family: f.split('|')[0], style: f.split('|')[1]})));
const ts = await figma.getLocalTextStylesAsync();
for (const [name, [fam, sty, size, lh]] of Object.entries(T)) {
  let s = ts.find(x => x.name === name); if (!s) { s = figma.createTextStyle(); s.name = name; }
  s.fontName = {family: fam, style: sty}; s.fontSize = size; s.lineHeight = {unit: 'PIXELS', value: lh};
  s.letterSpacing = {unit: 'PERCENT', value: name.startsWith('Display') || name.startsWith('Logo') ? -1 : 0};
}
log.push('text styles ' + Object.keys(T).length);
// ---- effect styles ----
const E = {
  'Shadow/Card': [{type: 'DROP_SHADOW', color: {r: 0.043, g: 0.122, b: 0.27, a: 0.07}, offset: {x: 0, y: 2}, radius: 10, spread: 0, visible: true, blendMode: 'NORMAL'}],
  'Shadow/Float': [{type: 'DROP_SHADOW', color: {r: 0.043, g: 0.122, b: 0.27, a: 0.14}, offset: {x: 0, y: 10}, radius: 28, spread: 0, visible: true, blendMode: 'NORMAL'}],
  'Shadow/Nav': [{type: 'DROP_SHADOW', color: {r: 0.043, g: 0.122, b: 0.27, a: 0.07}, offset: {x: 0, y: -2}, radius: 14, spread: 0, visible: true, blendMode: 'NORMAL'}],
  'Shadow/Button': [{type: 'DROP_SHADOW', color: {r: 0.04, g: 0.4, b: 0.78, a: 0.28}, offset: {x: 0, y: 6}, radius: 14, spread: 0, visible: true, blendMode: 'NORMAL'}],
  'Shadow/Phone': [{type: 'DROP_SHADOW', color: {r: 0.043, g: 0.122, b: 0.27, a: 0.22}, offset: {x: 0, y: 24}, radius: 60, spread: 0, visible: true, blendMode: 'NORMAL'}]
};
const es = await figma.getLocalEffectStylesAsync();
for (const [name, eff] of Object.entries(E)) { let s = es.find(x => x.name === name); if (!s) { s = figma.createEffectStyle(); s.name = name; } s.effects = eff; }
log.push('effects ' + Object.keys(E).length);
return log;
})()
