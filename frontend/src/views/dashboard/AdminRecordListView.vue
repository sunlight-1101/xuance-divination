<template>
  <div class="page">
    <div class="page-header">
      <div class="page-header-top">
        <h1 class="page-title">全部历史记录</h1>
        <el-button @click="$router.push('/admin')">← 返回首页</el-button>
      </div>
      <p class="page-desc">查看所有用户的分析记录。</p>
    </div>

    <div class="toolbar">
      <el-select v-model="query.type" placeholder="术数类型" style="width: 160px" clearable>
        <el-option label="六爻" value="LIUYAO" />
        <el-option label="八字" value="BAZI" />
        <el-option label="八字合盘" value="BAZI_COMPATIBILITY" />
        <el-option label="紫微" value="ZIWEI" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索问题" style="width: 260px" clearable />
      <el-button @click="load">搜索</el-button>
    </div>

    <div class="desktop-records">
      <div class="table-wrap">
        <el-table :data="records" stripe border v-loading="loading" empty-text="暂无记录">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="userEmail" label="用户邮箱" min-width="180" />
          <el-table-column label="类型" width="100">
            <template #default="{ row }">{{ typeLabel(row.type) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
          <el-table-column label="创建时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="show(row)">查看</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="mobile-records">
      <el-empty v-if="!records.length" description="暂无记录" />
      <div v-for="row in records" :key="row.id" class="record-card">
        <div class="record-card-head">
          <el-tag size="small">{{ typeLabel(row.type) }}</el-tag>
          <el-tag size="small" :type="statusTagType(row.status)" effect="plain">{{ statusLabel(row.status) }}</el-tag>
          <span>{{ formatTime(row.createTime) }}</span>
        </div>
        <strong>{{ row.question || '未填写问题' }}</strong>
        <div class="card-user">{{ row.userEmail || '-' }}</div>
        <div class="record-card-actions">
          <el-button type="primary" plain @click="show(row)">查看报告</el-button>
          <el-button type="danger" plain @click="remove(row)">删除</el-button>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="detailVisible"
      title="记录详情"
      width="860px"
      :fullscreen="isMobile"
      append-to-body
    >
      <div class="record-detail">
        <div class="record-meta">
          <span>用户：{{ current.userEmail || '-' }}（{{ current.userNickname || '-' }}）</span>
          <span>ID：{{ current.id }}</span>
        </div>
        <div class="detail-actions">
          <el-button type="primary" :disabled="!parsedResult" @click="copyCurrentReport">复制</el-button>
          <el-button :disabled="!parsedResult" @click="exportCurrentReport">导出</el-button>
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
        <el-alert v-if="isProcessing(current)" type="info" :closable="false" title="正在分析中..." />
        <el-alert v-else-if="current.status === 'FAILED'" type="error" :closable="false" title="分析失败" />
        <template v-else>
          <ResultReport :report="parsedResult" />
          <KnowledgeReferences :rules="current.knowledgeRules || []" />
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import KnowledgeReferences from '../../components/KnowledgeReferences.vue'
import ResultReport from '../../components/ResultReport.vue'
import { deleteRecord, getRecord, listAllRecords } from '../../api/record'
import { useUserStore } from '../../stores/user'
import { buildReportMarkdown, copyText, downloadMarkdown } from '../../utils/report'

const userStore = useUserStore()
const records = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const current = ref({})
const query = reactive({ type: '', keyword: '' })
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value <= 700)

const parsedResult = computed(() => {
  if (isProcessing(current.value)) return null
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
      { label: '流年', value: data.currentYearPillar }
    ]
  }
  if (current.value.type === 'BAZI_COMPATIBILITY') {
    return [
      { label: '关系类型', value: data.relationshipType },
      { label: '甲方', value: [data.personAName, data.personABirthDate].filter(Boolean).join(' ') },
      { label: '乙方', value: [data.personBName, data.personBBirthDate].filter(Boolean).join(' ') }
    ]
  }
  if (current.value.type === 'LIUYAO') {
    return [
      { label: '起卦时间', value: data.time },
      { label: '本卦', value: data.mainGua },
      { label: '变卦', value: data.changedGua },
      { label: '动爻', value: movingYaoText(data.yaoList || []) }
    ]
  }
  if (current.value.type === 'ZIWEI') {
    return [
      { label: '出生信息', value: [data.birthDate, data.birthTime, data.birthPlace].filter(Boolean).join(' ') }
    ]
  }
  return [{ label: '类型', value: current.value.type }]
})

async function load() {
  loading.value = true
  try {
    records.value = await listAllRecords({ ...query })
  } catch {
    ElMessage.error('获取记录失败')
  } finally {
    loading.value = false
  }
}

async function show(row) {
  current.value = await getRecord(row.id)
  current.value.userEmail = row.userEmail
  current.value.userNickname = row.userNickname
  detailVisible.value = true
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除这条记录吗？', '删除确认')
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
  return { LIUYAO: '六爻', BAZI: '八字', BAZI_COMPATIBILITY: '八字合盘', ZIWEI: '紫微', QIMEN: '奇门' }[type] || type || '记录'
}

function statusLabel(status) {
  return { PROCESSING: '正在分析中...', DONE: '已完成', FAILED: '分析失败' }[status] || '已完成'
}

function statusTagType(status) {
  return { PROCESSING: 'warning', FAILED: 'danger', DONE: 'success' }[status] || 'success'
}

function isProcessing(record) {
  return record?.status === 'PROCESSING' || (!record?.resultJson && record?.status !== 'FAILED')
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

function parseJson(value) {
  if (!value) return null
  try { return JSON.parse(value) } catch { return null }
}

function movingYaoText(list) {
  const moving = list.filter(item => item.moving)
  if (!moving.length) return '无动爻'
  return moving.map(item => `${item.position || '-'}${item.sixRelative || ''}${item.branch || ''}`).join('，')
}

function pretty(value) {
  if (!value) return ''
  try { return JSON.stringify(JSON.parse(value), null, 2) } catch { return value }
}

function handleResize() { windowWidth.value = window.innerWidth }

onMounted(() => {
  load()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => window.removeEventListener('resize', handleResize))
</script>

<style scoped>
.page-header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.table-wrap {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
}

.mobile-records {
  display: none;
}

.record-detail { display: grid; gap: 12px; }

.record-meta {
  display: flex;
  gap: 24px;
  color: #667085;
  font-size: 13px;
}

.detail-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.record-summary {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fbfcfd;
  padding: 14px;
}

.summary-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.summary-head strong {
  color: #101828;
  font-size: 16px;
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
  word-break: break-word;
}

.raw-collapse { margin-bottom: 14px; }

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

@media (max-width: 700px) {
  .desktop-records {
    display: none;
  }

  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar .el-select,
  .toolbar .el-input,
  .toolbar .el-button {
    width: 100% !important;
  }

  .mobile-records {
    display: grid;
    gap: 10px;
  }

  .record-card {
    min-width: 0;
    padding: 12px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background: #fff;
    box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
  }

  .record-card-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;
    color: #667085;
    font-size: 12px;
  }

  .record-card strong {
    display: block;
    color: #101828;
    font-size: 15px;
    line-height: 1.6;
    overflow-wrap: anywhere;
  }

  .card-user {
    margin-top: 4px;
    color: #667085;
    font-size: 12px;
  }

  .record-card-actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
    margin-top: 12px;
  }

  .record-card-actions .el-button {
    width: 100%;
    margin-left: 0;
  }

  .detail-actions {
    position: sticky;
    top: 0;
    z-index: 2;
    justify-content: stretch;
    margin: -8px -4px 4px;
    padding: 8px 4px;
    background: #fff;
    border-bottom: 1px solid #eef0f3;
  }

  .detail-actions .el-button {
    flex: 1;
    margin-left: 0;
  }

  .summary-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .record-summary {
    padding: 12px;
  }

  .raw-collapse {
    display: none;
  }

  pre {
    max-height: 220px;
    font-size: 12px;
  }
}
</style>

<style>
@media (max-width: 700px) {
  .record-dialog.el-dialog {
    --el-dialog-padding-primary: 12px;
    max-width: 100vw;
    overflow: hidden;
  }

  .record-dialog .el-dialog__header {
    position: sticky;
    top: 0;
    z-index: 3;
    margin: 0;
    padding: 12px 14px;
    background: #fff;
    border-bottom: 1px solid #eef0f3;
  }

  .record-dialog .el-dialog__body {
    height: calc(100vh - 52px);
    padding: 10px 10px 88px;
    overflow-x: hidden;
    overflow-y: auto;
  }
}
</style>
