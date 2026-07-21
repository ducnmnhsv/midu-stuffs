const test = require('node:test');
const assert = require('node:assert/strict');
const { loadHelpers } = require('./load-helpers');

test('harness smoke test: extracts and runs formatDateLabel from the live .jsx source', () => {
  const { formatDateLabel } = loadHelpers(['formatDateLabel']);
  assert.equal(formatDateLabel('2026-07-21'), '21/07/2026');
  assert.equal(formatDateLabel(''), '');
});

test('migrateV2ToV3 copies projects through unchanged', () => {
  const { migrateV2ToV3 } = loadHelpers(['migrateV2ToV3']);
  const oldData = { projects: [{ id: 'p1', name: 'Test', sections: [] }] };
  const migrated = migrateV2ToV3(oldData);
  assert.deepEqual(migrated, { projects: [{ id: 'p1', name: 'Test', sections: [] }] });
  assert.notEqual(migrated.projects, oldData.projects, 'must return a new array, not the same reference');
});
