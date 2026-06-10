#!/usr/bin/env node
/**
 * Flatten TradeX Postman collection for Environment-based use.
 * - One folder per category, one request per endpoint (no Prod subfolders).
 * - URL: {{baseUrl}}/rest/api/v1/...
 * - Auth: Authorization: jwt {{accessToken}}
 * - Login request: test script sets pm.environment.set("accessToken", jsonData.accessToken)
 *
 * Usage: node flatten-collection-for-env.js <path-to-collection-json>
 * Output: JSON to stdout (pipe to file)
 */

const fs = require('fs');
const path = require('path');

const SOURCE_PATH = process.argv[2];
const TARGET_UID = '34274942-d349da1f-7f4f-4182-b16b-1cacba636b5d';

if (!SOURCE_PATH) {
  process.stderr.write('Usage: node flatten-collection-for-env.js <path-to-collection-json>\n');
  process.exit(1);
}

const raw = fs.readFileSync(SOURCE_PATH, 'utf8');
const data = JSON.parse(raw);
const sourceItem = data.collection.item || [];

function getPathFromUrl(url) {
  if (!url) return '';
  if (typeof url === 'string') {
    try {
      const u = new URL(url.replace(/\{\{.*?\}\}/g, 'https://x'));
      return u.pathname || '';
    } catch (_) {
      const m = url.match(/(\/rest\/api\/v1\/[^?]*)/);
      return m ? m[1] : url;
    }
  }
  if (url.path && Array.isArray(url.path)) return '/' + url.path.join('/');
  if (url.raw) return getPathFromUrl(url.raw);
  return '';
}

function normalizeUrl(urlObj) {
  const pathPart = getPathFromUrl(urlObj);
  const pathClean = pathPart.startsWith('/') ? pathPart : '/' + pathPart;
  return {
    raw: '{{baseUrl}}' + pathClean,
    host: ['{{baseUrl}}'],
    path: pathClean.split('/').filter(Boolean),
    query: urlObj && urlObj.query ? urlObj.query : undefined
  };
}

function normalizeHeaders(headers) {
  if (!Array.isArray(headers)) return headers;
  return headers.map(h => {
    let v = h.value || '';
    v = v.replace(/\{\{nhsv-uat\}\}/g, '{{baseUrl}}').replace(/\{\{nhsv-prod\}\}/g, '{{baseUrl}}');
    if (/authorization|accessToken/i.test(h.key || '') && (v.includes('accessToken') || v.includes('accessToken-Prod')))
      v = 'jwt {{accessToken}}';
    return { ...h, value: v };
  });
}

function isLoginRequest(req) {
  const r = req.request || req;
  const path = getPathFromUrl(r.url);
  return path.includes('/login') && (r.method || '').toUpperCase() === 'POST' && !path.includes('verifyOTP') && !path.includes('refreshToken');
}

function ensureLoginTestScript(events) {
  const testScript = [
    'var jsonData = JSON.parse(responseBody);',
    'pm.environment.set("accessToken", jsonData.accessToken);'
  ];
  let hasTest = false;
  const ev = Array.isArray(events) ? events : [];
  const out = ev.map(e => {
    if (e.listen === 'test') {
      hasTest = true;
      return { ...e, script: { ...e.script, exec: testScript, type: 'text/javascript' } };
    }
    return e;
  });
  if (!hasTest) out.push({ listen: 'test', script: { exec: testScript, type: 'text/javascript' } });
  return out;
}

function extractRequests(node, into) {
  if (!node) return;
  if (node.request) {
    into.push(node);
    return;
  }
  if (node.item && Array.isArray(node.item)) {
    node.item.forEach(child => extractRequests(child, into));
  }
}

function key(req) {
  const r = req.request || req;
  return ((r.method || 'GET') + ' ' + getPathFromUrl(r.url)).toLowerCase();
}

function normalizeRequest(req, isLogin) {
  const r = req.request || {};
  const url = normalizeUrl(r.url || {});
  const headers = normalizeHeaders(r.header || []);
  const out = {
    name: (req.name || '').replace(/^\[Prod\]\s*/i, '').trim(),
    request: {
      method: r.method || 'GET',
      header: headers,
      body: r.body,
      url
    },
    response: req.response || []
  };
  if (r.protocolProfileBehavior) out.protocolProfileBehavior = r.protocolProfileBehavior;
  if (isLogin) {
    out.event = ensureLoginTestScript(req.event);
  } else if (req.event && Array.isArray(req.event)) {
    out.event = req.event.map(e => ({
      ...e,
      script: e.script ? { ...e.script, exec: (e.script.exec || []).map(line =>
        typeof line === 'string' ? line.replace(/\{\{nhsv-uat\}\}/g, '{{baseUrl}}').replace(/\{\{nhsv-prod\}\}/g, '{{baseUrl}}').replace(/accessToken-Prod/g, 'accessToken') : line
      ) } : e
    }));
  }
  return out;
}

const newItem = [];

sourceItem.forEach(folder => {
  if (!folder.item) return;
  const requests = [];
  folder.item.forEach(child => {
    if (child.name === 'Prod' && child.item) {
      child.item.forEach(prodReq => extractRequests(prodReq, requests));
    } else if (child.request) {
      requests.push(child);
    } else {
      extractRequests(child, requests);
    }
  });
  const byKey = new Map();
  requests.forEach(req => {
    const k = key(req);
    if (!byKey.has(k)) byKey.set(k, req);
    else {
      const existing = byKey.get(k);
      const r = req.request || req;
      const url = r.url;
      const prefer = (url && (typeof url === 'string' ? url.includes('uat') : (url.raw || '').includes('uat'))) ? req : existing;
      byKey.set(k, prefer);
    }
  });
  const normalized = Array.from(byKey.values()).map(req => {
    const isLogin = isLoginRequest(req);
    return normalizeRequest(req, isLogin);
  });
  newItem.push({ name: folder.name, item: normalized });
});

const output = {
  info: {
    name: 'TradeX API v2',
    schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
    uid: TARGET_UID
  },
  item: newItem
};

process.stdout.write(JSON.stringify(output, null, 0));
