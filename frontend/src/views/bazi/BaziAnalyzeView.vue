<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">八字分析</h1>
      <p class="page-desc">输入出生日期、时间和问题，系统会先自动排出四柱，再结合知识库生成分析。</p>
    </div>

    <div class="mode-switch">
      <button type="button" :class="{ active: analysisMode === 'single' }" @click="switchAnalysisMode('single')">个人分析</button>
      <button type="button" :class="{ active: analysisMode === 'compatibility' }" @click="switchAnalysisMode('compatibility')">合盘解析</button>
    </div>

    <el-row :gutter="16" class="mobile-stack">
      <el-col :xs="24" :lg="14">
        <div v-if="analysisMode === 'single'" class="panel">
          <h2 class="section-title">命盘信息</h2>
          <el-form :model="form" label-position="top">
            <div class="profile-picker">
              <el-select v-model="selectedProfileId" placeholder="选择已保存资料" clearable filterable @change="applySelectedProfileToSingle">
                <el-option
                  v-for="profile in savedProfiles"
                  :key="profile.id"
                  :label="profileLabel(profile)"
                  :value="profile.id"
                />
              </el-select>
              <el-button @click="saveBirthProfile">保存当前</el-button>
              <el-button :disabled="!selectedProfileId" @click="deleteSelectedProfile">删除</el-button>
            </div>
            <el-row :gutter="12">
              <el-col :xs="12" :sm="8">
                <el-form-item label="性别">
                  <el-select v-model="form.gender" placeholder="选择" @change="refreshLuckPillar">
                    <el-option label="男" value="男" />
                    <el-option label="女" value="女" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="12" :sm="8"><el-form-item label="出生日期"><el-input v-model="form.birthDate" placeholder="YYYY-MM-DD" @input="fillPillarsFromBirth" @change="fillPillarsFromBirth" /></el-form-item></el-col>
              <el-col :xs="12" :sm="8"><el-form-item label="出生时间"><el-input v-model="form.birthTime" placeholder="08:30 / 辰时 / 8点" @input="fillPillarsFromBirth" @change="fillPillarsFromBirth" /></el-form-item></el-col>
              <el-col :xs="12" :sm="8">
                <el-form-item label="出生省份">
                  <el-select
                    v-model="form.birthProvince"
                    filterable
                    placeholder="选择省份"
                    @change="onProvinceChange"
                  >
                    <el-option
                      v-for="province in provinceOptions"
                      :key="province.name"
                      :label="province.name"
                      :value="province.name"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="12" :sm="8">
                <el-form-item label="出生城市">
                  <el-select
                    v-model="form.birthPlace"
                    filterable
                    placeholder="选择城市"
                    :disabled="!form.birthProvince"
                    @change="onBirthPlaceChange"
                  >
                    <el-option
                      v-for="city in cityOptions"
                      :key="city.name"
                      :label="city.name"
                      :value="city.name"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="12" :sm="8"><el-form-item label="真太阳时"><el-switch v-model="form.useTrueSolarTime" @change="fillPillarsFromBirth" /></el-form-item></el-col>
              <el-col :xs="12" :sm="8"><el-form-item label="修正时间"><el-input :model-value="trueSolarTimeText" readonly /></el-form-item></el-col>
            </el-row>

            <el-alert
              class="auto-note"
              title="四柱会自动填写；打开真太阳时后，系统会按出生地自动匹配经度，并结合均时差修正出生时间。月柱目前按常用节气日期近似换月，临近节气日可手动校正。"
              type="info"
              :closable="false"
              show-icon
            />

            <div class="chart-preview">
              <div class="chart-tabs">
                <button
                  v-for="tab in chartTabs"
                  :key="tab.key"
                  type="button"
                  :class="{ active: activeChartTab === tab.key }"
                  @click="activeChartTab = tab.key"
                >{{ tab.label }}</button>
              </div>
              <div class="chart-banner">
                <div>
                  <span>阳历：{{ solarText }}</span>
                  <small>{{ chartStatus }}</small>
                </div>
                <el-button size="small" @click="rebuildPillars">重排</el-button>
              </div>

              <div v-if="activeChartTab === 'info'" class="tab-pane info-pane">
                <div class="info-grid">
                  <div><span>性别</span><strong>{{ form.gender || '未填' }}</strong></div>
                  <div><span>出生日期</span><strong>{{ form.birthDate || '未填' }}</strong></div>
                  <div><span>出生时间</span><strong>{{ form.birthTime || '未填' }}</strong></div>
                  <div><span>出生地</span><strong>{{ form.birthPlace || '未填' }}</strong></div>
                  <div><span>真太阳时</span><strong>{{ trueSolarTimeText }}</strong></div>
                  <div><span>日主</span><strong>{{ form.dayMaster || '未排' }}</strong></div>
                  <div><span>流年</span><strong>{{ form.currentYearPillar || '未排' }}</strong></div>
                </div>
              </div>

              <div v-else-if="activeChartTab === 'chart'" class="bazi-board">
                <div class="board-row header-row">
                  <div class="row-label">日期</div>
                  <div v-for="pillar in pillarCards" :key="pillar.key" class="board-cell">{{ pillar.label }}</div>
                </div>
                <div class="board-row">
                  <div class="row-label">主星</div>
                  <div v-for="pillar in pillarCards" :key="pillar.key" class="board-cell main-star">{{ pillar.tenGod || '-' }}</div>
                </div>
                <div class="board-row stem-row">
                  <div class="row-label">天干</div>
                  <div v-for="pillar in pillarCards" :key="pillar.key" class="board-cell">
                    <strong class="big-char" :class="elementClass(pillar.stemElement)">{{ pillar.stem || '-' }}</strong>
                  </div>
                </div>
                <div class="board-row branch-row">
                  <div class="row-label">地支</div>
                  <div v-for="pillar in pillarCards" :key="pillar.key" class="board-cell">
                    <strong class="big-char" :class="elementClass(pillar.branchElement)">{{ pillar.branch || '-' }}</strong>
                  </div>
                </div>
                <div class="board-row tall-row">
                  <div class="row-label">藏干</div>
                  <div v-for="pillar in pillarCards" :key="pillar.key" class="board-cell stack-cell">
                    <span
                      v-for="hidden in pillar.hiddenStems"
                      :key="hidden.stem"
                      :class="elementClass(hidden.element)"
                    >{{ hidden.stem }}{{ hidden.element }}</span>
                    <span v-if="!pillar.hiddenStems.length">-</span>
                  </div>
                </div>
                <div class="board-row tall-row">
                  <div class="row-label">副星</div>
                  <div v-for="pillar in pillarCards" :key="pillar.key" class="board-cell stack-cell">
                    <span v-for="hidden in pillar.hiddenStems" :key="hidden.stem">{{ hidden.tenGod }}</span>
                    <span v-if="!pillar.hiddenStems.length">-</span>
                  </div>
                </div>
                <div class="board-row">
                  <div class="row-label">星运</div>
                  <div v-for="pillar in pillarCards" :key="pillar.key" class="board-cell">{{ pillar.selfSitting || '-' }}</div>
                </div>
                <div class="board-row">
                  <div class="row-label">自坐</div>
                  <div v-for="pillar in pillarCards" :key="pillar.key" class="board-cell">{{ pillar.selfSitting || '-' }}</div>
                </div>
                <div class="board-row">
                  <div class="row-label">空亡</div>
                  <div v-for="pillar in pillarCards" :key="pillar.key" class="board-cell">{{ pillar.emptyText }}</div>
                </div>
                <div class="board-row">
                  <div class="row-label">纳音</div>
                  <div v-for="pillar in pillarCards" :key="pillar.key" class="board-cell">{{ pillar.naYin || '-' }}</div>
                </div>
                <div class="board-row shensha-row">
                  <div class="row-label">神煞</div>
                  <div v-for="pillar in pillarCards" :key="pillar.key" class="board-cell stack-cell shensha-cell">
                    <span v-for="name in pillar.shenSha" :key="name">{{ name }}</span>
                    <span v-if="!pillar.shenSha.length">-</span>
                  </div>
                </div>
              </div>

              <div v-else-if="activeChartTab === 'detail'" class="tab-pane detail-pane">
                <div class="detail-card">
                  <span>天干十神</span>
                  <strong>{{ baziDetails.summary.tenGods }}</strong>
                </div>
                <div class="detail-card">
                  <span>地支藏干</span>
                  <strong>{{ baziDetails.summary.hiddenStems }}</strong>
                </div>
                <div class="detail-card">
                  <span>旬空</span>
                  <strong>{{ baziDetails.summary.empty }}</strong>
                </div>
                <div class="detail-card">
                  <span>大运</span>
                  <strong>{{ baziDetails.summary.luck }}</strong>
                </div>
                <div class="detail-card">
                  <span>神煞</span>
                  <strong>{{ baziDetails.summary.shenSha }}</strong>
                </div>
                <div v-if="luckInfo.cycles.length" class="luck-grid">
                  <div
                    v-for="luck in luckInfo.cycles"
                    :key="luck.index"
                    :class="{ active: luck.active }"
                  >
                    <strong>{{ luck.pillar }}</strong>
                    <span>{{ luck.startAge }}-{{ luck.endAge }}岁</span>
                    <small>{{ luck.startYear }}-{{ luck.endYear }}</small>
                  </div>
                </div>
              </div>

              <div v-else class="tab-pane note-pane">
                <div class="note-line">
                  <span>求测方向</span>
                  <strong>{{ form.questionType }}</strong>
                </div>
                <div class="note-line">
                  <span>当前问题</span>
                  <strong>{{ form.question || '还没有填写问题' }}</strong>
                </div>
                <div class="note-line">
                  <span>引用规则</span>
                  <strong>{{ knowledgeRules.length ? `${knowledgeRules.length} 条，含 ${caseRuleCount} 条案例` : '分析后显示' }}</strong>
                </div>
                <div v-if="caseRules.length" class="case-list">
                  <span v-for="rule in caseRules" :key="rule.id">{{ rule.title }}</span>
                </div>
              </div>
            </div>

            <el-row :gutter="12">
              <el-col :xs="12" :sm="6"><el-form-item label="年柱"><el-input v-model="form.yearPillar" placeholder="如 庚午" @change="refreshLuckPillar" /></el-form-item></el-col>
              <el-col :xs="12" :sm="6"><el-form-item label="月柱"><el-input v-model="form.monthPillar" placeholder="如 己丑" @change="refreshLuckPillar" /></el-form-item></el-col>
              <el-col :xs="12" :sm="6"><el-form-item label="日柱"><el-input v-model="form.dayPillar" placeholder="自动或手填" @change="syncDayMaster" /></el-form-item></el-col>
              <el-col :xs="12" :sm="6"><el-form-item label="时柱"><el-input v-model="form.hourPillar" placeholder="如 壬辰" /></el-form-item></el-col>
              <el-col :xs="12" :sm="6"><el-form-item label="日主"><el-input v-model="form.dayMaster" readonly /></el-form-item></el-col>
              <el-col :xs="12" :sm="6"><el-form-item label="当前大运"><el-input v-model="form.luckPillar" placeholder="可选" /></el-form-item></el-col>
              <el-col :xs="12" :sm="6"><el-form-item label="当前流年"><el-input v-model="form.currentYearPillar" placeholder="可选" /></el-form-item></el-col>
              <el-col :xs="12" :sm="6">
                <el-form-item label="求测方向">
                  <el-select v-model="form.questionType">
                    <el-option label="综合" value="综合" />
                    <el-option label="感情" value="感情" />
                    <el-option label="事业" value="事业" />
                    <el-option label="财运" value="财运" />
                    <el-option label="学业" value="学业" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="具体问题">
              <el-input v-model="form.question" type="textarea" :rows="3" placeholder="例如：今年事业是否适合转型？" />
            </el-form-item>

            <div class="actions">
              <el-button size="large" @click="saveBirthProfile">保存出生资料</el-button>
              <el-button type="primary" size="large" :loading="loading" @click="submit">开始分析</el-button>
            </div>
            <el-alert
              v-if="loading"
              class="estimate-note"
              type="warning"
              :closable="false"
              title="正在调用 AI 细断，预计 60-120 秒，请不要重复点击。"
            />
          </el-form>
        </div>
        <div v-else class="panel compatibility-panel">
          <h2 class="section-title">合盘解析</h2>
          <p class="compatibility-desc">分别填写两个人的出生信息，系统会自动排出四柱，再从日柱、夫妻宫、五行、十神、大运流年角度做关系分析。</p>
          <el-form :model="compatForm" label-position="top">
            <div class="compatibility-saved-picker">
              <el-select v-model="compatSelectedA" placeholder="选择甲方资料" clearable filterable @change="applySelectedProfileToCompatibility('A')">
                <el-option v-for="profile in savedProfiles" :key="profile.id" :label="profileLabel(profile)" :value="profile.id" />
              </el-select>
              <el-select v-model="compatSelectedB" placeholder="选择乙方资料" clearable filterable @change="applySelectedProfileToCompatibility('B')">
                <el-option v-for="profile in savedProfiles" :key="profile.id" :label="profileLabel(profile)" :value="profile.id" />
              </el-select>
            </div>
            <div class="compatibility-people">
              <div class="compatibility-person">
                <div class="person-title">
                  <strong>甲方</strong>
                  <span>{{ compatibilityPillarSummary(compatForm.personA) }}</span>
                </div>
                <el-row :gutter="10">
                  <el-col :xs="12" :sm="8"><el-form-item label="姓名"><el-input v-model="compatForm.personA.name" placeholder="可选" /></el-form-item></el-col>
                  <el-col :xs="12" :sm="8">
                    <el-form-item label="性别">
                      <el-select v-model="compatForm.personA.gender" placeholder="选择" @change="fillCompatibilityPillars(compatForm.personA)">
                        <el-option label="男" value="男" />
                        <el-option label="女" value="女" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="12" :sm="8"><el-form-item label="出生日期"><el-input v-model="compatForm.personA.birthDate" placeholder="YYYY-MM-DD" @input="fillCompatibilityPillars(compatForm.personA)" /></el-form-item></el-col>
                  <el-col :xs="12" :sm="8"><el-form-item label="出生时间"><el-input v-model="compatForm.personA.birthTime" placeholder="00:30 / 子时" @input="fillCompatibilityPillars(compatForm.personA)" /></el-form-item></el-col>
                  <el-col :xs="12" :sm="8">
                    <el-form-item label="出生省份">
                      <el-select v-model="compatForm.personA.birthProvince" filterable placeholder="选择省份" @change="onCompatibilityProvinceChange(compatForm.personA)">
                        <el-option v-for="province in provinceOptions" :key="province.name" :label="province.name" :value="province.name" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="12" :sm="8">
                    <el-form-item label="出生城市">
                      <el-select v-model="compatForm.personA.birthPlace" filterable placeholder="选择城市" :disabled="!compatForm.personA.birthProvince" @change="onCompatibilityBirthPlaceChange(compatForm.personA)">
                        <el-option v-for="city in getCompatibilityCityOptions(compatForm.personA.birthProvince)" :key="city.name" :label="city.name" :value="city.name" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="12" :sm="8"><el-form-item label="真太阳时"><el-switch v-model="compatForm.personA.useTrueSolarTime" @change="fillCompatibilityPillars(compatForm.personA)" /></el-form-item></el-col>
                  <el-col :xs="12" :sm="8"><el-form-item label="日柱"><el-input v-model="compatForm.personA.dayPillar" placeholder="自动排出" @change="syncCompatibilityDayMaster(compatForm.personA)" /></el-form-item></el-col>
                  <el-col :xs="12" :sm="8"><el-form-item label="四柱"><el-input :model-value="compatibilityPillarSummary(compatForm.personA)" readonly /></el-form-item></el-col>
                </el-row>
              </div>

              <div class="compatibility-person">
                <div class="person-title">
                  <strong>乙方</strong>
                  <span>{{ compatibilityPillarSummary(compatForm.personB) }}</span>
                </div>
                <el-row :gutter="10">
                  <el-col :xs="12" :sm="8"><el-form-item label="姓名"><el-input v-model="compatForm.personB.name" placeholder="可选" /></el-form-item></el-col>
                  <el-col :xs="12" :sm="8">
                    <el-form-item label="性别">
                      <el-select v-model="compatForm.personB.gender" placeholder="选择" @change="fillCompatibilityPillars(compatForm.personB)">
                        <el-option label="男" value="男" />
                        <el-option label="女" value="女" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="12" :sm="8"><el-form-item label="出生日期"><el-input v-model="compatForm.personB.birthDate" placeholder="YYYY-MM-DD" @input="fillCompatibilityPillars(compatForm.personB)" /></el-form-item></el-col>
                  <el-col :xs="12" :sm="8"><el-form-item label="出生时间"><el-input v-model="compatForm.personB.birthTime" placeholder="00:30 / 子时" @input="fillCompatibilityPillars(compatForm.personB)" /></el-form-item></el-col>
                  <el-col :xs="12" :sm="8">
                    <el-form-item label="出生省份">
                      <el-select v-model="compatForm.personB.birthProvince" filterable placeholder="选择省份" @change="onCompatibilityProvinceChange(compatForm.personB)">
                        <el-option v-for="province in provinceOptions" :key="province.name" :label="province.name" :value="province.name" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="12" :sm="8">
                    <el-form-item label="出生城市">
                      <el-select v-model="compatForm.personB.birthPlace" filterable placeholder="选择城市" :disabled="!compatForm.personB.birthProvince" @change="onCompatibilityBirthPlaceChange(compatForm.personB)">
                        <el-option v-for="city in getCompatibilityCityOptions(compatForm.personB.birthProvince)" :key="city.name" :label="city.name" :value="city.name" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="12" :sm="8"><el-form-item label="真太阳时"><el-switch v-model="compatForm.personB.useTrueSolarTime" @change="fillCompatibilityPillars(compatForm.personB)" /></el-form-item></el-col>
                  <el-col :xs="12" :sm="8"><el-form-item label="日柱"><el-input v-model="compatForm.personB.dayPillar" placeholder="自动排出" @change="syncCompatibilityDayMaster(compatForm.personB)" /></el-form-item></el-col>
                  <el-col :xs="12" :sm="8"><el-form-item label="四柱"><el-input :model-value="compatibilityPillarSummary(compatForm.personB)" readonly /></el-form-item></el-col>
                </el-row>
              </div>
            </div>

            <el-row :gutter="12">
              <el-col :xs="24" :sm="8">
                <el-form-item label="关系类型">
                  <el-select v-model="compatForm.relationshipType">
                    <el-option label="恋爱婚姻" value="恋爱婚姻" />
                    <el-option label="合作事业" value="合作事业" />
                    <el-option label="亲子家人" value="亲子家人" />
                    <el-option label="朋友同事" value="朋友同事" />
                    <el-option label="综合关系" value="综合关系" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="16">
                <el-form-item label="想问的问题">
                  <el-input v-model="compatForm.question" type="textarea" :rows="3" placeholder="例如：我们适不适合长期发展？未来一年关系需要注意什么？" />
                </el-form-item>
              </el-col>
            </el-row>

            <div class="compatibility-preview">
              <div>
                <span>甲方真太阳时</span>
                <strong>{{ compatibilityTrueSolarText(compatForm.personA) }}</strong>
              </div>
              <div>
                <span>乙方真太阳时</span>
                <strong>{{ compatibilityTrueSolarText(compatForm.personB) }}</strong>
              </div>
            </div>

            <div class="actions">
              <el-button size="large" @click="copySingleProfileToPersonA">用我的出生资料填甲方</el-button>
              <el-button size="large" @click="saveCompatibilityPeople">保存甲乙方资料</el-button>
              <el-button type="primary" size="large" :loading="loading" @click="submitCompatibility">开始合盘解析</el-button>
            </div>
            <el-alert
              v-if="loading"
              class="estimate-note"
              type="warning"
              :closable="false"
              title="正在调用 AI 合盘细断，预计 60-120 秒，请不要重复点击。"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { updateProfile } from '../../api/auth'
import { analyzeBazi, analyzeBaziCompatibility } from '../../api/bazi'
import KnowledgeReferences from '../../components/KnowledgeReferences.vue'
import ResultReport from '../../components/ResultReport.vue'
import { useUserStore } from '../../stores/user'
import { getBaziDetails, getFourPillars, getLuckCycles, getYearGanZhi } from '../../utils/ganzhi'
import { buildReportMarkdown, copyText, downloadMarkdown } from '../../utils/report'
import { provinceOptions } from '../../utils/chinaCities'

const userStore = useUserStore()
const loading = ref(false)
const result = ref('')
const knowledgeRules = ref([])
const classicReferences = ref([])
const reportPanelRef = ref(null)
const analysisMode = ref('single')
const activeChartTab = ref('chart')
const savedProfiles = ref([])
const selectedProfileId = ref('')
const compatSelectedA = ref('')
const compatSelectedB = ref('')
const chartTabs = [
  { key: 'info', label: '基本信息' },
  { key: 'chart', label: '基本排盘' },
  { key: 'detail', label: '专业细盘' },
  { key: 'note', label: '断事笔记' }
]

const cityOptions = computed(() => provinceOptions.find(item => item.name === form.birthProvince)?.cities || [])

const form = reactive({
  userId: userStore.userId,
  gender: '',
  birthDate: '',
  birthTime: '',
  birthProvince: '',
  birthPlace: '',
  useTrueSolarTime: false,
  yearPillar: '',
  monthPillar: '',
  dayPillar: '',
  hourPillar: '',
  dayMaster: '',
  luckPillar: '',
  currentYearPillar: '',
  questionType: '综合',
  question: ''
})

const compatForm = reactive({
  relationshipType: '恋爱婚姻',
  question: '',
  personA: createCompatibilityPerson('甲方', ''),
  personB: createCompatibilityPerson('乙方', '')
})

const parsedResult = computed(() => {
  try {
    return result.value ? JSON.parse(result.value) : null
  } catch {
    return { coreConclusion: result.value, confidence: '未知', keyEvidence: [], detailedAnalysis: {}, timing: [], suggestion: '' }
  }
})

const pillarCards = computed(() => [
  buildPillarCard('year', '年柱', form.yearPillar),
  buildPillarCard('month', '月柱', form.monthPillar),
  buildPillarCard('day', '日柱', form.dayPillar),
  buildPillarCard('hour', '时柱', form.hourPillar)
])

const baziDetails = computed(() => getBaziDetails({
  yearPillar: form.yearPillar,
  monthPillar: form.monthPillar,
  dayPillar: form.dayPillar,
  hourPillar: form.hourPillar,
  dayMaster: form.dayMaster,
  luck: luckInfo.value
}))

const luckInfo = computed(() => getLuckCycles({
  birthDate: form.birthDate,
  birthTime: effectiveBirthTime.value || normalizeBirthTime(form.birthTime),
  gender: form.gender,
  yearPillar: form.yearPillar,
  monthPillar: form.monthPillar,
  currentDate: new Date()
}))

const selectedCity = computed(() => getCityLocation(form.birthPlace))
const trueSolarInfo = computed(() => getTrueSolarTimeInfo())
const effectiveBirthTime = computed(() => trueSolarInfo.value?.time || normalizeBirthTime(form.birthTime))
const trueSolarTimeText = computed(() => {
  if (!form.useTrueSolarTime) return '未启用'
  if (!normalizeBirthTime(form.birthTime)) return '待填写时间'
  if (!selectedCity.value) return '请选择出生省市'
  const info = trueSolarInfo.value
  if (!info) return '无法修正'
  const sign = info.offsetMinutes >= 0 ? '+' : '-'
  return `${info.date} ${info.time}（${selectedCity.value.name}，${sign}${Math.abs(info.offsetMinutes)}分钟）`
})

function buildPillarCard(key, label, value) {
  const detail = baziDetails.value.pillars.find(item => item.key === key) || {}
  return {
    key,
    label,
    value,
    stem: detail.stem || '',
    branch: detail.branch || '',
    stemElement: detail.stemElement || '',
    branchElement: detail.branchElement || '',
    tenGod: key === 'day' && value ? '日主' : detail.stemTenGod,
    hiddenStems: detail.hiddenStems || [],
    selfSitting: detail.selfSitting || '',
    naYin: detail.naYin || '',
    isEmpty: detail.isEmpty,
    emptyText: detail.emptyText || '-',
    shenSha: detail.shenSha || []
  }
}

function createCompatibilityPerson(name, gender) {
  return {
    name,
    gender,
    birthDate: '',
    birthTime: '',
    birthProvince: '',
    birthPlace: '',
    useTrueSolarTime: false,
    yearPillar: '',
    monthPillar: '',
    dayPillar: '',
    hourPillar: '',
    dayMaster: ''
  }
}

function switchAnalysisMode(mode) {
  analysisMode.value = mode
  result.value = ''
  knowledgeRules.value = []
  classicReferences.value = []
}

const solarText = computed(() => {
  if (!form.birthDate) return '未填写'
  const base = `${form.birthDate}${form.birthTime ? ` ${form.birthTime}` : ''}`
  return form.useTrueSolarTime && trueSolarInfo.value ? `${base} → ${trueSolarInfo.value.date} ${trueSolarInfo.value.time}` : base
})

function elementClass(element) {
  return {
    wood: element === '木',
    fire: element === '火',
    earth: element === '土',
    metal: element === '金',
    water: element === '水'
  }
}

const chartStatus = computed(() => {
  if (!form.birthDate) return '先填写出生日期'
  if (!form.birthTime) return '已排年/月/日，填写出生时间后可排时柱'
  if (form.useTrueSolarTime && !selectedCity.value) return '已启用真太阳时，请选择出生省市'
  if (!form.hourPillar) return '出生时间格式未识别，可填 08:30、8点、辰时'
  return `日主 ${form.dayMaster || '待定'}，流年 ${form.currentYearPillar || '待定'}`
})

const caseRules = computed(() => knowledgeRules.value.filter(rule => rule.category?.includes('案例')))
const caseRuleCount = computed(() => caseRules.value.length)
const reportTitle = computed(() => analysisMode.value === 'compatibility' ? '八字合盘解析报告' : '八字分析报告')

function refreshSavedProfiles() {
  savedProfiles.value = userStore.getBirthProfiles()
}

function applyProfile() {
  refreshSavedProfiles()
  const profile = userStore.getBirthProfile()
  if (!profile) return
  selectedProfileId.value = profile.id || ''
  applyProfileToForm(profile, form)
  copySingleProfileToPersonA({ silent: true })
}

function profileLabel(profile) {
  return [profile.name || '未命名', profile.birthDate, profile.birthTime, profile.birthPlace].filter(Boolean).join(' · ')
}

function profileFromForm(source, fallbackName = '') {
  return {
    name: source.name || fallbackName || source.birthName || source.profileName || '',
    gender: source.gender,
    birthDate: source.birthDate,
    birthTime: source.birthTime,
    birthProvince: source.birthProvince,
    birthPlace: source.birthPlace,
    useTrueSolarTime: source.useTrueSolarTime,
    yearPillar: source.yearPillar,
    monthPillar: source.monthPillar,
    dayPillar: source.dayPillar,
    hourPillar: source.hourPillar,
    dayMaster: source.dayMaster,
    birthDayGanZhi: source.dayPillar,
    birthDayMaster: source.dayMaster
  }
}

function findMatchingProfile(source) {
  return savedProfiles.value.find(item =>
    item.birthDate === source.birthDate
    && item.birthTime === source.birthTime
    && item.birthPlace === source.birthPlace
    && item.dayPillar === source.dayPillar
  )
}

function applyProfileToForm(profile, target) {
  target.name = profile.name || target.name || ''
  target.gender = profile.gender || ''
  target.birthDate = profile.birthDate || ''
  target.birthTime = profile.birthTime || ''
  target.birthPlace = profile.birthPlace || ''
  target.birthProvince = profile.birthProvince || findProvinceByCity(profile.birthPlace)?.name || ''
  target.useTrueSolarTime = Boolean(profile.useTrueSolarTime)
  target.yearPillar = profile.yearPillar || ''
  target.monthPillar = profile.monthPillar || ''
  target.dayPillar = profile.dayPillar || profile.birthDayGanZhi || ''
  target.hourPillar = profile.hourPillar || ''
  target.dayMaster = profile.dayMaster || profile.birthDayMaster || ''
  if (target === form) {
    fillPillarsFromBirth({ preserveExisting: true })
  } else {
    fillCompatibilityPillars(target, { preserveExisting: true })
  }
}

function applySelectedProfileToSingle(id) {
  const profile = savedProfiles.value.find(item => item.id === id)
  if (!profile) return
  applyProfileToForm(profile, form)
}

function applySelectedProfileToCompatibility(slot) {
  const id = slot === 'A' ? compatSelectedA.value : compatSelectedB.value
  const profile = savedProfiles.value.find(item => item.id === id)
  if (!profile) return
  applyProfileToForm(profile, slot === 'A' ? compatForm.personA : compatForm.personB)
}

async function promptProfileName(defaultName) {
  const { value } = await ElMessageBox.prompt('给这份出生资料取个名字，方便下次选择', '保存资料', {
    confirmButtonText: '保存',
    cancelButtonText: '取消',
    inputValue: defaultName || '未命名',
    inputPattern: /\S+/,
    inputErrorMessage: '请填写名称'
  })
  return value
}

async function saveBirthProfile(options = {}) {
  fillPillarsFromBirth({ preserveExisting: true })
  syncDayMaster()
  if (!selectedProfileId.value) {
    selectedProfileId.value = findMatchingProfile(form)?.id || ''
  }
  const accountUserId = userStore.userId
  const name = options.name
    || (options.silent ? (savedProfiles.value.find(item => item.id === selectedProfileId.value)?.name || '我的资料') : await promptProfileName(savedProfiles.value.find(item => item.id === selectedProfileId.value)?.name || '我的资料'))
  const profile = userStore.saveBirthProfile({
    id: selectedProfileId.value || undefined,
    name,
    gender: form.gender,
    birthDate: form.birthDate,
    birthTime: form.birthTime,
    birthProvince: form.birthProvince,
    birthPlace: form.birthPlace,
    useTrueSolarTime: form.useTrueSolarTime,
    yearPillar: form.yearPillar,
    monthPillar: form.monthPillar,
    dayPillar: form.dayPillar,
    hourPillar: form.hourPillar,
    dayMaster: form.dayMaster,
    birthDayGanZhi: form.dayPillar,
    birthDayMaster: form.dayMaster
  })
  selectedProfileId.value = profile.id
  refreshSavedProfiles()
  if (accountUserId) {
    const user = await updateProfile({
      userId: accountUserId,
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
  if (!options.silent) {
    ElMessage.success('出生资料已保存')
  }
}

async function saveCompatibilityPeople() {
  fillCompatibilityPillars(compatForm.personA, { preserveExisting: true })
  fillCompatibilityPillars(compatForm.personB, { preserveExisting: true })
  syncCompatibilityDayMaster(compatForm.personA)
  syncCompatibilityDayMaster(compatForm.personB)
  const saved = []
  if (compatForm.personA.birthDate || compatForm.personA.dayPillar) {
    const nameA = await promptProfileName(compatForm.personA.name || '甲方')
    const profileA = userStore.saveBirthProfileItem(profileFromForm(compatForm.personA, nameA), { setPrimary: false, syncUser: false })
    compatForm.personA.name = profileA.name
    compatSelectedA.value = profileA.id
    saved.push(profileA.name)
  }
  if (compatForm.personB.birthDate || compatForm.personB.dayPillar) {
    const nameB = await promptProfileName(compatForm.personB.name || '乙方')
    const profileB = userStore.saveBirthProfileItem(profileFromForm(compatForm.personB, nameB), { setPrimary: false, syncUser: false })
    compatForm.personB.name = profileB.name
    compatSelectedB.value = profileB.id
    saved.push(profileB.name)
  }
  refreshSavedProfiles()
  ElMessage.success(saved.length ? `已保存：${saved.join('、')}` : '没有可保存的资料')
}

async function deleteSelectedProfile() {
  const profile = savedProfiles.value.find(item => item.id === selectedProfileId.value)
  if (!profile) return
  await ElMessageBox.confirm(`确定删除「${profile.name || '未命名'}」吗？`, '删除资料')
  userStore.deleteBirthProfile(profile.id)
  selectedProfileId.value = ''
  refreshSavedProfiles()
  ElMessage.success('已删除')
}

function onProvinceChange() {
  const cities = cityOptions.value
  if (!cities.some(city => city.name === form.birthPlace)) {
    form.birthPlace = cities[0]?.name || ''
  }
  enableTrueSolarTimeWhenPlaceSelected()
  fillPillarsFromBirth()
}

function onBirthPlaceChange() {
  enableTrueSolarTimeWhenPlaceSelected()
  fillPillarsFromBirth()
}

function enableTrueSolarTimeWhenPlaceSelected() {
  if (form.birthPlace) {
    form.useTrueSolarTime = true
  }
}

function fillPillarsFromBirth(options = {}) {
  if (!form.birthDate) return
  const normalizedTime = normalizeBirthTime(form.birthTime)
  const hasBirthTime = Boolean(normalizedTime)
  const effectiveTime = hasBirthTime ? effectiveBirthTime.value : ''
  const effectiveDate = hasBirthTime && trueSolarInfo.value ? trueSolarInfo.value.date : form.birthDate
  const birthDateTime = hasBirthTime ? `${effectiveDate}T${effectiveTime}` : form.birthDate
  const pillars = getFourPillars(birthDateTime)
  const shouldWrite = (field) => !options.preserveExisting || !form[field]

  if (pillars.yearPillar && shouldWrite('yearPillar')) form.yearPillar = pillars.yearPillar
  if (pillars.monthPillar && shouldWrite('monthPillar')) form.monthPillar = pillars.monthPillar
  if (pillars.dayPillar && shouldWrite('dayPillar')) form.dayPillar = pillars.dayPillar
  if (pillars.dayMaster && shouldWrite('dayMaster')) form.dayMaster = pillars.dayMaster
  if (hasBirthTime && pillars.hourPillar && shouldWrite('hourPillar')) {
    form.hourPillar = pillars.hourPillar
  } else if (!hasBirthTime && !options.preserveExisting) {
    form.hourPillar = ''
  }
  if (!form.currentYearPillar) {
    form.currentYearPillar = getYearGanZhi(new Date())
  }
  if (luckInfo.value.currentLuck && shouldWrite('luckPillar')) {
    form.luckPillar = luckInfo.value.currentLuck
  }
}

function rebuildPillars() {
  form.yearPillar = ''
  form.monthPillar = ''
  form.dayPillar = ''
  form.hourPillar = ''
  form.dayMaster = ''
  form.luckPillar = ''
  form.currentYearPillar = ''
  fillPillarsFromBirth()
}

function refreshLuckPillar() {
  if (luckInfo.value.currentLuck) {
    form.luckPillar = luckInfo.value.currentLuck
  }
}

function getTrueSolarTimeInfo() {
  const normalizedTime = normalizeBirthTime(form.birthTime)
  const city = selectedCity.value
  return getTrueSolarTimeInfoFor(form.birthDate, normalizedTime, form.useTrueSolarTime, city)
}

function getTrueSolarTimeInfoFor(birthDate, normalizedTime, enabled, city) {
  if (!enabled || !birthDate || !normalizedTime || !city) return null
  const date = new Date(`${birthDate}T${normalizedTime}`)
  if (Number.isNaN(date.getTime())) return null
  const offsetMinutes = Math.round((city.longitude - 120) * 4 + getEquationOfTimeMinutes(date))
  const corrected = new Date(date.getTime() + offsetMinutes * 60 * 1000)
  return {
    date: formatDateValue(corrected),
    time: `${String(corrected.getHours()).padStart(2, '0')}:${String(corrected.getMinutes()).padStart(2, '0')}`,
    offsetMinutes
  }
}

function getEquationOfTimeMinutes(date) {
  const start = new Date(date.getFullYear(), 0, 0)
  const dayOfYear = Math.floor((date - start) / 86400000)
  const b = (2 * Math.PI * (dayOfYear - 81)) / 364
  return 9.87 * Math.sin(2 * b) - 7.53 * Math.cos(b) - 1.5 * Math.sin(b)
}

function formatDateValue(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function getCityLocation(place) {
  const value = String(place || '')
  return provinceOptions
    .flatMap(province => province.cities)
    .find(city => city.name === value || value.includes(city.name)) || null
}

function findProvinceByCity(place) {
  const value = String(place || '')
  return provinceOptions.find(province => province.cities.some(city => city.name === value || value.includes(city.name))) || null
}

function getCompatibilityCityOptions(provinceName) {
  return provinceOptions.find(item => item.name === provinceName)?.cities || []
}

function getCompatibilityCity(person) {
  const value = String(person.birthPlace || '')
  return provinceOptions
    .flatMap(province => province.cities)
    .find(city => city.name === value || value.includes(city.name)) || null
}

function getCompatibilityTrueSolarInfo(person) {
  return getTrueSolarTimeInfoFor(
    person.birthDate,
    normalizeBirthTime(person.birthTime),
    person.useTrueSolarTime,
    getCompatibilityCity(person)
  )
}

function compatibilityTrueSolarText(person) {
  if (!person.useTrueSolarTime) return '未启用'
  if (!normalizeBirthTime(person.birthTime)) return '待填写时间'
  const city = getCompatibilityCity(person)
  if (!city) return '请选择出生省市'
  const info = getCompatibilityTrueSolarInfo(person)
  if (!info) return '无法修正'
  const sign = info.offsetMinutes >= 0 ? '+' : '-'
  return `${info.date} ${info.time}（${city.name}，${sign}${Math.abs(info.offsetMinutes)}分钟）`
}

function compatibilityPillarSummary(person) {
  const pillars = [person.yearPillar, person.monthPillar, person.dayPillar, person.hourPillar].filter(Boolean)
  return pillars.length ? pillars.join(' ') : '待排盘'
}

function onCompatibilityProvinceChange(person) {
  const cities = getCompatibilityCityOptions(person.birthProvince)
  if (!cities.some(city => city.name === person.birthPlace)) {
    person.birthPlace = cities[0]?.name || ''
  }
  if (person.birthPlace) person.useTrueSolarTime = true
  fillCompatibilityPillars(person)
}

function onCompatibilityBirthPlaceChange(person) {
  if (person.birthPlace) person.useTrueSolarTime = true
  fillCompatibilityPillars(person)
}

function fillCompatibilityPillars(person, options = {}) {
  if (!person.birthDate) return
  const normalizedTime = normalizeBirthTime(person.birthTime)
  const hasBirthTime = Boolean(normalizedTime)
  const trueSolar = getCompatibilityTrueSolarInfo(person)
  const effectiveDate = hasBirthTime && trueSolar ? trueSolar.date : person.birthDate
  const effectiveTime = hasBirthTime && trueSolar ? trueSolar.time : normalizedTime
  const birthDateTime = hasBirthTime ? `${effectiveDate}T${effectiveTime}` : person.birthDate
  const pillars = getFourPillars(birthDateTime)
  const shouldWrite = (field) => !options.preserveExisting || !person[field]
  if (pillars.yearPillar && shouldWrite('yearPillar')) person.yearPillar = pillars.yearPillar
  if (pillars.monthPillar && shouldWrite('monthPillar')) person.monthPillar = pillars.monthPillar
  if (pillars.dayPillar && shouldWrite('dayPillar')) person.dayPillar = pillars.dayPillar
  if (pillars.dayMaster && shouldWrite('dayMaster')) person.dayMaster = pillars.dayMaster
  if (hasBirthTime && pillars.hourPillar && shouldWrite('hourPillar')) {
    person.hourPillar = pillars.hourPillar
  } else if (!hasBirthTime && !options.preserveExisting) {
    person.hourPillar = ''
  }
}

function syncCompatibilityDayMaster(person) {
  person.dayMaster = person.dayPillar ? person.dayPillar.slice(0, 1) : ''
}

function buildCompatibilityDetails(person) {
  const trueSolar = getCompatibilityTrueSolarInfo(person)
  const details = getBaziDetails({
    yearPillar: person.yearPillar,
    monthPillar: person.monthPillar,
    dayPillar: person.dayPillar,
    hourPillar: person.hourPillar,
    dayMaster: person.dayMaster,
    luck: getLuckCycles({
      birthDate: person.birthDate,
      birthTime: trueSolar?.time || normalizeBirthTime(person.birthTime),
      gender: person.gender,
      yearPillar: person.yearPillar,
      monthPillar: person.monthPillar,
      currentDate: new Date()
    })
  })
  return JSON.stringify({
    ...details,
    trueSolarTime: {
      enabled: person.useTrueSolarTime,
      province: person.birthProvince,
      city: getCompatibilityCity(person)?.name || person.birthPlace,
      display: compatibilityTrueSolarText(person),
      corrected: trueSolar
    }
  })
}

function flattenCompatibilityPerson(prefix, person) {
  return {
    [`${prefix}Name`]: person.name,
    [`${prefix}Gender`]: person.gender,
    [`${prefix}BirthDate`]: person.birthDate,
    [`${prefix}BirthTime`]: person.birthTime,
    [`${prefix}BirthPlace`]: person.birthPlace,
    [`${prefix}YearPillar`]: person.yearPillar,
    [`${prefix}MonthPillar`]: person.monthPillar,
    [`${prefix}DayPillar`]: person.dayPillar,
    [`${prefix}HourPillar`]: person.hourPillar,
    [`${prefix}DayMaster`]: person.dayMaster,
    [`${prefix}BaziDetails`]: buildCompatibilityDetails(person)
  }
}

function copySingleProfileToPersonA(options = {}) {
  fillPillarsFromBirth({ preserveExisting: true })
  Object.assign(compatForm.personA, {
    name: compatForm.personA.name || '甲方',
    gender: form.gender,
    birthDate: form.birthDate,
    birthTime: form.birthTime,
    birthProvince: form.birthProvince,
    birthPlace: form.birthPlace,
    useTrueSolarTime: form.useTrueSolarTime,
    yearPillar: form.yearPillar,
    monthPillar: form.monthPillar,
    dayPillar: form.dayPillar,
    hourPillar: form.hourPillar,
    dayMaster: form.dayMaster
  })
  fillCompatibilityPillars(compatForm.personA, { preserveExisting: true })
  if (!options.silent) {
    ElMessage.success('已填入甲方资料')
  }
}

function normalizeBirthTime(value) {
  if (!value || !String(value).trim()) return ''
  const raw = String(value).trim()
  const branchHours = {
    子: '00:00',
    丑: '02:00',
    寅: '04:00',
    卯: '06:00',
    辰: '08:00',
    巳: '10:00',
    午: '12:00',
    未: '14:00',
    申: '16:00',
    酉: '18:00',
    戌: '20:00',
    亥: '22:00'
  }
  const branch = raw.match(/[子丑寅卯辰巳午未申酉戌亥]/)?.[0]
  if (branch) return branchHours[branch]

  const colonMatch = raw.match(/^(\d{1,2})[:：](\d{1,2})$/)
  if (colonMatch) return formatTime(colonMatch[1], colonMatch[2])

  const chineseTimeMatch = raw.match(/^(\d{1,2})\s*(点|時|时)(?:\s*(\d{1,2})\s*(分)?)?$/)
  if (chineseTimeMatch) return formatTime(chineseTimeMatch[1], chineseTimeMatch[3] || '0')

  const compactMatch = raw.match(/^(\d{1,2})(\d{2})$/)
  if (compactMatch) return formatTime(compactMatch[1], compactMatch[2])

  const hourOnlyMatch = raw.match(/^(\d{1,2})$/)
  if (hourOnlyMatch) return formatTime(hourOnlyMatch[1], '0')

  return raw
}

function formatTime(hourValue, minuteValue) {
  const hour = Number(hourValue)
  const minute = Number(minuteValue)
  if (!Number.isInteger(hour) || !Number.isInteger(minute) || hour < 0 || hour > 23 || minute < 0 || minute > 59) {
    return ''
  }
  return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
}

function syncDayMaster() {
  form.dayMaster = form.dayPillar ? form.dayPillar.slice(0, 1) : ''
}

async function submit() {
  fillPillarsFromBirth({ preserveExisting: true })
  syncDayMaster()
  if (!form.question.trim()) {
    ElMessage.warning('请先填写问题')
    return
  }
  if (!form.birthDate && !form.dayPillar) {
    ElMessage.warning('请填写出生日期，或手动补充日柱')
    return
  }
  loading.value = true
  try {
    await saveBirthProfile({ silent: true })
    const accountUserId = userStore.userId
    const data = await analyzeBazi({
      ...form,
      baziDetails: JSON.stringify({
        ...baziDetails.value,
        trueSolarTime: {
          enabled: form.useTrueSolarTime,
          province: form.birthProvince,
          city: selectedCity.value?.name || form.birthPlace,
          longitude: selectedCity.value?.longitude || '',
          display: trueSolarTimeText.value,
          corrected: trueSolarInfo.value
        }
      }),
      userId: accountUserId
    })
    result.value = data.resultJson
    knowledgeRules.value = data.knowledgeRules || []
    classicReferences.value = data.classicReferences || []
    ElMessage.success('分析完成')
    await nextTick()
    reportPanelRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  } finally {
    loading.value = false
  }
}

async function submitCompatibility() {
  fillCompatibilityPillars(compatForm.personA, { preserveExisting: true })
  fillCompatibilityPillars(compatForm.personB, { preserveExisting: true })
  syncCompatibilityDayMaster(compatForm.personA)
  syncCompatibilityDayMaster(compatForm.personB)
  if (!compatForm.question.trim()) {
    ElMessage.warning('请先填写合盘问题')
    return
  }
  if (!compatForm.personA.birthDate && !compatForm.personA.dayPillar) {
    ElMessage.warning('请补充甲方出生日期或日柱')
    return
  }
  if (!compatForm.personB.birthDate && !compatForm.personB.dayPillar) {
    ElMessage.warning('请补充乙方出生日期或日柱')
    return
  }
  loading.value = true
  try {
    const accountUserId = userStore.userId
    const data = await analyzeBaziCompatibility({
      userId: accountUserId,
      relationshipType: compatForm.relationshipType,
      question: compatForm.question,
      ...flattenCompatibilityPerson('personA', compatForm.personA),
      ...flattenCompatibilityPerson('personB', compatForm.personB)
    })
    result.value = data.resultJson
    knowledgeRules.value = data.knowledgeRules || []
    classicReferences.value = data.classicReferences || []
    ElMessage.success('合盘解析完成')
    await nextTick()
    reportPanelRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  } finally {
    loading.value = false
  }
}

async function copyCurrentReport() {
  await copyText(buildReportMarkdown(parsedResult.value, knowledgeRules.value, {
    title: reportTitle.value,
    classicReferences: classicReferences.value
  }))
  ElMessage.success('报告已复制')
}

function exportCurrentReport() {
  downloadMarkdown(`${reportTitle.value}-${Date.now()}.md`, buildReportMarkdown(parsedResult.value, knowledgeRules.value, {
    title: reportTitle.value,
    classicReferences: classicReferences.value
  }))
}

onMounted(applyProfile)
</script>

<style scoped>
.result-panel {
  min-height: 520px;
  min-width: 0;
  overflow-x: hidden;
}

.auto-note {
  margin: 2px 0 14px;
}

.estimate-note {
  margin-top: 12px;
}

.mode-switch {
  display: inline-flex;
  padding: 4px;
  margin: 0 0 14px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #f8fafc;
  gap: 4px;
}

.mode-switch button {
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #606266;
  font-size: 14px;
  font-weight: 600;
  line-height: 34px;
  min-width: 96px;
  padding: 0 14px;
}

.mode-switch button.active {
  background: #111;
  color: #fff;
  box-shadow: 0 6px 16px rgba(17, 17, 17, 0.12);
}

.compatibility-desc {
  margin: -4px 0 16px;
  color: #606266;
  line-height: 1.7;
  font-size: 14px;
}

.profile-picker,
.compatibility-saved-picker {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 8px;
  align-items: center;
  margin-bottom: 14px;
}

.compatibility-saved-picker {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.compatibility-people {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.compatibility-person {
  border: 1px solid #eeeeee;
  border-radius: 8px;
  background: #fbfbfb;
  padding: 12px;
  min-width: 0;
}

.person-title {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}

.person-title strong {
  color: #111827;
  font-size: 16px;
}

.person-title span {
  color: #8a6b2d;
  font-size: 13px;
  line-height: 1.5;
  text-align: right;
  word-break: keep-all;
}

.compatibility-preview {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.compatibility-preview div {
  border: 1px solid #eeeeee;
  border-radius: 8px;
  background: #fff;
  padding: 10px 12px;
}

.compatibility-preview span,
.compatibility-preview strong {
  display: block;
}

.compatibility-preview span {
  color: #909399;
  font-size: 12px;
  margin-bottom: 4px;
}

.compatibility-preview strong {
  color: #303133;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.chart-preview {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 0;
  margin-bottom: 16px;
  background: #fff;
  overflow: hidden;
}

.chart-tabs {
  display: flex;
  background: #050505;
  min-height: 48px;
}

.chart-tabs button {
  flex: 1;
  border: 0;
  background: transparent;
  color: #f5f5f5;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 0;
}

.chart-tabs button.active {
  color: #c5a563;
}

.chart-banner {
  min-height: 58px;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #f2f2f2;
  background:
    linear-gradient(90deg, rgba(0, 0, 0, 0.92), rgba(19, 18, 15, 0.88)),
    radial-gradient(circle at 16% 30%, rgba(190, 155, 80, 0.38), transparent 30%);
}

.chart-banner span {
  display: block;
  font-size: 16px;
  line-height: 1.4;
}

.chart-banner small {
  display: block;
  color: #c9c9c9;
  font-size: 12px;
  margin-top: 2px;
}

.bazi-board {
  width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.board-row {
  display: grid;
  grid-template-columns: 62px repeat(4, minmax(86px, 1fr));
  min-width: 430px;
  border-bottom: 1px solid #ededed;
}

.board-row:nth-child(even) {
  background: #f7f7f7;
}

.header-row {
  background: #fff;
}

.row-label,
.board-cell {
  min-height: 52px;
  padding: 9px 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #3f3f46;
  font-size: 15px;
  line-height: 1.35;
  word-break: keep-all;
}

.row-label {
  color: #9a9a9a;
  font-weight: 500;
  justify-content: flex-start;
  padding-left: 18px;
}

.header-row .board-cell,
.header-row .row-label {
  color: #9a9a9a;
  font-size: 15px;
}

.main-star {
  font-size: 16px;
}

.stem-row .board-cell,
.branch-row .board-cell {
  min-height: 70px;
}

.big-char {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
}

.wood {
  color: #2ca24f;
}

.fire {
  color: #cf2f2f;
}

.earth {
  color: #987032;
}

.metal {
  color: #c98400;
}

.water {
  color: #2f80d9;
}

.tall-row .row-label,
.tall-row .board-cell {
  min-height: 96px;
}

.stack-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;
  white-space: normal;
}

.stack-cell span {
  display: block;
}

.shensha-row .row-label,
.shensha-row .board-cell {
  align-items: flex-start;
  min-height: 118px;
  padding-top: 14px;
}

.shensha-cell {
  justify-content: flex-start;
  color: #9a7a36;
  font-size: 14px;
}

.shensha-cell span {
  display: block;
  width: 100%;
  min-height: 24px;
  padding: 0;
  border-radius: 0;
  background: transparent;
  line-height: 24px;
  text-align: center;
}

.bazi-board::-webkit-scrollbar {
  height: 4px;
}

.bazi-board::-webkit-scrollbar-thumb {
  background: #d0d5dd;
  border-radius: 999px;
}

.tab-pane {
  padding: 14px;
  background: #fff;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.info-grid div,
.detail-card,
.note-line {
  border: 1px solid #eeeeee;
  border-radius: 8px;
  background: #fafafa;
  padding: 10px 12px;
}

.info-grid span,
.detail-card span,
.note-line span {
  display: block;
  color: #8b8b8b;
  font-size: 12px;
  margin-bottom: 6px;
}

.info-grid strong,
.detail-card strong,
.note-line strong {
  display: block;
  color: #303133;
  font-size: 15px;
  line-height: 1.6;
  word-break: break-word;
}

.detail-pane {
  display: grid;
  gap: 10px;
}

.luck-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 8px;
}

.luck-grid div {
  border: 1px solid #eeeeee;
  border-radius: 8px;
  background: #fafafa;
  padding: 8px 6px;
  text-align: center;
}

.luck-grid div.active {
  border-color: #c59a42;
  background: #fff8e8;
}

.luck-grid strong,
.luck-grid span,
.luck-grid small {
  display: block;
}

.luck-grid strong {
  color: #303133;
  font-size: 16px;
}

.luck-grid span {
  margin-top: 4px;
  color: #606266;
  font-size: 12px;
}

.luck-grid small {
  margin-top: 2px;
  color: #909399;
  font-size: 11px;
}

.note-pane {
  display: grid;
  gap: 10px;
}

.case-list {
  display: grid;
  gap: 8px;
}

.case-list span {
  border-left: 3px solid #c5a563;
  background: #fffaf0;
  color: #6b5523;
  padding: 8px 10px;
  border-radius: 6px;
  line-height: 1.5;
  font-size: 13px;
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

@media (max-width: 700px) {
  .page {
    padding: 10px 8px 88px;
  }

  .panel {
    padding: 10px;
  }

  .mode-switch {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr 1fr;
  }

  .mode-switch button {
    min-width: 0;
    width: 100%;
    font-size: 13px;
  }

  .compatibility-people,
  .compatibility-preview,
  .profile-picker,
  .compatibility-saved-picker {
    grid-template-columns: 1fr;
  }

  .compatibility-person {
    padding: 10px;
  }

  .person-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .person-title span {
    text-align: left;
    word-break: break-word;
  }

  .result-head {
    align-items: flex-start;
    flex-direction: column;
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

  .result-actions {
    width: 100%;
  }

  .result-actions .el-button {
    flex: 1;
  }

  .chart-tabs {
    min-height: 42px;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .chart-tabs button {
    min-width: 82px;
    padding: 0 6px;
    font-size: 13px;
    white-space: nowrap;
  }

  .chart-banner {
    min-height: 50px;
    padding: 8px 10px;
  }

  .chart-banner span {
    font-size: 13px;
  }

  .chart-banner small {
    font-size: 11px;
    line-height: 1.35;
  }

  .board-row {
    grid-template-columns: 44px repeat(4, minmax(0, 1fr));
    min-width: 0;
    width: 100%;
  }

  .row-label,
  .board-cell {
    min-width: 0;
    min-height: 46px;
    padding: 7px 3px;
    font-size: 12px;
    line-height: 1.3;
    word-break: break-word;
  }

  .row-label {
    padding-left: 6px;
    justify-content: center;
    text-align: center;
  }

  .big-char {
    font-size: 24px;
  }

  .stem-row .board-cell,
  .branch-row .board-cell {
    min-height: 58px;
  }

  .tall-row .row-label,
  .tall-row .board-cell {
    min-height: 82px;
  }

  .shensha-row .row-label,
  .shensha-row .board-cell {
    min-height: 92px;
    padding-top: 8px;
    align-items: flex-start;
  }

  .stack-cell {
    gap: 2px;
    font-size: 12px;
  }

  .shensha-cell {
    display: block;
    justify-content: flex-start;
    font-size: 12px;
    text-align: center;
    overflow: hidden;
  }

  .shensha-cell span {
    display: block;
    width: 100%;
    max-width: 100%;
    min-height: 22px;
    padding: 0;
    border-radius: 0;
    background: transparent;
    color: #8a6b2d;
    line-height: 22px;
    white-space: nowrap;
  }

  .tab-pane {
    padding: 10px;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .luck-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .info-grid div,
  .detail-card,
  .note-line {
    padding: 8px 10px;
  }

  .result-actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }
}
</style>
