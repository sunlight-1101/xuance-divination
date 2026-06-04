<template>
  <div class="page ziwei-page tool-page-ziwei">
    <header class="hero-head">
      <h1>紫微斗数排盘</h1>
      <p>输入出生信息，生成紫微命盘，获取解读与问答</p>
    </header>

    <section class="ziwei-shell">
      <h2 class="form-section-title">基本信息</h2>
      <el-form :model="form" label-position="top" class="plain-form">
        <el-row :gutter="22">
          <el-col :xs="24" :sm="12">
            <el-form-item label="姓名">
              <el-input v-model="form.name" placeholder="请输入姓名（可选）" />
              <div class="field-tip">姓名仅用于记录，不影响排盘结果</div>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="性别 *">
              <el-select v-model="form.gender">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <h2 class="form-section-title time-title">时间信息</h2>
        <el-row :gutter="22">
          <el-col :xs="24" :sm="6">
            <el-form-item label="出生日期 *">
              <div class="segmented">
                <button type="button" :class="{ active: form.calendarType === 'SOLAR' }" @click="form.calendarType = 'SOLAR'">阳历</button>
                <button type="button" :class="{ active: form.calendarType === 'LUNAR' }" @click="form.calendarType = 'LUNAR'">农历</button>
              </div>
              <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" placeholder="选择年月日" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="6" v-if="form.calendarType === 'LUNAR'">
            <el-form-item label="闰月">
              <el-switch v-model="form.leapMonth" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="出生时辰 *">
              <el-select v-model="form.birthHour">
                <el-option v-for="hour in hourOptions" :key="hour.value" :label="hour.label" :value="hour.value" />
              </el-select>
              <div class="field-tip">紫微斗数必须提供准确的出生时辰</div>
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="分钟">
              <el-select v-model="form.birthMinute">
                <el-option v-for="minute in minuteOptions" :key="minute" :label="`${minute}分`" :value="minute" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="出生地点 *">
          <el-row :gutter="10" class="place-row">
            <el-col :xs="12">
              <el-select v-model="form.birthProvince" filterable placeholder="选择省份" @change="onProvinceChange">
                <el-option v-for="province in provinceOptions" :key="province.name" :label="province.name" :value="province.name" />
              </el-select>
            </el-col>
            <el-col :xs="12">
              <el-select v-model="form.birthPlace" filterable placeholder="选择城市" :disabled="!form.birthProvince" @change="onBirthPlaceChange">
                <el-option v-for="city in cityOptions" :key="city.name" :label="city.name" :value="city.name" />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>

        <div class="switch-list">
          <label>
            <el-switch v-model="form.useTrueSolarTime" />
            <span>使用真太阳时</span>
          </label>
          <label>
            <el-switch v-model="form.useEarlyLateZi" disabled />
            <span>早晚子时</span>
          </label>
        </div>
        <p class="solar-text">{{ trueSolarTimeText }}</p>

        <el-button class="chart-button" :loading="loading" @click="submit">排盘</el-button>
      </el-form>

      <div v-if="chart" class="chart-wrap">
        <div class="ziwei-board">
          <template v-for="cell in boardCells" :key="cell.key">
            <div v-if="cell.center" class="center-cell">
              <strong>{{ form.name || '未命名' }} · {{ form.gender || '未填' }} · {{ chart.fiveElementBureau }}</strong>
              <span>命主 {{ chart.lifeMaster }} · 身主 {{ chart.bodyMaster }}</span>
              <span>命宫 {{ chart.mingGong }} · 身宫 {{ chart.shenGong }} · 斗君 {{ chart.douJun }}</span>
              <span>输入时间 {{ chart.input?.birthTime }}</span>
              <span>阳历生日 {{ chart.input?.birthDate }}</span>
              <span>农历生日 {{ chart.lunar?.display }}</span>
            </div>
            <div v-else class="palace-card" :class="{ ming: cell.palace?.isMing, shen: cell.palace?.isShen }">
              <div class="palace-title">
                <strong>{{ cell.palace?.name }}</strong>
                <span v-if="cell.palace?.isMing">[命]</span>
                <span v-if="cell.palace?.isShen">[身]</span>
              </div>
              <div class="palace-stars">
                <b v-for="star in cell.palace?.mainStars" :key="star">{{ star }}</b>
                <b v-if="!cell.palace?.mainStars?.length" class="empty-star">借宫</b>
                <em v-for="star in cell.palace?.transformations" :key="star">{{ star.slice(-2, -1) }}</em>
              </div>
              <div class="minor-stars">
                <span v-for="star in cell.palace?.luckyStars" :key="star">{{ star }}</span>
                <span v-for="star in cell.palace?.shaStars" :key="star" class="sha">{{ star }}</span>
                <span v-for="star in cell.palace?.otherStars" :key="star" class="other">{{ star }}</span>
              </div>
              <div class="palace-foot">
                <small>{{ cell.palace?.doctorStar || '' }}</small>
                <small>{{ cell.palace?.lifeStage || '' }}</small>
                <small>{{ cell.palace?.yearGod || '' }}</small>
                <small>{{ cell.palace?.generalGod || '' }}</small>
                <small v-if="cell.palace?.majorLuck" class="year-list">流年 {{ flowYears(cell.palace) }}</small>
                <small class="year-list">小限 {{ minorYears(cell.palace) }}</small>
                <strong>{{ cell.palace?.stem }}<br />{{ cell.palace?.branch }}</strong>
              </div>
            </div>
          </template>
        </div>

        <div class="copy-line">
          <el-button @click="copyChart">复制命盘</el-button>
        </div>

        <div class="chart-tabs">
          <button
            v-for="tab in chartTabs"
            :key="tab.key"
            type="button"
            :class="{ active: activeChartTab === tab.key }"
            @click="activeChartTab = tab.key"
          >{{ tab.label }}</button>
        </div>

        <div class="sihua-line">
          <template v-if="activeChartTab === 'year' && chart.flowYear">
            流年（{{ chart.flowYear.ganZhi }}）四化：
            <span v-for="[star, hua] in Object.entries(chart.flowYear.transformations || {})" :key="star">{{ star }} {{ hua.replace('化', '') }}</span>
          </template>
          <template v-else>
            生年（{{ chart.yearGanZhi }}）四化：
            <span v-for="[star, hua] in Object.entries(chart.transformations || {})" :key="star">{{ star }} {{ hua.replace('化', '') }}</span>
          </template>
        </div>

        <section class="fortune-panel" v-if="activeChartTab === 'luck'">
          <div class="fortune-timeline">
            <div class="fortune-track">
              <div class="fortune-line"></div>
              <div
                v-for="(item, idx) in activeDisplayItems"
                :key="item.title"
                class="fortune-node"
                :class="{ current: item.current, past: !item.current && idx < currentLuckIndex }"
              >
                <div class="node-dot">
                  <span v-if="item.current" class="node-pulse"></span>
                </div>
                <div class="node-body">
                  <div class="node-header">
                    <span class="node-palace">{{ item.label }}</span>
                    <small v-if="item.current" class="current-badge">当前</small>
                  </div>
                  <strong class="node-age">{{ item.title }}</strong>
                  <small class="node-year">{{ item.desc }}</small>
                  <div v-if="item.stars" class="node-stars">{{ item.stars }}</div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <section class="fortune-panel fortune-grid" v-else>
          <div v-for="item in activeDisplayItems" :key="item.title" class="fortune-card" :class="{ current: item.current }">
            <div class="fortune-card-head">
              <span>{{ item.label }}</span>
              <small v-if="item.current" class="current-badge">当前</small>
            </div>
            <strong>{{ item.title }}</strong>
            <small>{{ item.desc }}</small>
            <div v-if="item.stars" class="fortune-stars">{{ item.stars }}</div>
          </div>
        </section>

        <section class="ai-panel">
          <div>
            <h2>紫微分析</h2>
            <p>结合命盘结构、主星和四化，生成结构化的紫微斗数解读</p>
          </div>
          <div class="hecan-toggle">
            <el-switch v-model="analysisForm.hecan" />
            <span>同时参考八字（八字紫微合参）</span>
          </div>
          <el-button class="ai-button" :loading="analysisLoading" @click="analyzeCurrentChart">{{ analysisForm.hecan ? '开始合参分析' : '开始分析' }}</el-button>
          <el-form label-position="top" class="analysis-form">
            <el-form-item label="分析方向">
              <el-select v-model="analysisForm.questionType">
                <el-option label="综合命盘" value="综合命盘" />
                <el-option label="事业" value="事业" />
                <el-option label="财运" value="财运" />
                <el-option label="感情" value="感情" />
                <el-option label="健康" value="健康" />
                <el-option label="流年趋势" value="流年趋势" />
              </el-select>
            </el-form-item>
            <el-form-item label="具体问题">
              <el-input v-model="analysisForm.question" type="textarea" :rows="3" placeholder="例如：这张盘的事业发展和近几年机会如何？" />
            </el-form-item>
          </el-form>
          <div v-if="analysisLoading" class="analysis-placeholder">正在生成分析，预计 60-120 秒。切屏后可回到本页恢复，也可以在历史记录查看结果，请不要重复点击。</div>
          <div v-if="analysisNotice" class="analysis-placeholder notice">{{ analysisNotice }}</div>
          <ResultReport v-else-if="parsedAnalysis" :report="parsedAnalysis" />
          <KnowledgeReferences
            v-if="knowledgeRules.length || classicReferences.length"
            :rules="knowledgeRules"
            :classic-references="classicReferences"
          />
          <div v-if="!analysisLoading && !parsedAnalysis" class="analysis-placeholder">点击「开始分析」后，这里会展示完整的紫微斗数解读</div>
        </section>
      </div>

      <el-empty v-else description="填写出生信息后生成命盘" />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { updateProfile } from '../../api/auth'
import { analyzeZiwei, buildZiweiChart } from '../../api/ziwei'
import { analyzeHeCan } from '../../api/bazi'
import { getFourPillars, getBaziDetails, getLuckCycles, getTenGod } from '../../utils/ganzhi'
import KnowledgeReferences from '../../components/KnowledgeReferences.vue'
import ResultReport from '../../components/ResultReport.vue'
import { useUserStore } from '../../stores/user'
import { provinceOptions } from '../../utils/chinaCities'
import { buildHourOptions, buildMinuteOptions } from '../../utils/timeOptions'
import { clearAnalysisCache, finishPendingAnalysis, readAnalysisCache, readPendingAnalysis, saveAnalysisCache, startPendingAnalysis } from '../../utils/analysisCache'
import { waitForAnalysisRecord } from '../../utils/analysisPolling'

const userStore = useUserStore()
const loading = ref(false)
const analysisLoading = ref(false)
const chart = ref(null)
const activeChartTab = ref('base')
const analysisResult = ref('')
const knowledgeRules = ref([])
const classicReferences = ref([])
const analysisNotice = ref('')
const CACHE_KEY = 'zhexuan_last_ziwei_report'
const PENDING_KEY = 'zhexuan_pending_ziwei_analysis'

const hourOptions = buildHourOptions()
const minuteOptions = buildMinuteOptions().map(item => item.value)
const chartTabs = [
  { key: 'base', label: '本命盘' },
  { key: 'luck', label: '大限盘' },
  { key: 'year', label: '流年盘' }
]

const form = reactive({
  userId: userStore.userId,
  name: '',
  gender: '男',
  calendarType: 'SOLAR',
  birthDate: '',
  birthHour: '00',
  birthMinute: '30',
  birthProvince: '',
  birthPlace: '',
  leapMonth: false,
  useTrueSolarTime: false,
  useEarlyLateZi: false
})

const analysisForm = reactive({
  questionType: '综合命盘',
  question: '请综合分析这张紫微斗数命盘，重点说明性格底色、事业财运、感情关系、大限趋势和需要注意的风险。',
  hecan: false
})

const birthTime = computed(() => `${form.birthHour}:${form.birthMinute}`)
const cityOptions = computed(() => provinceOptions.find(item => item.name === form.birthProvince)?.cities || [])
const selectedCity = computed(() => getCityLocation(form.birthPlace))
const effectiveBirth = computed(() => getEffectiveBirth())

const boardCells = computed(() => {
  if (!chart.value) return []
  const byBranch = Object.fromEntries(chart.value.palaces.map(item => [item.branch, item]))
  return [
    { key: '巳', palace: byBranch['巳'] },
    { key: '午', palace: byBranch['午'] },
    { key: '未', palace: byBranch['未'] },
    { key: '申', palace: byBranch['申'] },
    { key: '辰', palace: byBranch['辰'] },
    { key: 'center', center: true },
    { key: '酉', palace: byBranch['酉'] },
    { key: '卯', palace: byBranch['卯'] },
    { key: '戌', palace: byBranch['戌'] },
    { key: '寅', palace: byBranch['寅'] },
    { key: '丑', palace: byBranch['丑'] },
    { key: '子', palace: byBranch['子'] },
    { key: '亥', palace: byBranch['亥'] }
  ]
})

const activeDisplayItems = computed(() => {
  if (!chart.value) return []
  if (activeChartTab.value === 'luck') {
    const currentYear = new Date().getFullYear()
    const birthYear = chart.value.lunar?.year || currentYear - 25
    const currentAge = currentYear - birthYear + 1
    return chart.value.palaces
      .filter(item => item.majorLuck)
      .sort((a, b) => a.majorLuck.startAge - b.majorLuck.startAge)
      .slice(0, 12)
      .map(item => ({
        label: item.name,
        title: `${item.majorLuck.startAge}-${item.majorLuck.endAge}岁`,
        desc: `${item.majorLuck.startYear}-${item.majorLuck.endYear} · ${item.ganZhi}`,
        current: currentAge >= item.majorLuck.startAge && currentAge <= item.majorLuck.endAge,
        stars: [...(item.mainStars || []), ...(item.transformations || [])].join(' ')
      }))
  }
  if (activeChartTab.value === 'year') {
    const fy = chart.value.flowYear
    if (fy && fy.palaces) {
      return fy.palaces.map(item => ({
        label: item.name,
        title: item.mainStars?.length ? item.mainStars.join('、') : item.basePalaceName,
        desc: `${item.ganZhi} · ${item.basePalaceName}${item.isFlowYearMing ? ' [流年命]' : ''}`
      }))
    }
    return chart.value.palaces.map(item => ({
      label: item.name,
      title: item.yearGod || '流年参考',
      desc: `${item.generalGod || ''} ${item.lifeStage || ''} ${item.ganZhi}`.trim()
    }))
  }
  return chart.value.palaces.map(item => ({
    label: item.name,
    title: item.mainStars?.length ? item.mainStars.join('、') : '无主星',
    desc: `${item.ganZhi} · ${item.transformations?.join(' ') || '无四化'}`
  }))
})

const currentLuckIndex = computed(() => {
  return activeDisplayItems.value.findIndex(item => item.current)
})

const trueSolarTimeText = computed(() => {
  if (!form.useTrueSolarTime) return '未启用真太阳时；选择出生城市后会自动开启。'
  if (!form.birthDate || !selectedCity.value) return '启用真太阳时：请填写出生日期并选择出生地。'
  const info = effectiveBirth.value
  return `真太阳时 ${info.birthDate} ${info.birthTime}（${selectedCity.value.name}，${info.offsetMinutes >= 0 ? '+' : ''}${info.offsetMinutes}分钟）`
})

const parsedAnalysis = computed(() => {
  try {
    return analysisResult.value ? JSON.parse(analysisResult.value) : null
  } catch {
    return analysisResult.value ? { coreConclusion: analysisResult.value, confidence: '未知', keyEvidence: [], detailedAnalysis: {}, timing: [], suggestion: '' } : null
  }
})

function applyProfile() {
  const profile = userStore.getBirthProfile()
  if (!profile) return
  form.gender = profile.gender || '男'
  form.birthDate = profile.birthDate || ''
  const normalizedTime = normalizeBirthTime(profile.birthTime || '')
  if (normalizedTime) {
    const [hour, minute] = normalizedTime.split(':')
    form.birthHour = hour
    form.birthMinute = minuteOptions.includes(minute) ? minute : '30'
  }
  form.birthProvince = profile.birthProvince || findProvinceByCity(profile.birthPlace)?.name || ''
  form.birthPlace = profile.birthPlace || ''
  form.useTrueSolarTime = Boolean(profile.useTrueSolarTime)
}

async function saveBirthProfile(options = {}) {
  const profile = userStore.saveBirthProfile({
    gender: form.gender,
    birthDate: form.birthDate,
    birthTime: birthTime.value,
    birthProvince: form.birthProvince,
    birthPlace: form.birthPlace,
    useTrueSolarTime: form.useTrueSolarTime
  })
  if (userStore.userId) {
    const user = await updateProfile({
      userId: userStore.userId,
      gender: profile.gender,
      birthDate: profile.birthDate,
      birthTime: profile.birthTime,
      birthPlace: profile.birthPlace
    })
    userStore.setUser(user)
    userStore.saveBirthProfile(profile)
  }
  if (!options.silent) ElMessage.success('出生资料已保存')
}

async function submit() {
  if (!form.birthDate || !birthTime.value) {
    ElMessage.warning('请填写出生日期和时间')
    return
  }
  loading.value = true
  try {
    await saveBirthProfile({ silent: true })
    chart.value = await buildZiweiChart({
      ...form,
      birthDate: effectiveBirth.value.birthDate,
      birthTime: effectiveBirth.value.birthTime,
      userId: userStore.userId
    })
    ElMessage.success('排盘完成')
  } finally {
    loading.value = false
  }
}

async function resolveAsyncAnalysis(data, fallbackRules = [], fallbackClassicReferences = []) {
  if (data?.status === 'PROCESSING' && data.recordId) {
    analysisNotice.value = '报告已提交，正在分析中...'
    ElMessage.success('分析已提交，后台正在生成报告')
    const record = await waitForAnalysisRecord(data.recordId, {
      onTick(current) {
        if (current?.status === 'PROCESSING') {
          analysisNotice.value = '报告正在分析中，请稍候...'
        }
      }
    })
    return {
      resultJson: record.resultJson,
      resultText: record.resultText,
      knowledgeRules: record.knowledgeRules?.length ? record.knowledgeRules : fallbackRules,
      classicReferences: record.classicReferences?.length ? record.classicReferences : fallbackClassicReferences
    }
  }
  return data
}

async function analyzeCurrentChart() {
  if (!chart.value) {
    ElMessage.warning('请先排盘')
    return
  }
  if (!analysisForm.question.trim()) {
    ElMessage.warning('请填写分析问题')
    return
  }
  if (readPendingAnalysis(PENDING_KEY)) {
    analysisNotice.value = '之前的报告正在分析中，请稍后再试；完成后可以在历史记录查看。'
    ElMessage.warning(analysisNotice.value)
    return
  }
  analysisLoading.value = true
  const isHecan = analysisForm.hecan
  startPendingAnalysis(PENDING_KEY, { type: isHecan ? 'BAZI_ZIWEI_HECAN' : 'ZIWEI', question: analysisForm.question })
  try {
    let data
    if (isHecan) {
      // 合参模式：自动算八字
      const birthDate = chart.value.input?.birthDate || form.birthDate
      const birthTimeVal = chart.value.input?.birthTime || birthTime.value
      const pillars = getFourPillars(birthDate, birthTimeVal)
      const luck = getLuckCycles({
        birthDate,
        birthTime: birthTimeVal,
        gender: form.gender,
        yearPillar: pillars.yearPillar,
        monthPillar: pillars.monthPillar
      })
      const details = getBaziDetails({ ...pillars, luck })
      const luckLines = (luck.cycles || []).map(l => {
        const tg = getTenGod(pillars.dayMaster, l.pillar?.slice(0, 1)) || ''
        const tag = l.active ? ' [当前]' : ''
        return `第${l.index}步 ${l.pillar}（${tg}） ${l.startAge}-${l.endAge}岁 ${l.startYear}-${l.endYear}${tag}`
      })
      const luckCyclesText = luck.cycles?.length
        ? `${luck.direction}，${luck.startAge}起运\n${luckLines.join('\n')}`
        : ''
      data = await analyzeHeCan({
        gender: form.gender,
        birthDate,
        birthTime: birthTimeVal,
        birthPlace: form.birthPlace,
        yearPillar: pillars.yearPillar,
        monthPillar: pillars.monthPillar,
        dayPillar: pillars.dayPillar,
        hourPillar: pillars.hourPillar,
        dayMaster: pillars.dayMaster,
        luckPillar: luck.currentLuck || '',
        currentYearPillar: new Date().getFullYear() + '',
        baziDetails: JSON.stringify(details),
        luckCycles: luckCyclesText,
        chartJson: JSON.stringify(chart.value),
        questionType: analysisForm.questionType,
        question: analysisForm.question,
        userId: userStore.userId
      })
    } else {
      data = await analyzeZiwei({
        userId: userStore.userId,
        gender: form.gender,
        birthDate: chart.value.input?.birthDate || form.birthDate,
        birthTime: chart.value.input?.birthTime || birthTime.value,
        birthPlace: form.birthPlace,
        questionType: analysisForm.questionType,
        question: analysisForm.question,
        chartJson: JSON.stringify(chart.value)
      })
    }
    const completed = await resolveAsyncAnalysis(data, data.knowledgeRules || [], data.classicReferences || [])
    analysisResult.value = completed.resultJson
    knowledgeRules.value = completed.knowledgeRules || []
    classicReferences.value = completed.classicReferences || []
    saveAnalysisCache(CACHE_KEY, {
      chart: chart.value,
      result: analysisResult.value,
      knowledgeRules: knowledgeRules.value,
      classicReferences: classicReferences.value
    })
    clearAnalysisCache(PENDING_KEY)
    ElMessage.success('紫微分析完成')
  } finally {
    finishPendingAnalysis(PENDING_KEY)
    analysisLoading.value = false
  }
}

async function copyChart() {
  if (!chart.value) return
  const lines = [
    `姓名：${form.name || '未命名'}`,
    `性别：${form.gender}`,
    `出生：${chart.value.input?.birthDate} ${chart.value.input?.birthTime}`,
    `农历：${chart.value.lunar?.display}`,
    `命宫：${chart.value.mingGong}，身宫：${chart.value.shenGong}，五行局：${chart.value.fiveElementBureau}`,
    `命主：${chart.value.lifeMaster}，身主：${chart.value.bodyMaster}，斗君：${chart.value.douJun}，来因宫：${chart.value.sourcePalace}`,
    ...chart.value.palaces.map(item => `${item.name}(${item.ganZhi})：${[...item.mainStars, ...item.luckyStars, ...item.shaStars, ...item.otherStars, ...item.transformations, item.doctorStar, item.lifeStage, item.yearGod, item.generalGod].filter(Boolean).join(' ')}`)
  ]
  await navigator.clipboard.writeText(lines.join('\n'))
  ElMessage.success('命盘已复制')
}

function onProvinceChange() {
  const cities = cityOptions.value
  if (!cities.some(city => city.name === form.birthPlace)) {
    form.birthPlace = cities[0]?.name || ''
  }
  enableTrueSolarTimeWhenPlaceSelected()
}

function onBirthPlaceChange() {
  enableTrueSolarTimeWhenPlaceSelected()
}

function enableTrueSolarTimeWhenPlaceSelected() {
  if (form.birthPlace) form.useTrueSolarTime = true
}

function flowYears(palace) {
  const luck = palace.majorLuck
  if (!luck) return ''
  return `${luck.startAge},${luck.startAge + 12},${luck.startAge + 24},${luck.startAge + 36}`
}

function minorYears(palace) {
  const luck = palace.majorLuck
  if (!luck) return ''
  return `${luck.startAge + 1},${luck.startAge + 13},${luck.startAge + 25},${luck.startAge + 37}`
}

function getEffectiveBirth() {
  if (!form.useTrueSolarTime || form.calendarType === 'LUNAR' || !form.birthDate || !selectedCity.value) {
    return { birthDate: form.birthDate, birthTime: birthTime.value, offsetMinutes: 0 }
  }
  const date = new Date(`${form.birthDate}T${birthTime.value}`)
  const offsetMinutes = Math.round((selectedCity.value.longitude - 120) * 4 + getEquationOfTimeMinutes(date))
  const corrected = new Date(date.getTime() + offsetMinutes * 60 * 1000)
  return {
    birthDate: formatDateValue(corrected),
    birthTime: `${String(corrected.getHours()).padStart(2, '0')}:${String(corrected.getMinutes()).padStart(2, '0')}`,
    offsetMinutes
  }
}

function getEquationOfTimeMinutes(date) {
  const start = new Date(date.getFullYear(), 0, 0)
  const dayOfYear = Math.floor((date - start) / 86400000)
  const b = (2 * Math.PI * (dayOfYear - 81)) / 364
  return 9.87 * Math.sin(2 * b) - 7.53 * Math.cos(b) - 1.5 * Math.sin(b)
}

function normalizeBirthTime(value) {
  if (!value || !String(value).trim()) return ''
  const raw = String(value).trim()
  const branchHours = { 子: '00:00', 丑: '02:00', 寅: '04:00', 卯: '06:00', 辰: '08:00', 巳: '10:00', 午: '12:00', 未: '14:00', 申: '16:00', 酉: '18:00', 戌: '20:00', 亥: '22:00' }
  const branch = raw.match(/[子丑寅卯辰巳午未申酉戌亥]/)?.[0]
  if (branch) return branchHours[branch]
  const match = raw.match(/^(\d{1,2})(?:[:：点时](\d{1,2})?)?/)
  if (!match) return raw
  const hour = Number(match[1])
  const minute = Number(match[2] || 0)
  if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return ''
  return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
}

function formatDateValue(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function getCityLocation(place) {
  const value = String(place || '')
  return provinceOptions.flatMap(province => province.cities).find(city => city.name === value || value.includes(city.name)) || null
}

function findProvinceByCity(place) {
  const value = String(place || '')
  return provinceOptions.find(province => province.cities.some(city => city.name === value || value.includes(city.name))) || null
}

onMounted(() => {
  applyProfile()
  const cached = readAnalysisCache(CACHE_KEY)
  if (cached?.result && !analysisResult.value) {
    // 只恢复包含流年数据的新版排盘
    if (cached.chart && cached.chart.flowYear) {
      chart.value = cached.chart
    }
    analysisResult.value = cached.result
    knowledgeRules.value = cached.knowledgeRules || []
    classicReferences.value = cached.classicReferences || []
    analysisNotice.value = '已恢复上一次紫微分析报告；如果切屏后没看到结果，也可以到历史记录查看。'
  } else if (readPendingAnalysis(PENDING_KEY)) {
    analysisNotice.value = '检测到上次有分析进行中；如果本页没有恢复结果，请到历史记录查看，避免重复消耗。'
  }
})
</script>

<style scoped>
.ziwei-page {
  position: relative;
  min-height: 100vh;
  color: #172f35;
  background:
    linear-gradient(180deg, rgba(8, 23, 52, 0.95) 0 230px, rgba(248, 241, 226, 0.97) 231px),
    radial-gradient(circle at 82% 92px, rgba(232, 214, 165, 0.18), transparent 30%),
    repeating-linear-gradient(105deg, transparent 0 38px, rgba(47, 111, 94, 0.03) 38px 40px);
}

.ziwei-page::before {
  content: "";
  position: absolute;
  inset: 0 0 auto;
  height: 265px;
  pointer-events: none;
  background:
    linear-gradient(90deg, rgba(6, 20, 45, 0.82), rgba(10, 24, 56, 0.46) 48%, rgba(10, 24, 56, 0.16)),
    linear-gradient(180deg, rgba(6, 20, 45, 0.08), rgba(6, 20, 45, 0.5)),
    url("/images/ziwei-page-bg.png") center / cover no-repeat;
  opacity: 0.96;
}

.ziwei-page > * {
  position: relative;
  z-index: 1;
}

.hero-head {
  text-align: left;
  max-width: 1180px;
  margin: 0 auto 16px;
  padding: 26px 28px;
  border: 1px solid rgba(232, 214, 165, 0.28);
  border-radius: 14px;
  color: #f8f0d8;
  background: linear-gradient(90deg, rgba(12, 32, 58, 0.5), rgba(12, 32, 58, 0.16));
  box-shadow: 0 18px 34px rgba(17, 47, 40, 0.16);
}

.hero-head h1 {
  margin: 0;
  font-family: "Kaiti SC", "STKaiti", "KaiTi", "Songti SC", serif;
  font-size: 34px;
  font-weight: 400;
  color: #f4d791;
  line-height: 1.2;
}

.hero-head p {
  margin: 10px 0 0;
  color: rgba(255, 248, 221, 0.88);
  font-size: 15px;
}

.ziwei-shell {
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px;
  border: 1px solid rgba(176, 138, 60, 0.24);
  border-radius: 14px;
  background:
    linear-gradient(180deg, rgba(255, 253, 246, 0.96), rgba(251, 250, 246, 0.98)),
    repeating-linear-gradient(90deg, transparent 0 72px, rgba(47, 111, 94, 0.026) 72px 74px);
  box-shadow: 0 14px 28px rgba(23, 63, 53, 0.08), inset 0 0 0 1px rgba(255, 255, 255, 0.42);
}

.form-section-title {
  margin: 0 0 20px;
  color: #173f35;
  font-size: 18px;
  font-weight: 700;
}

.time-title {
  margin-top: 20px;
}

.plain-form :deep(.el-form-item__label) {
  color: #111;
  font-weight: 700;
}

.field-tip {
  margin-top: 6px;
  color: #7b8190;
  font-size: 12px;
}

.segmented {
  height: 36px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  padding: 3px;
  margin-bottom: 8px;
  border-radius: 8px;
  background: #f1f1f1;
}

.segmented button {
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #555;
  font-weight: 600;
}

.segmented button.active {
  background: #fff;
  color: #111;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.08);
}

.place-row {
  width: 100%;
}

.switch-list {
  display: grid;
  gap: 10px;
  margin: 6px 0;
}

.switch-list label {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #333;
}

.solar-text {
  min-height: 18px;
  margin: 4px 0 18px;
  color: #7b8190;
  font-size: 12px;
}

.chart-button {
  width: 100%;
  height: 38px;
  border: 0;
  background: linear-gradient(180deg, #2f6f5e, #173f35);
  color: #fff;
  font-weight: 800;
}

.chart-button:hover,
.chart-button:focus {
  background: linear-gradient(180deg, #337965, #173f35);
  color: #fff;
}

.chart-wrap {
  margin-top: 24px;
}

.ziwei-board {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: repeat(4, 188px);
  gap: 4px;
  border: 4px solid rgba(176, 138, 60, 0.28);
  border-radius: 8px;
  overflow: hidden;
  background: rgba(176, 138, 60, 0.24);
  box-shadow: 0 12px 26px rgba(16, 28, 48, 0.1);
}

.palace-card,
.center-cell {
  background: #fff;
  padding: 8px;
  position: relative;
}

.palace-card {
  padding-bottom: 62px;
}

.palace-card.ming {
  background: #d9d9d9;
  box-shadow: inset 0 0 0 2px #111;
}

.palace-card.shen:not(.ming) {
  box-shadow: inset 0 0 0 2px #b9841e;
}

.center-cell {
  grid-column: span 2;
  grid-row: span 2;
  display: grid;
  place-content: center;
  text-align: center;
  gap: 7px;
  background:
    linear-gradient(rgba(10, 8, 28, 0.58), rgba(10, 8, 28, 0.68)),
    url('/images/ziwei-center-bg.png') center / cover no-repeat;
  color: #fff7dd;
  font-size: 13px;
  text-shadow: 0 1px 5px rgba(0, 0, 0, 0.9);
}

.center-cell strong {
  font-size: 17px;
  color: #fff;
  font-weight: 800;
}

.palace-title {
  display: flex;
  gap: 4px;
  align-items: center;
  font-size: 14px;
  font-weight: 800;
  color: #111;
}

.palace-title span {
  color: #cf2f2f;
  font-size: 11px;
}

.palace-stars {
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  gap: 2px 6px;
  min-height: 42px;
  margin-top: 8px;
  color: #111;
}

.palace-stars b {
  font-size: 13px;
  line-height: 1.25;
}

.palace-stars .empty-star {
  color: #8a8f98;
  font-weight: 600;
}

.palace-stars em {
  width: 16px;
  height: 16px;
  border-radius: 3px;
  background: #3b82f6;
  color: #fff;
  font-style: normal;
  font-size: 11px;
  text-align: center;
  line-height: 16px;
}

.minor-stars {
  display: flex;
  flex-wrap: wrap;
  gap: 2px 5px;
  margin-top: 4px;
  max-height: 70px;
  overflow: hidden;
}

.minor-stars span {
  color: #4b5563;
  font-size: 11px;
  line-height: 1.3;
}

.minor-stars .sha {
  color: #b42318;
}

.minor-stars .other {
  color: #6b7280;
}

.palace-foot {
  position: absolute;
  left: 8px;
  right: 8px;
  bottom: 6px;
  display: grid;
  grid-template-columns: 42px 1fr auto;
  gap: 2px 8px;
  align-items: end;
  color: #8a8f98;
}

.palace-foot small {
  font-size: 10px;
  line-height: 1.25;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.palace-foot strong {
  grid-row: 1 / span 6;
  grid-column: 3;
  color: #111;
  text-align: center;
  font-size: 15px;
  line-height: 1.25;
}

.copy-line {
  display: flex;
  justify-content: flex-end;
  margin: 18px 0;
}

.chart-tabs {
  width: 250px;
  height: 38px;
  margin: 0 auto 18px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  padding: 3px;
  border: 1px solid #e5e7eb;
  border-radius: 9px;
  background: #fafafa;
}

.chart-tabs button {
  border: 0;
  border-radius: 7px;
  background: transparent;
  color: #6b7280;
  font-weight: 700;
}

.chart-tabs button.active {
  background: #fff;
  color: #111;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.08);
}

.sihua-line {
  border-top: 1px solid #eeeeee;
  padding: 16px 0 26px;
  color: #6b7280;
  text-align: center;
  font-size: 13px;
}

.sihua-line span {
  display: inline-block;
  margin-left: 8px;
  padding: 2px 7px;
  border-radius: 4px;
  background: #f3f4f6;
  color: #111;
  font-weight: 700;
}

.fortune-panel {
  margin-bottom: 20px;
}

.fortune-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.fortune-timeline {
  overflow-x: auto;
  padding: 16px 0 24px;
  -webkit-overflow-scrolling: touch;
}

.fortune-track {
  display: flex;
  position: relative;
  min-width: max-content;
  padding: 0 20px;
}

.fortune-line {
  position: absolute;
  top: 10px;
  left: 20px;
  right: 20px;
  height: 2px;
  background: #e5e7eb;
}

.fortune-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 100px;
  position: relative;
  z-index: 1;
}

.fortune-node + .fortune-node {
  margin-left: 4px;
}

.node-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #e5e7eb;
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px #e5e7eb;
  flex-shrink: 0;
  position: relative;
}

.fortune-node.past .node-dot {
  background: #9ca3af;
  box-shadow: 0 0 0 2px #9ca3af;
}

.fortune-node.current .node-dot {
  background: #b08a3c;
  box-shadow: 0 0 0 2px #b08a3c;
}

.node-pulse {
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  border: 2px solid #b08a3c;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0; transform: scale(0.8); }
  50% { opacity: 0.6; transform: scale(1.2); }
}

.node-body {
  margin-top: 10px;
  padding: 10px;
  border: 1px solid #eceef2;
  border-radius: 8px;
  background: #fafafa;
  text-align: center;
  min-width: 90px;
  transition: all 0.2s;
}

.fortune-node.current .node-body {
  border-color: #b08a3c;
  background: linear-gradient(135deg, #fffdf6 0, #fff9eb 100%);
  box-shadow: 0 2px 12px rgba(176, 138, 60, 0.15);
}

.node-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 4px;
}

.node-palace {
  color: #6b7280;
  font-size: 12px;
}

.current-badge {
  display: inline-block !important;
  padding: 1px 6px;
  border-radius: 4px;
  background: #b08a3c;
  color: #fff !important;
  font-size: 10px !important;
  font-weight: 700;
}

.node-age {
  display: block;
  color: #111;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.3;
}

.node-year {
  display: block;
  color: #9ca3af;
  font-size: 11px;
  line-height: 1.4;
  margin-top: 2px;
}

.node-stars {
  margin-top: 6px;
  color: #6b7280;
  font-size: 11px;
  line-height: 1.4;
}

/* grid card styles for other tabs */
.fortune-card {
  border: 1px solid #eceef2;
  border-radius: 10px;
  padding: 12px;
  background: #fafafa;
  transition: all 0.2s;
}

.fortune-card.current {
  border-color: #b08a3c;
  background: linear-gradient(135deg, #fffdf6 0, #fff9eb 100%);
  box-shadow: 0 2px 12px rgba(176, 138, 60, 0.15);
}

.fortune-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.fortune-card span,
.fortune-card small {
  display: block;
  color: #7b8190;
  font-size: 12px;
  line-height: 1.4;
}

.fortune-card strong {
  display: block;
  margin: 4px 0;
  color: #111;
  font-size: 14px;
  line-height: 1.35;
}

.fortune-stars {
  margin-top: 6px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ai-panel {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 18px;
  align-items: start;
  margin-bottom: 36px;
  padding: 26px;
  border: 1px solid rgba(176, 138, 60, 0.24);
  border-radius: 12px;
  background: rgba(255, 253, 246, 0.82);
  box-shadow: 0 12px 24px rgba(23, 63, 53, 0.08);
}

.ai-panel h2 {
  margin: 0 0 8px;
  font-size: 19px;
}

.ai-panel p {
  margin: 0;
  color: #6b7280;
}

.hecan-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
}

.ai-button {
  min-width: 140px;
  background: #111;
  color: #fff;
}

.analysis-placeholder {
  grid-column: 1 / -1;
  min-height: 150px;
  padding: 20px;
  border: 1px solid #edf0f3;
  border-radius: 8px;
  color: #8a8f98;
}

.analysis-placeholder.notice {
  min-height: 0;
  border-color: #d8c696;
  background: #fffaf0;
  color: #806326;
}

.analysis-form {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 12px;
}

.ai-panel :deep(.report),
.ai-panel :deep(.references) {
  grid-column: 1 / -1;
  width: 100%;
  min-width: 0;
}

.ai-panel :deep(.references) {
  margin-top: 0;
}

@media (max-width: 900px) {
  .ziwei-shell {
    max-width: 100%;
  }
}

@media (max-width: 700px) {
  .ziwei-page {
    background:
      linear-gradient(180deg, rgba(8, 23, 52, 0.96) 0 270px, rgba(248, 241, 226, 0.98) 271px),
      radial-gradient(circle at 70% 95px, rgba(232, 214, 165, 0.13), transparent 30%);
  }

  .ziwei-page::before {
    height: 300px;
    background-position: 54% top;
  }

  .hero-head {
    margin-bottom: 12px;
    padding: 18px 16px;
    border-radius: 12px;
    text-align: left;
  }

  .hero-head h1 {
    font-size: 28px;
  }

  .hero-head p {
    margin-top: 10px;
    font-size: 13px;
  }

  .ziwei-shell {
    padding: 14px 10px;
    border-radius: 12px;
  }

  .form-section-title {
    margin-bottom: 12px;
    font-size: 16px;
  }

  .time-title {
    margin-top: 10px;
  }

  .chart-wrap {
    width: 100%;
    overflow-x: hidden;
  }

  .ziwei-board {
    width: 100%;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    grid-template-rows: repeat(4, minmax(118px, auto));
    gap: 2px;
    border-width: 2px;
    border-radius: 6px;
  }

  .palace-card,
  .center-cell {
    min-width: 0;
    min-height: 138px;
    padding: 4px;
  }

  .palace-card {
    padding-bottom: 48px;
  }

  .palace-title {
    gap: 2px;
    font-size: 11px;
    line-height: 1.1;
  }

  .palace-title span {
    font-size: 9px;
  }

  .palace-stars {
    min-height: 28px;
    margin-top: 4px;
    gap: 1px 3px;
  }

  .palace-stars b {
    font-size: 10px;
    line-height: 1.15;
  }

  .palace-stars em {
    width: 13px;
    height: 13px;
    font-size: 9px;
    line-height: 13px;
  }

  .minor-stars {
    gap: 1px 3px;
    margin-top: 2px;
    max-height: 32px;
  }

  .minor-stars span {
    font-size: 9px;
    line-height: 1.18;
  }

  .palace-foot {
    left: 4px;
    right: 4px;
    bottom: 3px;
    grid-template-columns: 24px minmax(0, 1fr) auto;
    gap: 0 3px;
    padding-top: 2px;
    background: linear-gradient(to bottom, rgba(255, 255, 255, 0.72), #fff 28%);
  }

  .palace-foot small {
    font-size: 8px;
    line-height: 1.1;
  }

  .palace-foot .year-list {
    grid-column: 1 / span 2;
    max-width: 100%;
    letter-spacing: 0;
  }

  .palace-foot strong {
    font-size: 11px;
    line-height: 1.1;
  }

  .center-cell {
    gap: 3px;
    padding: 8px;
    background:
      linear-gradient(rgba(9, 7, 25, 0.62), rgba(9, 7, 25, 0.7)),
      url('/images/ziwei-center-bg.png') center / cover no-repeat;
    color: #fff;
    text-shadow: 0 1px 4px rgba(0, 0, 0, 0.9);
  }

  .center-cell strong {
    color: #fff;
    font-size: 13px;
    line-height: 1.25;
  }

  .center-cell span {
    color: #fff7dd;
    font-size: 10px;
    line-height: 1.25;
  }

  .copy-line {
    margin: 12px 0;
  }

  .chart-tabs {
    width: 100%;
    max-width: 260px;
  }

  .sihua-line {
    padding: 12px 0 16px;
    font-size: 13px;
    line-height: 2;
  }

  .sihua-line span {
    margin: 2px 3px;
    font-size: 12px;
  }

  .fortune-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .fortune-card {
    padding: 12px;
  }

  .fortune-card strong {
    font-size: 15px;
  }

  .fortune-card small {
    font-size: 13px;
  }

  .fortune-stars {
    font-size: 13px;
  }

  .fortune-timeline {
    padding: 12px 0 20px;
  }

  .fortune-node {
    min-width: 85px;
  }

  .node-body {
    padding: 8px;
    min-width: 78px;
  }

  .node-age {
    font-size: 12px;
  }

  .node-year {
    font-size: 10px;
  }

  .node-stars {
    font-size: 10px;
  }

  .ai-panel {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 18px;
  }

  .analysis-form {
    grid-template-columns: 1fr;
  }

  .ai-button {
    width: 100%;
  }
}
</style>
