<template>
  <div class="home-page">
    <section class="home-hero">
      <div class="hero-copy">
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
      <div class="almanac-layout">
        <div class="almanac-main">
          <div class="almanac-date">
            <span>今日干支</span>
            <strong>{{ todayPillars.yearPillar }}年 {{ todayPillars.monthPillar }}月 {{ todayPillars.dayPillar }}日</strong>
            <small>月柱按常用节气近似换月，临近节气日可参考万年历校验。</small>
          </div>
          <div class="almanac-pillars">
            <div>
              <span>年</span>
              <strong>{{ todayPillars.yearPillar || '-' }}</strong>
            </div>
            <div>
              <span>月</span>
              <strong>{{ todayPillars.monthPillar || '-' }}</strong>
            </div>
            <div>
              <span>日</span>
              <strong>{{ todayPillars.dayPillar || '-' }}</strong>
            </div>
          </div>
        </div>
        <div class="almanac-advice">
          <div class="good">
            <span>宜</span>
            <strong>{{ dailyAdvice.good.join('、') }}</strong>
          </div>
          <div class="avoid">
            <span>忌</span>
            <strong>{{ dailyAdvice.avoid.join('、') }}</strong>
          </div>
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
import { getFourPillars } from '../../utils/ganzhi'

const todayText = computed(() => new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }))
const todayPillars = computed(() => getFourPillars(new Date()))
const dailyAdvice = computed(() => {
  const branch = todayPillars.value.dayPillar?.slice(1, 2) || ''
  const adviceMap = {
    子: { good: ['求学', '整理', '沟通'], avoid: ['冲动决策', '熬夜'] },
    丑: { good: ['储蓄', '修整', '复盘'], avoid: ['急进', '争执'] },
    寅: { good: ['开局', '拜访', '学习'], avoid: ['拖延', '过劳'] },
    卯: { good: ['协商', '创作', '养护'], avoid: ['口舌', '散漫'] },
    辰: { good: ['规划', '签订', '整理资料'], avoid: ['冒进', '借贷'] },
    巳: { good: ['表达', '学习', '求财'], avoid: ['急躁', '隐瞒'] },
    午: { good: ['行动', '见人', '推进'], avoid: ['冲突', '贪快'] },
    未: { good: ['安顿', '修补', '储备'], avoid: ['犹豫', '过度承诺'] },
    申: { good: ['执行', '谈判', '检查'], avoid: ['轻信', '分心'] },
    酉: { good: ['收尾', '审美', '理财'], avoid: ['固执', '冷战'] },
    戌: { good: ['守成', '复核', '定计划'], avoid: ['争强', '冒险'] },
    亥: { good: ['休养', '学习', '内省'], avoid: ['拖泥带水', '夜行'] }
  }
  return adviceMap[branch] || { good: ['整理', '学习', '复盘'], avoid: ['冲动', '争执'] }
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

.almanac-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(220px, 0.7fr);
  gap: 14px;
  align-items: stretch;
}

.almanac-main,
.almanac-advice {
  min-width: 0;
}

.almanac-date {
  margin-bottom: 10px;
}

.almanac-date span {
  display: block;
  color: #806326;
  font-size: 13px;
  margin-bottom: 6px;
}

.almanac-date strong {
  display: block;
  color: #173f35;
  font-size: 22px;
  line-height: 1.45;
  word-break: keep-all;
}

.almanac-date small {
  display: block;
  margin-top: 6px;
  color: #6f654e;
  font-size: 12px;
  line-height: 1.5;
}

.almanac-pillars {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  border: 1px solid rgba(176, 138, 60, 0.28);
  border-radius: 8px;
  overflow: hidden;
}

.almanac-pillars div {
  min-height: 82px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 8px;
  border-right: 1px solid rgba(176, 138, 60, 0.22);
}

.almanac-pillars div:last-child {
  border-right: 0;
}

.almanac-pillars span {
  color: #6f654e;
  font-size: 13px;
}

.almanac-pillars strong {
  color: #173f35;
  font-size: 24px;
}

.almanac-advice {
  border-radius: 8px;
  background: rgba(47, 111, 94, 0.08);
  padding: 12px;
  display: grid;
  gap: 10px;
}

.almanac-advice div {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
}

.almanac-advice span {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 14px;
  font-weight: 800;
}

.almanac-advice .good span {
  background: #2f6f5e;
}

.almanac-advice .avoid span {
  background: #a64e3d;
}

.almanac-advice strong {
  color: #173f35;
  font-size: 15px;
  line-height: 1.5;
  word-break: break-word;
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

  .almanac-layout,
  .entry-grid {
    grid-template-columns: 1fr;
  }

  .almanac-date strong {
    font-size: 18px;
  }

  .almanac-pillars strong {
    font-size: 22px;
  }

  .feature-card {
    min-height: 138px;
  }
}
</style>
