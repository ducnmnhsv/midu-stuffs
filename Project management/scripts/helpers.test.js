const test = require('node:test');
const assert = require('node:assert/strict');
const { loadHelpers } = require('./load-helpers');

test('harness smoke test: extracts and runs formatDateLabel from the live .jsx source', () => {
  const { formatDateLabel } = loadHelpers(['formatDateLabel']);
  assert.equal(formatDateLabel('2026-07-21'), '21/07/2026');
  assert.equal(formatDateLabel(''), '');
});
