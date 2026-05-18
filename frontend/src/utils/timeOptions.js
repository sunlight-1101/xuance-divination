const BRANCH_RANGES = [
  { branch: '子', start: 23, end: 0 },
  { branch: '丑', start: 1, end: 2 },
  { branch: '寅', start: 3, end: 4 },
  { branch: '卯', start: 5, end: 6 },
  { branch: '辰', start: 7, end: 8 },
  { branch: '巳', start: 9, end: 10 },
  { branch: '午', start: 11, end: 12 },
  { branch: '未', start: 13, end: 14 },
  { branch: '申', start: 15, end: 16 },
  { branch: '酉', start: 17, end: 18 },
  { branch: '戌', start: 19, end: 20 },
  { branch: '亥', start: 21, end: 22 }
]

export function getTimeBranch(hour) {
  const value = Number(hour)
  return BRANCH_RANGES.find(item => item.start <= item.end
    ? value >= item.start && value <= item.end
    : value >= item.start || value <= item.end)?.branch || ''
}

export function buildHourOptions() {
  return Array.from({ length: 24 }, (_, hour) => ({
    value: String(hour).padStart(2, '0'),
    label: `${hour}点 · ${getTimeBranch(hour)}时`
  }))
}

export function buildMinuteOptions() {
  return Array.from({ length: 60 }, (_, minute) => ({
    value: String(minute).padStart(2, '0'),
    label: `${minute}分`
  }))
}

export function combineTimeParts(hour, minute = '00') {
  if (hour === '' || hour == null) return ''
  return `${String(hour).padStart(2, '0')}:${String(minute || '00').padStart(2, '0')}`
}

export function splitTimeParts(value) {
  const normalized = normalizeToOptionTime(value)
  const match = normalized.match(/^(\d{2}):(\d{2})$/)
  return match ? { hour: match[1], minute: match[2] } : { hour: '', minute: '30' }
}

export function normalizeToOptionTime(value) {
  const text = String(value || '').trim()
  const branch = BRANCH_RANGES.find(item => text.includes(`${item.branch}时`) || text === item.branch)
  if (branch) {
    const hour = branch.branch === '子' ? 0 : branch.start
    return `${String(hour).padStart(2, '0')}:30`
  }
  const match = text.match(/^(\d{1,2})[:：点时](\d{1,2})?/)
  if (!match) return text
  const hour = Math.min(23, Math.max(0, Number(match[1])))
  const minute = Math.min(59, Math.max(0, Number(match[2] || 0)))
  return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
}
