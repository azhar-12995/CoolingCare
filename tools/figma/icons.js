(async () => {
await figma.loadAllPagesAsync();
const page = figma.root.children.find(p => p.name === 'Foundations');
const icons = await (await fetch('http://localhost:9230/cc/icons.json')).json();
let sec = page.children.find(n => n.type === 'SECTION' && n.name === 'Icons');
const old = {};
if (sec) for (const c of [...sec.children]) { if (c.type === 'COMPONENT') old[c.name] = c; else c.remove(); }
else { sec = figma.createSection(); sec.name = 'Icons'; page.appendChild(sec); }
const vars = await figma.variables.getLocalVariablesAsync('COLOR');
const prim = vars.find(v => v.name === 'color/primary');
const paint = figma.variables.setBoundVariableForPaint({type: 'SOLID', color: {r: 0.039, g: 0.4, b: 0.784}}, 'color', prim);
const map = {};
const names = Object.keys(icons).sort();
const COLS = 16, CELL = 64;
names.forEach((name, i) => {
  let c = old['Icon/' + name];
  if (c) { for (const k of [...c.children]) k.remove(); } else { c = figma.createComponent(); sec.appendChild(c); }
  c.name = 'Icon/' + name; c.resize(24, 24); c.fills = []; c.clipsContent = false;
  const svg = figma.createNodeFromSvg(icons[name]);
  for (const k of [...svg.children]) {
    c.appendChild(k);
  }
  svg.remove();
  for (const v of c.findAll(n => n.type !== 'GROUP' && n.type !== 'FRAME')) {
    if ('strokes' in v && v.strokes.length) { v.strokes = [paint]; }
    if ('fills' in v && v.fills.length && v.fills[0].type === 'SOLID') v.fills = [paint];
    v.constraints = {horizontal: 'SCALE', vertical: 'SCALE'};
  }
  c.x = 40 + (i % COLS) * CELL; c.y = 80 + Math.floor(i / COLS) * CELL;
  map[name] = c.id;
});
sec.x = 0; sec.y = 0;
sec.resizeWithoutConstraints(40 * 2 + COLS * CELL, 80 + Math.ceil(names.length / COLS) * CELL + 40);
await figma.clientStorage.setAsync('cc_icons', map);
return {count: names.length};
})()
