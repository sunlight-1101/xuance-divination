<template>
  <div class="hecan-page">
    <div class="page-header">
      <h2>八字紫微合参</h2>
      <p class="subtitle">同时运用八字五行与紫微星曜两套体系，交叉验证给出更准确的综合判断</p>
    </div>

    <!-- 步骤一：出生信息 -->
    <el-card class="step-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="step-num">1</span>
          <span>填写出生信息</span>
        </div>
      </template>

      <el-form :model="form" label-width="80px" label-position="top">
        <el-row :gutter="16">
          <el-col :xs="12" :sm="6">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio-button label="男" />
                <el-radio-button label="女" />
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="8">
            <el-form-item label="出生日期">
              <el-date-picker v-model="form.birthDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="出生时间">
              <el-time-picker v-model="form.birthTime" placeholder="选择时间" value-format="HH:mm" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="4">
            <el-form-item label="真太阳时">
              <el-switch v-model="form.useTrueSolarTime" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="12" :sm="8">
            <el-form-item label="出生地">
              <el-input v-model="form.birthPlace" placeholder="如：北京" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 排盘结果预览 -->
      <div v-if="baziReady || ziweiReady" class="chart-preview">
        <el-divider content-position="left">排盘预览</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="chart-box">
              <h4>八字四柱</h4>
              <div v-if="baziReady" class="pillars-row">
                <div v-for="p in pillarCards" :key="p.key" class="pillar-card">
                  <small>{{ p.label }}</small>
                  <strong>{{ p.pillar }}</strong>
                  <small class="ten-god">{{ p.stemTenGod }}</small>
                </div>
              </div>
              <div v-else class="chart-loading">排盘中...</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="chart-box">
              <h4>紫微命盘</h4>
              <div v-if="ziweiReady" class="ziwei-summary">
                <div>命宫：<strong>{{ ziweiChart?.mingGong }}</strong></div>
                <div>身宫：<strong>{{ ziweiChart?.shenGong }}</strong></div>
                <div>命主：<strong>{{ ziweiChart?.lifeMaster }}</strong></div>
                <div>身主：<strong>{{ ziweiChart?.bodyMaster }}</strong></div>
                <div>五行局：<strong>{{ ziweiChart?.fiveElementBureau }}</strong></div>
              </div>
              <div v-else class="chart-loading">排盘中...</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 步骤二：选择问题 -->
    <el-card class="step-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="step-num">2</span>
          <span>选择分析方向</span>
        </div>
      </template>

      <el-form :model="form" label-width="80px" label-position="top">
        <el-form-item label="问题类型">
          <el-radio-group v-model="form.questionType">
            <el-radio-button label="综合" />
            <el-radio-button label="感情" />
            <el-radio-button label="事业" />
            <el-radio-button label="财运" />
            <el-radio-button label="学业" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="具体问题">
          <el-input v-model="form.question" type="textarea" :rows="3" placeholder="描述你想了解的问题，例如：今年财运怎么样？适合换工作吗？" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 提交按钮 -->
    <div class="submit-area">
      <el-button type="primary" size="large" :loading="loading" :disabled="!canSubmit" @click="submit">
        开始合参分析
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getFourPillars, getBaziDetails, getLuckCycles, getTenGod } from '../../utils/ganzhi'
import { buildZiweiChart } from '../../api/ziwei'
import { analyzeHeCan } from '../../api/bazi'
import { readPendingAnalysis, startPendingAnalysis, clearAnalysisCache } from '../../utils/analysisCache'
import { waitForAnalysisRecord } from '../../utils/analysisPolling'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const PENDING_KEY = 'zhexuan_pending_hecan_analysis'

async function resolveAsyncAnalysis(data, fallbackRules = [], fallbackClassicReferences = []) {
  const recordId = data.recordId
  if (!recordId) return { result: data, knowledgeRules: fallbackRules, classicReferences: fallbackClassicReferences }
  const record = await waitForAnalysisRecord(recordId)
  return {
    result: record.resultJson ? JSON.parse(record.resultJson) : data,
    knowledgeRules: fallbackRules,
    classicReferences: fallbackClassicReferences
  }
}

const form = ref({
  gender: '男',
  birthDate: '',
  birthTime: '',
  birthPlace: '',
  useTrueSolarTime: false,
  questionType: '综合',
  question: ''
})

const loading = ref(false)
const baziReady = ref(false)
const ziweiReady = ref(false)
const baziData = ref(null)
const ziweiChart = ref(null)
const pillarCards = ref([])

// 监听出生信息变化，自动排盘
watch(
  () => [form.value.gender, form.value.birthDate, form.value.birthTime, form.value.birthPlace],
  async () => {
    if (!form.value.birthDate) return
    baziReady.value = false
    ziweiReady.value = false
    baziData.value = null
    ziweiChart.value = null

    // 八字排盘（前端计算）
    const pillars = getFourPillars(form.value.birthDate, form.value.birthTime)
    if (pillars.dayPillar) {
      baziData.value = {
        ...pillars,
        luck: getLuckCycles({
          birthDate: form.value.birthDate,
          birthTime: form.value.birthTime,
          gender: form.value.gender,
          yearPillar: pillars.yearPillar,
          monthPillar: pillars.monthPillar
        })
      }
      const details = getBaziDetails(baziData.value)
      pillarCards.value = details.pillars || []
      baziReady.value = true
    }

    // 紫微排盘（后端计算）
    try {
      const chartRes = await buildZiweiChart({
        gender: form.value.gender,
        birthDate: form.value.birthDate,
        birthTime: form.value.birthTime || '',
        birthPlace: form.value.birthPlace || '',
        calendarType: 'SOLAR',
        useTrueSolarTime: form.value.useTrueSolarTime
      })
      if (chartRes?.data) {
        ziweiChart.value = chartRes.data
        ziweiReady.value = true
      }
    } catch (e) {
      console.error('紫微排盘失败:', e)
    }
  },
  { immediate: true }
)

const canSubmit = computed(() => {
  return form.value.birthDate && form.value.question.trim() && (baziReady.value || ziweiReady.value) && !loading.value
})

async function submit() {
  if (!canSubmit.value) return
  if (readPendingAnalysis(PENDING_KEY)) {
    ElMessage.warning('之前的报告正在分析中，请稍后再试')
    return
  }

  loading.value = true
  startPendingAnalysis(PENDING_KEY, { type: 'BAZI_ZIWEI_HECAN', question: form.value.question })

  try {
    // 构建八字信息
    const pillars = baziData.value || {}
    const details = getBaziDetails(pillars)
    const luck = getLuckCycles({
      birthDate: form.value.birthDate,
      birthTime: form.value.birthTime,
      gender: form.value.gender,
      yearPillar: pillars.yearPillar,
      monthPillar: pillars.monthPillar
    })
    const luckLines = (luck.cycles || []).map(l => {
      const tg = getTenGod(pillars.dayMaster, l.pillar?.slice(0, 1)) || ''
      const tag = l.active ? ' [当前]' : ''
      return `第${l.index}步 ${l.pillar}（${tg}） ${l.startAge}-${l.endAge}岁 ${l.startYear}-${l.endYear}${tag}`
    })
    const luckCyclesText = luck.cycles?.length
      ? `${luck.direction}，${luck.startAge}起运\n${luckLines.join('\n')}`
      : ''

    const data = await analyzeHeCan({
      gender: form.value.gender,
      birthDate: form.value.birthDate,
      birthTime: form.value.birthTime,
      birthPlace: form.value.birthPlace,
      yearPillar: pillars.yearPillar || '',
      monthPillar: pillars.monthPillar || '',
      dayPillar: pillars.dayPillar || '',
      hourPillar: pillars.hourPillar || '',
      dayMaster: pillars.dayMaster || '',
      luckPillar: luck.currentLuck || '',
      currentYearPillar: new Date().getFullYear() + '',
      baziDetails: JSON.stringify(details),
      luckCycles: luckCyclesText,
      chartJson: ziweiChart.value ? JSON.stringify(ziweiChart.value) : '',
      questionType: form.value.questionType,
      question: form.value.question,
      userId: userStore.userId
    })

    const completed = await resolveAsyncAnalysis(data, data.knowledgeRules || [], data.classicReferences || [])
    clearAnalysisCache(PENDING_KEY)
    ElMessage.success('合参分析完成')
    // 跳转到结果页（复用记录列表）
    router.push('/records')
  } catch (e) {
    clearAnalysisCache(PENDING_KEY)
    ElMessage.error('分析失败：' + (e.message || '请稍后重试'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.hecan-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.page-header {
  text-align: center;
  margin-bottom: 24px;
}
.page-header h2 {
  margin: 0 0 8px;
  font-size: 24px;
}
.subtitle {
  color: #666;
  margin: 0;
}
.step-card {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
.step-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  font-size: 13px;
}
.chart-preview {
  margin-top: 16px;
}
.chart-box {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  min-height: 100px;
}
.chart-box h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #333;
}
.pillars-row {
  display: flex;
  gap: 8px;
  justify-content: center;
}
.pillar-card {
  text-align: center;
  padding: 8px 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  min-width: 60px;
}
.pillar-card strong {
  display: block;
  font-size: 18px;
  margin: 4px 0;
}
.ten-god {
  color: #909399;
  font-size: 12px;
}
.ziwei-summary {
  font-size: 13px;
  line-height: 2;
}
.chart-loading {
  text-align: center;
  color: #909399;
  padding: 20px;
}
.submit-area {
  text-align: center;
  padding: 16px 0;
}
</style>
