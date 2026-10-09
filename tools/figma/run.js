(async () => {
const base = 'http://localhost:9230/cc/';
const Z = await eval(await (await fetch(base + 'lib.js?' + Date.now())).text());
const A = globalThis.__ccArgs || {lang: 'en', files: []};
Z.setLang(A.lang);
const page = figma.root.children.find(p => p.name === (A.lang === 'ur' ? 'App Flow · اردو' : 'App Flow · English'));
Z.page = page;
Z.reuse = {}; Z.S = {};
for (const n of page.findAll(n => n.type === 'COMPONENT' && n.getPluginData('screen'))) { const k = (n.getPluginData('lang') === 'ur' ? 'u' : '') + n.getPluginData('screen'); Z.reuse[k] = n; Z.S[n.getPluginData('screen')] = n; }
const res = [];
for (const f of A.files) { const fn = eval(await (await fetch(base + f + '?' + Date.now())).text()); res.push(await fn(Z)); }
return res;
})()
