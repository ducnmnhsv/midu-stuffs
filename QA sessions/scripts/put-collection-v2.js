#!/usr/bin/env node
/**
 * Push the flattened collection JSON to Postman collection "TradeX API v2".
 * Requires: POSTMAN_API_KEY environment variable.
 *
 * Usage: POSTMAN_API_KEY=your_key node put-collection-v2.js [path-to-flattened.json]
 * Default path: ../tradex-api-v2-flattened.json (relative to this script).
 */

const fs = require('fs');
const path = require('path');
const https = require('https');

const COLLECTION_UID = '34274942-d349da1f-7f4f-4182-b16b-1cacba636b5d';
const jsonPath = process.argv[2] || path.join(__dirname, '../tradex-api-v2-flattened.json');
const apiKey = process.env.POSTMAN_API_KEY;

if (!apiKey) {
  console.error('Set POSTMAN_API_KEY environment variable.');
  process.exit(1);
}

const collection = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));
const body = JSON.stringify({ collection });

const url = new URL(`https://api.getpostman.com/collections/${COLLECTION_UID}`);
const opts = {
  hostname: url.hostname,
  path: url.pathname,
  method: 'PUT',
  headers: {
    'X-API-Key': apiKey,
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(body)
  }
};

const req = https.request(opts, (res) => {
  let data = '';
  res.on('data', (c) => (data += c));
  res.on('end', () => {
    if (res.statusCode >= 200 && res.statusCode < 300) {
      console.log('Collection TradeX API v2 updated successfully.');
    } else {
      console.error('Error:', res.statusCode, data);
      process.exit(1);
    }
  });
});
req.on('error', (e) => {
  console.error(e);
  process.exit(1);
});
req.write(body);
req.end();
