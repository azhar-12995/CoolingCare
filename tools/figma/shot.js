(async () => {
const A = globalThis.__shot || {};
const page = figma.root.children.find(p => p.name === A.page);
const ids = A.ids;
const nodes = page.findAll(n => n.type === 'COMPONENT' && n.getPluginData('screen') && (!ids || ids.includes(n.getPluginData('screen'))));
nodes.sort((a, b) => a.getPluginData('screen').localeCompare(b.getPluginData('screen')));
const out = [];
for (const n of nodes) {
  const bytes = await n.exportAsync({format: 'PNG', constraint: {type: 'SCALE', value: A.scale || 1}});
  const name = (A.prefix || '') + n.getPluginData('screen') + '.png';
  await fetch('http://localhost:9230/shots/' + name, {method: 'POST', body: bytes});
  out.push(name);
}
return out;
})()
