const ECFG_ID = 'ecfg_dvimtkxuyjp21hguxdh7meeg1bdv';
const TEAM_ID = 'team_AErJ44wJm1M3Izx2QKe9lyiQ';
const KEY_PREFIX = 'c_';

async function parseBody(req) {
  if (req.body && typeof req.body === 'object') return req.body;
  return new Promise((resolve) => {
    let raw = '';
    req.on('data', c => (raw += c));
    req.on('end', () => { try { resolve(JSON.parse(raw)); } catch { resolve({}); } });
    req.on('error', () => resolve({}));
  });
}

module.exports = async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') { res.status(200).end(); return; }

  const TOKEN = process.env.VERCEL_TOKEN;
  const authHeaders = { Authorization: `Bearer ${TOKEN}`, 'Content-Type': 'application/json' };

  if (req.method === 'GET') {
    const r = await fetch(
      `https://api.vercel.com/v1/edge-config/${ECFG_ID}/items?teamId=${TEAM_ID}`,
      { headers: authHeaders }
    );
    if (!r.ok) { res.status(500).json({ error: 'fetch failed', status: r.status }); return; }
    const payload = await r.json();
    const items = Array.isArray(payload) ? payload : (payload.items || []);
    const notes = {};
    items.forEach(item => {
      if (item.key.startsWith(KEY_PREFIX)) {
        notes[item.key.slice(KEY_PREFIX.length)] = item.value;
      }
    });
    res.status(200).json(notes);
    return;
  }

  if (req.method === 'POST') {
    const body = await parseBody(req);
    const { id, data } = body;
    if (!id) { res.status(400).json({ error: 'Missing id' }); return; }
    const r = await fetch(
      `https://api.vercel.com/v1/edge-config/${ECFG_ID}/items?teamId=${TEAM_ID}`,
      {
        method: 'PATCH',
        headers: authHeaders,
        body: JSON.stringify({
          items: [{ operation: 'upsert', key: `${KEY_PREFIX}${id}`, value: data }]
        })
      }
    );
    const result = await r.json();
    res.status(r.ok ? 200 : 500).json(result);
    return;
  }

  res.status(405).json({ error: 'Method not allowed' });
};
