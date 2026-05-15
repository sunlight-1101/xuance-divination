<template>
  <div class="references">
    <div class="references-head">
      <h3>引用规则</h3>
      <span>{{ rules.length + classicReferences.length }} 条</span>
    </div>
    <el-empty v-if="rules.length === 0 && classicReferences.length === 0" description="暂无引用资料" />
    <div v-if="classicReferences.length" class="classic-block">
      <h4>引用典籍</h4>
      <article v-for="(item, index) in classicReferences" :key="index" class="classic-reference">
        {{ item }}
      </article>
    </div>
    <el-collapse v-if="rules.length">
      <el-collapse-item v-for="rule in rules" :key="rule.id" :name="rule.id">
        <template #title>
          <div class="rule-title">
            <el-tag size="small">{{ rule.category || '未分类' }}</el-tag>
            <span>{{ rule.title }}</span>
          </div>
        </template>
        <p>{{ rule.ruleContent }}</p>
        <div v-if="rule.example" class="example">
          <strong>示例</strong>
          <span>{{ rule.example }}</span>
        </div>
        <div v-if="rule.keywords" class="keywords">
          <el-tag v-for="keyword in splitKeywords(rule.keywords)" :key="keyword" size="small" type="info">
            {{ keyword }}
          </el-tag>
        </div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup>
defineProps({
  rules: {
    type: Array,
    default: () => []
  },
  classicReferences: {
    type: Array,
    default: () => []
  }
})

function splitKeywords(value) {
  return value.split(/[,\s，、]+/).filter(Boolean)
}
</script>

<style scoped>
.references {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.references-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

h3 {
  margin: 0;
  font-size: 15px;
}

.references-head span {
  color: #667085;
  font-size: 13px;
}

.rule-title {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.rule-title span:last-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

p {
  margin: 0;
  color: #344054;
  line-height: 1.7;
}

.example {
  margin-top: 10px;
  padding: 10px;
  border-radius: 8px;
  background: #f6f7f9;
  color: #475467;
  line-height: 1.6;
}

.example strong {
  display: block;
  margin-bottom: 4px;
  color: #344054;
}

.keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.classic-block {
  margin-bottom: 12px;
}

.classic-block h4 {
  margin: 0 0 8px;
  color: #344054;
  font-size: 14px;
}

.classic-reference {
  margin-bottom: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fffaf0;
  border: 1px solid #f1dfb8;
  color: #57451f;
  line-height: 1.7;
  font-size: 13px;
  word-break: break-word;
}
</style>
