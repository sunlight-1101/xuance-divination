export const STEMS = ['甲', '乙', '丙', '丁', '戊', '己', '庚', '辛', '壬', '癸']
export const BRANCHES = ['子', '丑', '寅', '卯', '辰', '巳', '午', '未', '申', '酉', '戌', '亥']
const JIAZI = Array.from({ length: 60 }, (_, index) => `${STEMS[index % 10]}${BRANCHES[index % 12]}`)
const STEM_ELEMENTS = {
  甲: '木',
  乙: '木',
  丙: '火',
  丁: '火',
  戊: '土',
  己: '土',
  庚: '金',
  辛: '金',
  壬: '水',
  癸: '水'
}
const STEM_YIN_YANG = {
  甲: '阳',
  乙: '阴',
  丙: '阳',
  丁: '阴',
  戊: '阳',
  己: '阴',
  庚: '阳',
  辛: '阴',
  壬: '阳',
  癸: '阴'
}
const GENERATES = { 木: '火', 火: '土', 土: '金', 金: '水', 水: '木' }
const CONTROLS = { 木: '土', 土: '水', 水: '火', 火: '金', 金: '木' }
const HIDDEN_STEMS = {
  子: ['癸'],
  丑: ['己', '癸', '辛'],
  寅: ['甲', '丙', '戊'],
  卯: ['乙'],
  辰: ['戊', '乙', '癸'],
  巳: ['丙', '戊', '庚'],
  午: ['丁', '己'],
  未: ['己', '丁', '乙'],
  申: ['庚', '壬', '戊'],
  酉: ['辛'],
  戌: ['戊', '辛', '丁'],
  亥: ['壬', '甲']
}
const NAYIN = {
  甲子: '海中金', 乙丑: '海中金', 丙寅: '炉中火', 丁卯: '炉中火', 戊辰: '大林木', 己巳: '大林木',
  庚午: '路旁土', 辛未: '路旁土', 壬申: '剑锋金', 癸酉: '剑锋金', 甲戌: '山头火', 乙亥: '山头火',
  丙子: '涧下水', 丁丑: '涧下水', 戊寅: '城头土', 己卯: '城头土', 庚辰: '白蜡金', 辛巳: '白蜡金',
  壬午: '杨柳木', 癸未: '杨柳木', 甲申: '泉中水', 乙酉: '泉中水', 丙戌: '屋上土', 丁亥: '屋上土',
  戊子: '霹雳火', 己丑: '霹雳火', 庚寅: '松柏木', 辛卯: '松柏木', 壬辰: '长流水', 癸巳: '长流水',
  甲午: '砂中金', 乙未: '砂中金', 丙申: '山下火', 丁酉: '山下火', 戊戌: '平地木', 己亥: '平地木',
  庚子: '壁上土', 辛丑: '壁上土', 壬寅: '金箔金', 癸卯: '金箔金', 甲辰: '覆灯火', 乙巳: '覆灯火',
  丙午: '天河水', 丁未: '天河水', 戊申: '大驿土', 己酉: '大驿土', 庚戌: '钗钏金', 辛亥: '钗钏金',
  壬子: '桑柘木', 癸丑: '桑柘木', 甲寅: '大溪水', 乙卯: '大溪水', 丙辰: '沙中土', 丁巳: '沙中土',
  戊午: '天上火', 己未: '天上火', 庚申: '石榴木', 辛酉: '石榴木', 壬戌: '大海水', 癸亥: '大海水'
}
const CHANG_SHENG = {
  甲: { 亥: '长生', 子: '沐浴', 丑: '冠带', 寅: '临官', 卯: '帝旺', 辰: '衰', 巳: '病', 午: '死', 未: '墓', 申: '绝', 酉: '胎', 戌: '养' },
  乙: { 午: '长生', 巳: '沐浴', 辰: '冠带', 卯: '临官', 寅: '帝旺', 丑: '衰', 子: '病', 亥: '死', 戌: '墓', 酉: '绝', 申: '胎', 未: '养' },
  丙: { 寅: '长生', 卯: '沐浴', 辰: '冠带', 巳: '临官', 午: '帝旺', 未: '衰', 申: '病', 酉: '死', 戌: '墓', 亥: '绝', 子: '胎', 丑: '养' },
  丁: { 酉: '长生', 申: '沐浴', 未: '冠带', 午: '临官', 巳: '帝旺', 辰: '衰', 卯: '病', 寅: '死', 丑: '墓', 子: '绝', 亥: '胎', 戌: '养' },
  戊: { 寅: '长生', 卯: '沐浴', 辰: '冠带', 巳: '临官', 午: '帝旺', 未: '衰', 申: '病', 酉: '死', 戌: '墓', 亥: '绝', 子: '胎', 丑: '养' },
  己: { 酉: '长生', 申: '沐浴', 未: '冠带', 午: '临官', 巳: '帝旺', 辰: '衰', 卯: '病', 寅: '死', 丑: '墓', 子: '绝', 亥: '胎', 戌: '养' },
  庚: { 巳: '长生', 午: '沐浴', 未: '冠带', 申: '临官', 酉: '帝旺', 戌: '衰', 亥: '病', 子: '死', 丑: '墓', 寅: '绝', 卯: '胎', 辰: '养' },
  辛: { 子: '长生', 亥: '沐浴', 戌: '冠带', 酉: '临官', 申: '帝旺', 未: '衰', 午: '病', 巳: '死', 辰: '墓', 卯: '绝', 寅: '胎', 丑: '养' },
  壬: { 申: '长生', 酉: '沐浴', 戌: '冠带', 亥: '临官', 子: '帝旺', 丑: '衰', 寅: '病', 卯: '死', 辰: '墓', 巳: '绝', 午: '胎', 未: '养' },
  癸: { 卯: '长生', 寅: '沐浴', 丑: '冠带', 子: '临官', 亥: '帝旺', 戌: '衰', 酉: '病', 申: '死', 未: '墓', 午: '绝', 巳: '胎', 辰: '养' }
}

// 2000-01-01 is 戊午日 in common perpetual-calendar references.
const BASE_UTC = Date.UTC(2000, 0, 1)
const BASE_INDEX = 54
const DAY_MS = 24 * 60 * 60 * 1000

export function getDayGanZhi(value) {
  const date = parseDate(value)
  if (!date) return { stem: '', branch: '', ganzhi: '' }
  const utc = Date.UTC(date.getFullYear(), date.getMonth(), date.getDate())
  const offset = Math.floor((utc - BASE_UTC) / DAY_MS)
  const index = mod(BASE_INDEX + offset, 60)
  const ganzhi = JIAZI[index]
  return { stem: ganzhi.slice(0, 1), branch: ganzhi.slice(1), ganzhi }
}

export function getYearGanZhi(value) {
  const date = parseDate(value)
  if (!date) return ''
  let year = date.getFullYear()
  // Practical first pass: use Feb 4 as the approximate Li Chun boundary.
  if (date.getMonth() === 0 || (date.getMonth() === 1 && date.getDate() < 4)) {
    year -= 1
  }
  const stem = STEMS[mod(year - 4, 10)]
  const branch = BRANCHES[mod(year - 4, 12)]
  return `${stem}${branch}`
}

export function getMonthGanZhi(dateValue, yearStem) {
  const date = parseDate(dateValue)
  if (!date || !yearStem) return ''
  const monthBranch = getApproxSolarMonthBranch(date)
  const stemStartByYearStem = {
    '甲': 2,
    '己': 2,
    '乙': 4,
    '庚': 4,
    '丙': 6,
    '辛': 6,
    '丁': 8,
    '壬': 8,
    '戊': 0,
    '癸': 0
  }
  const branchIndexFromYin = ['寅', '卯', '辰', '巳', '午', '未', '申', '酉', '戌', '亥', '子', '丑'].indexOf(monthBranch)
  if (branchIndexFromYin < 0) return ''
  const stem = STEMS[(stemStartByYearStem[yearStem] + branchIndexFromYin) % 10]
  return `${stem}${monthBranch}`
}

export function getHourGanZhi(dateValue, dayStem) {
  const date = parseDate(dateValue)
  if (!date || !dayStem) return ''
  const hour = date.getHours()
  const branchIndex = getHourBranchIndex(hour)
  const stemStartByDayStem = {
    '甲': 0,
    '己': 0,
    '乙': 2,
    '庚': 2,
    '丙': 4,
    '辛': 4,
    '丁': 6,
    '壬': 6,
    '戊': 8,
    '癸': 8
  }
  const stem = STEMS[(stemStartByDayStem[dayStem] + branchIndex) % 10]
  return `${stem}${BRANCHES[branchIndex]}`
}

export function getFourPillars(dateValue) {
  const date = parseDate(dateValue)
  if (!date) {
    return { yearPillar: '', monthPillar: '', dayPillar: '', hourPillar: '', dayMaster: '' }
  }
  const yearPillar = getYearGanZhi(date)
  const yearStem = yearPillar.slice(0, 1)
  const monthPillar = getMonthGanZhi(date, yearStem)
  const day = getDayGanZhi(date)
  const hourPillar = getHourGanZhi(date, day.stem)
  return {
    yearPillar,
    monthPillar,
    dayPillar: day.ganzhi,
    hourPillar,
    dayMaster: day.stem
  }
}

export function getBaziDetails(pillars) {
  const dayStem = pillars?.dayMaster || pillars?.dayPillar?.slice(0, 1) || ''
  const normalizedPillars = [
    { key: 'year', label: '年柱', pillar: pillars?.yearPillar || '' },
    { key: 'month', label: '月柱', pillar: pillars?.monthPillar || '' },
    { key: 'day', label: '日柱', pillar: pillars?.dayPillar || '' },
    { key: 'hour', label: '时柱', pillar: pillars?.hourPillar || '' }
  ]
  const emptyBranches = getEmptyBranches(pillars?.dayPillar || '')
  const pillarDetails = normalizedPillars.map((item) => {
    const stem = item.pillar.slice(0, 1)
    const branch = item.pillar.slice(1, 2)
    const hiddenStems = HIDDEN_STEMS[branch] || []
    const pillarEmptyBranches = getEmptyBranches(item.pillar)
    return {
      ...item,
      stem,
      branch,
      stemElement: STEM_ELEMENTS[stem] || '',
      branchElement: getBranchElement(branch),
      stemTenGod: item.key === 'day' ? '日主' : getTenGod(dayStem, stem),
      hiddenStems: hiddenStems.map(hiddenStem => ({
        stem: hiddenStem,
        element: STEM_ELEMENTS[hiddenStem] || '',
        tenGod: getTenGod(dayStem, hiddenStem)
      })),
      selfSitting: dayStem && branch ? CHANG_SHENG[dayStem]?.[branch] || '' : '',
      isEmpty: emptyBranches.includes(branch),
      emptyBranches: pillarEmptyBranches,
      emptyText: pillarEmptyBranches.length ? pillarEmptyBranches.join('') : '',
      naYin: NAYIN[item.pillar] || '',
      shenSha: getShenShaForPillar(dayStem, stem, branch, normalizedPillars)
    }
  })
  return {
    dayMaster: dayStem,
    emptyBranches,
    pillars: pillarDetails,
    summary: {
      tenGods: pillarDetails.map(item => `${item.label}${item.stemTenGod || '-'}`).join('，'),
      hiddenStems: pillarDetails.map(item => `${item.label}${item.hiddenStems.map(hidden => hidden.stem + hidden.tenGod).join('/') || '-'}`).join('，'),
      empty: emptyBranches.length ? emptyBranches.join('') : '无',
      shenSha: pillarDetails.flatMap(item => item.shenSha.map(name => `${item.label}${name}`)).join('，') || '无',
      luck: pillars?.luck?.currentLuck
        ? `${pillars.luck.direction}，${pillars.luck.startAge}起运，当前大运${pillars.luck.currentLuck}`
        : '未排'
    },
    luck: pillars?.luck || null
  }
}

export function getLuckCycles({ birthDate, birthTime, gender, yearPillar, monthPillar, currentDate = new Date() }) {
  const birth = parseDate(birthTime ? `${birthDate}T${birthTime}` : birthDate)
  if (!birth || !monthPillar || monthPillar.length < 2) {
    return { direction: '', startAge: '', currentLuck: '', cycles: [] }
  }
  const yearStem = yearPillar?.slice(0, 1) || getYearGanZhi(birth).slice(0, 1)
  const forward = isForwardLuck(gender, yearStem)
  const boundary = forward ? getNextSolarTermBoundary(birth) : getPrevSolarTermBoundary(birth)
  const diffDays = Math.abs(boundary.getTime() - birth.getTime()) / DAY_MS
  const startAgeNumber = Math.max(0, diffDays / 3)
  const startAge = formatStartAge(startAgeNumber)
  const monthIndex = JIAZI.indexOf(monthPillar)
  if (monthIndex < 0) {
    return { direction: forward ? '顺行' : '逆行', startAge, currentLuck: '', cycles: [] }
  }
  const birthYear = birth.getFullYear()
  const current = parseDate(currentDate) || new Date()
  const currentAge = Math.max(0, current.getFullYear() - birthYear + ((current.getMonth() + current.getDate() / 31) - (birth.getMonth() + birth.getDate() / 31)) / 12)
  const cycles = Array.from({ length: 10 }, (_, index) => {
    const pillarIndex = mod(monthIndex + (forward ? index + 1 : -(index + 1)), 60)
    const start = startAgeNumber + index * 10
    const end = start + 10
    return {
      index: index + 1,
      pillar: JIAZI[pillarIndex],
      startAge: Math.floor(start),
      endAge: Math.floor(end),
      startYear: birthYear + Math.floor(start),
      endYear: birthYear + Math.floor(end) - 1,
      active: currentAge >= start && currentAge < end
    }
  })
  const currentLuck = cycles.find(item => item.active)?.pillar || cycles[0]?.pillar || ''
  return {
    direction: forward ? '顺行' : '逆行',
    startAge,
    startAgeNumber,
    currentLuck,
    cycles
  }
}

function isForwardLuck(gender, yearStem) {
  const isYangYear = ['甲', '丙', '戊', '庚', '壬'].includes(yearStem)
  const normalizedGender = String(gender || '')
  const isMale = normalizedGender.includes('男')
  const isFemale = normalizedGender.includes('女')
  if (!isMale && !isFemale) return isYangYear
  return (isMale && isYangYear) || (isFemale && !isYangYear)
}

function formatStartAge(age) {
  let years = Math.floor(age)
  let months = Math.round((age - years) * 12)
  if (months >= 12) {
    years += 1
    months = 0
  }
  if (!years && !months) return '出生后不久'
  if (!months) return `${years}岁`
  if (!years) return `${months}个月`
  return `${years}岁${months}个月`
}

function getNextSolarTermBoundary(date) {
  return getSolarTermBoundariesAround(date).find(item => item.getTime() > date.getTime()) || new Date(date.getFullYear() + 1, 1, 4, 0, 0, 0)
}

function getPrevSolarTermBoundary(date) {
  const boundaries = getSolarTermBoundariesAround(date).filter(item => item.getTime() <= date.getTime())
  return boundaries[boundaries.length - 1] || new Date(date.getFullYear() - 1, 11, 7, 0, 0, 0)
}

function getSolarTermBoundariesAround(date) {
  const year = date.getFullYear()
  return [year - 1, year, year + 1].flatMap(buildSolarTermBoundaries).sort((a, b) => a.getTime() - b.getTime())
}

function buildSolarTermBoundaries(year) {
  return [
    [1, 6], [2, 4], [3, 6], [4, 5], [5, 6], [6, 6],
    [7, 7], [8, 8], [9, 8], [10, 8], [11, 7], [12, 7]
  ].map(([month, day]) => new Date(year, month - 1, day, 0, 0, 0))
}

function getBranchElement(branch) {
  if (['寅', '卯'].includes(branch)) return '木'
  if (['巳', '午'].includes(branch)) return '火'
  if (['申', '酉'].includes(branch)) return '金'
  if (['亥', '子'].includes(branch)) return '水'
  if (['辰', '戌', '丑', '未'].includes(branch)) return '土'
  return ''
}

export function getTenGod(dayStem, targetStem) {
  if (!dayStem || !targetStem || dayStem === targetStem) return dayStem === targetStem ? '比肩' : ''
  const dayElement = STEM_ELEMENTS[dayStem]
  const targetElement = STEM_ELEMENTS[targetStem]
  if (!dayElement || !targetElement) return ''
  const sameYinYang = STEM_YIN_YANG[dayStem] === STEM_YIN_YANG[targetStem]
  if (dayElement === targetElement) return sameYinYang ? '比肩' : '劫财'
  if (GENERATES[targetElement] === dayElement) return sameYinYang ? '偏印' : '正印'
  if (GENERATES[dayElement] === targetElement) return sameYinYang ? '食神' : '伤官'
  if (CONTROLS[dayElement] === targetElement) return sameYinYang ? '偏财' : '正财'
  if (CONTROLS[targetElement] === dayElement) return sameYinYang ? '七杀' : '正官'
  return ''
}

export function getEmptyBranches(dayPillar) {
  const index = JIAZI.indexOf(dayPillar)
  if (index < 0) return []
  const groupStart = Math.floor(index / 10) * 10
  const used = new Set(Array.from({ length: 10 }, (_, offset) => JIAZI[groupStart + offset].slice(1)))
  return BRANCHES.filter(branch => !used.has(branch))
}

function getShenShaForPillar(dayStem, stem, branch, pillars) {
  if (!dayStem || !branch) return []
  const names = []
  const branches = pillars.map(item => item.pillar.slice(1, 2)).filter(Boolean)
  const monthBranch = pillars.find(item => item.key === 'month')?.pillar.slice(1, 2)
  const yearBranch = pillars.find(item => item.key === 'year')?.pillar.slice(1, 2)
  const dayBranch = pillars.find(item => item.key === 'day')?.pillar.slice(1, 2)
  if (getTianYiBranches(dayStem).includes(branch)) names.push('天乙')
  if (getTaiJiBranches(dayStem).includes(branch)) names.push('太极')
  if (getGuoYinBranch(dayStem) === branch) names.push('国印')
  if (getWenChangBranch(dayStem) === branch) names.push('文昌')
  if (getLuBranch(dayStem) === branch) names.push('禄神')
  if (getJinYuBranch(dayStem) === branch) names.push('金舆')
  if (getYangRenBranch(dayStem) === branch) names.push('羊刃')
  if (getJiangXingBranch(dayBranch) === branch) names.push('将星')
  if (getTaoHuaBranch(dayBranch) === branch) names.push('桃花')
  if (getYiMaBranch(dayBranch) === branch) names.push('驿马')
  if (getHuaGaiBranch(dayBranch) === branch) names.push('华盖')
  if (getHongLuanBranch(yearBranch) === branch) names.push('红鸾')
  if (getTianXiBranch(yearBranch) === branch) names.push('天喜')
  if (getJieShaBranch(dayBranch) === branch) names.push('劫煞')
  if (getZaiShaBranch(dayBranch) === branch) names.push('灾煞')
  if (getWangShenBranch(dayBranch) === branch) names.push('亡神')
  if (matchesMonthVirtue(monthBranch, stem, branch, 'tianDe')) names.push('天德')
  if (matchesMonthVirtue(monthBranch, stem, branch, 'yueDe')) names.push('月德')
  if (branches.filter(item => item === branch).length > 1) names.push('伏吟')
  return Array.from(new Set(names))
}

function getTianYiBranches(dayStem) {
  return {
    甲: ['丑', '未'],
    戊: ['丑', '未'],
    庚: ['丑', '未'],
    乙: ['子', '申'],
    己: ['子', '申'],
    丙: ['亥', '酉'],
    丁: ['亥', '酉'],
    壬: ['卯', '巳'],
    癸: ['卯', '巳'],
    辛: ['寅', '午']
  }[dayStem] || []
}

function getTaiJiBranches(dayStem) {
  return {
    甲: ['子', '午'],
    乙: ['子', '午'],
    丙: ['卯', '酉'],
    丁: ['卯', '酉'],
    戊: ['辰', '戌', '丑', '未'],
    己: ['辰', '戌', '丑', '未'],
    庚: ['寅', '亥'],
    辛: ['寅', '亥'],
    壬: ['巳', '申'],
    癸: ['巳', '申']
  }[dayStem] || []
}

function getGuoYinBranch(dayStem) {
  return { 甲: '戌', 乙: '亥', 丙: '丑', 丁: '寅', 戊: '丑', 己: '寅', 庚: '辰', 辛: '巳', 壬: '未', 癸: '申' }[dayStem]
}

function getWenChangBranch(dayStem) {
  return { 甲: '巳', 乙: '午', 丙: '申', 丁: '酉', 戊: '申', 己: '酉', 庚: '亥', 辛: '子', 壬: '寅', 癸: '卯' }[dayStem]
}

function getLuBranch(dayStem) {
  return { 甲: '寅', 乙: '卯', 丙: '巳', 丁: '午', 戊: '巳', 己: '午', 庚: '申', 辛: '酉', 壬: '亥', 癸: '子' }[dayStem]
}

function getJinYuBranch(dayStem) {
  return { 甲: '辰', 乙: '巳', 丙: '未', 丁: '申', 戊: '未', 己: '申', 庚: '戌', 辛: '亥', 壬: '丑', 癸: '寅' }[dayStem]
}

function getYangRenBranch(dayStem) {
  return { 甲: '卯', 乙: '寅', 丙: '午', 丁: '巳', 戊: '午', 己: '巳', 庚: '酉', 辛: '申', 壬: '子', 癸: '亥' }[dayStem]
}

function getTaoHuaBranch(dayBranch) {
  if (['申', '子', '辰'].includes(dayBranch)) return '酉'
  if (['寅', '午', '戌'].includes(dayBranch)) return '卯'
  if (['亥', '卯', '未'].includes(dayBranch)) return '子'
  if (['巳', '酉', '丑'].includes(dayBranch)) return '午'
  return ''
}

function getJiangXingBranch(dayBranch) {
  if (['申', '子', '辰'].includes(dayBranch)) return '子'
  if (['寅', '午', '戌'].includes(dayBranch)) return '午'
  if (['亥', '卯', '未'].includes(dayBranch)) return '卯'
  if (['巳', '酉', '丑'].includes(dayBranch)) return '酉'
  return ''
}

function getYiMaBranch(dayBranch) {
  if (['申', '子', '辰'].includes(dayBranch)) return '寅'
  if (['寅', '午', '戌'].includes(dayBranch)) return '申'
  if (['亥', '卯', '未'].includes(dayBranch)) return '巳'
  if (['巳', '酉', '丑'].includes(dayBranch)) return '亥'
  return ''
}

function getHuaGaiBranch(dayBranch) {
  if (['申', '子', '辰'].includes(dayBranch)) return '辰'
  if (['寅', '午', '戌'].includes(dayBranch)) return '戌'
  if (['亥', '卯', '未'].includes(dayBranch)) return '未'
  if (['巳', '酉', '丑'].includes(dayBranch)) return '丑'
  return ''
}

function getHongLuanBranch(yearBranch) {
  return {
    子: '卯',
    丑: '寅',
    寅: '丑',
    卯: '子',
    辰: '亥',
    巳: '戌',
    午: '酉',
    未: '申',
    申: '未',
    酉: '午',
    戌: '巳',
    亥: '辰'
  }[yearBranch] || ''
}

function getTianXiBranch(yearBranch) {
  return {
    子: '酉',
    丑: '申',
    寅: '未',
    卯: '午',
    辰: '巳',
    巳: '辰',
    午: '卯',
    未: '寅',
    申: '丑',
    酉: '子',
    戌: '亥',
    亥: '戌'
  }[yearBranch] || ''
}

function getJieShaBranch(dayBranch) {
  if (['申', '子', '辰'].includes(dayBranch)) return '巳'
  if (['寅', '午', '戌'].includes(dayBranch)) return '亥'
  if (['亥', '卯', '未'].includes(dayBranch)) return '申'
  if (['巳', '酉', '丑'].includes(dayBranch)) return '寅'
  return ''
}

function getZaiShaBranch(dayBranch) {
  if (['申', '子', '辰'].includes(dayBranch)) return '午'
  if (['寅', '午', '戌'].includes(dayBranch)) return '子'
  if (['亥', '卯', '未'].includes(dayBranch)) return '酉'
  if (['巳', '酉', '丑'].includes(dayBranch)) return '卯'
  return ''
}

function getWangShenBranch(dayBranch) {
  if (['申', '子', '辰'].includes(dayBranch)) return '亥'
  if (['寅', '午', '戌'].includes(dayBranch)) return '巳'
  if (['亥', '卯', '未'].includes(dayBranch)) return '寅'
  if (['巳', '酉', '丑'].includes(dayBranch)) return '申'
  return ''
}

function matchesMonthVirtue(monthBranch, stem, branch, type) {
  const tianDe = {
    寅: '丁',
    卯: '申',
    辰: '壬',
    巳: '辛',
    午: '亥',
    未: '甲',
    申: '癸',
    酉: '寅',
    戌: '丙',
    亥: '乙',
    子: '巳',
    丑: '庚'
  }
  const yueDe = {
    寅: '丙',
    午: '丙',
    戌: '丙',
    申: '壬',
    子: '壬',
    辰: '壬',
    亥: '甲',
    卯: '甲',
    未: '甲',
    巳: '庚',
    酉: '庚',
    丑: '庚'
  }
  const target = type === 'tianDe' ? tianDe[monthBranch] : yueDe[monthBranch]
  return Boolean(target && (stem === target || branch === target))
}

export function parseDate(value) {
  if (!value) return null
  if (value instanceof Date && !Number.isNaN(value.getTime())) return value
  const normalized = String(value).replace(/\//g, '-').replace(' ', 'T')
  const date = new Date(normalized)
  if (Number.isNaN(date.getTime())) return null
  return date
}

function mod(value, divisor) {
  return ((value % divisor) + divisor) % divisor
}

function getApproxSolarMonthBranch(date) {
  const month = date.getMonth() + 1
  const day = date.getDate()
  const boundaries = [
    { m: 2, d: 4, b: '寅' },
    { m: 3, d: 6, b: '卯' },
    { m: 4, d: 5, b: '辰' },
    { m: 5, d: 6, b: '巳' },
    { m: 6, d: 6, b: '午' },
    { m: 7, d: 7, b: '未' },
    { m: 8, d: 8, b: '申' },
    { m: 9, d: 8, b: '酉' },
    { m: 10, d: 8, b: '戌' },
    { m: 11, d: 7, b: '亥' },
    { m: 12, d: 7, b: '子' },
    { m: 1, d: 6, b: '丑' }
  ]
  const key = month * 100 + day
  if (key >= 204 && key < 306) return '寅'
  if (key >= 306 && key < 405) return '卯'
  if (key >= 405 && key < 506) return '辰'
  if (key >= 506 && key < 606) return '巳'
  if (key >= 606 && key < 707) return '午'
  if (key >= 707 && key < 808) return '未'
  if (key >= 808 && key < 908) return '申'
  if (key >= 908 && key < 1008) return '酉'
  if (key >= 1008 && key < 1107) return '戌'
  if (key >= 1107 && key < 1207) return '亥'
  if (key >= 1207 || key < 106) return '子'
  return '丑'
}

function getHourBranchIndex(hour) {
  if (hour === 23 || hour === 0) return 0
  return Math.floor((hour + 1) / 2) % 12
}
