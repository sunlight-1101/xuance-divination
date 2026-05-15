const TRIGRAMS = {
  '111': '乾',
  '110': '兑',
  '101': '离',
  '100': '震',
  '011': '巽',
  '010': '坎',
  '001': '艮',
  '000': '坤'
}

const HEXAGRAMS = {
  '乾-乾': '乾为天',
  '坤-坤': '坤为地',
  '坎-震': '水雷屯',
  '艮-坎': '山水蒙',
  '坎-乾': '水天需',
  '乾-坎': '天水讼',
  '坤-坎': '地水师',
  '坎-坤': '水地比',
  '巽-乾': '风天小畜',
  '乾-兑': '天泽履',
  '坤-乾': '地天泰',
  '乾-坤': '天地否',
  '乾-离': '天火同人',
  '离-乾': '火天大有',
  '坤-艮': '地山谦',
  '震-坤': '雷地豫',
  '兑-震': '泽雷随',
  '艮-巽': '山风蛊',
  '坤-兑': '地泽临',
  '巽-坤': '风地观',
  '离-震': '火雷噬嗑',
  '艮-离': '山火贲',
  '艮-坤': '山地剥',
  '坤-震': '地雷复',
  '乾-震': '天雷无妄',
  '艮-乾': '山天大畜',
  '艮-震': '山雷颐',
  '兑-巽': '泽风大过',
  '坎-坎': '坎为水',
  '离-离': '离为火',
  '兑-艮': '泽山咸',
  '震-巽': '雷风恒',
  '乾-艮': '天山遁',
  '震-乾': '雷天大壮',
  '离-坤': '火地晋',
  '坤-离': '地火明夷',
  '巽-离': '风火家人',
  '离-兑': '火泽睽',
  '坎-艮': '水山蹇',
  '震-坎': '雷水解',
  '艮-兑': '山泽损',
  '巽-震': '风雷益',
  '兑-乾': '泽天夬',
  '乾-巽': '天风姤',
  '兑-坤': '泽地萃',
  '坤-巽': '地风升',
  '兑-坎': '泽水困',
  '坎-巽': '水风井',
  '兑-离': '泽火革',
  '离-巽': '火风鼎',
  '震-震': '震为雷',
  '艮-艮': '艮为山',
  '巽-艮': '风山渐',
  '震-兑': '雷泽归妹',
  '震-离': '雷火丰',
  '离-艮': '火山旅',
  '巽-巽': '巽为风',
  '兑-兑': '兑为泽',
  '巽-坎': '风水涣',
  '坎-兑': '水泽节',
  '巽-兑': '风泽中孚',
  '震-艮': '雷山小过',
  '坎-离': '水火既济',
  '离-坎': '火水未济'
}

const TRIGRAM_BRANCHES = {
  '乾': ['子', '寅', '辰', '午', '申', '戌'],
  '坤': ['未', '巳', '卯', '丑', '亥', '酉'],
  '震': ['子', '寅', '辰', '午', '申', '戌'],
  '巽': ['丑', '亥', '酉', '未', '巳', '卯'],
  '坎': ['寅', '辰', '午', '申', '戌', '子'],
  '离': ['卯', '丑', '亥', '酉', '未', '巳'],
  '艮': ['辰', '午', '申', '戌', '子', '寅'],
  '兑': ['巳', '卯', '丑', '亥', '酉', '未']
}

const BRANCH_ELEMENTS = {
  '子': '水',
  '亥': '水',
  '寅': '木',
  '卯': '木',
  '巳': '火',
  '午': '火',
  '申': '金',
  '酉': '金',
  '辰': '土',
  '戌': '土',
  '丑': '土',
  '未': '土'
}

const PALACE_ELEMENTS = {
  '乾': '金',
  '兑': '金',
  '离': '火',
  '震': '木',
  '巽': '木',
  '坎': '水',
  '艮': '土',
  '坤': '土'
}

const PALACE_GROUPS = {
  '乾': ['乾为天', '天风姤', '天山遁', '天地否', '风地观', '山地剥', '火地晋', '火天大有'],
  '兑': ['兑为泽', '泽水困', '泽地萃', '泽山咸', '水山蹇', '地山谦', '雷山小过', '雷泽归妹'],
  '离': ['离为火', '火山旅', '火风鼎', '火水未济', '山水蒙', '风水涣', '天水讼', '天火同人'],
  '震': ['震为雷', '雷地豫', '雷水解', '雷风恒', '地风升', '水风井', '泽风大过', '泽雷随'],
  '巽': ['巽为风', '风天小畜', '风火家人', '风雷益', '天雷无妄', '火雷噬嗑', '山雷颐', '山风蛊'],
  '坎': ['坎为水', '水泽节', '水雷屯', '水火既济', '泽火革', '雷火丰', '地火明夷', '地水师'],
  '艮': ['艮为山', '山火贲', '山天大畜', '山泽损', '火泽睽', '天泽履', '风泽中孚', '风山渐'],
  '坤': ['坤为地', '地雷复', '地泽临', '地天泰', '雷天大壮', '泽天夬', '水天需', '水地比']
}

const SHI_BY_PALACE_INDEX = [6, 1, 2, 3, 4, 5, 4, 3]

const SIX_GODS_START = {
  '甲': 0,
  '乙': 0,
  '丙': 1,
  '丁': 1,
  '戊': 2,
  '己': 3,
  '庚': 4,
  '辛': 4,
  '壬': 5,
  '癸': 5
}

const SIX_GODS = ['青龙', '朱雀', '勾陈', '螣蛇', '白虎', '玄武']

const GENERATES = {
  '木': '火',
  '火': '土',
  '土': '金',
  '金': '水',
  '水': '木'
}

const CONTROLS = {
  '木': '土',
  '土': '水',
  '水': '火',
  '火': '金',
  '金': '木'
}

export function tossCoins() {
  const coins = Array.from({ length: 3 }, () => secureRandomBit())
  const value = coins.reduce((sum, coin) => sum + (coin ? 3 : 2), 0)
  const detail = {
    6: { lineType: '老阴', changedLineType: '少阳', moving: true, yang: false, changedYang: true },
    7: { lineType: '少阳', changedLineType: '少阳', moving: false, yang: true, changedYang: true },
    8: { lineType: '少阴', changedLineType: '少阴', moving: false, yang: false, changedYang: false },
    9: { lineType: '老阳', changedLineType: '少阴', moving: true, yang: true, changedYang: false }
  }[value]
  return { coins, value, ...detail }
}

export function buildHexagram(yaoList, changed = false) {
  const detail = buildHexagramDetail(yaoList, changed)
  return detail.name
}

export function buildHexagramDetail(yaoList, changed = false) {
  if (yaoList.length !== 6 || yaoList.some(yao => typeof yao.yang !== 'boolean')) {
    return { name: '', upper: '', lower: '' }
  }
  const bits = yaoList.map(yao => changed ? yao.changedYang : yao.yang)
  const lower = TRIGRAMS[bits.slice(0, 3).map(bit => bit ? '1' : '0').join('')]
  const upper = TRIGRAMS[bits.slice(3, 6).map(bit => bit ? '1' : '0').join('')]
  const name = HEXAGRAMS[`${upper}-${lower}`] || `${upper}${lower}`
  return { name, upper, lower }
}

export function installHexagram(yaoList, dayStem) {
  const detail = buildHexagramDetail(yaoList)
  const palace = findPalace(detail.name)
  const palaceElement = PALACE_ELEMENTS[palace] || ''
  const lowerBranches = TRIGRAM_BRANCHES[detail.lower]?.slice(0, 3) || []
  const upperBranches = TRIGRAM_BRANCHES[detail.upper]?.slice(3, 6) || []
  const branches = [...lowerBranches, ...upperBranches]
  const shiPosition = findShiPosition(detail.name)
  const yingPosition = shiPosition ? ((shiPosition + 2) % 6) + 1 : 0
  const gods = buildSixGods(dayStem)

  yaoList.forEach((yao, index) => {
    const branch = branches[index] || ''
    const element = BRANCH_ELEMENTS[branch] || ''
    yao.branch = branch
    yao.element = element
    yao.sixRelative = buildSixRelative(palaceElement, element)
    yao.sixGod = gods[index] || ''
    yao.shi = index + 1 === shiPosition
    yao.ying = index + 1 === yingPosition
  })
  return { palace, palaceElement, shiPosition, yingPosition }
}

function findPalace(hexagramName) {
  return Object.entries(PALACE_GROUPS).find(([, names]) => names.includes(hexagramName))?.[0] || ''
}

function findShiPosition(hexagramName) {
  for (const names of Object.values(PALACE_GROUPS)) {
    const index = names.indexOf(hexagramName)
    if (index >= 0) return SHI_BY_PALACE_INDEX[index]
  }
  return 0
}

function buildSixRelative(palaceElement, yaoElement) {
  if (!palaceElement || !yaoElement) return ''
  if (palaceElement === yaoElement) return '兄弟'
  if (GENERATES[yaoElement] === palaceElement) return '父母'
  if (GENERATES[palaceElement] === yaoElement) return '子孙'
  if (CONTROLS[yaoElement] === palaceElement) return '官鬼'
  if (CONTROLS[palaceElement] === yaoElement) return '妻财'
  return ''
}

function buildSixGods(dayStem) {
  const start = SIX_GODS_START[dayStem]
  if (start === undefined) return []
  return Array.from({ length: 6 }, (_, index) => SIX_GODS[(start + index) % 6])
}

function secureRandomBit() {
  const values = new Uint32Array(1)
  crypto.getRandomValues(values)
  return values[0] % 2 === 1
}
