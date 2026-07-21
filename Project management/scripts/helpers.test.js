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

test('dateToWeekIndex converts a date to the correct week index', () => {
  const { dateToWeekIndex } = loadHelpers(['dateToWeekIndex']);
  assert.equal(dateToWeekIndex('2026-07-01', '2026-07-01'), 0);
  assert.equal(dateToWeekIndex('2026-07-08', '2026-07-01'), 1);
  assert.equal(dateToWeekIndex('2026-07-14', '2026-07-01'), 1);
  assert.equal(dateToWeekIndex('2026-07-15', '2026-07-01'), 2);
});

test('weekIndexToDate is the inverse of dateToWeekIndex at week boundaries', () => {
  const { weekIndexToDate } = loadHelpers(['weekIndexToDate']);
  assert.equal(weekIndexToDate(0, '2026-07-01'), '2026-07-01');
  assert.equal(weekIndexToDate(2, '2026-07-01'), '2026-07-15');
});

test('ensureWeeksLength appends labels only when needed', () => {
  const { ensureWeeksLength } = loadHelpers(['ensureWeeksLength']);
  assert.deepEqual(ensureWeeksLength(['W1', 'W2'], 2), ['W1', 'W2']);
  assert.deepEqual(ensureWeeksLength(['W1', 'W2'], 4), ['W1', 'W2', 'W3', 'W4']);
});

test('computeTrackOffWeeks is null unless both plan and actual end exist', () => {
  const { computeTrackOffWeeks } = loadHelpers(['computeTrackOffWeeks']);
  assert.equal(computeTrackOffWeeks(5, null), null);
  assert.equal(computeTrackOffWeeks(null, 5), null);
  assert.equal(computeTrackOffWeeks(5, 7), 2);
  assert.equal(computeTrackOffWeeks(5, 3), -2);
});

test('computeAvgTrackOffWeeks excludes tasks without actual data, never treats them as zero', () => {
  const { computeAvgTrackOffWeeks } = loadHelpers(['computeAvgTrackOffWeeks']);
  assert.equal(computeAvgTrackOffWeeks([{ end: 5, actualEnd: null }]), null);
  assert.equal(computeAvgTrackOffWeeks([{ end: 5, actualEnd: 7 }, { end: 3, actualEnd: null }]), 2);
  assert.equal(computeAvgTrackOffWeeks([{ end: 5, actualEnd: 7 }, { end: 3, actualEnd: 5 }]), 2);
});
