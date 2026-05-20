<template>
  <div class="page tool-page-liuyao">
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
                  <span class="hint">{{ userStore.userId ? '已登录时会同步到账号' : '登录后可同步到账号' }}</span>
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
            <div class="turtle-stage" :class="{ shaking: isShaking, settled: animatedCoins.length && !isShaking }">
              <div class="ink-scene">
                <div class="shell-wrap">
                  <img src="/images/xuangui-shell.png" alt="玄龟" />
                </div>
                <div class="turtle-stage-label">
                  <strong>{{ isShaking ? `请继续摇晃手机 ${activeShakeIndex + 1}/6` : shakenCount ? `已成 ${shakenCount}/6 爻` : '请摇晃手机六次' }}</strong>
                  <span>{{ isShaking ? '玄龟摇动，铜钱将落入卦盘' : animatedCoins.length ? '铜钱已落定，可继续摇下一爻' : '也可以点击下方按钮逐爻起卦' }}</span>
                </div>
                <div class="bagua-plate">
                  <span class="plate-ring ring-outer"></span>
                  <span class="plate-ring ring-inner"></span>
                  <span class="trigram trigram-qian">☰</span>
                  <span class="trigram trigram-kun">☷</span>
                  <span class="trigram trigram-li">☲</span>
                  <span class="trigram trigram-kan">☵</span>
                  <div class="stage-lines">
                    <span
                      v-for="(row, index) in stageLines"
                      :key="`${row.position}-${index}`"
                      class="stage-line"
                      :class="{ yang: row.yang, moving: row.moving }"
                    ></span>
                    <em v-if="!stageLines.length">待起爻</em>
                  </div>
                </div>
                <div v-if="isShaking || animatedCoins.length" class="animated-coins">
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
                  <img
                    :src="animatedCoins[index - 1] ? '/images/xuangui-coin-back.png' : '/images/xuangui-coin-front.png'"
                    :alt="animatedCoins[index - 1] ? '铜钱背面' : '铜钱正面'"
                  />
                </span>
                </div>
                <div class="coin-legend">
                  <span><img src="/images/xuangui-coin-front.png" alt="" />字面为正，记 2</span>
                  <span><img src="/images/xuangui-coin-back.png" alt="" />背面为反，记 3</span>
                </div>
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
import { clearAnalysisCache, finishPendingAnalysis, readAnalysisCache, readPendingAnalysis, saveAnalysisCache, startPendingAnalysis } from '../../utils/analysisCache'

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

const stageLines = computed(() => form.yaoList.slice(0, shakenCount.value).reverse())

const wait = ms => new Promise(resolve => setTimeout(resolve, ms))

async function shakeNext() {
  if (shakenCount.value >= 6 || isShaking.value) return
  isShaking.value = true
  const token = ++shakeToken.value
  activeShakeIndex.value = shakenCount.value
  animatedCoins.value = []
  const toss = tossCoins()
  await wait(680)
  if (token !== shakeToken.value) return
  animatedCoins.value = toss.coins
  await wait(180)
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
  await wait(680)
  if (token !== shakeToken.value) return
  animatedCoins.value = toss.coins
  await wait(180)
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
  if (readPendingAnalysis(PENDING_KEY)) {
    analysisNotice.value = '之前的报告正在分析中，请稍后再试；完成后可以在历史记录查看。'
    ElMessage.warning(analysisNotice.value)
    return
  }
  loading.value = true
  startPendingAnalysis(PENDING_KEY, { type: 'LIUYAO', question: form.question })
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
    finishPendingAnalysis(PENDING_KEY)
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
  } else if (readPendingAnalysis(PENDING_KEY)) {
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
.tool-page-liuyao {
  position: relative;
  min-height: 100vh;
  color: #183a33;
  background:
    linear-gradient(180deg, rgba(0, 54, 48, 0.94) 0 230px, rgba(248, 241, 226, 0.97) 231px),
    radial-gradient(circle at 82% 94px, rgba(232, 214, 165, 0.17), transparent 30%),
    repeating-linear-gradient(105deg, transparent 0 38px, rgba(47, 111, 94, 0.035) 38px 40px);
}

.tool-page-liuyao::before {
  content: "";
  position: absolute;
  inset: 0 0 auto;
  height: 265px;
  pointer-events: none;
  background:
    linear-gradient(90deg, rgba(4, 35, 31, 0.82), rgba(22, 30, 22, 0.48) 48%, rgba(22, 30, 22, 0.18)),
    linear-gradient(180deg, rgba(4, 35, 31, 0.12), rgba(4, 35, 31, 0.54)),
    url("/images/liuyao-page-bg.png") center / cover no-repeat;
  opacity: 0.96;
}

.tool-page-liuyao > * {
  position: relative;
  z-index: 1;
}

.tool-page-liuyao .page-header {
  max-width: 1180px;
  margin: 0 auto 16px;
  padding: 26px 28px;
  border: 1px solid rgba(232, 214, 165, 0.28);
  border-radius: 14px;
  color: #f8f0d8;
  background: linear-gradient(90deg, rgba(10, 44, 38, 0.5), rgba(48, 39, 25, 0.18));
  box-shadow: 0 18px 34px rgba(17, 47, 40, 0.16);
}

.tool-page-liuyao .page-title {
  color: #f4d791;
  font-family: "Kaiti SC", "STKaiti", "KaiTi", "Songti SC", serif;
  font-size: 34px;
  font-weight: 400;
}

.tool-page-liuyao .page-desc {
  color: rgba(255, 248, 221, 0.88);
}

.tool-page-liuyao .panel,
.tool-page-liuyao .mobile-steps {
  border-color: rgba(176, 138, 60, 0.25);
  background:
    linear-gradient(180deg, rgba(255, 253, 246, 0.96), rgba(251, 250, 246, 0.98)),
    repeating-linear-gradient(90deg, transparent 0 72px, rgba(47, 111, 94, 0.026) 72px 74px);
  box-shadow: 0 14px 28px rgba(23, 63, 53, 0.08), inset 0 0 0 1px rgba(255, 255, 255, 0.42);
}

.tool-page-liuyao .mobile-steps {
  max-width: 1180px;
  margin: 0 auto 16px;
  padding: 14px 18px;
  border: 1px solid rgba(176, 138, 60, 0.25);
  border-radius: 12px;
}

.sub-title {
  margin: 0 0 4px;
  font-size: 15px;
}

.flow-section {
  margin: 0 0 14px;
  padding: 14px;
  border: 1px solid rgba(176, 138, 60, 0.22);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.72);
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
  border: 1px solid rgba(176, 138, 60, 0.22);
  border-radius: 8px;
  background: rgba(255, 253, 246, 0.76);
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

.turtle-stage {
  margin: 0 0 14px;
}

.ink-scene {
  position: relative;
  min-height: 620px;
  border: 1px solid rgba(196, 176, 125, 0.54);
  border-radius: 10px;
  background:
    linear-gradient(180deg, rgba(255, 255, 246, 0.16), rgba(244, 242, 221, 0.2)),
    url("/images/xuangui-bg.jpg") center / cover no-repeat;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.64), 0 10px 26px rgba(69, 54, 21, 0.09);
  overflow: hidden;
}

.ink-scene::before,
.ink-scene::after {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.ink-scene::before {
  background:
    radial-gradient(ellipse at 50% 38%, rgba(255, 255, 255, 0.34), transparent 33%),
    radial-gradient(ellipse at 14% 20%, rgba(47, 89, 76, 0.08), transparent 28%),
    radial-gradient(ellipse at 90% 23%, rgba(43, 75, 69, 0.08), transparent 26%),
    linear-gradient(135deg, transparent 58%, rgba(35, 79, 55, 0.12) 59%, transparent 61%),
    linear-gradient(112deg, transparent 72%, rgba(35, 79, 55, 0.1) 73%, transparent 75%);
  filter: blur(0.2px);
}

.ink-scene::after {
  top: auto;
  height: 46%;
  background: linear-gradient(180deg, transparent, rgba(255, 255, 255, 0.34));
}

.shell-wrap {
  position: absolute;
  top: 54px;
  left: 50%;
  z-index: 2;
  width: 270px;
  transform: translateX(-50%);
  filter: drop-shadow(0 20px 20px rgba(90, 58, 22, 0.2));
}

.turtle-stage.shaking .shell-wrap {
  animation: shellShake 0.68s ease-in-out both;
}

.shell-wrap img {
  display: block;
  width: 100%;
  user-select: none;
}

.turtle-stage-label {
  position: absolute;
  top: 258px;
  left: 50%;
  z-index: 3;
  display: grid;
  gap: 5px;
  width: min(300px, 86%);
  text-align: center;
  transform: translateX(-50%);
}

.turtle-stage-label strong {
  color: #4f3712;
  font-size: 19px;
  font-weight: 800;
  letter-spacing: 1px;
}

.turtle-stage-label span {
  color: #7a6239;
  font-size: 13px;
}

.bagua-plate {
  position: absolute;
  left: 50%;
  bottom: 54px;
  z-index: 1;
  width: min(360px, 86vw);
  aspect-ratio: 1.72;
  transform: translateX(-50%);
}

.plate-ring {
  position: absolute;
  inset: 0;
  border: 2px solid rgba(255, 255, 232, 0.72);
  border-radius: 50%;
  box-shadow: 0 0 18px rgba(255, 255, 235, 0.38);
}

.ring-inner {
  inset: 20%;
  opacity: 0.78;
}

.bagua-plate::before {
  content: "";
  position: absolute;
  inset: 10%;
  border-radius: 50%;
  background:
    linear-gradient(0deg, transparent 44%, rgba(255, 255, 236, 0.54) 45% 48%, transparent 49%),
    linear-gradient(45deg, transparent 44%, rgba(255, 255, 236, 0.4) 45% 47%, transparent 48%),
    linear-gradient(90deg, transparent 44%, rgba(255, 255, 236, 0.54) 45% 48%, transparent 49%),
    linear-gradient(135deg, transparent 44%, rgba(255, 255, 236, 0.4) 45% 47%, transparent 48%);
  opacity: 0.42;
}

.trigram {
  position: absolute;
  color: rgba(255, 255, 235, 0.58);
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.trigram-qian {
  top: 2px;
  left: 50%;
  transform: translateX(-50%);
}

.trigram-kun {
  left: 50%;
  bottom: 2px;
  transform: translateX(-50%);
}

.trigram-li {
  top: 50%;
  left: 16px;
  transform: translateY(-50%);
}

.trigram-kan {
  top: 50%;
  right: 16px;
  transform: translateY(-50%);
}

.stage-lines {
  position: absolute;
  top: 50%;
  left: 50%;
  z-index: 4;
  display: grid;
  gap: 5px;
  justify-items: center;
  min-width: 70px;
  min-height: 54px;
  transform: translate(-50%, -50%);
}

.stage-lines em {
  align-self: center;
  color: rgba(92, 82, 54, 0.55);
  font-size: 13px;
  font-style: normal;
}

.stage-line {
  position: relative;
  display: block;
  width: 52px;
  height: 6px;
}

.stage-line::before,
.stage-line::after {
  content: "";
  position: absolute;
  top: 0;
  width: 21px;
  height: 6px;
  border-radius: 1px;
  background: #1f1b16;
}

.stage-line::before {
  left: 0;
}

.stage-line::after {
  right: 0;
}

.stage-line.yang::before {
  width: 52px;
}

.stage-line.yang::after {
  display: none;
}

.stage-line.moving::before,
.stage-line.moving::after {
  background: #9d1f16;
}

.animated-coins {
  position: absolute;
  left: 50%;
  bottom: 150px;
  z-index: 5;
  width: 250px;
  height: 150px;
  transform: translateX(-50%);
}

.coin-legend {
  position: absolute;
  left: 50%;
  bottom: 18px;
  z-index: 6;
  display: flex;
  gap: 10px;
  justify-content: center;
  width: min(360px, 90%);
  padding: 7px 10px;
  border: 1px solid rgba(182, 154, 93, 0.32);
  border-radius: 999px;
  background: rgba(255, 253, 241, 0.72);
  box-shadow: 0 6px 16px rgba(80, 58, 18, 0.08);
  color: #6b5326;
  font-size: 12px;
  transform: translateX(-50%);
}

.coin-legend span {
  display: inline-flex;
  gap: 5px;
  align-items: center;
  white-space: nowrap;
}

.coin-legend img {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.animated-coin {
  position: absolute;
  top: 36px;
  left: 98px;
  width: 62px;
  height: 62px;
  border-radius: 50%;
  display: block;
  background: transparent;
  border: 0;
  filter: drop-shadow(0 10px 8px rgba(75, 55, 18, 0.22));
  will-change: transform, opacity;
}

.animated-coin img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
  user-select: none;
}

.animated-coin.back img {
  filter: brightness(0.72) contrast(1.16) sepia(0.28);
}

.animated-coin.empty {
  opacity: 0.82;
}

.turtle-stage.shaking .animated-coin {
  animation: realisticCoinDrop 0.78s cubic-bezier(0.18, 0.86, 0.24, 1) both;
}

.turtle-stage.shaking .coin-2 {
  animation-delay: 0.06s;
}

.turtle-stage.shaking .coin-3 {
  animation-delay: 0.12s;
}

.turtle-stage.settled .coin-1 {
  transform: translate(-90px, 42px) rotate(-18deg);
}

.turtle-stage.settled .coin-2 {
  transform: translate(0, 3px) rotate(6deg);
}

.turtle-stage.settled .coin-3 {
  transform: translate(92px, 44px) rotate(20deg);
}

@keyframes shellShake {
  0%,
  100% {
    transform: translateX(-50%) translateY(0) rotate(0deg);
  }
  14% {
    transform: translateX(calc(-50% - 8px)) translateY(-2px) rotate(-3deg);
  }
  30% {
    transform: translateX(calc(-50% + 8px)) translateY(1px) rotate(3deg);
  }
  48% {
    transform: translateX(calc(-50% - 6px)) translateY(-1px) rotate(-2deg);
  }
  66% {
    transform: translateX(calc(-50% + 5px)) translateY(1px) rotate(1.5deg);
  }
  82% {
    transform: translateX(calc(-50% - 2px)) rotate(-0.7deg);
  }
}

@keyframes realisticCoinDrop {
  0% {
    transform: translate(0, -92px) rotate(0deg) scale(0.72);
    opacity: 0;
  }
  18% {
    opacity: 1;
  }
  48% {
    transform: translate(calc(var(--coin-x, 0) * 0.42), -18px) rotate(calc(var(--coin-rotate, 12deg) * 2.5)) scale(1.04);
  }
  78% {
    transform: translate(calc(var(--coin-x, 0) * 0.94), calc(var(--coin-y, 36px) - 8px)) rotate(calc(var(--coin-rotate, 12deg) * 0.7)) scale(0.98);
  }
  100% {
    transform: translate(var(--coin-x, 0), var(--coin-y, 36px)) rotate(var(--coin-rotate, 12deg)) scale(1);
    opacity: 1;
  }
}

.coin-1 {
  --coin-x: -90px;
  --coin-y: 42px;
  --coin-rotate: -18deg;
}

.coin-2 {
  --coin-x: 0px;
  --coin-y: 3px;
  --coin-rotate: 6deg;
}

.coin-3 {
  --coin-x: 92px;
  --coin-y: 44px;
  --coin-rotate: 20deg;
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
  .tool-page-liuyao {
    background:
      linear-gradient(180deg, rgba(0, 54, 48, 0.96) 0 270px, rgba(248, 241, 226, 0.98) 271px),
      radial-gradient(circle at 70% 95px, rgba(232, 214, 165, 0.13), transparent 30%);
  }

  .tool-page-liuyao::before {
    height: 300px;
    background-position: 52% top;
  }

  .page {
    padding: 10px 8px 88px;
  }

  .tool-page-liuyao .page-header {
    margin-bottom: 12px;
    padding: 18px 16px;
    border-radius: 12px;
  }

  .tool-page-liuyao .page-title {
    font-size: 28px;
  }

  .tool-page-liuyao .mobile-steps {
    margin-bottom: 12px;
    padding: 12px 10px;
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

  .ink-scene {
    min-height: 570px;
  }

  .shell-wrap {
    top: 42px;
    width: 230px;
  }

  .turtle-stage-label {
    top: 224px;
  }

  .turtle-stage-label strong {
    font-size: 17px;
  }

  .bagua-plate {
    bottom: 46px;
    width: min(320px, 92vw);
  }

  .animated-coins {
    bottom: 138px;
    width: 220px;
    height: 136px;
  }

  .coin-legend {
    bottom: 14px;
    gap: 6px;
    width: calc(100% - 24px);
    padding: 6px 8px;
    font-size: 11px;
  }

  .coin-legend img {
    width: 16px;
    height: 16px;
  }

  .animated-coin {
    left: 80px;
    width: 60px;
    height: 60px;
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
