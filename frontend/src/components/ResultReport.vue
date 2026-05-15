<template>
  <el-empty v-if="!report" description="分析后显示结果" />
  <div v-else class="report">
    <div class="conclusion">
      <div class="conclusion-head">
        <div class="label">核心结论</div>
        <el-tag size="small" type="warning">信心：{{ report.confidence || '-' }}</el-tag>
      </div>
      <div class="text">{{ report.coreConclusion || '-' }}</div>
    </div>

    <div v-if="report.keyEvidence?.length" class="block">
      <h3>关键依据</h3>
      <ul>
        <li v-for="item in report.keyEvidence" :key="item">{{ item }}</li>
      </ul>
    </div>

    <div v-if="plainSummary" class="block plain-block">
      <h3>具体分析</h3>
      <p>{{ plainSummary }}</p>
    </div>

    <div v-if="report.detailedAnalysis" class="block">
      <h3>专业细盘</h3>
      <dl>
        <template v-for="(value, key) in report.detailedAnalysis" :key="key">
          <dt>{{ labels[key] || key }}</dt>
          <dd>{{ stringifyValue(value) }}</dd>
        </template>
      </dl>
    </div>

    <div v-if="timingItems.length" class="block">
      <h3>时间窗口</h3>
      <div v-for="item in timingItems" :key="`${item.time}-${item.reason || item}`" class="timing">
        <strong>{{ item.time || '-' }}</strong>
        <span>{{ item.reason || '-' }}</span>
      </div>
    </div>

    <div v-if="report.suggestion" class="block">
      <h3>行动建议</h3>
      <p>{{ report.suggestion }}</p>
    </div>

    <div v-if="report.missingFields?.length" class="block">
      <h3>需要补充</h3>
      <el-tag v-for="field in report.missingFields" :key="field" class="field-tag">{{ field }}</el-tag>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  report: {
    type: Object,
    default: null
  }
})

const plainSummary = computed(() => props.report?.plainSummary || props.report?.plainLanguage || props.report?.userFriendlySummary || '')
const timingItems = computed(() => {
  const value = props.report?.timing
  if (!Array.isArray(value)) return []
  return value.filter(item => item && (item.time || item.reason))
})

const labels = {
  yongShen: '用神怎么取',
  shiYing: '自己和对方',
  usefulGodState: '用神状态',
  movingYao: '变化点',
  hiddenSpirit: '隐藏因素',
  timing: '应期判断',
  basicChart: '命盘结构',
  dayMasterStrength: '日主强弱',
  tenGods: '十神关系',
  usefulGod: '喜忌用神',
  luckYear: '大运流年',
  questionFocus: '问题重点',
  caseReference: '参考类象',
  supportAndObstacle: '助力阻力',
  guaTrend: '卦势走向'
}

function stringifyValue(value) {
  if (value == null || value === '') return '-'
  if (Array.isArray(value)) return value.map(stringifyValue).join('；')
  if (typeof value === 'object') {
    return Object.entries(value).map(([key, val]) => `${labels[key] || key}：${stringifyValue(val)}`).join('；')
  }
  return String(value)
}
</script>

<style scoped>
.report {
  display: grid;
  gap: 14px;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.conclusion {
  min-width: 0;
  max-width: 100%;
  padding: 14px;
  border: 1px solid #ead7ae;
  border-radius: 8px;
  background: #fff8ea;
}

.conclusion-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
  margin-bottom: 8px;
}

.label {
  color: #8a6116;
  font-weight: 700;
}

.text {
  margin-bottom: 10px;
  line-height: 1.7;
  font-size: 16px;
}

.plain-summary {
  margin: 0;
  color: #5f4b20;
  line-height: 1.75;
}

.plain-block p {
  white-space: pre-wrap;
}

.block {
  min-width: 0;
  max-width: 100%;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

h3 {
  margin: 0 0 10px;
  font-size: 15px;
}

ul {
  margin: 0;
  padding-left: 18px;
  line-height: 1.7;
  overflow-wrap: anywhere;
}

dl {
  margin: 0;
}

dt {
  margin-top: 10px;
  color: #344054;
  font-weight: 700;
}

dt:first-child {
  margin-top: 0;
}

dd {
  margin: 4px 0 0;
  color: #475467;
  line-height: 1.7;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.timing {
  display: grid;
  grid-template-columns: 90px 1fr;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #eef0f3;
}

.timing:last-child {
  border-bottom: 0;
}

p {
  margin: 0;
  line-height: 1.7;
  overflow-wrap: anywhere;
}

.field-tag {
  margin: 0 8px 8px 0;
}

@media (max-width: 700px) {
  .report {
    gap: 10px;
  }

  .conclusion,
  .block {
    padding: 12px;
  }

  .conclusion-head {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }

  .text,
  dd,
  ul,
  p {
    font-size: 15px;
    line-height: 1.75;
  }

  dl {
    display: grid;
    gap: 10px;
  }

  dt {
    margin-top: 0;
    padding-bottom: 4px;
    border-bottom: 1px solid #eef0f3;
  }

  .timing {
    grid-template-columns: 1fr;
    gap: 4px;
  }
}
</style>
