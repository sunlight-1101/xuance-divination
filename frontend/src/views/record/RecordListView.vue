<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">历史记录</h1>
      <p class="page-desc">查看每次测算的输入数据和结构化分析结果。</p>
    </div>

    <el-alert
      v-if="isGuest"
      type="warning"
      :closable="false"
      title="游客模式不会展示历史记录，请注册或登录后保存个人记录。"
      class="guest-alert"
    />

    <template v-else>
    <div class="toolbar">
      <el-select v-model="query.type" placeholder="术数类型" style="width: 160px" clearable>
        <el-option label="六爻" value="LIUYAO" />
        <el-option label="八字" value="BAZI" />
        <el-option label="奇门" value="QIMEN" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索问题" style="width: 260px" clearable />
      <el-button @click="load">搜索</el-button>
    </div>

    <div class="panel">
      <el-table :data="records" border class="record-table">
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="question" label="问题" min-width="260" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="show(row)">查看</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    </template>

    <el-dialog v-model="detailVisible" title="记录详情" width="800px" class="record-dialog">
      <div class="detail-actions">
        <el-button type="primary" :disabled="!parsedResult" @click="copyCurrentReport">复制报告</el-button>
        <el-button :disabled="!parsedResult" @click="exportCurrentReport">导出 Markdown</el-button>
      </div>
      <div class="record-summary">
        <div class="summary-head">
          <el-tag>{{ typeLabel(current.type) }}</el-tag>
          <strong>{{ current.question || '-' }}</strong>
        </div>
        <div class="summary-grid">
          <div v-for="item in inputSummary" :key="item.label">
            <span>{{ item.label }}</span>
            <strong>{{ item.value || '-' }}</strong>
          </div>
        </div>
      </div>
      <el-collapse class="raw-collapse">
        <el-collapse-item title="原始输入数据" name="raw">
          <pre>{{ pretty(current.inputJson) }}</pre>
        </el-collapse-item>
      </el-collapse>
      <h3>分析结果</h3>
      <ResultReport :report="parsedResult" />
      <KnowledgeReferences :rules="current.knowledgeRules || []" />
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import KnowledgeReferences from '../../components/KnowledgeReferences.vue'
import ResultReport from '../../components/ResultReport.vue'
import { deleteRecord, getRecord, listRecords } from '../../api/record'
import { useUserStore } from '../../stores/user'
import { computed } from 'vue'
import { buildReportMarkdown, copyText, downloadMarkdown } from '../../utils/report'

const userStore = useUserStore()
const records = ref([])
const detailVisible = ref(false)
const current = ref({})
const query = reactive({ userId: userStore.userId, type: '', keyword: '' })
const isGuest = computed(() => !userStore.userId)
const parsedResult = computed(() => {
  try {
    return current.value.resultJson ? JSON.parse(current.value.resultJson) : null
  } catch {
    return { coreConclusion: current.value.resultJson, confidence: '未知', keyEvidence: [], detailedAnalysis: {}, timing: [], suggestion: '' }
  }
})
const inputData = computed(() => parseJson(current.value.inputJson))
const inputSummary = computed(() => {
  const data = inputData.value || {}
  if (current.value.type === 'BAZI') {
    return [
      { label: '出生信息', value: [data.birthDate, data.birthTime, data.birthPlace].filter(Boolean).join(' ') },
      { label: '四柱', value: [data.yearPillar, data.monthPillar, data.dayPillar, data.hourPillar].filter(Boolean).join(' / ') },
      { label: '日主', value: data.dayMaster },
      { label: '流年', value: data.currentYearPillar },
      { label: '方向', value: data.questionType }
    ]
  }
  if (current.value.type === 'LIUYAO') {
    return [
      { label: '起卦时间', value: data.time },
      { label: '本卦', value: data.mainGua },
      { label: '变卦', value: data.changedGua },
      { label: '日辰/月建', value: [data.dayGanZhi || data.dayBranch, data.monthBranch].filter(Boolean).join(' / ') },
      { label: '动爻', value: movingYaoText(data.yaoList || []) }
    ]
  }
  return [
    { label: '类型', value: current.value.type },
    { label: '创建时间', value: current.value.createTime }
  ]
})

async function load() {
  if (isGuest.value) {
    records.value = []
    return
  }
  records.value = await listRecords({ ...query, userId: userStore.userId })
}

async function show(row) {
  current.value = await getRecord(row.id)
  detailVisible.value = true
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除这条记录吗？`, '删除确认')
  await deleteRecord(row.id)
  ElMessage.success('已删除')
  load()
}

async function copyCurrentReport() {
  await copyText(buildReportMarkdown(parsedResult.value, current.value.knowledgeRules || [], { title: `${typeLabel(current.value.type)}分析报告` }))
  ElMessage.success('报告已复制')
}

function exportCurrentReport() {
  const label = typeLabel(current.value.type)
  downloadMarkdown(`${label}历史报告-${current.value.id || Date.now()}.md`, buildReportMarkdown(parsedResult.value, current.value.knowledgeRules || [], { title: `${label}分析报告` }))
}

function typeLabel(type) {
  return { LIUYAO: '六爻', BAZI: '八字', QIMEN: '奇门' }[type] || type || '记录'
}

function parseJson(value) {
  if (!value) return null
  try {
    return JSON.parse(value)
  } catch {
    return null
  }
}

function movingYaoText(list) {
  const moving = list.filter(item => item.moving)
  if (!moving.length) return '无动爻'
  return moving.map(item => `${item.position || '-'}${item.sixRelative || ''}${item.branch || ''}`).join('，')
}

function pretty(value) {
  if (!value) return ''
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

onMounted(load)
</script>

<style scoped>
pre {
  max-height: 320px;
  overflow: auto;
  padding: 12px;
  background: #f6f7f9;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.record-summary {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fbfcfd;
  padding: 14px;
  margin-bottom: 12px;
}

.summary-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.summary-head strong {
  min-width: 0;
  color: #101828;
  font-size: 16px;
  line-height: 1.5;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.summary-grid div {
  border: 1px solid #eef0f3;
  border-radius: 8px;
  background: #fff;
  padding: 10px;
}

.summary-grid span {
  display: block;
  color: #667085;
  font-size: 12px;
  margin-bottom: 5px;
}

.summary-grid strong {
  display: block;
  color: #344054;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.raw-collapse {
  margin-bottom: 14px;
}

.guest-alert {
  margin-bottom: 16px;
}

@media (max-width: 700px) {
  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar .el-select,
  .toolbar .el-input,
  .toolbar .el-button {
    width: 100% !important;
  }

  .detail-actions {
    justify-content: stretch;
  }

  .detail-actions .el-button {
    flex: 1;
  }

  .summary-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
