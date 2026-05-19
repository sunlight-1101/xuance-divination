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
                      <div class="time-pair">
                        <el-select v-model="form.birthHour" filterable clearable placeholder="小时" @change="syncBirthTime">
                          <el-option v-for="item in birthHourOptions" :key="item.value" :label="item.label" :value="item.value" />
                        </el-select>
                        <el-select v-model="form.birthMinute" filterable placeholder="分钟" :disabled="!form.birthHour" @change="syncBirthTime">
                          <el-option v-for="item in birthMinuteOptions" :key="item.value" :label="item.label" :value="item.value" />
                        </el-select>
                      </div>
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
                <el-button :type="motionEnabled ? 'success' : 'default'" @click="enableMotionShake">
                  {{ motionEnabled ? '摇一摇已开启' : '开启摇一摇' }}
                </el-button>
                <el-button type="primary" size="large" :disabled="shakenCount >= 6 || isShaking" :loading="isShaking" @click="shakeNext">
                  {{ shakenCount >= 6 ? '已成卦' : isShaking ? '铜钱落定中' : `摇第 ${shakenCount + 1} 爻` }}
                </el-button>
              </div>
            </div>
            <div class="beginner-guide">
              <strong>{{ beginnerGuideTitle }}</strong>
              <span>{{ motionEnabled ? '现在可以拿起手机轻轻摇动，每摇一次生成一爻。' : beginnerGuideText }}</span>
            </div>
            <div class="coin-stage" :class="{ shaking: isShaking, settled: animatedCoins.length && !isShaking }">
              <div class="coin-stage-label">
                <strong>{{ isShaking ? `正在摇第 ${activeShakeIndex + 1} 爻` : animatedCoins.length ? `第 ${activeShakeIndex + 1} 爻已落定` : '铜钱待摇' }}</strong>
                <span>{{ isShaking ? '三枚铜钱翻转中，请稍候' : animatedCoins.length ? '结果已写入下方卦象' : '点击摇卦，或开启摇一摇后轻摇手机' }}</span>
              </div>
              <div class="animated-coins">
                <span
                  v-for="index in 3"
                  :key="index"
                  class="animated-coin"
                  :class="[
                    `coin-${index}`,
                    animatedCoins[index - 1] ? 'back' : 'front',
                    { empty: !animatedCoins.length }
                  ]"
                >
                  <i></i>
                  <b>{{ animatedCoins.length ? (animatedCoins[index - 1] ? '背' : '乾') : '玄' }}</b>
                  <small>{{ animatedCoins[index - 1] ? '坤' : '元' }}</small>
                </span>
              </div>
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
              title="正在调用 AI 细断，预计 60-120 秒。切屏后可回到本页恢复，也可以在历史记录查看结果，请不要重复点击。"
            />
            <el-alert
              v-if="analysisNotice"
              class="estimate-note"
              type="info"
              :closable="true"
              :title="analysisNotice"
              @close="analysisNotice = ''"
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
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { updateProfile } from '../../api/auth'
import { analyzeLiuyao } from '../../api/liuyao'
import KnowledgeReferences from '../../components/KnowledgeReferences.vue'
import ResultReport from '../../components/ResultReport.vue'
import { useUserStore } from '../../stores/user'
import { buildHexagram, installHexagram, tossCoins } from '../../utils/liuyao'
import { getDayGanZhi } from '../../utils/ganzhi'
import { buildReportMarkdown, copyText, downloadMarkdown } from '../../utils/report'
import { buildHourOptions, buildMinuteOptions, combineTimeParts, splitTimeParts } from '../../utils/timeOptions'
import { clearAnalysisCache, readAnalysisCache, saveAnalysisCache } from '../../utils/analysisCache'

const userStore = useUserStore()
const loading = ref(false)
const result = ref('')
const knowledgeRules = ref([])
const classicReferences = ref([])
const guaPanelRef = ref(null)
const reportPanelRef = ref(null)
const showDetailHint = ref(false)
const motionEnabled = ref(false)
const analysisNotice = ref('')
const lastMotion = ref(null)
const lastShakeAt = ref(0)
const isShaking = ref(false)
const animatedCoins = ref([])
const activeShakeIndex = ref(0)
const shakeToken = ref(0)
const positions = ['初爻', '二爻', '三爻', '四爻', '五爻', '上爻']
const CACHE_KEY = 'zhexuan_last_liuyao_report'
const PENDING_KEY = 'zhexuan_pending_liuyao_analysis'
const birthHourOptions = buildHourOptions()
const birthMinuteOptions = buildMinuteOptions()
const shakenCount = ref(0)
const installInfo = reactive({ palace: '', palaceElement: '', shiPosition: 0, yingPosition: 0 })
const form = reactive({
  userId: userStore.userId,
  question: '',
  gender: '',
  birthDate: '',
  birthTime: '',
  birthHour: '',
  birthMinute: '30',
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

const wait = ms => new Promise(resolve => setTimeout(resolve, ms))

async function shakeNext() {
  if (shakenCount.value >= 6 || isShaking.value) return
  isShaking.value = true
  const token = ++shakeToken.value
  activeShakeIndex.value = shakenCount.value
  animatedCoins.value = []
  const toss = tossCoins()
  await wait(760)
  if (token !== shakeToken.value) return
  animatedCoins.value = toss.coins
  await wait(220)
  if (token !== shakeToken.value) return
  Object.assign(form.yaoList[shakenCount.value], toss)
  shakenCount.value += 1
  updateGuaNames()
  if (shakenCount.value === 6) {
    scrollToGua()
  }
  isShaking.value = false
}

async function reshakeYao(index) {
  if (index >= shakenCount.value || isShaking.value) return
  isShaking.value = true
  const token = ++shakeToken.value
  activeShakeIndex.value = index
  animatedCoins.value = []
  const toss = tossCoins()
  await wait(760)
  if (token !== shakeToken.value) return
  animatedCoins.value = toss.coins
  await wait(220)
  if (token !== shakeToken.value) return
  Object.assign(form.yaoList[index], toss)
  updateGuaNames()
  result.value = ''
  knowledgeRules.value = []
  classicReferences.value = []
  isShaking.value = false
}

function resetGua() {
  shakeToken.value += 1
  shakenCount.value = 0
  isShaking.value = false
  animatedCoins.value = []
  activeShakeIndex.value = 0
  form.mainGua = ''
  form.changedGua = ''
  result.value = ''
  knowledgeRules.value = []
  classicReferences.value = []
  clearAnalysisCache(CACHE_KEY)
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

async function enableMotionShake() {
  if (motionEnabled.value) return
  if (!window.DeviceMotionEvent) {
    ElMessage.warning('当前浏览器不支持摇一摇，可以继续点击按钮摇卦')
    return
  }
  try {
    if (typeof DeviceMotionEvent.requestPermission === 'function') {
      const permission = await DeviceMotionEvent.requestPermission()
      if (permission !== 'granted') {
        ElMessage.warning('未获得传感器权限，可以继续点击按钮摇卦')
        return
      }
    }
    window.addEventListener('devicemotion', handleDeviceMotion)
    motionEnabled.value = true
    ElMessage.success('摇一摇已开启，轻轻摇动手机即可起爻')
  } catch {
    ElMessage.warning('开启摇一摇失败，可以继续点击按钮摇卦')
  }
}

function handleDeviceMotion(event) {
  if (shakenCount.value >= 6 || loading.value || isShaking.value) return
  const acc = event.accelerationIncludingGravity || event.acceleration
  if (!acc) return
  const current = { x: acc.x || 0, y: acc.y || 0, z: acc.z || 0, time: Date.now() }
  if (!lastMotion.value) {
    lastMotion.value = current
    return
  }
  const previous = lastMotion.value
  const deltaTime = Math.max((current.time - previous.time) / 1000, 0.08)
  const speed = Math.abs(current.x + current.y + current.z - previous.x - previous.y - previous.z) / deltaTime
  lastMotion.value = current
  if (speed > 90 && current.time - lastShakeAt.value > 900) {
    lastShakeAt.value = current.time
    shakeNext()
    if (shakenCount.value < 6) {
      ElMessage.success(`已摇出第 ${shakenCount.value} 爻`)
    }
  }
}

function syncBirthTime() {
  form.birthTime = combineTimeParts(form.birthHour, form.birthMinute)
}

function applyUserProfile() {
  const profile = userStore.getBirthProfile()
  if (!profile) return
  const timeParts = splitTimeParts(profile.birthTime || '')
  form.gender = profile.gender || ''
  form.birthDate = profile.birthDate || ''
  form.birthHour = timeParts.hour
  form.birthMinute = timeParts.minute
  form.birthTime = combineTimeParts(timeParts.hour, timeParts.minute)
  form.birthPlace = profile.birthPlace || ''
  form.birthDayGanZhi = profile.birthDayGanZhi || profile.dayPillar || ''
  form.birthDayMaster = profile.birthDayMaster || profile.dayMaster || ''
  if (form.birthDate && !form.birthDayGanZhi) {
    updateBirthDayMaster()
  }
}

async function saveProfile() {
  syncBirthTime()
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
  syncBirthTime()
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
  saveAnalysisCache(PENDING_KEY, { type: 'LIUYAO', question: form.question, startedAt: Date.now() })
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
    const { birthHour, birthMinute, ...submitForm } = form
    const payload = {
      ...submitForm,
      userId: userStore.userId,
      yaoList: form.yaoList.map(({ coins, ...yao }) => ({ ...yao, coins }))
    }
    const data = await analyzeLiuyao(payload)
    result.value = data.resultJson
    knowledgeRules.value = data.knowledgeRules || []
    classicReferences.value = data.classicReferences || []
    saveAnalysisCache(CACHE_KEY, {
      result: result.value,
      knowledgeRules: knowledgeRules.value,
      classicReferences: classicReferences.value
    })
    clearAnalysisCache(PENDING_KEY)
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
  const cached = readAnalysisCache(CACHE_KEY)
  if (cached?.result && !result.value) {
    result.value = cached.result
    knowledgeRules.value = cached.knowledgeRules || []
    classicReferences.value = cached.classicReferences || []
    analysisNotice.value = '已恢复上一次六爻分析报告；如果切屏后没看到结果，也可以到历史记录查看。'
  } else if (readAnalysisCache(PENDING_KEY)) {
    analysisNotice.value = '检测到上次有分析进行中；如果本页没有恢复结果，请到历史记录查看，避免重复消耗。'
  }
})

onBeforeUnmount(() => {
  if (motionEnabled.value) {
    window.removeEventListener('devicemotion', handleDeviceMotion)
  }
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

.coin-stage {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  margin: 0 0 12px;
  padding: 14px 16px;
  border: 1px solid rgba(181, 142, 68, 0.42);
  border-radius: 8px;
  background:
    linear-gradient(115deg, rgba(255, 255, 255, 0.44), transparent 42%),
    radial-gradient(circle at 84% 24%, rgba(203, 161, 86, 0.22), transparent 30%),
    repeating-linear-gradient(90deg, rgba(67, 103, 74, 0.035) 0 1px, transparent 1px 9px),
    linear-gradient(135deg, #fffaf0, #f4efe0);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.56), 0 8px 22px rgba(71, 53, 20, 0.07);
  overflow: hidden;
}

.coin-stage::before,
.coin-stage::after {
  content: "";
  position: absolute;
  pointer-events: none;
}

.coin-stage::before {
  right: 16px;
  bottom: -18px;
  width: 126px;
  height: 72px;
  border-radius: 50%;
  background: radial-gradient(ellipse, rgba(89, 111, 80, 0.16), transparent 68%);
}

.coin-stage::after {
  right: 4px;
  top: 0;
  width: 86px;
  height: 100%;
  background:
    linear-gradient(108deg, transparent 36%, rgba(44, 86, 66, 0.16) 37%, transparent 39%),
    linear-gradient(96deg, transparent 56%, rgba(44, 86, 66, 0.12) 57%, transparent 59%);
  opacity: 0.55;
}

.coin-stage.shaking {
  animation: trayShake 0.82s ease-in-out both;
}

.coin-stage-label {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 4px;
  min-width: 0;
}

.coin-stage-label strong {
  color: #173f35;
  font-size: 15px;
}

.coin-stage-label span {
  color: #806326;
  font-size: 13px;
  line-height: 1.5;
}

.animated-coins {
  position: relative;
  z-index: 1;
  width: 162px;
  height: 82px;
}

.animated-coin {
  position: absolute;
  top: 20px;
  left: 58px;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  display: inline-grid;
  place-items: center;
  border: 2px solid #9f7130;
  background:
    radial-gradient(circle at 34% 24%, rgba(255, 247, 203, 0.9), transparent 18%),
    radial-gradient(circle at center, #d9ae55 0 34%, #b27a2e 35% 50%, #8d5b22 51% 56%, #d3a552 57% 72%, #754819 100%);
  box-shadow:
    inset 0 1px 3px rgba(255, 244, 191, 0.76),
    inset 0 -4px 7px rgba(69, 41, 13, 0.28),
    0 10px 18px rgba(80, 58, 18, 0.22);
  color: #4d310d;
  font-size: 12px;
  font-weight: 900;
  transform-style: preserve-3d;
}

.animated-coin.back {
  border-color: #6a4b2a;
  background:
    radial-gradient(circle at 36% 25%, rgba(246, 213, 140, 0.42), transparent 18%),
    radial-gradient(circle at center, #a97835 0 34%, #765023 35% 50%, #4f351b 51% 56%, #8a632f 57% 72%, #352419 100%);
  color: #f7e5b5;
}

.animated-coin::before,
.animated-coin::after {
  content: "";
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.animated-coin::before {
  inset: 5px;
  border: 1px solid rgba(91, 57, 14, 0.42);
  box-shadow: inset 0 0 0 2px rgba(255, 229, 150, 0.2);
}

.animated-coin::after {
  inset: 13px;
  border-radius: 3px;
  background: #f6efcf;
  border: 1px solid rgba(91, 57, 14, 0.48);
  box-shadow: inset 0 2px 4px rgba(51, 31, 10, 0.2);
}

.animated-coin.back::after {
  background: #2b2117;
  border-color: rgba(247, 229, 181, 0.35);
}

.animated-coin i {
  position: absolute;
  inset: 16px;
  z-index: 1;
  border-radius: 2px;
  background: transparent;
}

.animated-coin b,
.animated-coin small {
  position: absolute;
  z-index: 2;
  line-height: 1;
  text-shadow: 0 1px 0 rgba(255, 239, 180, 0.32);
}

.animated-coin b {
  top: 8px;
}

.animated-coin small {
  bottom: 8px;
  font-size: 10px;
  font-weight: 900;
}

.animated-coin.empty {
  opacity: 0.52;
  filter: saturate(0.7);
  transform: scale(0.9);
}

.coin-stage.shaking .animated-coin {
  animation: coinToss 0.82s cubic-bezier(0.18, 0.82, 0.22, 1) both;
}

.coin-stage.shaking .coin-2 {
  animation-delay: 0.08s;
}

.coin-stage.shaking .coin-3 {
  animation-delay: 0.16s;
}

.coin-stage.settled .coin-1 {
  transform: translate(-52px, 10px) rotate(-16deg);
}

.coin-stage.settled .coin-2 {
  transform: translate(1px, -8px) rotate(8deg);
}

.coin-stage.settled .coin-3 {
  transform: translate(52px, 12px) rotate(17deg);
}

@keyframes trayShake {
  0%,
  100% {
    transform: translateX(0);
  }
  18% {
    transform: translateX(-3px) rotate(-0.4deg);
  }
  36% {
    transform: translateX(3px) rotate(0.4deg);
  }
  54% {
    transform: translateX(-2px) rotate(-0.25deg);
  }
  72% {
    transform: translateX(2px) rotate(0.2deg);
  }
}

@keyframes coinToss {
  0% {
    transform: translate(0, 16px) rotateX(0deg) rotateY(0deg) rotateZ(0deg) scale(0.9);
    opacity: 0.78;
  }
  26% {
    transform: translate(calc(var(--coin-x, 0) * 0.55), -24px) rotateX(64deg) rotateY(260deg) rotateZ(var(--coin-rotate, 16deg)) scale(1.02);
    opacity: 1;
  }
  62% {
    transform: translate(calc(var(--coin-x, 0) * -0.25), -4px) rotateX(18deg) rotateY(520deg) rotateZ(calc(var(--coin-rotate, 16deg) * -0.7)) scale(0.97);
  }
  100% {
    transform: translate(var(--coin-x, 0), var(--coin-y, 10px)) rotateX(0deg) rotateY(720deg) rotateZ(var(--coin-rotate, 16deg)) scale(1);
    opacity: 1;
  }
}

.coin-1 {
  --coin-x: -52px;
  --coin-y: 10px;
  --coin-rotate: -16deg;
}

.coin-2 {
  --coin-x: 1px;
  --coin-y: -8px;
  --coin-rotate: 8deg;
}

.coin-3 {
  --coin-x: 52px;
  --coin-y: 12px;
  --coin-rotate: 17deg;
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
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  .shake-actions .el-button:last-child {
    grid-column: 1 / -1;
    min-height: 56px;
    white-space: normal;
    font-size: 16px;
    font-weight: 800;
  }

  .coin-stage {
    grid-template-columns: 1fr;
    gap: 10px;
    padding: 12px;
  }

  .animated-coins {
    width: 100%;
    max-width: 164px;
    height: 74px;
    justify-self: center;
  }

  .animated-coin {
    left: calc(50% - 23px);
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
