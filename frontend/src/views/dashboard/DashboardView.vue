<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">管理后台</h1>
      <p class="page-desc">用户管理与系统工具。</p>
    </div>

    <h3 class="section-title">管理功能</h3>
    <div class="grid-3">
      <div class="entry active" @click="$router.push('/admin/users')">
        <Users :size="28" />
        <h2>用户管理</h2>
        <p>查看所有注册用户的邮箱、昵称、角色、状态和注册时间。</p>
      </div>
      <div class="entry active" @click="$router.push('/admin/records')">
        <ScrollText :size="28" />
        <h2>全部记录</h2>
        <p>查看所有用户的历史分析记录，支持按类型和问题搜索。</p>
      </div>
      <div class="entry active" @click="$router.push('/knowledge')">
        <BookOpen :size="28" />
        <h2>知识库管理</h2>
        <p>管理八字、六爻、紫微相关规则、案例和典籍资料。</p>
      </div>
    </div>

    <h3 class="section-title" style="margin-top: 32px;">排盘工具</h3>
    <div class="grid-3">
      <div class="entry active" @click="$router.push('/liuyao')">
        <ScrollText :size="28" />
        <h2>六爻解卦</h2>
        <p>手动录入六个爻位，调用知识库和 AI 生成结构化报告。</p>
      </div>
      <div class="entry active" @click="$router.push('/bazi')">
        <CalendarDays :size="28" />
        <h2>八字排盘</h2>
        <p>根据出生信息生成四柱、大运、流年，支持真太阳时与合盘。</p>
      </div>
      <div class="entry active" @click="$router.push('/ziwei')">
        <Sparkles :size="28" />
        <h2>紫微斗数</h2>
        <p>命盘排盘、十二宫展示与星曜分析。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { BookOpen, CalendarDays, ScrollText, Sparkles, Users } from 'lucide-vue-next'
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
