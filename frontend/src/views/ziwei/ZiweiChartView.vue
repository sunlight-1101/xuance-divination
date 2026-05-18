<template>
  <div class="page ziwei-page">
    <header class="hero-head">
      <h1>AI 紫微斗数排盘</h1>
      <p>输入出生信息，生成紫微命盘，获取 AI 解读与问答</p>
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
          生年（{{ chart.yearGanZhi }}）四化：
          <span v-for="[star, hua] in Object.entries(chart.transformations || {})" :key="star">{{ star }} {{ hua.replace('化', '') }}</span>
        </div>

        <section class="fortune-panel">
          <div v-for="item in activeDisplayItems" :key="item.title" class="fortune-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.title }}</strong>
            <small>{{ item.desc }}</small>
          </div>
        </section>

        <section class="ai-panel">
          <div>
            <h2>AI 紫微分析</h2>
            <p>结合命盘结构、主星和四化，生成结构化的紫微斗数解读</p>
          </div>
          <el-button class="ai-button" :loading="analysisLoading" @click="analyzeCurrentChart">开始 AI 分析</el-button>
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
          <div v-if="analysisLoading" class="analysis-placeholder">正在调用 AI 分析，预计 60-120 秒，请不要重复点击。</div>
          <ResultReport v-else-if="parsedAnalysis" :report="parsedAnalysis" />
          <KnowledgeReferences
            v-if="knowledgeRules.length || classicReferences.length"
            :rules="knowledgeRules"
            :classic-references="classicReferences"
          />
          <div v-if="!analysisLoading && !parsedAnalysis" class="analysis-placeholder">点击「开始 AI 分析」后，这里会展示完整的紫微斗数解读</div>
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
import KnowledgeReferences from '../../components/KnowledgeReferences.vue'
import ResultReport from '../../components/ResultReport.vue'
import { useUserStore } from '../../stores/user'
import { provinceOptions } from '../../utils/chinaCities'

const userStore = useUserStore()
const loading = ref(false)
const analysisLoading = ref(false)
const chart = ref(null)
const activeChartTab = ref('base')
const analysisResult = ref('')
const knowledgeRules = ref([])
const classicReferences = ref([])

const hourOptions = [
  { label: '00时', value: '00' }, { label: '01时', value: '01' }, { label: '02时', value: '02' },
  { label: '03时', value: '03' }, { label: '04时', value: '04' }, { label: '05时', value: '05' },
  { label: '06时', value: '06' }, { label: '07时', value: '07' }, { label: '08时', value: '08' },
  { label: '09时', value: '09' }, { label: '10时', value: '10' }, { label: '11时', value: '11' },
  { label: '12时', value: '12' }, { label: '13时', value: '13' }, { label: '14时', value: '14' },
  { label: '15时', value: '15' }, { label: '16时', value: '16' }, { label: '17时', value: '17' },
  { label: '18时', value: '18' }, { label: '19时', value: '19' }, { label: '20时', value: '20' },
  { label: '21时', value: '21' }, { label: '22时', value: '22' }, { label: '23时', value: '23' }
]
const minuteOptions = ['00', '10', '20', '30', '40', '50']
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
  question: '请综合分析这张紫微斗数命盘，重点说明性格底色、事业财运、感情关系、大限趋势和需要注意的风险。'
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
    return chart.value.palaces
      .filter(item => item.majorLuck)
      .sort((a, b) => a.majorLuck.startAge - b.majorLuck.startAge)
      .slice(0, 12)
      .map(item => ({
        label: item.name,
        title: `${item.majorLuck.startAge}-${item.majorLuck.endAge}岁`,
        desc: `${item.majorLuck.startYear}-${item.majorLuck.endYear} · ${item.ganZhi}`
      }))
  }
  if (activeChartTab.value === 'year') {
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

async function analyzeCurrentChart() {
  if (!chart.value) {
    ElMessage.warning('请先排盘')
    return
  }
  if (!analysisForm.question.trim()) {
    ElMessage.warning('请填写分析问题')
    return
  }
  analysisLoading.value = true
  try {
    const data = await analyzeZiwei({
      userId: userStore.userId,
      gender: form.gender,
      birthDate: chart.value.input?.birthDate || form.birthDate,
      birthTime: chart.value.input?.birthTime || birthTime.value,
      birthPlace: form.birthPlace,
      questionType: analysisForm.questionType,
      question: analysisForm.question,
      chartJson: JSON.stringify(chart.value)
    })
    analysisResult.value = data.resultJson
    knowledgeRules.value = data.knowledgeRules || []
    classicReferences.value = data.classicReferences || []
    ElMessage.success('紫微分析完成')
  } finally {
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

onMounted(applyProfile)
</script>

<style scoped>
.ziwei-page {
  background: #fff;
}

.hero-head {
  text-align: center;
  padding: 24px 0 28px;
}

.hero-head h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #111;
}

.hero-head p {
  margin: 22px 0 0;
  color: #6b7280;
  font-size: 15px;
}

.ziwei-shell {
  max-width: 880px;
  margin: 0 auto;
}

.form-section-title {
  margin: 0 0 20px;
  color: #6b7280;
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
  background: #111;
  color: #fff;
  font-weight: 800;
}

.chart-button:hover,
.chart-button:focus {
  background: #222;
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
  border: 4px solid #e7e7e7;
  border-radius: 8px;
  overflow: hidden;
  background: #e7e7e7;
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
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 20px;
}

.fortune-card {
  border: 1px solid #eceef2;
  border-radius: 8px;
  padding: 10px;
  background: #fafafa;
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

.ai-panel {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 18px;
  align-items: start;
  margin-bottom: 36px;
  padding: 26px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.05);
}

.ai-panel h2 {
  margin: 0 0 8px;
  font-size: 19px;
}

.ai-panel p {
  margin: 0;
  color: #6b7280;
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

.analysis-form {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 12px;
}

.ai-panel :deep(.result-report) {
  grid-column: 1 / -1;
}

@media (max-width: 900px) {
  .ziwei-shell {
    max-width: 100%;
  }
}

@media (max-width: 700px) {
  .hero-head {
    padding: 14px 0 18px;
  }

  .hero-head h1 {
    font-size: 22px;
  }

  .hero-head p {
    margin-top: 10px;
    font-size: 13px;
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
    font-size: 12px;
    line-height: 1.7;
  }

  .sihua-line span {
    margin: 4px 2px 0;
  }

  .fortune-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .fortune-card {
    padding: 8px;
  }

  .ai-panel {
    grid-template-columns: 1fr;
  }

  .analysis-form {
    grid-template-columns: 1fr;
  }
}
</style>
