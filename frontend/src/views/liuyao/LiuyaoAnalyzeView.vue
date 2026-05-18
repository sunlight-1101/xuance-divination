<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">六爻解卦</h1>
      <p class="page-desc">三步完成：提问，摇卦，查看报告。</p>
    </div>

    <el-steps :active="activeStep" finish-status="success" class="mobile-steps">
      <el-step title="提问" />
      <el-step title="摇卦" />
      <el-step title="报告" />
    </el-steps>

    <el-row :gutter="16" class="mobile-stack">
      <el-col :xs="24" :lg="14">
        <div class="panel">
          <h2 class="section-title">提问与起卦</h2>
          <el-form :model="form" label-position="top">
            <section class="flow-section active">
              <div class="flow-head">
                <span>01</span>
                <div>
                  <h3>写下要问的事</h3>
                  <p>一句话说清楚问题即可，不用懂六爻术语。</p>
                </div>
              </div>
              <el-form-item label="求测问题">
                <el-input v-model="form.question" type="textarea" :rows="3" placeholder="例如：这次面试能否顺利通过？" />
              </el-form-item>
            </section>

            <el-collapse class="bazi-collapse helper-collapse">
              <el-collapse-item title="八字信息（可选，填了会一起进入分析）" name="bazi">
                <el-row :gutter="12">
                  <el-col :xs="12" :sm="8">
                    <el-form-item label="性别">
                      <el-select v-model="form.gender" placeholder="选择">
                        <el-option label="男" value="男" />
                        <el-option label="女" value="女" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="12" :sm="8">
                    <el-form-item label="出生日期">
                      <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" placeholder="选择年月日" @change="updateBirthDayMaster" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="12" :sm="8">
                    <el-form-item label="出生时间">
                      <el-select v-model="form.birthTime" filterable clearable placeholder="选择时辰">
                        <el-option v-for="item in birthTimeOptions" :key="item.value" :label="item.label" :value="item.value" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="12" :sm="8">
                    <el-form-item label="出生地">
                      <el-input v-model="form.birthPlace" placeholder="城市" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="12" :sm="8">
                    <el-form-item label="日柱">
                      <el-input v-model="form.birthDayGanZhi" readonly />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="12" :sm="8">
                    <el-form-item label="日主">
                      <el-input v-model="form.birthDayMaster" readonly />
                    </el-form-item>
                  </el-col>
                </el-row>
                <div class="profile-actions">
                  <el-button @click="saveProfile">保存出生资料</el-button>
                  <span class="hint">{{ userStore.userId ? '已登录时会同步到账号' : '游客模式会保存到本机' }}</span>
                </div>
              </el-collapse-item>
            </el-collapse>

            <el-row :gutter="12">
              <el-col :xs="24" :sm="12" :lg="8">
                <el-form-item label="起卦时间">
                  <el-input v-model="form.time" @change="updateDivinationDay">
                    <template #append>
                      <el-button @click="setCurrentTime">现在</el-button>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="12" :sm="12" :lg="8">
                <el-form-item label="起卦日柱">
                  <el-input v-model="form.dayGanZhi" readonly />
                </el-form-item>
              </el-col>
            </el-row>

            <el-collapse class="time-extra-collapse helper-collapse">
              <el-collapse-item title="高级时间信息（可选）" name="timeExtra">
                <el-row :gutter="12">
                  <el-col :xs="12" :sm="8"><el-form-item label="月建"><el-input v-model="form.monthBranch" /></el-form-item></el-col>
                  <el-col :xs="12" :sm="8"><el-form-item label="日辰"><el-input v-model="form.dayBranch" /></el-form-item></el-col>
                  <el-col :xs="12" :sm="8"><el-form-item label="旬空"><el-input v-model="form.emptyBranches" /></el-form-item></el-col>
                </el-row>
              </el-collapse-item>
            </el-collapse>

            <section class="flow-section" :class="{ active: form.question.trim(), done: shakenCount >= 6 }">
              <div class="flow-head">
                <span>02</span>
                <div>
                  <h3>摇卦</h3>
                  <p>从初爻开始，一次摇一爻；六爻摇满后系统自动装卦。</p>
                </div>
              </div>
            <div class="shake-card">
              <div>
                <h3 class="sub-title">摇卦记录</h3>
                <p class="hint">从初爻开始，一次摇一爻；摇满后自动装卦。</p>
              </div>
              <div class="shake-actions">
                <el-button @click="resetGua">重摇</el-button>
                <el-button type="primary" size="large" :disabled="shakenCount >= 6" @click="shakeNext">
                  {{ shakenCount >= 6 ? '已成卦' : `摇第 ${shakenCount + 1} 爻` }}
                </el-button>
              </div>
            </div>
            <div class="beginner-guide">
              <strong>{{ beginnerGuideTitle }}</strong>
              <span>{{ beginnerGuideText }}</span>
            </div>
            <el-progress
              class="shake-progress"
              :percentage="Math.round((shakenCount / 6) * 100)"
              :format="() => `${shakenCount}/6`"
            />
            <div class="live-yao-panel">
              <div class="live-yao-title">
                <strong>已摇出的爻象</strong>
                <span>{{ shakenCount ? `已完成 ${shakenCount} 爻` : '从初爻开始摇' }}</span>
              </div>
              <div class="yao-cards live-yao-cards">
                <div v-for="(row, index) in form.yaoList" :key="row.position" class="yao-card" :class="{ done: index < shakenCount }">
                  <div class="yao-card-head">
                    <strong>{{ row.position }}</strong>
                    <el-tag v-if="row.moving" type="danger" size="small">动爻</el-tag>
                    <el-button link type="primary" :disabled="index >= shakenCount" @click="reshakeYao(index)">重摇</el-button>
                  </div>
                  <div class="coins mobile-coins" v-if="row.coins?.length">
                    <span v-for="(coin, coinIndex) in row.coins" :key="coinIndex" class="coin" :class="{ back: coin }">
                      {{ coin ? '背' : '字' }}
                    </span>
                    <strong>{{ row.coinValue }}</strong>
                    <span>{{ row.lineType }} → {{ row.changedLineType }}</span>
                  </div>
                  <div v-else class="muted">待摇</div>
                  <div class="line-preview" v-if="row.lineType">
                    <div class="line" :class="{ yang: row.yang, moving: row.moving }"></div>
                    <em>{{ row.moving ? '会变' : '不变' }}</em>
                  </div>
                </div>
              </div>
            </div>
            <el-alert
              v-if="installInfo.palace"
              class="install-info"
              type="success"
              :closable="false"
              title="卦象已自动生成，可以直接开始分析"
            />
            <div v-if="shakenCount >= 6" class="gua-summary">
              <div>
                <span>当前卦象</span>
                <strong>{{ form.mainGua || '本卦' }} → {{ form.changedGua || '变卦' }}</strong>
                <small>这是系统根据你的摇卦结果自动排出的卦，不需要手动填写。</small>
              </div>
              <button type="button" @click="showDetailHint = !showDetailHint">
                {{ showDetailHint ? '收起说明' : '这是什么？' }}
              </button>
            </div>
            <p v-if="showDetailHint && shakenCount >= 6" class="gua-explain">
              “本卦”代表事情当前的状态，“变卦”代表动爻变化后的趋势。系统会把它们连同世应、六亲、六神一起交给 AI 分析，新手不用理解每个术语也能使用。
            </p>
            </section>

            <el-collapse class="divination-detail-collapse">
              <el-collapse-item title="查看系统自动排出的卦象（可选）" name="details">
            <div class="yao-cards">
              <div v-for="(row, index) in form.yaoList" :key="row.position" class="yao-card" :class="{ done: index < shakenCount }">
                <div class="yao-card-head">
                  <strong>{{ row.position }}</strong>
                  <el-tag v-if="row.moving" type="danger" size="small">动爻</el-tag>
                  <el-button link type="primary" :disabled="index >= shakenCount" @click="reshakeYao(index)">重摇</el-button>
                </div>
                <div class="coins mobile-coins" v-if="row.coins?.length">
                  <span v-for="(coin, coinIndex) in row.coins" :key="coinIndex" class="coin" :class="{ back: coin }">
                    {{ coin ? '背' : '字' }}
                  </span>
                  <strong>{{ row.coinValue }}</strong>
                  <span>{{ row.lineType }} → {{ row.changedLineType }}</span>
                </div>
                <div v-else class="muted">待摇</div>
                <div class="yao-meta">
                  <span>六神：{{ row.sixGod || '-' }}</span>
                  <span>六亲：{{ row.sixRelative || '-' }}</span>
                  <span>地支：{{ row.branch || '-' }}</span>
                  <span>五行：{{ row.element || '-' }}</span>
                  <span v-if="row.shi">世爻</span>
                  <span v-if="row.ying">应爻</span>
                </div>
              </div>
            </div>

            <el-collapse class="advanced-collapse">
              <el-collapse-item title="高级校正" name="advanced">
                <el-table :data="form.yaoList" border>
              <el-table-column label="爻位" width="82">
                <template #default="{ row }">{{ row.position }}</template>
              </el-table-column>
              <el-table-column label="铜钱" width="150">
                <template #default="{ row }">
                  <div v-if="row.coins?.length" class="coins">
                    <span v-for="(coin, index) in row.coins" :key="index" class="coin" :class="{ back: coin }">
                      {{ coin ? '背' : '字' }}
                    </span>
                    <strong>{{ row.coinValue }}</strong>
                  </div>
                  <span v-else class="muted">待摇</span>
                </template>
              </el-table-column>
              <el-table-column label="本爻" width="110">
                <template #default="{ row }">
                  <span v-if="row.lineType">{{ row.lineType }} {{ row.yang ? '⚊' : '⚋' }}</span>
                  <span v-else class="muted">-</span>
                </template>
              </el-table-column>
              <el-table-column label="变爻" width="110">
                <template #default="{ row }">
                  <span v-if="row.changedLineType">{{ row.changedLineType }} {{ row.changedYang ? '⚊' : '⚋' }}</span>
                  <span v-else class="muted">-</span>
                </template>
              </el-table-column>
              <el-table-column label="六神" width="86">
                <template #default="{ row }"><el-input v-model="row.sixGod" /></template>
              </el-table-column>
              <el-table-column label="六亲" width="98">
                <template #default="{ row }"><el-input v-model="row.sixRelative" /></template>
              </el-table-column>
              <el-table-column label="地支" width="82">
                <template #default="{ row }"><el-input v-model="row.branch" /></template>
              </el-table-column>
              <el-table-column label="五行" width="82">
                <template #default="{ row }"><el-input v-model="row.element" /></template>
              </el-table-column>
              <el-table-column label="世" width="58" align="center">
                <template #default="{ row }"><el-checkbox v-model="row.shi" /></template>
              </el-table-column>
              <el-table-column label="应" width="58" align="center">
                <template #default="{ row }"><el-checkbox v-model="row.ying" /></template>
              </el-table-column>
              <el-table-column label="动" width="58" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.moving" type="danger" size="small">动</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="变爻" min-width="150">
                <template #default="{ row }">
                  <div class="inline-fields">
                    <el-input v-model="row.changedRelative" placeholder="六亲" />
                    <el-input v-model="row.changedBranch" placeholder="地支" />
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="伏神" min-width="110">
                <template #default="{ row }"><el-input v-model="row.hiddenSpirit" /></template>
              </el-table-column>
              <el-table-column label="重摇" width="72" align="center">
                <template #default="{ $index }">
                  <el-button link type="primary" :disabled="$index >= shakenCount" @click="reshakeYao($index)">重摇</el-button>
                </template>
              </el-table-column>
                </el-table>
              </el-collapse-item>
            </el-collapse>

            <div ref="guaPanelRef" class="panel gua-panel inline-gua-panel">
              <h2 class="section-title">卦象</h2>
              <div class="gua-visuals">
                <div>
                  <h3>本卦 {{ form.mainGua || '-' }}</h3>
                  <div class="lines">
                    <div v-for="line in visualLines(false)" :key="line.position" class="line-row">
                      <span>{{ line.position }}</span>
                      <div class="line" :class="{ yang: line.yang, moving: line.moving }"></div>
                      <em>{{ line.lineType || '-' }}</em>
                    </div>
                  </div>
                </div>
                <div>
                  <h3>变卦 {{ form.changedGua || '-' }}</h3>
                  <div class="lines">
                    <div v-for="line in visualLines(true)" :key="line.position" class="line-row">
                      <span>{{ line.position }}</span>
                      <div class="line" :class="{ yang: line.yang }"></div>
                      <em>{{ line.lineType || '-' }}</em>
                    </div>
                  </div>
                </div>
              </div>
            </div>

              </el-collapse-item>
            </el-collapse>

            <section class="flow-section" :class="{ active: shakenCount >= 6, done: !!result }">
              <div class="flow-head">
                <span>03</span>
                <div>
                  <h3>开始分析</h3>
                  <p>系统会结合卦象、问题和知识库生成报告。</p>
                </div>
              </div>
            <div class="actions">
              <el-button type="primary" size="large" :disabled="shakenCount < 6" :loading="loading" @click="submit">开始分析</el-button>
            </div>
            </section>
            <el-alert
              v-if="loading"
              class="estimate-note"
              type="warning"
              :closable="false"
              title="正在调用 AI 细断，预计 60-120 秒，请不要重复点击。"
            />
          </el-form>
        </div>
      </el-col>
      <el-col v-if="result || loading" :xs="24" :lg="10">
        <div ref="reportPanelRef" class="panel result-panel">
          <div class="result-head">
            <h2 class="section-title">分析报告</h2>
            <div class="result-actions">
              <el-button :disabled="!parsedResult" @click="copyCurrentReport">复制报告</el-button>
              <el-button :disabled="!parsedResult" @click="exportCurrentReport">导出 Markdown</el-button>
              <el-button :disabled="!result" @click="resetGua">重新起卦</el-button>
            </div>
          </div>
          <ResultReport :report="parsedResult" />
          <KnowledgeReferences
            v-if="knowledgeRules.length || classicReferences.length"
            :rules="knowledgeRules"
            :classic-references="classicReferences"
          />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { updateProfile } from '../../api/auth'
import { analyzeLiuyao } from '../../api/liuyao'
import KnowledgeReferences from '../../components/KnowledgeReferences.vue'
import ResultReport from '../../components/ResultReport.vue'
import { useUserStore } from '../../stores/user'
import { buildHexagram, installHexagram, tossCoins } from '../../utils/liuyao'
import { getDayGanZhi } from '../../utils/ganzhi'
import { buildReportMarkdown, copyText, downloadMarkdown } from '../../utils/report'
import { buildBirthTimeOptions, normalizeToOptionTime } from '../../utils/timeOptions'

const userStore = useUserStore()
const loading = ref(false)
const result = ref('')
const knowledgeRules = ref([])
const classicReferences = ref([])
const guaPanelRef = ref(null)
const reportPanelRef = ref(null)
const showDetailHint = ref(false)
const positions = ['初爻', '二爻', '三爻', '四爻', '五爻', '上爻']
const birthTimeOptions = buildBirthTimeOptions()
const shakenCount = ref(0)
const installInfo = reactive({ palace: '', palaceElement: '', shiPosition: 0, yingPosition: 0 })
const form = reactive({
  userId: userStore.userId,
  question: '',
  gender: '',
  birthDate: '',
  birthTime: '',
  birthPlace: '',
  birthDayGanZhi: '',
  birthDayMaster: '',
  time: '',
  dayGanZhi: '',
  dayStem: '',
  monthBranch: '',
  dayBranch: '',
  emptyBranches: '',
  mainGua: '',
  changedGua: '',
  yaoList: positions.map(position => ({
    position,
    coins: [],
    coinValue: null,
    lineType: '',
    changedLineType: '',
    yang: null,
    changedYang: null,
    sixGod: '',
    sixRelative: '',
    branch: '',
    element: '',
    shi: false,
    ying: false,
    moving: false,
    changedRelative: '',
    changedBranch: '',
    hiddenSpirit: ''
  }))
})

const parsedResult = computed(() => {
  try {
    return result.value ? JSON.parse(result.value) : null
  } catch {
    return { coreConclusion: result.value, confidence: '未知', keyEvidence: [], detailedAnalysis: {}, timing: [], suggestion: '' }
  }
})

const activeStep = computed(() => {
  if (result.value) return 3
  if (shakenCount.value > 0) return 2
  return 1
})

const beginnerGuideTitle = computed(() => {
  if (!form.question.trim()) return '第一步：先写下你要问的事'
  if (shakenCount.value < 6) return `第二步：摇第 ${shakenCount.value + 1} 爻`
  if (!result.value) return '第三步：卦已生成，可以开始分析'
  return '报告已生成'
})

const beginnerGuideText = computed(() => {
  if (!form.question.trim()) return '不用懂六爻术语，把问题写具体一点即可，比如“这次面试能否通过”。'
  if (shakenCount.value < 6) return '每次点击只摇一爻，下面会实时显示铜钱、阴阳和动爻；摇满六爻后再自动装卦。'
  if (!result.value) return '高级校正可以先不管，确认问题没错后直接点“开始分析”。'
  return '你可以复制报告，也可以重新起卦再问另一个问题。'
})

function shakeNext() {
  if (shakenCount.value >= 6) return
  const toss = tossCoins()
  Object.assign(form.yaoList[shakenCount.value], toss)
  shakenCount.value += 1
  updateGuaNames()
  if (shakenCount.value === 6) {
    scrollToGua()
  }
}

function reshakeYao(index) {
  if (index >= shakenCount.value) return
  Object.assign(form.yaoList[index], tossCoins())
  updateGuaNames()
  result.value = ''
  knowledgeRules.value = []
  classicReferences.value = []
}

function resetGua() {
  shakenCount.value = 0
  form.mainGua = ''
  form.changedGua = ''
  result.value = ''
  knowledgeRules.value = []
  classicReferences.value = []
  showDetailHint.value = false
  Object.assign(installInfo, { palace: '', palaceElement: '', shiPosition: 0, yingPosition: 0 })
  form.yaoList.forEach((yao, index) => {
    Object.assign(yao, {
      position: positions[index],
      coins: [],
      coinValue: null,
      lineType: '',
      changedLineType: '',
      yang: null,
      changedYang: null,
      moving: false,
      changedRelative: '',
      changedBranch: ''
    })
  })
}

function updateGuaNames() {
  if (shakenCount.value === 6) {
    form.mainGua = buildHexagram(form.yaoList)
    form.changedGua = buildHexagram(form.yaoList, true)
    autoInstall()
  }
}

async function scrollToGua() {
  await nextTick()
  guaPanelRef.value?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

function autoInstall() {
  if (shakenCount.value !== 6) return
  Object.assign(installInfo, installHexagram(form.yaoList, form.dayStem))
}

function setCurrentTime() {
  form.time = formatDateTime(new Date())
  updateDivinationDay()
}

function formatDateTime(date) {
  const pad = value => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function visualLines(changed) {
  return [...form.yaoList].reverse().map(yao => ({
    position: yao.position,
    yang: changed ? yao.changedYang : yao.yang,
    moving: !changed && yao.moving,
    lineType: changed ? yao.changedLineType : yao.lineType
  }))
}

function updateDivinationDay() {
  const day = getDayGanZhi(form.time)
  form.dayGanZhi = day.ganzhi
  form.dayStem = day.stem
  autoInstall()
}

function updateBirthDayMaster() {
  const day = getDayGanZhi(form.birthDate)
  form.birthDayGanZhi = day.ganzhi
  form.birthDayMaster = day.stem
}

function applyUserProfile() {
  const profile = userStore.getBirthProfile()
  if (!profile) return
  form.gender = profile.gender || ''
  form.birthDate = profile.birthDate || ''
  form.birthTime = normalizeToOptionTime(profile.birthTime || '')
  form.birthPlace = profile.birthPlace || ''
  form.birthDayGanZhi = profile.birthDayGanZhi || profile.dayPillar || ''
  form.birthDayMaster = profile.birthDayMaster || profile.dayMaster || ''
  if (form.birthDate && !form.birthDayGanZhi) {
    updateBirthDayMaster()
  }
}

async function saveProfile() {
  updateBirthDayMaster()
  const profile = userStore.saveBirthProfile({
    gender: form.gender,
    birthDate: form.birthDate,
    birthTime: form.birthTime,
    birthPlace: form.birthPlace,
    birthDayGanZhi: form.birthDayGanZhi,
    birthDayMaster: form.birthDayMaster
  })
  if (userStore.userId) {
    const user = await updateProfile({
      userId: userStore.userId,
      gender: profile.gender,
      birthDate: profile.birthDate,
      birthTime: profile.birthTime,
      birthPlace: profile.birthPlace,
      birthDayGanZhi: profile.birthDayGanZhi,
      birthDayMaster: profile.birthDayMaster
    })
    userStore.setUser(user)
    userStore.saveBirthProfile(profile)
  }
  ElMessage.success(userStore.userId ? '出生资料已保存到账号' : '出生资料已保存到本机')
}

async function submit() {
  if (!form.question.trim()) {
    ElMessage.warning('请先填写求测问题')
    return
  }
  if (shakenCount.value < 6) {
    ElMessage.warning('请先摇满六爻')
    return
  }
  updateDivinationDay()
  if (!form.dayStem) {
    ElMessage.warning('请检查起卦时间，系统需要自动计算日干')
    return
  }
  loading.value = true
  try {
    updateBirthDayMaster()
    userStore.saveBirthProfile({
      gender: form.gender,
      birthDate: form.birthDate,
      birthTime: form.birthTime,
      birthPlace: form.birthPlace,
      birthDayGanZhi: form.birthDayGanZhi,
      birthDayMaster: form.birthDayMaster
    })
    const payload = {
      ...form,
      userId: userStore.userId,
      yaoList: form.yaoList.map(({ coins, ...yao }) => ({ ...yao, coins }))
    }
    const data = await analyzeLiuyao(payload)
    result.value = data.resultJson
    knowledgeRules.value = data.knowledgeRules || []
    classicReferences.value = data.classicReferences || []
    ElMessage.success('分析完成')
    scrollToReport()
  } finally {
    loading.value = false
  }
}

async function scrollToReport() {
  await nextTick()
  reportPanelRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function copyCurrentReport() {
  await copyText(buildReportMarkdown(parsedResult.value, knowledgeRules.value, {
    title: '六爻分析报告',
    classicReferences: classicReferences.value
  }))
  ElMessage.success('报告已复制')
}

function exportCurrentReport() {
  downloadMarkdown(`六爻分析报告-${Date.now()}.md`, buildReportMarkdown(parsedResult.value, knowledgeRules.value, {
    title: '六爻分析报告',
    classicReferences: classicReferences.value
  }))
}

onMounted(() => {
  applyUserProfile()
  setCurrentTime()
})
</script>

<style scoped>
.sub-title {
  margin: 0 0 4px;
  font-size: 15px;
}

.flow-section {
  margin: 0 0 14px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.flow-section.active {
  border-color: #d6a954;
  box-shadow: 0 8px 24px rgba(214, 169, 84, 0.08);
}

.flow-section.done {
  border-color: #98c27d;
  background: #fbfef8;
}

.flow-head {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 12px;
}

.flow-head span {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: #151a22;
  color: #f4d38a;
  font-weight: 800;
  font-size: 13px;
  flex: 0 0 auto;
}

.flow-head h3 {
  margin: 0 0 4px;
  font-size: 17px;
}

.flow-head p {
  margin: 0;
  color: #667085;
  line-height: 1.55;
}

.helper-collapse,
.divination-detail-collapse {
  margin-bottom: 14px;
}

.shake-card {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin: 10px 0 14px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}

.shake-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.shake-actions .el-button:last-child {
  min-width: 150px;
  font-weight: 800;
}

.beginner-guide {
  display: grid;
  gap: 4px;
  margin: -4px 0 12px;
  padding: 12px 14px;
  border: 1px solid #ead7a8;
  border-radius: 8px;
  background: #fffaf0;
  color: #4f3712;
}

.beginner-guide strong {
  font-size: 15px;
}

.beginner-guide span {
  color: #7a5b1e;
  font-size: 13px;
  line-height: 1.6;
}

.install-info {
  margin: 8px 0 14px;
}

.gua-summary {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin: 10px 0 4px;
  padding: 12px 14px;
  border: 1px solid #dfe7f1;
  border-radius: 8px;
  background: #f8fafc;
}

.gua-summary div {
  display: grid;
  gap: 3px;
  min-width: 0;
}

.gua-summary span,
.gua-summary small {
  color: #667085;
  font-size: 13px;
  line-height: 1.5;
}

.gua-summary strong {
  color: #101828;
  font-size: 18px;
  line-height: 1.4;
}

.gua-summary button {
  border: 1px solid #d0d5dd;
  border-radius: 999px;
  background: #fff;
  color: #475467;
  padding: 7px 12px;
  white-space: nowrap;
}

.gua-explain {
  margin: 8px 0 0;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fffaf0;
  color: #7a5b1e;
  line-height: 1.7;
  font-size: 13px;
}

.shake-progress {
  margin: 8px 0 14px;
}

.hint {
  margin: 0;
  color: #667085;
  font-size: 13px;
}

.profile-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.bazi-collapse,
.time-extra-collapse {
  margin-bottom: 12px;
}

.muted {
  color: #98a2b3;
}

.coins {
  display: flex;
  align-items: center;
  gap: 5px;
}

.mobile-steps {
  margin-bottom: 16px;
}

.yao-cards {
  display: grid;
  gap: 10px;
}

.live-yao-panel {
  margin: 10px 0 14px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fbfcfe;
}

.live-yao-title {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
  margin-bottom: 10px;
}

.live-yao-title strong {
  color: #101828;
  font-size: 15px;
}

.live-yao-title span {
  color: #667085;
  font-size: 13px;
}

.live-yao-cards {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.yao-card {
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  min-width: 0;
}

.yao-card.done {
  border-color: #d6a954;
  background: #fffdf7;
}

.yao-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.mobile-coins {
  margin: 10px 0;
  flex-wrap: wrap;
}

.line-preview {
  display: grid;
  grid-template-columns: 88px auto;
  gap: 8px;
  align-items: center;
  color: #667085;
  font-size: 13px;
}

.line-preview em {
  font-style: normal;
}

.advanced-collapse {
  margin-top: 12px;
}

.coin {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: inline-grid;
  place-items: center;
  border: 1px solid #c9a24f;
  background: #fff7df;
  color: #7a5614;
  font-size: 12px;
  font-weight: 700;
}

.coin.back {
  background: #2f3a4a;
  border-color: #2f3a4a;
  color: #fff;
}

.inline-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
}

.actions {
  margin-top: 18px;
  text-align: right;
}

.estimate-note {
  margin-top: 12px;
}

.result-panel {
  min-height: 520px;
  min-width: 0;
  overflow-x: hidden;
}

.gua-panel {
  margin-top: 16px;
  padding: 14px;
  background: #fbfcfe;
}

.inline-gua-panel {
  margin-bottom: 16px;
}

.gua-visuals {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.gua-visuals h3 {
  margin: 0 0 10px;
  font-size: 15px;
}

.lines {
  display: grid;
  gap: 8px;
}

.line-row {
  display: grid;
  grid-template-columns: 42px 88px 54px;
  align-items: center;
  gap: 8px;
  color: #667085;
  font-size: 13px;
}

.line {
  height: 12px;
  position: relative;
}

.line::before,
.line::after {
  content: "";
  position: absolute;
  top: 0;
  height: 12px;
  border-radius: 2px;
  background: #1f2937;
}

.line::before {
  left: 0;
  width: 36px;
}

.line::after {
  right: 0;
  width: 36px;
}

.line.yang::before {
  width: 88px;
}

.line.yang::after {
  display: none;
}

.line.moving::before,
.line.moving::after {
  background: #b42318;
}

.line-row em {
  font-style: normal;
}

@media (max-width: 700px) {
  .page {
    padding: 10px 8px 88px;
  }

  .mobile-stack {
    display: flex;
    flex-direction: column;
  }

  .panel {
    padding: 10px;
  }

  .shake-card {
    display: grid;
    gap: 12px;
    padding: 10px;
  }

  .shake-actions {
    display: grid;
    grid-template-columns: 76px 1fr;
    gap: 8px;
  }

  .shake-actions .el-button:nth-child(2) {
    grid-column: 2;
    min-height: 56px;
    white-space: normal;
    font-size: 16px;
    font-weight: 800;
  }

  .live-yao-cards {
    grid-template-columns: 1fr;
  }

  .gua-summary {
    align-items: stretch;
    flex-direction: column;
    padding: 10px 12px;
  }

  .gua-summary strong {
    font-size: 16px;
  }

  .gua-summary button {
    width: fit-content;
  }

  .yao-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    color: #667085;
    font-size: 13px;
  }

  .yao-meta span {
    padding: 3px 7px;
    border-radius: 6px;
    background: #f2f4f7;
  }

  .advanced-collapse {
    display: none;
  }

  .gua-visuals {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .line-row {
    grid-template-columns: 36px minmax(78px, 96px) 48px;
    gap: 6px;
  }

  .line.yang::before {
    width: 82px;
  }

  .line::before,
  .line::after {
    width: 33px;
  }

  .actions {
    display: grid;
    grid-template-columns: 1fr;
    gap: 8px;
    text-align: stretch;
  }

  .actions .el-button {
    width: 100%;
  }

  .result-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .result-actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .result-actions .el-button {
    width: 100%;
    flex: 1;
    margin-left: 0;
  }
}

.result-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 14px;
}

.result-head .section-title {
  margin-bottom: 0;
}

.result-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
</style>
