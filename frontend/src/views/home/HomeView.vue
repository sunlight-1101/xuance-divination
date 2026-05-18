<template>
  <div class="home-page">
    <section class="home-hero">
      <div class="hero-copy">
        <img class="hero-logo" src="/icons/zhexuan-logo.png" alt="哲玄" />
        <h1>哲玄</h1>
        <p>八字 · 六爻 · 紫微</p>
      </div>
      <button class="wisdom-button" type="button" @click="$router.push('/knowledge')">
        <Sparkles :size="18" />
        智囊
      </button>
    </section>

    <section class="today-card paper-card">
      <div class="card-title">
        <span class="leaf-mark">竹</span>
        <h2>今日概览</h2>
        <small>{{ todayText }}</small>
      </div>
      <div class="overview-grid">
        <div class="mini-chart">
          <div class="mini-label">八字四柱</div>
          <div class="mini-pillars">
            <div v-for="pillar in pillarPreview" :key="pillar.label">
              <span>{{ pillar.label }}</span>
              <strong>{{ pillar.value || '-' }}</strong>
            </div>
          </div>
        </div>
        <div class="day-master">
          <span>日主</span>
          <strong>{{ dayMaster || '未排' }}</strong>
          <small>{{ birthSummary }}</small>
        </div>
      </div>
    </section>

    <section class="entry-grid">
      <button class="feature-card dark" type="button" @click="$router.push('/bazi')">
        <CalendarDays :size="28" />
        <strong>八字排盘</strong>
        <span>出生信息 · 命理分析</span>
        <i>→</i>
      </button>
      <button class="feature-card" type="button" @click="$router.push('/liuyao')">
        <ScrollText :size="28" />
        <strong>六爻解卦</strong>
        <span>摇卦断事 · 洞察吉凶</span>
        <i>→</i>
      </button>
      <button class="feature-card" type="button" @click="$router.push('/ziwei')">
        <Sparkles :size="28" />
        <strong>紫微斗数</strong>
        <span>命盘格局 · 运势分析</span>
        <i>→</i>
      </button>
    </section>

    <section class="paper-card advice-card">
      <div class="card-title">
        <span class="leaf-mark">宜</span>
        <h2>使用建议</h2>
        <button type="button" @click="$router.push('/records')">查看记录 ›</button>
      </div>
      <div class="advice-content">
        <p><strong>先存资料：</strong>常用出生信息可以保存多人，八字和合盘都能直接调用。</p>
        <p><strong>再问问题：</strong>问题越具体，报告越容易给出清楚判断和行动建议。</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { CalendarDays, ScrollText, Sparkles } from 'lucide-vue-next'
import { useUserStore } from '../../stores/user'
import { getFourPillars } from '../../utils/ganzhi'

const userStore = useUserStore()
const profile = computed(() => userStore.getBirthProfile())
const todayText = computed(() => new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }))
const pillars = computed(() => {
  const item = profile.value
  if (!item?.birthDate) return {}
  const normalizedTime = item.birthTime || '00:00'
  return getFourPillars(`${item.birthDate}T${normalizedTime}`) || {}
})
const pillarPreview = computed(() => [
  { label: '年柱', value: profile.value?.yearPillar || pillars.value.yearPillar },
  { label: '月柱', value: profile.value?.monthPillar || pillars.value.monthPillar },
  { label: '日柱', value: profile.value?.dayPillar || pillars.value.dayPillar || profile.value?.birthDayGanZhi },
  { label: '时柱', value: profile.value?.hourPillar || pillars.value.hourPillar }
])
const dayMaster = computed(() => profile.value?.dayMaster || profile.value?.birthDayMaster || pillars.value.dayMaster || '')
const birthSummary = computed(() => {
  const item = profile.value
  return item ? [item.birthDate, item.birthTime, item.birthPlace].filter(Boolean).join(' ') : '先到八字页保存出生资料'
})
</script>

<style scoped>
.home-page {
  min-height: calc(100vh - 58px);
  padding: 18px 18px 92px;
  color: #173f35;
  background:
    linear-gradient(180deg, rgba(12, 48, 41, 0.92), rgba(23, 63, 53, 0.88) 320px, #f8f4e8 321px),
    repeating-linear-gradient(105deg, transparent 0 36px, rgba(232, 214, 165, 0.08) 36px 38px);
}

.home-hero {
  position: relative;
  min-height: 230px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px 18px 40px;
  color: #f8f0d8;
  overflow: hidden;
}

.home-hero::before {
  content: "";
  position: absolute;
  inset: -20px 0 auto auto;
  width: 210px;
  height: 260px;
  opacity: 0.18;
  background:
    linear-gradient(90deg, transparent 48%, #f4e6b8 49% 51%, transparent 52%),
    radial-gradient(ellipse at 60% 18%, transparent 0 30%, #f4e6b8 31% 34%, transparent 35%),
    radial-gradient(ellipse at 42% 40%, transparent 0 30%, #f4e6b8 31% 34%, transparent 35%),
    radial-gradient(ellipse at 62% 64%, transparent 0 30%, #f4e6b8 31% 34%, transparent 35%);
  transform: rotate(14deg);
}

.hero-copy {
  position: relative;
  z-index: 1;
}

.hero-logo {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  object-fit: cover;
  margin-bottom: 12px;
}

.hero-copy h1 {
  margin: 0;
  font-size: 54px;
  line-height: 1;
  letter-spacing: 0;
  color: #f8f0d8;
}

.hero-copy p {
  margin: 14px 0 0;
  font-size: 17px;
  color: rgba(248, 240, 216, 0.86);
}

.wisdom-button {
  position: relative;
  z-index: 1;
  width: 62px;
  height: 62px;
  border-radius: 50%;
  border: 1px solid #e8d6a5;
  background: rgba(12, 48, 41, 0.78);
  color: #e8d6a5;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 2px;
  font-size: 12px;
}

.paper-card,
.feature-card {
  border: 1px solid #d8c696;
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(255, 252, 243, 0.97), rgba(248, 244, 230, 0.97)),
    repeating-linear-gradient(0deg, transparent 0 34px, rgba(91, 112, 95, 0.035) 34px 36px);
  box-shadow: 0 10px 24px rgba(23, 63, 53, 0.12), inset 0 0 0 2px rgba(255, 255, 255, 0.48);
}

.today-card {
  padding: 16px;
  margin-top: -30px;
  position: relative;
  z-index: 2;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.card-title h2 {
  margin: 0;
  font-size: 22px;
  color: #173f35;
}

.card-title small {
  margin-left: auto;
  color: #806326;
  font-size: 13px;
}

.leaf-mark {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #f8f0d8;
  background: #2f6f5e;
  font-weight: 800;
}

.overview-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 128px;
  gap: 14px;
  align-items: stretch;
}

.mini-label {
  color: #806326;
  font-size: 13px;
  margin-bottom: 8px;
}

.mini-pillars {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  border: 1px solid rgba(176, 138, 60, 0.28);
  border-radius: 8px;
  overflow: hidden;
}

.mini-pillars div {
  min-height: 86px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 8px;
  border-right: 1px solid rgba(176, 138, 60, 0.22);
}

.mini-pillars div:last-child {
  border-right: 0;
}

.mini-pillars span {
  color: #6f654e;
  font-size: 13px;
}

.mini-pillars strong {
  color: #173f35;
  font-size: 24px;
}

.day-master {
  border-radius: 8px;
  background: rgba(47, 111, 94, 0.08);
  display: grid;
  place-items: center;
  align-content: center;
  text-align: center;
  padding: 12px;
}

.day-master span {
  color: #2f6f5e;
  font-size: 13px;
  font-weight: 700;
}

.day-master strong {
  margin: 4px 0;
  color: #173f35;
  font-size: 36px;
}

.day-master small {
  color: #6f654e;
  line-height: 1.5;
}

.entry-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin: 18px 0;
}

.feature-card {
  position: relative;
  min-height: 164px;
  border-color: #d8c696;
  text-align: left;
  padding: 20px;
  color: #173f35;
}

.feature-card.dark {
  color: #f8f0d8;
  background:
    linear-gradient(180deg, rgba(23, 63, 53, 0.94), rgba(18, 55, 47, 0.96)),
    radial-gradient(circle at 70% 70%, rgba(232, 214, 165, 0.18), transparent 38%);
}

.feature-card strong,
.feature-card span {
  display: block;
}

.feature-card strong {
  margin-top: 18px;
  font-size: 24px;
}

.feature-card span {
  margin-top: 8px;
  color: inherit;
  opacity: 0.78;
  font-size: 14px;
}

.feature-card i {
  position: absolute;
  right: 18px;
  bottom: 16px;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: #e8d6a5;
  color: #173f35;
  font-style: normal;
  font-size: 20px;
}

.advice-card {
  padding: 16px;
}

.card-title button {
  margin-left: auto;
  border: 0;
  background: transparent;
  color: #806326;
  font-size: 14px;
}

.advice-content {
  display: grid;
  gap: 10px;
}

.advice-content p {
  margin: 0;
  color: #3f4f49;
  line-height: 1.7;
}

@media (max-width: 700px) {
  .home-page {
    min-height: 100dvh;
    padding: 0 10px 90px;
  }

  .home-hero {
    min-height: 260px;
    padding: 26px 14px 48px;
  }

  .hero-copy h1 {
    font-size: 52px;
  }

  .overview-grid,
  .entry-grid {
    grid-template-columns: 1fr;
  }

  .mini-pillars strong {
    font-size: 22px;
  }

  .feature-card {
    min-height: 138px;
  }
}
</style>
