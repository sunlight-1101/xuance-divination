<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">首页</h1>
      <p class="page-desc">当前优先开发六爻 MVP，八字和奇门先保留产品入口。</p>
    </div>

    <div class="grid-3">
      <div class="entry active" @click="$router.push('/bazi')">
        <ScrollText :size="28" />
        <h2>六爻解卦</h2>
        <p>手动录入六个爻位，调用知识库和 AI 生成结构化报告。</p>
      </div>
      <div class="entry">
        <CalendarDays :size="28" />
        <h2>八字分析</h2>
        <p>第二阶段补充四柱、大运、流年和十神结构分析。</p>
      </div>
      <div class="entry">
        <Grid3X3 :size="28" />
        <h2>奇门遁甲</h2>
        <p>第三阶段补充九宫盘、门星神和方位策略分析。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { CalendarDays, Grid3X3, ScrollText } from 'lucide-vue-next'
</script>

<style scoped>
.entry {
  min-height: 176px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 22px;
}

.entry.active {
  cursor: pointer;
  border-color: #d6a954;
}

.entry h2 {
  margin: 18px 0 8px;
  font-size: 20px;
}

.entry p {
  margin: 0;
  color: #667085;
  line-height: 1.7;
}
</style>
