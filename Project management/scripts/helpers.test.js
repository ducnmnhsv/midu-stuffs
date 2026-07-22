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
  const { dateToWeekIndex } = loadHelpers(['MS_PER_DAY', 'MS_PER_WEEK', 'dateToWeekIndex']);
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
  const { computeAvgTrackOffWeeks } = loadHelpers(['computeTrackOffWeeks', 'computeAvgTrackOffWeeks']);
  assert.equal(computeAvgTrackOffWeeks([{ end: 5, actualEnd: null }]), null);
  assert.equal(computeAvgTrackOffWeeks([{ end: 5, actualEnd: 7 }, { end: 3, actualEnd: null }]), 2);
  assert.equal(computeAvgTrackOffWeeks([{ end: 5, actualEnd: 7 }, { end: 3, actualEnd: 5 }]), 2);
});

test('computeDateAxis pads around the real due dates present in a project', () => {
  const { computeDateAxis, realTaskDates, AXIS_PAD_MS, MS_PER_DAY } = loadHelpers(['MS_PER_DAY', 'AXIS_PAD_MS', 'computeDateAxis', 'realTaskDates']);
  const project = { sections: [{ tasks: [
    { dueDate: '2026-08-01' }, { dueDate: 'TBD' }, { dueDate: '2026-09-01' },
  ] }] };
  const axis = computeDateAxis(project);
  assert.ok(axis.min < new Date('2026-08-01T00:00:00Z').getTime());
  assert.ok(axis.max > new Date('2026-09-01T00:00:00Z').getTime());
});

test('dateToAxisPercent / axisPercentToDate round-trip and clamp to [0,100]', () => {
  const { dateToAxisPercent, axisPercentToDate } = loadHelpers(['dateToAxisPercent', 'axisPercentToDate']);
  const axis = { min: new Date('2026-01-01T00:00:00Z').getTime(), max: new Date('2026-02-01T00:00:00Z').getTime() };
  assert.equal(dateToAxisPercent('2026-01-01', axis), 0);
  assert.equal(dateToAxisPercent('2026-02-01', axis), 100);
  assert.equal(dateToAxisPercent('2025-01-01', axis), 0);
  assert.equal(axisPercentToDate(0, axis), '2026-01-01');
});

test('computeTrackOffDays ignores TBD/Done sentinels and missing actual dates', () => {
  const { computeTrackOffDays, MS_PER_DAY } = loadHelpers(['computeTrackOffDays', 'MS_PER_DAY']);
  assert.equal(computeTrackOffDays('TBD', '2026-08-05'), null);
  assert.equal(computeTrackOffDays('2026-08-01', null), null);
  assert.equal(computeTrackOffDays('2026-08-01', '2026-08-05'), 4);
  assert.equal(computeTrackOffDays('2026-08-05', '2026-08-01'), -4);
});

test('computeAvgTrackOffDays excludes tasks without actual data', () => {
  const { computeAvgTrackOffDays, computeTrackOffDays, MS_PER_DAY } = loadHelpers(['computeAvgTrackOffDays', 'computeTrackOffDays', 'MS_PER_DAY']);
  assert.equal(computeAvgTrackOffDays([{ dueDate: 'TBD', actualCompletionDate: null }]), null);
  assert.equal(computeAvgTrackOffDays([{ dueDate: '2026-08-01', actualCompletionDate: '2026-08-05' }, { dueDate: 'TBD', actualCompletionDate: null }]), 4);
});

test('displayName falls back to the VI name when nameEn is empty', () => {
  const { displayName } = loadHelpers(['displayName']);
  assert.equal(displayName({ name: 'Việt', nameEn: 'English' }, 'en'), 'English');
  assert.equal(displayName({ name: 'Việt', nameEn: '' }, 'en'), 'Việt');
  assert.equal(displayName({ name: 'Việt', nameEn: undefined }, 'en'), 'Việt');
  assert.equal(displayName({ name: 'Việt', nameEn: 'English' }, 'vi'), 'Việt');
});

test('displayClause falls back the same way as displayName', () => {
  const { displayClause } = loadHelpers(['displayClause']);
  assert.equal(displayClause({ clause: 'Điều 5', clauseEn: 'Article 5' }, 'en'), 'Article 5');
  assert.equal(displayClause({ clause: 'Điều 5', clauseEn: '' }, 'en'), 'Điều 5');
});

test('normalizeReportForLang wraps an old flat draft/history into {vi, en}', () => {
  const { normalizeReportForLang } = loadHelpers(['emptyDraft', 'normalizeReportForLang']);
  const oldProject = {
    id: 'p1',
    report: {
      draft: { doneLastWeek: 'a', planNextWeek: 'b', issues: 'c' },
      history: [{ id: 'h1', date: '2026-07-01', doneLastWeek: 'x', planNextWeek: 'y', issues: 'z', overall: 50, overdueSnapshot: [] }],
    },
  };
  const normalized = normalizeReportForLang(oldProject);
  assert.deepEqual(normalized.report.draft.vi, { doneLastWeek: 'a', planNextWeek: 'b', issues: 'c' });
  assert.deepEqual(normalized.report.draft.en, { doneLastWeek: '', planNextWeek: '', issues: '' });
  assert.deepEqual(normalized.report.history[0].vi, { doneLastWeek: 'x', planNextWeek: 'y', issues: 'z' });
  assert.equal(normalized.report.history[0].overall, 50);
});

test('normalizeReportForLang is a no-op on an already-normalized project', () => {
  const { normalizeReportForLang } = loadHelpers(['emptyDraft', 'normalizeReportForLang']);
  const newProject = {
    id: 'p1',
    report: { draft: { vi: { doneLastWeek: 'a', planNextWeek: '', issues: '' }, en: { doneLastWeek: '', planNextWeek: '', issues: '' } }, history: [] },
  };
  const normalized = normalizeReportForLang(newProject);
  assert.deepEqual(normalized, newProject);
});
