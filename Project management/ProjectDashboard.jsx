import React, { useState, useEffect, useCallback } from 'react';
import { Plus, Trash2, Copy, Check, AlertTriangle, Loader2, RotateCcw, X, History, RefreshCcw } from 'lucide-react';

const STORAGE_KEY = 'ptd:state:v3';
const LEGACY_STORAGE_KEY = 'ptd:state:v2';

const COLORS = {
  navy: '#0B2545',
  navyLight: '#13315C',
  teal: '#0FA3A3',
  tealSoft: '#E4F5F5',
  bg: '#F4F6F8',
  card: '#FFFFFF',
  success: '#1E9E6B',
  successBg: '#E3F5EC',
  warn: '#C97A1E',
  warnBg: '#FDF1DF',
  danger: '#C43F55',
  dangerBg: '#FBE7EA',
  textMuted: '#6B7684',
  textFaint: '#98A2AE',
  border: '#E3E7EC',
};

function uid() {
  return Math.random().toString(36).slice(2, 9);
}

function emptyDraft() {
  return { doneLastWeek: '', planNextWeek: '', issues: '' };
}

// ---------- seed data — real data pulled from Midu's Google Sheet ----------
// (TT134 draft circular checklist, Research team feature Gantt, C06 - RAR biometric enrollment Gantt)
// Week-column placement (which cells are shaded) isn't retrievable via text export of the sheet,
// so gantt bar spans below are carried over from the screenshots Midu shared; task names, sections,
// PIC, clause numbers, due dates and progress % are the real values read from the sheet.

function seedData() {
  const p1weeks = ['Jul W1','Jul W2','Jul W3','Jul W4','Jul W5','Aug W1','Aug W2','Aug W3','Aug W4','Aug W5','Sep W1','Sep W2','Sep W3','Sep W4'];
  const project1 = {
    id: uid(),
    name: 'C06 - RAR — Biometric Enrollment',
    viewType: 'gantt',
    weeks: p1weeks,
    currentWeekIndex: 3,
    sections: [
      {
        id: uid(), name: 'Assessment',
        tasks: [
          { id: uid(), name: 'Assess the current situation (Infra + Estimate business works)', pic: 'Mr Duc', progress: 100, start: 0, end: 0 },
          { id: uid(), name: 'BOM approval for proceeding', pic: 'Ms Nhai', progress: 1, start: 1, end: 2 },
        ],
      },
      {
        id: uid(), name: 'Infrastructure',
        tasks: [
          { id: uid(), name: 'Setup server & License', pic: 'Mr Van', progress: 0, start: 3, end: 6 },
          { id: uid(), name: 'Pentest security', pic: 'Vendor - Mr Van', progress: 0, start: 5, end: 10 },
          { id: uid(), name: 'C06 deployment AgentGW', pic: 'C06', progress: 0, start: 8, end: 10 },
        ],
      },
      {
        id: uid(), name: 'Business development',
        tasks: [
          { id: uid(), name: 'eKYC VNeID', pic: 'Dev team', progress: 0, start: 3, end: 6 },
          { id: uid(), name: 'Biometric enrollment', pic: 'Dev team', progress: 0, start: 8, end: 10 },
          { id: uid(), name: 'Withdrawal > 10M', pic: 'Dev team', progress: 0, start: 11, end: 13 },
          { id: uid(), name: 'Online trading', pic: 'Dev team', progress: 0, start: null, end: null },
          { id: uid(), name: 'Biometric management & DB', pic: 'Dev team', progress: 0, start: null, end: null },
        ],
      },
      {
        id: uid(), name: 'Closing',
        tasks: [
          { id: uid(), name: 'UAT testing', pic: 'Business team', progress: 0, start: null, end: null },
          { id: uid(), name: 'Golive', pic: '', progress: 0, start: null, end: null },
        ],
      },
    ],
    report: { draft: emptyDraft(), history: [] },
  };

  const project2 = {
    id: uid(),
    name: 'TT134 Draft Circular — Compliance Checklist',
    viewType: 'checklist',
    weeks: [],
    currentWeekIndex: 0,
    sections: [
      {
        id: uid(), name: 'Section 1 - General Provisions', pic: 'Mr Duc',
        tasks: [
          { id: uid(), no: 3, name: 'Xac minh SDT di dong thuoc quyen su dung hop phap cua KH', clause: 'Dieu 5', dueDate: 'TBD', progress: 0 },
          { id: uid(), no: 5, name: 'Xac thuc Smart OTP cho giao dich dau tien moi phien dang nhap', clause: 'Dieu 5', dueDate: '2026-10-16', progress: 0 },
          { id: uid(), no: 7, name: 'Ra soat thoi han hieu luc OTP: SMS / The ma tran / Soft OTP', clause: 'Dieu 7-9', dueDate: '2026-08-14', progress: 0 },
          { id: uid(), no: 8, name: 'Kiem tra sinh trac hoc dat chuan FIDO + phuong an rut >=10tr', clause: 'Dieu 7-9', dueDate: 'TBD', progress: 0 },
          { id: uid(), no: 9, name: 'Chung nhan PAD FIDO cua doi tac (VNPT eKYC)', clause: 'Dieu 7-9', dueDate: 'Done', progress: 100 },
          { id: uid(), no: 10, name: 'Doi chieu khop dung sinh trac hoc theo khoan 6 Dieu 9', clause: 'Dieu 7-9', dueDate: 'TBD', progress: 0 },
          { id: uid(), no: 12, name: 'Xac thuc sinh trac hoc cho GDCK truc tuyen + cung 1 thiet bi', clause: 'Dieu 12-13', dueDate: '2026-10-30', progress: 0 },
          { id: uid(), no: 14, name: 'Luong xac thuc rut/chuyen tien theo gia tri (<10tr / >=10tr)', clause: 'Dieu 12-13', dueDate: '2026-08-28', progress: 0 },
          { id: uid(), no: 35, name: 'Luu dinh danh thiet bi trong nhat ky GDCK & rut/chuyen tien', clause: 'Dieu 18', dueDate: '2026-08-28', progress: 0 },
          { id: uid(), no: 36, name: 'Quan ly thiet bi Mobile App (1 thiet bi/TK) + chong can thiep', clause: 'Dieu 18', dueDate: '2026-10-30', progress: 0 },
        ],
      },
      {
        id: uid(), name: 'Section 2 - API Governance', pic: 'Mr Hoang',
        tasks: [
          { id: uid(), no: 17, name: 'Ra soat tuan thu phap luat CK & ANM khi trien khai API', clause: 'Dieu 14', dueDate: 'Done', progress: 100 },
          { id: uid(), no: 18, name: 'Ra soat dieu khoan bao ve du lieu ca nhan trong HD STAG', clause: 'Dieu 14', dueDate: 'Done', progress: 100 },
          { id: uid(), no: 19, name: 'Bo sung dieu khoan quan ly/su dung du lieu dung muc dich', clause: 'Dieu 14', dueDate: '2026-08-01', progress: 0 },
          { id: uid(), no: 20, name: 'Bo sung HD API ben thu 3 day du noi dung a-i (khoan 5 Dieu 14)', clause: 'Dieu 14', dueDate: '2026-08-01', progress: 0 },
          { id: uid(), no: 21, name: 'Ho so ky thuat & phoi hop trien khai API (khoan 6 Dieu 14)', clause: 'Dieu 14', dueDate: '2026-10-01', progress: 0 },
          { id: uid(), no: 22, name: 'Xay he thong Open API + tai lieu huong dan ket noi', clause: 'Dieu 15', dueDate: '2026-09-30', progress: 0 },
          { id: uid(), no: 23, name: 'Cong cu tra cuu/rut lai su dong y du lieu KH cho ben thu 3', clause: 'Dieu 15', dueDate: '2026-09-30', progress: 0 },
          { id: uid(), no: 24, name: 'Kiem soat truy cap API - rate limit theo nhom doi tac', clause: 'Dieu 15', dueDate: '2026-09-30', progress: 0 },
          { id: uid(), no: 26, name: 'Quy dinh lua chon - tham dinh - giam sat ben thu 3', clause: 'Dieu 15', dueDate: '2026-09-30', progress: 0 },
          { id: uid(), no: 27, name: 'Cap/thu hoi quyen truy cap API theo tung doi tac', clause: 'Dieu 15', dueDate: '2026-09-30', progress: 0 },
          { id: uid(), no: 28, name: 'Giam sat & ghi nhat ky su dung API (log, API key rolling)', clause: 'Dieu 15', dueDate: '2026-09-30', progress: 0 },
          { id: uid(), no: 30, name: 'Danh muc API dang trien khai gui UBCKNN (chuyen tiep)', clause: 'Dieu 31', dueDate: '2026-11-30', progress: 0 },
          { id: uid(), no: 45, name: 'Cong khai danh muc dich vu API tren website', clause: 'Dieu 26', dueDate: '2026-09-30', progress: 0 },
        ],
      },
      {
        id: uid(), name: 'Section 3 - Infrastructure & Security', pic: 'Mr Van',
        tasks: [
          { id: uid(), no: 31, name: 'Checklist quan ly & van hanh he thong theo Phu luc II', clause: 'Dieu 16-17', dueDate: '2026-08-01', progress: 0 },
          { id: uid(), no: 32, name: 'Danh gia ANM truoc van hanh & dinh ky boi don vi du tham quyen', clause: 'Dieu 16-17', dueDate: '2026-07-15', progress: 0 },
          { id: uid(), no: 33, name: 'Quy trinh van hanh/su co/backup/rui ro/bao ve du lieu', clause: 'Dieu 16-17', dueDate: '2026-12-31', progress: 0 },
          { id: uid(), no: 34, name: 'Kiem soat phien dang nhap & giai phap DLP tren ung dung online', clause: 'Dieu 18', dueDate: '2027-06-30', progress: 0 },
          { id: uid(), no: 37, name: 'Quy trinh truy cap he thong GDTT tu ngoai mang noi bo (VPN)', clause: 'Dieu 19', dueDate: '2026-10-31', progress: 0 },
          { id: uid(), no: 38, name: 'Kiem tra kha nang khoi phuc backup dinh ky 6 thang/lan', clause: 'Dieu 20-22', dueDate: '2026-10-31', progress: 0 },
          { id: uid(), no: 39, name: 'Ma hoa du lieu luu tru (toi thieu 3 ban sao)', clause: 'Dieu 20-22', dueDate: '2026-12-31', progress: 0 },
          { id: uid(), no: 40, name: 'CSDL luu tru, bao quan thong tin sinh trac hoc KH', clause: 'Dieu 20-22', dueDate: 'TBD', progress: 0 },
        ],
      },
    ],
    report: { draft: emptyDraft(), history: [] },
  };

  const p3weeks = ['Jul W1','Jul W2','Jul W3','Jul W4','Jul W5','Aug W1','Aug W2','Aug W3','Aug W4','Aug W5'];
  const project3 = {
    id: uid(),
    name: 'Research team — Content Features',
    viewType: 'gantt',
    weeks: p3weeks,
    currentWeekIndex: 3,
    sections: [
      { id: uid(), name: 'NH Research', tasks: [
        { id: uid(), name: 'Implementation', progress: 100, start: 1, end: 1 },
        { id: uid(), name: 'Golive', progress: 0, start: 2, end: 2 },
      ]},
      { id: uid(), name: 'Event calendar', tasks: [
        { id: uid(), name: 'Implementation', progress: 100, start: 3, end: 4 },
        { id: uid(), name: 'Testing', progress: 50, start: 4, end: 5 },
        { id: uid(), name: 'Golive', progress: 0, start: null, end: null },
      ]},
      { id: uid(), name: 'Market watch', tasks: [
        { id: uid(), name: 'Implementation', progress: 100, start: 1, end: 2 },
        { id: uid(), name: 'Testing', progress: 50, start: 3, end: 3 },
        { id: uid(), name: 'Golive', progress: 0, start: 4, end: 4 },
      ]},
      { id: uid(), name: 'Buy/sell recomendation', tasks: [
        { id: uid(), name: 'Analyzing & Design', progress: 10, start: 3, end: 4 },
        { id: uid(), name: 'Implementation', progress: 0, start: 5, end: 7 },
        { id: uid(), name: 'Testing', progress: 0, start: 8, end: 8 },
        { id: uid(), name: 'Golive', progress: 0, start: 9, end: 9 },
      ]},
    ],
    report: { draft: emptyDraft(), history: [] },
  };

  return [project1, project2, project3];
}

// ---------- helpers ----------

function statusColor(progress) {
  if (progress >= 100) return { bg: COLORS.successBg, fg: COLORS.success };
  if (progress > 0) return { bg: COLORS.warnBg, fg: COLORS.warn };
  return { bg: COLORS.dangerBg, fg: COLORS.danger };
}

function flattenTasks(project) {
  const out = [];
  project.sections.forEach(sec => sec.tasks.forEach(t => out.push({ ...t, sectionName: sec.name })));
  return out;
}

function overallProgress(project) {
  const tasks = flattenTasks(project);
  if (!tasks.length) return 0;
  return Math.round(tasks.reduce((s, t) => s + (t.progress || 0), 0) / tasks.length);
}

function isTaskOverdue(task, project) {
  if (task.progress >= 100) return false;
  if (project.viewType === 'gantt') {
    return task.end !== null && task.end !== undefined && task.end < project.currentWeekIndex;
  }
  if (project.viewType === 'checklist') {
    if (!task.dueDate || task.dueDate === 'TBD' || task.dueDate === 'Done') return false;
    const d = new Date(task.dueDate);
    if (isNaN(d.getTime())) return false;
    return d < new Date();
  }
  return false;
}

function getOverdueTasks(project) {
  return flattenTasks(project).filter(t => isTaskOverdue(t, project));
}

const MS_PER_DAY = 24 * 60 * 60 * 1000;
const MS_PER_WEEK = 7 * MS_PER_DAY;

function dateToWeekIndex(dateStr, weekStartDate) {
  const ms = new Date(`${dateStr}T00:00:00Z`).getTime() - new Date(`${weekStartDate}T00:00:00Z`).getTime();
  return Math.floor(ms / MS_PER_WEEK);
}

function weekIndexToDate(index, weekStartDate) {
  const base = new Date(`${weekStartDate}T00:00:00Z`);
  base.setUTCDate(base.getUTCDate() + index * 7);
  return base.toISOString().slice(0, 10);
}

function ensureWeeksLength(weeks, neededLength) {
  if (neededLength <= weeks.length) return weeks;
  const extra = [];
  for (let i = weeks.length; i < neededLength; i++) extra.push(`W${i + 1}`);
  return [...weeks, ...extra];
}

function computeTrackOffWeeks(planEnd, actualEnd) {
  if (planEnd === null || planEnd === undefined || actualEnd === null || actualEnd === undefined) return null;
  return actualEnd - planEnd;
}

function computeAvgTrackOffWeeks(tasks) {
  const diffs = tasks
    .map(t => computeTrackOffWeeks(t.end, t.actualEnd))
    .filter(d => d !== null);
  if (!diffs.length) return null;
  return diffs.reduce((s, d) => s + d, 0) / diffs.length;
}

function todayISO() {
  return new Date().toISOString().slice(0, 10);
}

function todayLabel() {
  const d = new Date();
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function migrateV2ToV3(oldParsed) {
  return { projects: (oldParsed.projects || []).map(p => ({ ...p })) };
}

function formatDateLabel(iso) {
  if (!iso) return '';
  const [y, m, d] = iso.split('-');
  return `${d}/${m}/${y}`;
}

function composeReportBlock(name, overall, data, overdueNames) {
  const lines = [];
  lines.push(`${name} — ${overall}%`);
  lines.push(`- Tuần trước: ${(data.doneLastWeek || '').trim() || '(chưa cập nhật)'}`);
  lines.push(`- Tuần tới: ${(data.planNextWeek || '').trim() || '(chưa cập nhật)'}`);
  lines.push(`- Issue/bottleneck: ${(data.issues || '').trim() || 'Không có'}`);
  if (overdueNames && overdueNames.length) {
    lines.push(`- Quá hạn: ${overdueNames.join('; ')}`);
  }
  return lines.join('\n');
}

function composeProjectReport(project) {
  return composeReportBlock(project.name, overallProgress(project), project.report.draft, getOverdueTasks(project).map(t => t.name));
}

function findEntryForDate(project, dateStr) {
  const candidates = project.report.history.filter(h => h.date <= dateStr);
  if (!candidates.length) return null;
  return candidates.reduce((a, b) => (a.date >= b.date ? a : b));
}

function composeDigest(projects, headerDateLabel, selection) {
  const header = `Weekly Status Update — ${headerDateLabel}`;
  const body = projects.map(p => {
    if (selection === 'latest') {
      return composeProjectReport(p);
    }
    const entry = findEntryForDate(p, selection);
    if (!entry) {
      return composeProjectReport(p) + '\n(chưa có log cho ngày này, hiển thị bản nháp hiện tại)';
    }
    return composeReportBlock(p.name, entry.overall, entry, entry.overdueSnapshot || []);
  }).join('\n\n');
  return `${header}\n\n${body}`;
}

// ---------- small UI atoms ----------

function IconBtn({ onClick, title, children }) {
  return (
    <button
      onClick={onClick}
      title={title}
      className="p-1 rounded"
      style={{ color: COLORS.textMuted, background: 'transparent', border: 'none', cursor: 'pointer' }}
      onMouseEnter={e => (e.currentTarget.style.background = COLORS.border)}
      onMouseLeave={e => (e.currentTarget.style.background = 'transparent')}
    >
      {children}
    </button>
  );
}

function ProgressPill({ progress }) {
  const c = statusColor(progress);
  return (
    <span
      className="mono"
      style={{
        background: c.bg, color: c.fg, padding: '2px 8px', borderRadius: 999,
        fontSize: 12, fontWeight: 600, minWidth: 42, textAlign: 'center', display: 'inline-block',
      }}
    >
      {progress}%
    </span>
  );
}

function MiniBar({ progress, width = 60 }) {
  return (
    <div style={{ width, height: 6, borderRadius: 999, background: COLORS.border, overflow: 'hidden' }}>
      <div style={{ width: `${progress}%`, height: '100%', background: progress >= 100 ? COLORS.success : COLORS.teal, borderRadius: 999 }} />
    </div>
  );
}

// ---------- main component ----------

export default function ProjectDashboard() {
  const [projects, setProjects] = useState(null);
  const [selectedId, setSelectedId] = useState(null);
  const [mainView, setMainView] = useState('project');
  const [projectTab, setProjectTab] = useState('timeline');
  const [saveState, setSaveState] = useState('idle');
  const [copied, setCopied] = useState(null);
  const [editingProgressId, setEditingProgressId] = useState(null);
  const [confirmDelete, setConfirmDelete] = useState(null);
  const [addingTaskSection, setAddingTaskSection] = useState(null);
  const [taskDraft, setTaskDraft] = useState({ name: '', clause: '', dueDate: '', start: '', end: '' });
  const [addingSectionProject, setAddingSectionProject] = useState(null);
  const [sectionDraftName, setSectionDraftName] = useState('');
  const [addingWeek, setAddingWeek] = useState(false);
  const [weekDraftLabel, setWeekDraftLabel] = useState('');
  const [newProjectName, setNewProjectName] = useState('');
  const [confirmReset, setConfirmReset] = useState(false);
  const [digestSelection, setDigestSelection] = useState(null);
  const [digestDate, setDigestDate] = useState('latest');
  const [expandedHistoryId, setExpandedHistoryId] = useState(null);

  useEffect(() => { load(); }, []);

  async function load() {
    try {
      const res = await window.storage.get(STORAGE_KEY);
      if (res && res.value) {
        const parsed = JSON.parse(res.value);
        setProjects(parsed.projects || []);
        setSelectedId((parsed.projects || [])[0]?.id ?? null);
        setDigestSelection((parsed.projects || []).map(p => p.id));
        return;
      }
      const legacy = await window.storage.get(LEGACY_STORAGE_KEY);
      if (legacy && legacy.value) {
        const migrated = migrateV2ToV3(JSON.parse(legacy.value));
        setProjects(migrated.projects);
        setSelectedId(migrated.projects[0]?.id ?? null);
        setDigestSelection(migrated.projects.map(p => p.id));
        persist(migrated.projects);
        return;
      }
      const seed = seedData();
      setProjects(seed);
      setSelectedId(seed[0].id);
      setDigestSelection(seed.map(p => p.id));
      persist(seed);
    } catch (e) {
      const seed = seedData();
      setProjects(seed);
      setSelectedId(seed[0].id);
      setDigestSelection(seed.map(p => p.id));
    }
  }

  async function persist(next) {
    setSaveState('saving');
    try {
      const ok = await window.storage.set(STORAGE_KEY, JSON.stringify({ projects: next }));
      setSaveState(ok ? 'saved' : 'error');
      if (ok) setTimeout(() => setSaveState('idle'), 1500);
    } catch (e) {
      console.error('save failed', e);
      setSaveState('error');
    }
  }

  const update = useCallback((updater) => {
    setProjects(prev => {
      const next = updater(prev);
      persist(next);
      return next;
    });
  }, []);

  if (!projects) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 320, color: COLORS.textMuted, fontFamily: 'Inter, system-ui, sans-serif' }}>
        <Loader2 className="animate-spin" size={18} style={{ marginRight: 8 }} /> Đang tải dashboard...
      </div>
    );
  }

  const selectedProject = projects.find(p => p.id === selectedId) || null;
  const totalOverdue = projects.reduce((s, p) => s + getOverdueTasks(p).length, 0);
  const avgOverall = projects.length ? Math.round(projects.reduce((s, p) => s + overallProgress(p), 0) / projects.length) : 0;
  const allHistoryDates = Array.from(new Set(projects.flatMap(p => p.report.history.map(h => h.date)))).sort((a, b) => b.localeCompare(a));

  function updateTaskProgress(projectId, sectionId, taskId, progress) {
    update(prev => prev.map(p => p.id !== projectId ? p : {
      ...p,
      sections: p.sections.map(s => s.id !== sectionId ? s : {
        ...s,
        tasks: s.tasks.map(t => t.id !== taskId ? t : { ...t, progress: Math.max(0, Math.min(100, progress)) }),
      }),
    }));
  }

  function updateTaskSpan(projectId, sectionId, taskId, fields) {
    update(prev => prev.map(p => p.id !== projectId ? p : {
      ...p,
      sections: p.sections.map(s => s.id !== sectionId ? s : {
        ...s,
        tasks: s.tasks.map(t => t.id !== taskId ? t : { ...t, ...fields }),
      }),
    }));
  }

  function deleteTask(projectId, sectionId, taskId) {
    update(prev => prev.map(p => p.id !== projectId ? p : {
      ...p,
      sections: p.sections.map(s => s.id !== sectionId ? s : { ...s, tasks: s.tasks.filter(t => t.id !== taskId) }),
    }));
    setConfirmDelete(null);
  }

  function deleteSection(projectId, sectionId) {
    update(prev => prev.map(p => p.id !== projectId ? p : { ...p, sections: p.sections.filter(s => s.id !== sectionId) }));
    setConfirmDelete(null);
  }

  function deleteProjectFn(projectId) {
    update(prev => prev.filter(p => p.id !== projectId));
    setConfirmDelete(null);
    if (selectedId === projectId) {
      const remaining = projects.filter(p => p.id !== projectId);
      setSelectedId(remaining[0]?.id ?? null);
    }
  }

  function addTask(project, sectionId) {
    if (!taskDraft.name.trim()) return;
    update(prev => prev.map(p => p.id !== project.id ? p : {
      ...p,
      sections: p.sections.map(s => s.id !== sectionId ? s : {
        ...s,
        tasks: [...s.tasks, project.viewType === 'gantt'
          ? { id: uid(), name: taskDraft.name.trim(), pic: '', progress: 0, start: taskDraft.start === '' ? project.currentWeekIndex : Number(taskDraft.start), end: taskDraft.end === '' ? project.currentWeekIndex : Number(taskDraft.end) }
          : { id: uid(), name: taskDraft.name.trim(), progress: 0, clause: taskDraft.clause || '', dueDate: taskDraft.dueDate || 'TBD' }],
      }),
    }));
    setTaskDraft({ name: '', clause: '', dueDate: '', start: '', end: '' });
    setAddingTaskSection(null);
  }

  function addSection(projectId) {
    if (!sectionDraftName.trim()) return;
    update(prev => prev.map(p => p.id !== projectId ? p : { ...p, sections: [...p.sections, { id: uid(), name: sectionDraftName.trim(), tasks: [] }] }));
    setSectionDraftName('');
    setAddingSectionProject(null);
  }

  function addWeek(projectId) {
    if (!weekDraftLabel.trim()) return;
    update(prev => prev.map(p => p.id !== projectId ? p : { ...p, weeks: [...p.weeks, weekDraftLabel.trim()] }));
    setWeekDraftLabel('');
    setAddingWeek(false);
  }

  function setCurrentWeek(projectId, idx) {
    update(prev => prev.map(p => p.id !== projectId ? p : { ...p, currentWeekIndex: idx }));
  }

  function addProject() {
    if (!newProjectName.trim()) return;
    const np = { id: uid(), name: newProjectName.trim(), viewType: 'gantt', weeks: ['W1', 'W2', 'W3', 'W4'], currentWeekIndex: 0, sections: [{ id: uid(), name: 'General', tasks: [] }], report: { draft: emptyDraft(), history: [] } };
    update(prev => [...prev, np]);
    setSelectedId(np.id);
    setNewProjectName('');
    setDigestSelection(prev => [...(prev || []), np.id]);
  }

  function updateReportField(projectId, field, value) {
    update(prev => prev.map(p => p.id !== projectId ? p : { ...p, report: { ...p.report, draft: { ...p.report.draft, [field]: value } } }));
  }

  function appendIssue(projectId, text) {
    update(prev => prev.map(p => p.id !== projectId ? p : {
      ...p,
      report: { ...p.report, draft: { ...p.report.draft, issues: p.report.draft.issues ? `${p.report.draft.issues}\n- ${text}` : `- ${text}` } },
    }));
  }

  function logReport(projectId) {
    update(prev => prev.map(p => {
      if (p.id !== projectId) return p;
      const dateIso = todayISO();
      const overall = overallProgress(p);
      const overdueSnapshot = getOverdueTasks(p).map(t => t.name);
      const entry = { id: uid(), date: dateIso, ...p.report.draft, overall, overdueSnapshot };
      const existingIdx = p.report.history.findIndex(h => h.date === dateIso);
      const history = existingIdx >= 0
        ? p.report.history.map((h, i) => i === existingIdx ? entry : h)
        : [entry, ...p.report.history];
      return { ...p, report: { ...p.report, history } };
    }));
  }

  function restoreDraftFromEntry(projectId, entry) {
    update(prev => prev.map(p => p.id !== projectId ? p : {
      ...p,
      report: { ...p.report, draft: { doneLastWeek: entry.doneLastWeek, planNextWeek: entry.planNextWeek, issues: entry.issues } },
    }));
  }

  function copyText(key, text) {
    setCopied(key);
    try { navigator.clipboard.writeText(text).catch(() => {}); } catch (e) {}
    setTimeout(() => setCopied(null), 1500);
  }

  function resetToSeed() {
    const seed = seedData();
    update(() => seed);
    setSelectedId(seed[0].id);
    setDigestSelection(seed.map(p => p.id));
    setConfirmReset(false);
  }

  return (
    <div style={{ fontFamily: 'Inter, system-ui, sans-serif', background: COLORS.bg, minHeight: '100vh', color: COLORS.navy }}>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap');
        .mono { font-family: 'JetBrains Mono', ui-monospace, monospace; }
        input, textarea, select { font-family: inherit; }
        textarea:focus, input:focus, select:focus { outline: 2px solid ${COLORS.teal}; outline-offset: 1px; }
        ::-webkit-scrollbar { height: 8px; width: 8px; }
        ::-webkit-scrollbar-thumb { background: ${COLORS.border}; border-radius: 999px; }
      `}</style>

      <div className="flex" style={{ minHeight: '100vh' }}>
        <aside className="w-64 flex flex-col" style={{ background: COLORS.navy, color: '#fff', flexShrink: 0 }}>
          <div className="p-4" style={{ borderBottom: '1px solid rgba(255,255,255,0.12)' }}>
            <div style={{ fontSize: 11, letterSpacing: 1, color: 'rgba(255,255,255,0.55)', textTransform: 'uppercase' }}>Project Tracker</div>
            <div style={{ fontSize: 17, fontWeight: 700, marginTop: 2 }}>Dashboard</div>
          </div>

          <button
            onClick={() => setMainView('digest')}
            className="mx-3 mt-3 p-2 rounded flex items-center justify-between"
            style={{ background: mainView === 'digest' ? COLORS.teal : 'rgba(255,255,255,0.06)', border: 'none', cursor: 'pointer', color: '#fff', fontSize: 13, fontWeight: 600 }}
          >
            <span>📋 Weekly Digest</span>
            {totalOverdue > 0 && <span className="mono" style={{ background: COLORS.danger, borderRadius: 999, padding: '1px 7px', fontSize: 11 }}>{totalOverdue}</span>}
          </button>

          <div className="px-3 mt-4" style={{ fontSize: 11, letterSpacing: 1, color: 'rgba(255,255,255,0.4)', textTransform: 'uppercase' }}>Dự án</div>
          <div className="flex-1 overflow-auto px-2 mt-1">
            {projects.map(p => {
              const overdueCount = getOverdueTasks(p).length;
              const isActive = mainView === 'project' && selectedId === p.id;
              return (
                <div key={p.id} className="group relative mb-1">
                  <button
                    onClick={() => { setSelectedId(p.id); setMainView('project'); setProjectTab('timeline'); }}
                    className="w-full text-left p-2 rounded"
                    style={{ background: isActive ? 'rgba(15,163,163,0.25)' : 'transparent', border: isActive ? `1px solid ${COLORS.teal}` : '1px solid transparent', cursor: 'pointer' }}
                  >
                    <div style={{ fontSize: 13, fontWeight: 600, color: '#fff', paddingRight: 18 }}>{p.name}</div>
                    <div className="flex items-center gap-2 mt-1.5">
                      <MiniBar progress={overallProgress(p)} width={80} />
                      <span className="mono" style={{ fontSize: 11, color: 'rgba(255,255,255,0.6)' }}>{overallProgress(p)}%</span>
                      {overdueCount > 0 && <span className="flex items-center gap-1" style={{ fontSize: 11, color: '#FFB4C0' }}><AlertTriangle size={11} /> {overdueCount}</span>}
                      {p.report.history.length > 0 && <span className="mono" style={{ fontSize: 10, color: 'rgba(255,255,255,0.4)' }}>· {p.report.history.length} log</span>}
                    </div>
                  </button>
                  <span style={{ position: 'absolute', top: 6, right: 4 }} className="opacity-0 group-hover:opacity-100">
                    {confirmDelete && confirmDelete.type === 'project' && confirmDelete.id === p.id ? (
                      <span className="flex gap-1">
                        <button onClick={() => deleteProjectFn(p.id)} className="mono" style={{ fontSize: 10, background: COLORS.danger, color: '#fff', border: 'none', borderRadius: 4, padding: '2px 5px', cursor: 'pointer' }}>Xóa</button>
                        <button onClick={() => setConfirmDelete(null)} style={{ fontSize: 10, background: 'rgba(255,255,255,0.2)', color: '#fff', border: 'none', borderRadius: 4, padding: '2px 5px', cursor: 'pointer' }}>Hủy</button>
                      </span>
                    ) : (
                      <IconBtn title="Xóa dự án" onClick={() => setConfirmDelete({ type: 'project', id: p.id })}><Trash2 size={13} color="rgba(255,255,255,0.6)" /></IconBtn>
                    )}
                  </span>
                </div>
              );
            })}
          </div>

          <div className="p-3" style={{ borderTop: '1px solid rgba(255,255,255,0.12)' }}>
            <div className="flex gap-1">
              <input value={newProjectName} onChange={e => setNewProjectName(e.target.value)} onKeyDown={e => e.key === 'Enter' && addProject()} placeholder="Dự án mới..." className="flex-1 rounded px-2 py-1.5" style={{ background: 'rgba(255,255,255,0.08)', border: '1px solid rgba(255,255,255,0.15)', color: '#fff', fontSize: 12 }} />
              <button onClick={addProject} style={{ background: COLORS.teal, border: 'none', borderRadius: 6, padding: '0 8px', cursor: 'pointer' }}><Plus size={14} color="#fff" /></button>
            </div>
            <div className="mt-2">
              {confirmReset ? (
                <div className="flex gap-1">
                  <button onClick={resetToSeed} className="mono" style={{ fontSize: 11, background: COLORS.danger, color: '#fff', border: 'none', borderRadius: 4, padding: '3px 8px', cursor: 'pointer' }}>Xác nhận reset</button>
                  <button onClick={() => setConfirmReset(false)} style={{ fontSize: 11, background: 'rgba(255,255,255,0.15)', color: '#fff', border: 'none', borderRadius: 4, padding: '3px 8px', cursor: 'pointer' }}>Hủy</button>
                </div>
              ) : (
                <button onClick={() => setConfirmReset(true)} className="flex items-center gap-1" style={{ fontSize: 11, color: 'rgba(255,255,255,0.45)', background: 'transparent', border: 'none', cursor: 'pointer' }}><RotateCcw size={11} /> Reset về dữ liệu mẫu</button>
              )}
            </div>
          </div>
        </aside>

        <main className="flex-1 overflow-auto">
          <div className="flex items-center gap-6 px-6 py-3" style={{ background: COLORS.card, borderBottom: `1px solid ${COLORS.border}` }}>
            <div>
              <div style={{ fontSize: 11, color: COLORS.textFaint, textTransform: 'uppercase', letterSpacing: 0.5 }}>Số dự án</div>
              <div className="mono" style={{ fontSize: 20, fontWeight: 700 }}>{projects.length}</div>
            </div>
            <div>
              <div style={{ fontSize: 11, color: COLORS.textFaint, textTransform: 'uppercase', letterSpacing: 0.5 }}>Trung bình tiến độ</div>
              <div className="mono" style={{ fontSize: 20, fontWeight: 700, color: COLORS.teal }}>{avgOverall}%</div>
            </div>
            <div>
              <div style={{ fontSize: 11, color: COLORS.textFaint, textTransform: 'uppercase', letterSpacing: 0.5 }}>Task quá hạn</div>
              <div className="mono" style={{ fontSize: 20, fontWeight: 700, color: totalOverdue > 0 ? COLORS.danger : COLORS.success }}>{totalOverdue}</div>
            </div>
            <div className="ml-auto flex items-center gap-2" style={{ fontSize: 12, color: COLORS.textMuted }}>
              {saveState === 'saving' && <><Loader2 size={13} className="animate-spin" /> Đang lưu...</>}
              {saveState === 'saved' && <><Check size={13} color={COLORS.success} /> Đã lưu</>}
              {saveState === 'error' && <span style={{ color: COLORS.danger }}>Lỗi lưu dữ liệu</span>}
            </div>
          </div>

          {mainView === 'digest' && (
            <DigestView
              projects={projects} selection={digestSelection} setSelection={setDigestSelection}
              onCopy={copyText} copied={copied}
              digestDate={digestDate} setDigestDate={setDigestDate} allHistoryDates={allHistoryDates}
            />
          )}

          {mainView === 'project' && selectedProject && (
            <div className="p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 style={{ fontSize: 19, fontWeight: 700 }}>{selectedProject.name}</h2>
                <div className="flex gap-1 rounded p-1" style={{ background: COLORS.border }}>
                  {[['timeline', 'Timeline'], ['report', 'Weekly Report']].map(([k, label]) => (
                    <button key={k} onClick={() => setProjectTab(k)} style={{ fontSize: 13, fontWeight: 600, padding: '5px 12px', borderRadius: 5, border: 'none', cursor: 'pointer', background: projectTab === k ? COLORS.card : 'transparent', color: projectTab === k ? COLORS.navy : COLORS.textMuted }}>
                      {label}
                    </button>
                  ))}
                </div>
              </div>

              {projectTab === 'timeline' && (
                selectedProject.viewType === 'gantt'
                  ? <GanttView
                      project={selectedProject}
                      onUpdateTaskSpan={(sectionId, taskId, fields) => updateTaskSpan(selectedProject.id, sectionId, taskId, fields)}
                      editingProgressId={editingProgressId} setEditingProgressId={setEditingProgressId}
                      onProgressChange={(sid, tid, val) => updateTaskProgress(selectedProject.id, sid, tid, val)}
                      onSetCurrentWeek={idx => setCurrentWeek(selectedProject.id, idx)}
                      addingTaskSection={addingTaskSection} setAddingTaskSection={setAddingTaskSection}
                      taskDraft={taskDraft} setTaskDraft={setTaskDraft} onAddTask={addTask}
                      addingSectionProject={addingSectionProject} setAddingSectionProject={setAddingSectionProject}
                      sectionDraftName={sectionDraftName} setSectionDraftName={setSectionDraftName} onAddSection={addSection}
                      addingWeek={addingWeek} setAddingWeek={setAddingWeek}
                      weekDraftLabel={weekDraftLabel} setWeekDraftLabel={setWeekDraftLabel} onAddWeek={addWeek}
                      confirmDelete={confirmDelete} setConfirmDelete={setConfirmDelete}
                      onDeleteTask={deleteTask} onDeleteSection={deleteSection}
                    />
                  : <ChecklistView
                      project={selectedProject}
                      editingProgressId={editingProgressId} setEditingProgressId={setEditingProgressId}
                      onProgressChange={(sid, tid, val) => updateTaskProgress(selectedProject.id, sid, tid, val)}
                      addingTaskSection={addingTaskSection} setAddingTaskSection={setAddingTaskSection}
                      taskDraft={taskDraft} setTaskDraft={setTaskDraft} onAddTask={addTask}
                      addingSectionProject={addingSectionProject} setAddingSectionProject={setAddingSectionProject}
                      sectionDraftName={sectionDraftName} setSectionDraftName={setSectionDraftName} onAddSection={addSection}
                      confirmDelete={confirmDelete} setConfirmDelete={setConfirmDelete}
                      onDeleteTask={deleteTask} onDeleteSection={deleteSection}
                    />
              )}

              {projectTab === 'report' && (
                <ReportForm
                  project={selectedProject}
                  onChange={(field, val) => updateReportField(selectedProject.id, field, val)}
                  overdue={getOverdueTasks(selectedProject)}
                  onAppendIssue={text => appendIssue(selectedProject.id, text)}
                  onCopy={copyText} copied={copied}
                  onLog={() => logReport(selectedProject.id)}
                  onRestore={entry => restoreDraftFromEntry(selectedProject.id, entry)}
                  currentOverall={overallProgress(selectedProject)}
                  expandedHistoryId={expandedHistoryId} setExpandedHistoryId={setExpandedHistoryId}
                />
              )}
            </div>
          )}

          {mainView === 'project' && !selectedProject && (
            <div className="p-10 text-center" style={{ color: COLORS.textMuted }}>Chọn hoặc thêm một dự án ở sidebar.</div>
          )}
        </main>
      </div>
    </div>
  );
}

// ---------- Gantt view ----------

function GanttView({
  project, onUpdateTaskSpan, editingProgressId, setEditingProgressId, onProgressChange, onSetCurrentWeek,
  addingTaskSection, setAddingTaskSection, taskDraft, setTaskDraft, onAddTask,
  addingSectionProject, setAddingSectionProject, sectionDraftName, setSectionDraftName, onAddSection,
  addingWeek, setAddingWeek, weekDraftLabel, setWeekDraftLabel, onAddWeek,
  confirmDelete, setConfirmDelete, onDeleteTask, onDeleteSection,
}) {
  const nameColWidth = 280;
  const weeks = project.weeks;
  const gridRef = React.useRef(null);
  const [drag, setDrag] = React.useState(null); // { sectionId, taskId, mode: 'move'|'resize-start'|'resize-end', startClientX, origStart, origEnd, previewStart, previewEnd }
  const nowLeft = weeks.length ? `calc(${nameColWidth}px + (100% - ${nameColWidth}px) * ${(project.currentWeekIndex + 0.5) / weeks.length})` : null;
  const gridTemplate = `${nameColWidth}px repeat(${weeks.length}, minmax(46px, 1fr))`;

  function colWidth() {
    if (!gridRef.current) return 46;
    const rect = gridRef.current.getBoundingClientRect();
    return (rect.width - nameColWidth) / Math.max(weeks.length, 1);
  }

  function beginDrag(e, sectionId, taskId, mode, origStart, origEnd) {
    e.preventDefault();
    setDrag({ sectionId, taskId, mode, startClientX: e.clientX, origStart, origEnd, previewStart: origStart, previewEnd: origEnd });
  }

  React.useEffect(() => {
    if (!drag) return undefined;
    function onMove(e) {
      const deltaWeeks = Math.round((e.clientX - drag.startClientX) / colWidth());
      setDrag(d => {
        if (!d) return d;
        let previewStart = d.origStart;
        let previewEnd = d.origEnd;
        if (d.mode === 'move') {
          previewStart = d.origStart + deltaWeeks;
          previewEnd = d.origEnd + deltaWeeks;
        } else if (d.mode === 'resize-start') {
          previewStart = Math.min(d.origStart + deltaWeeks, d.origEnd);
        } else if (d.mode === 'resize-end') {
          previewEnd = Math.max(d.origEnd + deltaWeeks, d.origStart);
        }
        previewStart = Math.max(0, Math.min(previewStart, weeks.length - 1));
        previewEnd = Math.max(0, Math.min(previewEnd, weeks.length - 1));
        return { ...d, previewStart, previewEnd };
      });
    }
    function onUp() {
      setDrag(d => {
        if (d) onUpdateTaskSpan(d.sectionId, d.taskId, { start: d.previewStart, end: d.previewEnd });
        return null;
      });
    }
    window.addEventListener('pointermove', onMove);
    window.addEventListener('pointerup', onUp, { once: true });
    return () => window.removeEventListener('pointermove', onMove);
  }, [drag, weeks.length]);

  return (
    <div className="rounded" style={{ background: COLORS.card, border: `1px solid ${COLORS.border}`, overflow: 'hidden' }}>
      <div ref={gridRef} className="relative" style={{ overflowX: 'auto' }}>
        {nowLeft && (
          <div style={{ position: 'absolute', top: 0, bottom: 0, left: nowLeft, width: 2, background: COLORS.navy, zIndex: 5, pointerEvents: 'none' }}>
            <div className="mono" style={{ position: 'absolute', top: -2, left: 4, fontSize: 10, background: COLORS.navy, color: '#fff', padding: '1px 5px', borderRadius: 3, whiteSpace: 'nowrap' }}>hôm nay</div>
          </div>
        )}

        <div style={{ display: 'grid', gridTemplateColumns: gridTemplate, minWidth: nameColWidth + weeks.length * 46, borderBottom: `1px solid ${COLORS.border}`, background: '#FAFBFC' }}>
          <div className="p-2 flex items-center" style={{ fontSize: 12, fontWeight: 700, color: COLORS.textMuted }}>Sections & Tasks</div>
          {weeks.map((w, idx) => (
            <button key={idx} onClick={() => onSetCurrentWeek(idx)} title="Đặt làm tuần hiện tại" className="mono text-center py-2" style={{ fontSize: 11, fontWeight: idx === project.currentWeekIndex ? 700 : 500, color: idx === project.currentWeekIndex ? COLORS.navy : COLORS.textMuted, background: 'transparent', border: 'none', borderLeft: `1px solid ${COLORS.border}`, cursor: 'pointer' }}>
              {w}
            </button>
          ))}
        </div>

        {project.sections.map(section => {
          const secProgress = section.tasks.length ? Math.round(section.tasks.reduce((s, t) => s + t.progress, 0) / section.tasks.length) : 0;
          return (
            <div key={section.id}>
              <div style={{ display: 'grid', gridTemplateColumns: gridTemplate, minWidth: nameColWidth + weeks.length * 46, background: COLORS.tealSoft, borderBottom: `1px solid ${COLORS.border}` }}>
                <div className="px-2 py-1.5 flex items-center justify-between group">
                  <span style={{ fontSize: 12.5, fontWeight: 700, color: COLORS.navy }}>{section.name}</span>
                  <span className="flex items-center gap-2 opacity-0 group-hover:opacity-100">
                    <span className="mono" style={{ fontSize: 11, color: COLORS.textMuted }}>{secProgress}%</span>
                    {confirmDelete && confirmDelete.type === 'section' && confirmDelete.id === section.id ? (
                      <span className="flex gap-1">
                        <button onClick={() => onDeleteSection(project.id, section.id)} className="mono" style={{ fontSize: 10, background: COLORS.danger, color: '#fff', border: 'none', borderRadius: 4, padding: '1px 5px', cursor: 'pointer' }}>Xóa</button>
                        <button onClick={() => setConfirmDelete(null)} style={{ fontSize: 10, background: COLORS.border, border: 'none', borderRadius: 4, padding: '1px 5px', cursor: 'pointer' }}>Hủy</button>
                      </span>
                    ) : (
                      <IconBtn title="Xóa section" onClick={() => setConfirmDelete({ type: 'section', id: section.id })}><Trash2 size={12} /></IconBtn>
                    )}
                  </span>
                </div>
                {weeks.map((_, i) => <div key={i} style={{ borderLeft: `1px solid ${COLORS.border}` }} />)}
              </div>

              {section.tasks.map(task => {
                const overdue = isTaskOverdue(task, project);
                const c = statusColor(task.progress);
                const hasSpan = task.start !== null && task.start !== undefined && task.end !== null && task.end !== undefined;
                return (
                  <div key={task.id} style={{ display: 'grid', gridTemplateColumns: gridTemplate, minWidth: nameColWidth + weeks.length * 46, borderBottom: `1px solid ${COLORS.border}` }} className="group">
                    <div className="px-2 py-1.5 flex items-center gap-2" style={{ fontSize: 12.5 }}>
                      <button onClick={() => setEditingProgressId(editingProgressId === task.id ? null : task.id)} style={{ background: 'transparent', border: 'none', cursor: 'pointer', padding: 0 }}>
                        <ProgressPill progress={task.progress} />
                      </button>
                      <span style={{ flex: 1 }}>
                        {task.name}
                        {task.pic && <span className="mono" style={{ display: 'block', fontSize: 10.5, color: COLORS.textFaint }}>{task.pic}</span>}
                      </span>
                      {overdue && <AlertTriangle size={13} color={COLORS.danger} title="Quá hạn" />}
                      <span className="opacity-0 group-hover:opacity-100">
                        {confirmDelete && confirmDelete.type === 'task' && confirmDelete.id === task.id ? (
                          <span className="flex gap-1">
                            <button onClick={() => onDeleteTask(project.id, section.id, task.id)} className="mono" style={{ fontSize: 10, background: COLORS.danger, color: '#fff', border: 'none', borderRadius: 4, padding: '1px 5px', cursor: 'pointer' }}>Xóa</button>
                            <button onClick={() => setConfirmDelete(null)} style={{ fontSize: 10, background: COLORS.border, border: 'none', borderRadius: 4, padding: '1px 5px', cursor: 'pointer' }}>Hủy</button>
                          </span>
                        ) : (
                          <IconBtn title="Xóa task" onClick={() => setConfirmDelete({ type: 'task', id: task.id })}><Trash2 size={12} /></IconBtn>
                        )}
                      </span>
                    </div>
                    {weeks.map((_, i) => (
                      <div key={i} style={{ borderLeft: `1px solid ${COLORS.border}`, gridColumn: i + 2, gridRow: 1 }} />
                    ))}
                    {hasSpan && (() => {
                      const isDragging = drag && drag.taskId === task.id;
                      const barStart = isDragging ? drag.previewStart : task.start;
                      const barEnd = isDragging ? drag.previewEnd : task.end;
                      return (
                        <div
                          data-bar-for={task.id}
                          onPointerDown={e => beginDrag(e, section.id, task.id, 'move', task.start, task.end)}
                          style={{
                            gridColumn: `${barStart + 2} / ${barEnd + 3}`,
                            gridRow: 1,
                            alignSelf: 'center',
                            height: 16,
                            margin: '0 4px',
                            borderRadius: 4,
                            background: c.fg,
                            opacity: task.progress >= 100 ? 1 : 0.75,
                            position: 'relative',
                            cursor: 'grab',
                          }}
                        >
                          <div
                            onPointerDown={e => { e.stopPropagation(); beginDrag(e, section.id, task.id, 'resize-start', task.start, task.end); }}
                            style={{ position: 'absolute', left: -4, top: 0, bottom: 0, width: 8, cursor: 'ew-resize' }}
                          />
                          <div
                            onPointerDown={e => { e.stopPropagation(); beginDrag(e, section.id, task.id, 'resize-end', task.start, task.end); }}
                            style={{ position: 'absolute', right: -4, top: 0, bottom: 0, width: 8, cursor: 'ew-resize' }}
                          />
                        </div>
                      );
                    })()}
                    {editingProgressId === task.id && (
                      <div style={{ gridColumn: `1 / span ${weeks.length + 1}`, background: '#FAFBFC', borderTop: `1px dashed ${COLORS.border}`, padding: '6px 12px' }} className="flex items-center gap-3">
                        <input type="range" min="0" max="100" step="5" value={task.progress} onChange={e => onProgressChange(section.id, task.id, Number(e.target.value))} style={{ flex: 1, maxWidth: 240 }} />
                        <span className="mono" style={{ fontSize: 12, fontWeight: 600 }}>{task.progress}%</span>
                        <button onClick={() => setEditingProgressId(null)} style={{ background: COLORS.teal, color: '#fff', border: 'none', borderRadius: 5, padding: '3px 10px', fontSize: 12, cursor: 'pointer' }}>Xong</button>
                      </div>
                    )}
                  </div>
                );
              })}

              {addingTaskSection === section.id ? (
                <div style={{ display: 'grid', gridTemplateColumns: gridTemplate, minWidth: nameColWidth + weeks.length * 46, borderBottom: `1px solid ${COLORS.border}`, background: '#FAFBFC' }}>
                  <div className="px-2 py-1.5 flex items-center gap-1" style={{ gridColumn: `1 / span ${weeks.length + 1}` }}>
                    <input autoFocus value={taskDraft.name} onChange={e => setTaskDraft(d => ({ ...d, name: e.target.value }))} placeholder="Tên task" className="rounded px-2 py-1" style={{ border: `1px solid ${COLORS.border}`, fontSize: 12, flex: 1, maxWidth: 260 }} />
                    <span style={{ fontSize: 11, color: COLORS.textMuted }}>Từ tuần</span>
                    <select value={taskDraft.start} onChange={e => setTaskDraft(d => ({ ...d, start: e.target.value }))} className="rounded px-1 py-1" style={{ border: `1px solid ${COLORS.border}`, fontSize: 12 }}>
                      {weeks.map((w, i) => <option key={i} value={i}>{w}</option>)}
                    </select>
                    <span style={{ fontSize: 11, color: COLORS.textMuted }}>đến</span>
                    <select value={taskDraft.end} onChange={e => setTaskDraft(d => ({ ...d, end: e.target.value }))} className="rounded px-1 py-1" style={{ border: `1px solid ${COLORS.border}`, fontSize: 12 }}>
                      {weeks.map((w, i) => <option key={i} value={i}>{w}</option>)}
                    </select>
                    <button onClick={() => onAddTask(project, section.id)} style={{ background: COLORS.teal, color: '#fff', border: 'none', borderRadius: 5, padding: '4px 10px', fontSize: 12, cursor: 'pointer' }}>Thêm</button>
                    <IconBtn onClick={() => { setAddingTaskSection(null); setTaskDraft({ name: '', clause: '', dueDate: '', start: '', end: '' }); }}><X size={14} /></IconBtn>
                  </div>
                </div>
              ) : (
                <div style={{ display: 'grid', gridTemplateColumns: gridTemplate, minWidth: nameColWidth + weeks.length * 46, borderBottom: `1px solid ${COLORS.border}` }}>
                  <button onClick={() => setAddingTaskSection(section.id)} className="px-2 py-1.5 flex items-center gap-1" style={{ fontSize: 12, color: COLORS.teal, background: 'transparent', border: 'none', cursor: 'pointer', gridColumn: `1 / span ${weeks.length + 1}`, justifyContent: 'flex-start', width: '100%', textAlign: 'left' }}>
                    <Plus size={13} /> Thêm task
                  </button>
                </div>
              )}
            </div>
          );
        })}
      </div>

      <div className="flex items-center gap-4 p-3" style={{ borderTop: `1px solid ${COLORS.border}` }}>
        {addingSectionProject === project.id ? (
          <div className="flex items-center gap-1">
            <input autoFocus value={sectionDraftName} onChange={e => setSectionDraftName(e.target.value)} onKeyDown={e => e.key === 'Enter' && onAddSection(project.id)} placeholder="Tên section" className="rounded px-2 py-1" style={{ border: `1px solid ${COLORS.border}`, fontSize: 12 }} />
            <button onClick={() => onAddSection(project.id)} style={{ background: COLORS.teal, color: '#fff', border: 'none', borderRadius: 5, padding: '4px 10px', fontSize: 12, cursor: 'pointer' }}>Thêm</button>
            <IconBtn onClick={() => setAddingSectionProject(null)}><X size={14} /></IconBtn>
          </div>
        ) : (
          <button onClick={() => setAddingSectionProject(project.id)} className="flex items-center gap-1" style={{ fontSize: 12.5, color: COLORS.teal, background: 'transparent', border: 'none', cursor: 'pointer', fontWeight: 600 }}><Plus size={13} /> Thêm section</button>
        )}
        {addingWeek ? (
          <div className="flex items-center gap-1">
            <input autoFocus value={weekDraftLabel} onChange={e => setWeekDraftLabel(e.target.value)} onKeyDown={e => e.key === 'Enter' && onAddWeek(project.id)} placeholder="vd. Oct W1" className="rounded px-2 py-1" style={{ border: `1px solid ${COLORS.border}`, fontSize: 12, width: 100 }} />
            <button onClick={() => onAddWeek(project.id)} style={{ background: COLORS.teal, color: '#fff', border: 'none', borderRadius: 5, padding: '4px 10px', fontSize: 12, cursor: 'pointer' }}>Thêm</button>
            <IconBtn onClick={() => setAddingWeek(false)}><X size={14} /></IconBtn>
          </div>
        ) : (
          <button onClick={() => setAddingWeek(true)} className="flex items-center gap-1" style={{ fontSize: 12.5, color: COLORS.textMuted, background: 'transparent', border: 'none', cursor: 'pointer' }}><Plus size={13} /> Thêm cột tuần</button>
        )}
        <span style={{ fontSize: 11, color: COLORS.textFaint, marginLeft: 'auto' }}>Click vào tiêu đề tuần để đặt "hôm nay"</span>
      </div>
    </div>
  );
}

// ---------- Checklist view ----------

function ChecklistView({
  project, editingProgressId, setEditingProgressId, onProgressChange,
  addingTaskSection, setAddingTaskSection, taskDraft, setTaskDraft, onAddTask,
  addingSectionProject, setAddingSectionProject, sectionDraftName, setSectionDraftName, onAddSection,
  confirmDelete, setConfirmDelete, onDeleteTask, onDeleteSection,
}) {
  return (
    <div className="rounded" style={{ background: COLORS.card, border: `1px solid ${COLORS.border}`, overflow: 'hidden' }}>
      {project.sections.map(section => {
        const secProgress = section.tasks.length ? Math.round(section.tasks.reduce((s, t) => s + t.progress, 0) / section.tasks.length) : 0;
        return (
          <div key={section.id}>
            <div className="px-3 py-2 flex items-center justify-between group" style={{ background: COLORS.tealSoft, borderBottom: `1px solid ${COLORS.border}` }}>
              <span style={{ fontSize: 13, fontWeight: 700, color: COLORS.navy }}>
                {section.name}{section.pic ? <span className="mono" style={{ fontWeight: 500, color: COLORS.textMuted, fontSize: 12 }}> · PIC: {section.pic}</span> : null}
              </span>
              <span className="flex items-center gap-2">
                <span className="mono" style={{ fontSize: 12, color: COLORS.textMuted }}>{secProgress}%</span>
                {confirmDelete && confirmDelete.type === 'section' && confirmDelete.id === section.id ? (
                  <span className="flex gap-1">
                    <button onClick={() => onDeleteSection(project.id, section.id)} className="mono" style={{ fontSize: 10, background: COLORS.danger, color: '#fff', border: 'none', borderRadius: 4, padding: '1px 5px', cursor: 'pointer' }}>Xóa</button>
                    <button onClick={() => setConfirmDelete(null)} style={{ fontSize: 10, background: COLORS.border, border: 'none', borderRadius: 4, padding: '1px 5px', cursor: 'pointer' }}>Hủy</button>
                  </span>
                ) : (
                  <IconBtn title="Xóa section" onClick={() => setConfirmDelete({ type: 'section', id: section.id })}><Trash2 size={12} /></IconBtn>
                )}
              </span>
            </div>
            {section.tasks.map(task => {
              const overdue = isTaskOverdue(task, project);
              return (
                <div key={task.id} className="px-3 py-2 flex items-center gap-3 group" style={{ borderBottom: `1px solid ${COLORS.border}` }}>
                  <button onClick={() => setEditingProgressId(editingProgressId === task.id ? null : task.id)} style={{ background: 'transparent', border: 'none', cursor: 'pointer', padding: 0 }}>
                    <ProgressPill progress={task.progress} />
                  </button>
                  {task.no != null && <span className="mono" style={{ fontSize: 10.5, color: COLORS.textFaint, minWidth: 20 }}>#{task.no}</span>}
                  <span style={{ fontSize: 13, flex: 1 }}>{task.name}</span>
                  {task.clause && <span className="mono" style={{ fontSize: 11.5, color: COLORS.teal, background: COLORS.tealSoft, padding: '2px 7px', borderRadius: 4 }}>{task.clause}</span>}
                  <span className="mono" style={{ fontSize: 11.5, color: overdue ? COLORS.danger : COLORS.textMuted, fontWeight: overdue ? 700 : 500, minWidth: 90, textAlign: 'right' }}>
                    {task.dueDate === 'TBD' ? 'TBD' : task.dueDate === 'Done' ? 'Done' : task.dueDate}
                  </span>
                  {overdue && <AlertTriangle size={13} color={COLORS.danger} title="Quá hạn" />}
                  <span className="opacity-0 group-hover:opacity-100">
                    {confirmDelete && confirmDelete.type === 'task' && confirmDelete.id === task.id ? (
                      <span className="flex gap-1">
                        <button onClick={() => onDeleteTask(project.id, section.id, task.id)} className="mono" style={{ fontSize: 10, background: COLORS.danger, color: '#fff', border: 'none', borderRadius: 4, padding: '1px 5px', cursor: 'pointer' }}>Xóa</button>
                        <button onClick={() => setConfirmDelete(null)} style={{ fontSize: 10, background: COLORS.border, border: 'none', borderRadius: 4, padding: '1px 5px', cursor: 'pointer' }}>Hủy</button>
                      </span>
                    ) : (
                      <IconBtn title="Xóa task" onClick={() => setConfirmDelete({ type: 'task', id: task.id })}><Trash2 size={12} /></IconBtn>
                    )}
                  </span>
                </div>
              );
            })}
            {editingProgressId && section.tasks.some(t => t.id === editingProgressId) && (
              <div className="px-3 py-2 flex items-center gap-3" style={{ background: '#FAFBFC', borderBottom: `1px solid ${COLORS.border}` }}>
                {(() => {
                  const t = section.tasks.find(t => t.id === editingProgressId);
                  return (
                    <>
                      <input type="range" min="0" max="100" step="5" value={t.progress} onChange={e => onProgressChange(section.id, t.id, Number(e.target.value))} style={{ flex: 1, maxWidth: 240 }} />
                      <span className="mono" style={{ fontSize: 12, fontWeight: 600 }}>{t.progress}%</span>
                      <button onClick={() => setEditingProgressId(null)} style={{ background: COLORS.teal, color: '#fff', border: 'none', borderRadius: 5, padding: '3px 10px', fontSize: 12, cursor: 'pointer' }}>Xong</button>
                    </>
                  );
                })()}
              </div>
            )}
            {addingTaskSection === section.id ? (
              <div className="px-3 py-2 flex items-center gap-1 flex-wrap" style={{ borderBottom: `1px solid ${COLORS.border}`, background: '#FAFBFC' }}>
                <input autoFocus value={taskDraft.name} onChange={e => setTaskDraft(d => ({ ...d, name: e.target.value }))} placeholder="Tên task" className="rounded px-2 py-1" style={{ border: `1px solid ${COLORS.border}`, fontSize: 12, flex: 1, minWidth: 200 }} />
                <input value={taskDraft.clause} onChange={e => setTaskDraft(d => ({ ...d, clause: e.target.value }))} placeholder="Điều/clause" className="rounded px-2 py-1" style={{ border: `1px solid ${COLORS.border}`, fontSize: 12, width: 100 }} />
                <input value={taskDraft.dueDate} onChange={e => setTaskDraft(d => ({ ...d, dueDate: e.target.value }))} placeholder="YYYY-MM-DD / TBD" className="rounded px-2 py-1" style={{ border: `1px solid ${COLORS.border}`, fontSize: 12, width: 130 }} />
                <button onClick={() => onAddTask(project, section.id)} style={{ background: COLORS.teal, color: '#fff', border: 'none', borderRadius: 5, padding: '4px 10px', fontSize: 12, cursor: 'pointer' }}>Thêm</button>
                <IconBtn onClick={() => { setAddingTaskSection(null); setTaskDraft({ name: '', clause: '', dueDate: '', start: '', end: '' }); }}><X size={14} /></IconBtn>
              </div>
            ) : (
              <button onClick={() => setAddingTaskSection(section.id)} className="px-3 py-1.5 flex items-center gap-1" style={{ fontSize: 12, color: COLORS.teal, background: 'transparent', border: 'none', cursor: 'pointer', borderBottom: `1px solid ${COLORS.border}`, width: '100%', textAlign: 'left' }}>
                <Plus size={13} /> Thêm task
              </button>
            )}
          </div>
        );
      })}
      <div className="p-3">
        {addingSectionProject === project.id ? (
          <div className="flex items-center gap-1">
            <input autoFocus value={sectionDraftName} onChange={e => setSectionDraftName(e.target.value)} onKeyDown={e => e.key === 'Enter' && onAddSection(project.id)} placeholder="Tên section" className="rounded px-2 py-1" style={{ border: `1px solid ${COLORS.border}`, fontSize: 12 }} />
            <button onClick={() => onAddSection(project.id)} style={{ background: COLORS.teal, color: '#fff', border: 'none', borderRadius: 5, padding: '4px 10px', fontSize: 12, cursor: 'pointer' }}>Thêm</button>
            <IconBtn onClick={() => setAddingSectionProject(null)}><X size={14} /></IconBtn>
          </div>
        ) : (
          <button onClick={() => setAddingSectionProject(project.id)} className="flex items-center gap-1" style={{ fontSize: 12.5, color: COLORS.teal, background: 'transparent', border: 'none', cursor: 'pointer', fontWeight: 600 }}><Plus size={13} /> Thêm section</button>
        )}
      </div>
    </div>
  );
}

// ---------- Report form (draft + history) ----------

function ReportForm({ project, onChange, overdue, onAppendIssue, onCopy, copied, onLog, onRestore, currentOverall, expandedHistoryId, setExpandedHistoryId }) {
  const text = composeProjectReport(project);
  const key = `report-${project.id}`;
  const history = project.report.history;

  return (
    <div>
      <div className="grid gap-4" style={{ gridTemplateColumns: '1fr 1fr' }}>
        <div className="rounded p-4" style={{ background: COLORS.card, border: `1px solid ${COLORS.border}` }}>
          <label style={{ fontSize: 12, fontWeight: 700, color: COLORS.textMuted }}>✅ Tuần trước đã làm gì</label>
          <textarea value={project.report.draft.doneLastWeek} onChange={e => onChange('doneLastWeek', e.target.value)} rows={4} className="w-full rounded p-2 mt-1" style={{ border: `1px solid ${COLORS.border}`, fontSize: 13, resize: 'vertical' }} placeholder="- Hoàn tất BOM approval..." />

          <label style={{ fontSize: 12, fontWeight: 700, color: COLORS.textMuted, marginTop: 12, display: 'block' }}>🔜 Tuần tới định làm gì</label>
          <textarea value={project.report.draft.planNextWeek} onChange={e => onChange('planNextWeek', e.target.value)} rows={4} className="w-full rounded p-2 mt-1" style={{ border: `1px solid ${COLORS.border}`, fontSize: 13, resize: 'vertical' }} placeholder="- Setup server & license..." />

          <label style={{ fontSize: 12, fontWeight: 700, color: COLORS.textMuted, marginTop: 12, display: 'block' }}>⚠️ Issue / bottleneck</label>
          <textarea value={project.report.draft.issues} onChange={e => onChange('issues', e.target.value)} rows={3} className="w-full rounded p-2 mt-1" style={{ border: `1px solid ${COLORS.border}`, fontSize: 13, resize: 'vertical' }} placeholder="Không có" />

          {overdue.length > 0 && (
            <div className="mt-2 flex flex-wrap gap-1.5">
              <span style={{ fontSize: 11, color: COLORS.textFaint }}>Gợi ý (tự phát hiện task quá hạn):</span>
              {overdue.map(t => (
                <button key={t.id} onClick={() => onAppendIssue(`${t.name} (quá hạn)`)} className="flex items-center gap-1" style={{ fontSize: 11, background: COLORS.dangerBg, color: COLORS.danger, border: 'none', borderRadius: 999, padding: '2px 8px', cursor: 'pointer' }}>
                  <Plus size={10} /> {t.name}
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="rounded p-4" style={{ background: COLORS.card, border: `1px solid ${COLORS.border}` }}>
          <div className="flex items-center justify-between mb-2">
            <label style={{ fontSize: 12, fontWeight: 700, color: COLORS.textMuted }}>Preview — sẵn sàng gửi chat</label>
            <div className="flex gap-1.5">
              <button onClick={onLog} className="flex items-center gap-1" style={{ fontSize: 12, background: COLORS.navyLight, color: '#fff', border: 'none', borderRadius: 6, padding: '4px 10px', cursor: 'pointer' }}>
                <History size={13} /> Log tuần này
              </button>
              <button onClick={() => onCopy(key, text)} className="flex items-center gap-1" style={{ fontSize: 12, background: copied === key ? COLORS.success : COLORS.navy, color: '#fff', border: 'none', borderRadius: 6, padding: '4px 10px', cursor: 'pointer' }}>
                {copied === key ? <><Check size={13} /> Đã copy</> : <><Copy size={13} /> Copy</>}
              </button>
            </div>
          </div>
          <textarea readOnly value={text} rows={11} className="w-full rounded p-3 mono" style={{ border: `1px solid ${COLORS.border}`, fontSize: 12.5, background: '#FAFBFC', resize: 'vertical' }} />
          <div style={{ fontSize: 11, color: COLORS.textFaint, marginTop: 6 }}>"Log tuần này" lưu lại bản ghi có ngày tháng vào lịch sử bên dưới — không ghi đè bản trước.</div>
        </div>
      </div>

      <div className="rounded mt-4" style={{ background: COLORS.card, border: `1px solid ${COLORS.border}` }}>
        <div className="px-4 py-2.5 flex items-center gap-2" style={{ borderBottom: `1px solid ${COLORS.border}` }}>
          <History size={14} color={COLORS.textMuted} />
          <span style={{ fontSize: 13, fontWeight: 700, color: COLORS.textMuted }}>Lịch sử các lần đã log ({history.length})</span>
        </div>
        {history.length === 0 && <div className="px-4 py-4" style={{ fontSize: 12.5, color: COLORS.textFaint }}>Chưa có lần log nào. Điền nội dung ở trên rồi bấm "Log tuần này".</div>}
        {history.map(entry => {
          const delta = currentOverall - entry.overall;
          const isExpanded = expandedHistoryId === entry.id;
          return (
            <div key={entry.id} style={{ borderBottom: `1px solid ${COLORS.border}` }}>
              <button onClick={() => setExpandedHistoryId(isExpanded ? null : entry.id)} className="w-full px-4 py-2 flex items-center gap-3" style={{ background: 'transparent', border: 'none', cursor: 'pointer', textAlign: 'left' }}>
                <span className="mono" style={{ fontSize: 12.5, fontWeight: 700, minWidth: 90 }}>{formatDateLabel(entry.date)}</span>
                <ProgressPill progress={entry.overall} />
                {delta !== 0 && (
                  <span className="mono" style={{ fontSize: 11, color: delta > 0 ? COLORS.success : COLORS.danger }}>
                    {delta > 0 ? `+${delta}%` : `${delta}%`} đến hiện tại
                  </span>
                )}
                {entry.overdueSnapshot && entry.overdueSnapshot.length > 0 && (
                  <span className="flex items-center gap-1" style={{ fontSize: 11, color: COLORS.danger }}><AlertTriangle size={11} /> {entry.overdueSnapshot.length} quá hạn</span>
                )}
                <span style={{ marginLeft: 'auto', fontSize: 11, color: COLORS.textFaint }}>{isExpanded ? 'Thu gọn ▲' : 'Xem ▾'}</span>
              </button>
              {isExpanded && (
                <div className="px-4 pb-3 flex items-start gap-3">
                  <pre className="mono" style={{ flex: 1, fontSize: 12, background: '#FAFBFC', border: `1px solid ${COLORS.border}`, borderRadius: 6, padding: 10, whiteSpace: 'pre-wrap', margin: 0 }}>
                    {composeReportBlock(project.name, entry.overall, entry, entry.overdueSnapshot || [])}
                  </pre>
                  <button onClick={() => onRestore(entry)} className="flex items-center gap-1" style={{ fontSize: 11.5, background: COLORS.tealSoft, color: COLORS.teal, border: 'none', borderRadius: 6, padding: '5px 10px', cursor: 'pointer', whiteSpace: 'nowrap' }}>
                    <RefreshCcw size={12} /> Khôi phục vào draft
                  </button>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

// ---------- Digest view ----------

function DigestView({ projects, selection, setSelection, onCopy, copied, digestDate, setDigestDate, allHistoryDates }) {
  const sel = selection && selection.length ? selection : projects.map(p => p.id);
  const included = projects.filter(p => sel.includes(p.id));
  const headerLabel = digestDate === 'latest' ? todayLabel() : formatDateLabel(digestDate);
  const text = composeDigest(included, headerLabel, digestDate);
  const key = 'digest';

  function toggle(id) {
    setSelection(prev => {
      const cur = prev && prev.length ? prev : projects.map(p => p.id);
      return cur.includes(id) ? cur.filter(x => x !== id) : [...cur, id];
    });
  }

  return (
    <div className="p-6 grid gap-4" style={{ gridTemplateColumns: '280px 1fr' }}>
      <div className="rounded p-3" style={{ background: COLORS.card, border: `1px solid ${COLORS.border}`, height: 'fit-content' }}>
        <div style={{ fontSize: 12, fontWeight: 700, color: COLORS.textMuted, marginBottom: 6 }}>Xem lại theo ngày log</div>
        <select value={digestDate} onChange={e => setDigestDate(e.target.value)} className="w-full rounded px-2 py-1.5 mb-3" style={{ border: `1px solid ${COLORS.border}`, fontSize: 12.5 }}>
          <option value="latest">Hiện tại (bản nháp mới nhất)</option>
          {allHistoryDates.map(d => <option key={d} value={d}>{formatDateLabel(d)}</option>)}
        </select>

        <div style={{ fontSize: 12, fontWeight: 700, color: COLORS.textMuted, marginBottom: 8 }}>Chọn dự án đưa vào digest</div>
        {projects.map(p => (
          <label key={p.id} className="flex items-center gap-2 py-1.5" style={{ fontSize: 13, cursor: 'pointer' }}>
            <input type="checkbox" checked={sel.includes(p.id)} onChange={() => toggle(p.id)} />
            <span style={{ flex: 1 }}>{p.name}</span>
            <span className="mono" style={{ fontSize: 11, color: COLORS.textMuted }}>{overallProgress(p)}%</span>
          </label>
        ))}
      </div>

      <div className="rounded p-4" style={{ background: COLORS.card, border: `1px solid ${COLORS.border}` }}>
        <div className="flex items-center justify-between mb-2">
          <h3 style={{ fontSize: 15, fontWeight: 700 }}>Weekly Status Update — {headerLabel}</h3>
          <button onClick={() => onCopy(key, text)} className="flex items-center gap-1" style={{ fontSize: 12.5, background: copied === key ? COLORS.success : COLORS.navy, color: '#fff', border: 'none', borderRadius: 6, padding: '5px 12px', cursor: 'pointer' }}>
            {copied === key ? <><Check size={14} /> Đã copy</> : <><Copy size={14} /> Copy toàn bộ</>}
          </button>
        </div>
        <textarea readOnly value={text} rows={22} className="w-full rounded p-3 mono" style={{ border: `1px solid ${COLORS.border}`, fontSize: 12.5, background: '#FAFBFC', resize: 'vertical' }} />
        <div style={{ fontSize: 11, color: COLORS.textFaint, marginTop: 6 }}>
          Chọn "Hiện tại" để lấy bản nháp mới nhất, hoặc chọn 1 ngày đã log để xem lại digest tuần đó.
        </div>
      </div>
    </div>
  );
}
