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

export function buildBirthTimeOptions(stepMinutes = 10) {
  const options = []
  for (let hour = 0; hour < 24; hour += 1) {
    for (let minute = 0; minute < 60; minute += stepMinutes) {
      const value = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
      options.push({
        value,
        label: `${hour}点${String(minute).padStart(2, '0')}分 · ${getTimeBranch(hour)}时`
      })
    }
  }
  return options
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
