(async (Z) => {
const pg = n => figma.root.children.find(p => p.name === n);
const EN = {}, UR = {};
for (const n of pg('App Flow · English').findAll(n => n.type === 'COMPONENT' && n.getPluginData('screen'))) EN[n.getPluginData('screen')] = n;
for (const n of pg('App Flow · اردو').findAll(n => n.type === 'COMPONENT' && n.getPluginData('screen'))) UR[n.getPluginData('screen')] = n;
const phone = (comp, scale = 1) => { const i = comp.createInstance(); if (scale !== 1) i.rescale(scale); const f = Z.box(i.width, i.height, {name: 'phone/' + comp.name, r: 36 * scale, clip: false, fx: 'Shadow/Phone'}); f.appendChild(i); i.x = 0; i.y = 0; return f; };

// ---------- Cover ----------
{
  const page = pg('Cover');
  for (const n of [...page.children]) n.remove();
  const f = Z.box(1920, 1080, {name: 'Cover', clip: true}); f.fills = [Z.G.splash()]; page.appendChild(f);
  for (const [x, y, sz, op] of [[-120, 640, 520, 0.06], [1500, -120, 420, 0.06], [880, 760, 260, 0.08]]) { const l = Z.ic('snowflake', sz, 'on-primary', {sw: 1}); l.opacity = op; f.appendChild(l); l.x = x; l.y = y; }
  const left = Z.col({gap: 26, w: 820}, [
    Z.row({gap: 22, cross: 'CENTER'}, [Z.logo(110, {fill: 'surface', fg: 'primary', fx: 'Shadow/Float', sw: 1.8}), Z.col({gap: 2}, [Z.t('Cooling Care', 'Board/H1', 'on-primary', {size: 84, lh: 96}), Z.t('AC & Refrigeration Services · Pasrur', 'Board/H3', 'on-primary', {op: 0.85})])]),
    Z.t('Android & iOS app for customers and the shop — log in with your mobile number, book a visit in under a minute, Cooling Care confirms it from the Admin Dashboard and every step arrives as a push. English + اردو (right-to-left).', 'Board/Body', 'on-primary', {w: 780, op: 0.92, size: 24, lh: 36}),
    Z.row({gap: 12, wrap: true, wgap: 12, w: 800}, [Object.keys(EN).length + ' screens × 2 languages', 'Phone login · Firebase', 'Admin dashboard · push alerts', 'Compose Multiplatform'].map(t => Z.pill(t, {fill: 'surface', color: 'primary', style: 'Board/Label', pad: [10, 18]})))]);
  f.appendChild(left); left.x = 120; left.y = 300;
  const p1 = phone(EN['10'], 1.02), p2 = phone(EN['41'], 0.92), p3 = phone(EN['70'], 0.92);
  for (const [p, x, y, rot] of [[p2, 1030, 200, 0], [p3, 1530, 230, 0], [p1, 1240, 120, 0]]) { f.appendChild(p); p.x = x; p.y = y; p.rotation = rot; }
  await Z.done();
  await figma.setFileThumbnailNodeAsync(f);
}

// ---------- Overview Board (client poster rebuilt with real screens) ----------
{
  const page = pg('Overview Board');
  for (const n of [...page.children]) n.remove();
  const W = 3300, H = 2300;
  const b = Z.box(W, H, {name: 'Overview Board', clip: true, fill: 'board'}); page.appendChild(b);
  b.fills = [Z.grad([['#F4F9FF', 0], ['#E2EEFB', 1]], 'down')];
  const feat = (icon, t) => Z.row({gap: 18, cross: 'CENTER'}, [Z.ic(icon, 34, 'primary-bright'), Z.t(t, 'Board/H3', 'text', {size: 26})]);
  const left = Z.col({gap: 22, w: 640}, [
    Z.t('Cooling Care', 'Board/H1', 'navy', {size: 96, lh: 104}), Z.t('AC & Refrigeration Services', 'Board/H2', 'navy', {size: 44, lh: 54}), Z.rect(110, 8, 'primary-bright', 4),
    Z.col({gap: 26, pad: [24, 0, 0, 0]}, [feat('snowflake', 'Residential & Commercial'), feat('shield-check', 'Professional Technicians'), feat('settings', 'Reliable & On-Time Service'), feat('map-pin', 'Serving Pasrur and Nearby Areas')])]);
  b.appendChild(left); left.x = 90; left.y = 90;
  const scene = Z.img('welcome_scene', 640, 870, 24, {top: 0}); b.appendChild(scene); scene.x = 90; scene.y = 760;
  const ids = [['10', '1. Home'], ['20', '2. Services'], ['23', '3. Service Details'], ['30', '4. Book a Visit'], ['41', '5. Booking Status'], ['50', '6. Support & My Bookings']];
  ids.forEach(([id, t], i) => {
    const col = i % 3, row = Math.floor(i / 3); const x = 860 + col * 800, y = 70 + row * 1010;
    const tt = Z.t(t, 'Board/H2', 'navy', {size: 42}); b.appendChild(tt); tt.x = x; tt.y = y;
    const p = phone(EN[id], 1.08); b.appendChild(p); p.x = x; p.y = y + 74;
  });
  const steps = [['search', 'Browse services'], ['calendar-days', 'Place order'], ['bell-ring', 'Shop confirms · push'], ['star', 'Service & rating']];
  const strip = Z.row({name: 'How it works', gap: 26, cross: 'CENTER', pad: [22, 36], r: 24, fill: 'surface', fx: 'Shadow/Card'}, steps.flatMap(([ic, t], i) => [Z.row({gap: 12, cross: 'CENTER'}, [Z.ic(ic, 30, 'primary'), Z.t(t, 'Board/H3', 'text')]), i < 3 ? Z.ic('chevron-right', 26, 'text-3') : null]).concat([Z.btn('Place Order', 'Primary', {arrow: true, h: 56, px: 28})]));
  b.appendChild(strip); strip.x = 90; strip.y = H - 150;
  await Z.done();
  strip.x = (W - strip.width) / 2;
  // Urdu + everything-else band
  const band = Z.col({name: 'Urdu & added screens', w: W, pad: [80, 90], gap: 40, fill: 'surface'}, [
    Z.col({gap: 8}, [Z.t('Also in اردو — every screen mirrors right-to-left', 'Board/H2', 'navy'), Z.t('Language is picked on first launch and can be switched in Settings at any time. Below: the same six screens in Urdu, the Admin Dashboard (orders, services open / close, business info) and every other screen needed to go live: phone log-in, sheets, validation, order placed / no connection, states, settings, privacy, notifications.', 'Board/Body', 'text-2', {w: 2600})]),
    Z.row({gap: 60}, ids.map(([id]) => phone(UR[id], 0.9)))]);
  band.appendChild(Z.col({gap: 8}, [Z.t('Admin app — same install, unlocked for the numbers in config/roles', 'Board/H2', 'navy'), Z.t('New orders arrive as a push; Confirm / Technician left / Completed notify the customer in their language. Services can be added, edited, opened and closed — closed ones vanish from the customer app.', 'Board/Body', 'text-2', {w: 2600})]));
  band.appendChild(Z.row({gap: 60}, ['70', '71', '72', '74', '75', '76'].filter(k => EN[k]).map(k => phone(EN[k], 0.9))));
  page.appendChild(band); band.x = 0; band.y = H + 80;
  const added = Object.keys(EN).filter(k => !ids.find(x => x[0] === k) && !['70', '71', '72', '74', '75', '76'].includes(k)).sort();
  const grid = Z.row({name: 'Added screens', gap: 40, wrap: true, wgap: 60, w: W - 180}, added.map(id => Z.col({gap: 12, w: 300}, [phone(EN[id], 0.8), Z.t(id + ' · ' + EN[id].getPluginData('title'), 'Title/M', 'text')])));
  Z.put(band, grid);
  await Z.done();
}
return 'ok';
})
