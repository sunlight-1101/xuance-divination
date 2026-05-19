<template>
  <div class="home-page">
    <section class="home-hero">
      <div class="hero-copy">
        <h1>哲玄</h1>
        <p>八字 · 六爻 · 紫微</p>
        <small>洞察天时 · 明辨吉凶 · 趋吉避凶 · 顺势而为</small>
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
        <span class="lunar-pill">{{ selectedDisplay.lunar || '农历加载中' }}</span>
        <button class="expand-button" type="button" @click="almanacExpanded = !almanacExpanded">
          {{ almanacExpanded ? '收起' : '展开' }}
        </button>
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
      <div v-if="almanacExpanded" class="almanac-expanded">
        <div class="expanded-grid">
          <div>
            <span>查看日期</span>
            <strong>{{ selectedDateText }}</strong>
          </div>
          <div>
            <span>农历</span>
            <strong>{{ selectedDisplay.lunar || '接口加载中' }}</strong>
          </div>
          <div>
            <span>节气</span>
            <strong>{{ selectedDisplay.jieqi || '暂无' }}</strong>
          </div>
          <div>
            <span>时柱（午时）</span>
            <strong>{{ selectedPillars.hourPillar || '-' }}</strong>
          </div>
          <div>
            <span>日主</span>
            <strong>{{ selectedPillars.dayMaster || '-' }}</strong>
          </div>
          <div>
            <span>冲煞</span>
            <strong>{{ selectedDisplay.chong || '暂无' }}</strong>
          </div>
          <div>
            <span>黄黑道</span>
            <strong>{{ [selectedDisplay.tianShen, selectedDisplay.tianShenType].filter(Boolean).join(' · ') || '暂无' }}</strong>
          </div>
          <div>
            <span>彭祖百忌</span>
            <strong>{{ selectedDisplay.pengzu || '暂无' }}</strong>
          </div>
          <div>
            <span>宜</span>
            <strong>{{ selectedAdvice.goodText }}</strong>
          </div>
          <div>
            <span>忌</span>
            <strong>{{ selectedAdvice.avoidText }}</strong>
          </div>
          <div>
            <span>旬空 / 纳音</span>
            <strong>{{ [selectedDisplay.xunKong, selectedDisplay.naYin].filter(Boolean).join(' · ') || '暂无' }}</strong>
          </div>
        </div>
        <div v-if="almanacLoading || almanacError || remoteAlmanac?.available === false" class="almanac-status">
          {{ almanacLoading ? '正在读取万年历接口...' : (almanacError || remoteAlmanac?.message || '万年历接口暂时不可用，已显示本地干支。') }}
        </div>
        <div class="month-calendar">
          <div class="calendar-head">
            <button type="button" @click="shiftCalendarMonth(-1)">‹</button>
            <strong>{{ calendarTitle }}</strong>
            <button type="button" class="today-link" @click="resetCalendarToToday">今</button>
            <button type="button" @click="shiftCalendarMonth(1)">›</button>
          </div>
          <div class="weekday-row">
            <span v-for="day in weekdays" :key="day">{{ day }}</span>
          </div>
          <div class="calendar-grid">
            <button
              v-for="cell in calendarCells"
              :key="cell.key"
              type="button"
              :class="{ muted: !cell.inMonth, today: cell.isToday, selected: cell.isSelected }"
              @click="selectAlmanacDate(cell.date)"
            >
              <span>{{ cell.day }}</span>
              <strong>{{ cell.pillars.dayPillar || '-' }}</strong>
              <small>{{ cell.advice.good[0] }}</small>
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="entry-grid">
      <button class="feature-card feature-bazi" type="button" @click="$router.push('/bazi')">
        <span class="feature-emblem bazi-emblem" aria-hidden="true">
          <span>年</span><span>月</span><span>日</span><span>时</span>
          <strong>甲</strong><strong>乙</strong><strong>丙</strong><strong>丁</strong>
          <strong>子</strong><strong>丑</strong><strong>寅</strong><strong>卯</strong>
        </span>
        <span class="feature-copy">
          <strong>八字排盘</strong>
          <small>出生信息 · 命理分析</small>
          <em>洞悉命局，把握人生轨迹</em>
        </span>
        <i aria-hidden="true">→</i>
      </button>
      <button class="feature-card feature-liuyao" type="button" @click="$router.push('/liuyao')">
        <span class="feature-emblem hexagram-emblem" aria-hidden="true">
          <b></b><b></b><b></b><b></b><b></b><b></b>
        </span>
        <span class="feature-copy">
          <strong>六爻解卦</strong>
          <small>摇卦断事 · 洞察吉凶</small>
          <em>解读当下，指引决策方向</em>
        </span>
        <i aria-hidden="true">→</i>
      </button>
      <button class="feature-card feature-ziwei" type="button" @click="$router.push('/ziwei')">
        <span class="feature-emblem astrolabe-emblem" aria-hidden="true">
          <b></b><b></b><b></b>
        </span>
        <span class="feature-copy">
          <strong>紫微斗数</strong>
          <small>命盘格局 · 运势分析</small>
          <em>星曜布局，洞见运势起伏</em>
        </span>
        <i aria-hidden="true">→</i>
      </button>
    </section>

    <section class="home-lower-grid">
      <div class="paper-card advice-card">
        <div class="card-title">
          <span class="leaf-mark">竹</span>
          <h2>使用建议</h2>
        </div>
        <div class="advice-content">
          <p><strong>先存资料：</strong>常用出生信息可以保存多人，八字和命盘都能直接调用。</p>
          <p><strong>再问问题：</strong>问题越具体，报告越容易给出清楚判断和行动建议。</p>
          <p><strong>遵循参考：</strong>命理是趋势参考，真正的改变，始于你的选择与行动。</p>
        </div>
        <button class="support-strip" type="button" @click="$router.push('/quota')">
          <span>支持开发</span>
          <strong>制作不易，觉得好用可以打赏；建议反馈 QQ 1559087584</strong>
          <i>›</i>
        </button>
      </div>

      <div class="paper-card recent-card">
        <div class="card-title">
          <span class="leaf-mark">时</span>
          <h2>最近记录</h2>
          <button type="button" @click="$router.push('/records')">查看全部 ›</button>
        </div>
        <div class="recent-empty">
          <span>卷</span>
          <strong>暂无记录</strong>
          <p>开始使用工具，记录将显示在这里</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { Sparkles } from 'lucide-vue-next'
import { getAlmanacDay } from '../../api/almanac'
import { getFourPillars } from '../../utils/ganzhi'

const todayText = computed(() => new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }))
const todayPillars = computed(() => getFourPillars(new Date()))
const almanacExpanded = ref(false)
const selectedAlmanacDate = ref(toDateKey(new Date()))
const calendarMonth = ref(toMonthKey(new Date()))
const remoteAlmanac = ref(null)
const almanacLoading = ref(false)
const almanacError = ref('')
const weekdays = ['一', '二', '三', '四', '五', '六', '日']

const dailyAdvice = computed(() => getDailyAdvice(todayPillars.value.dayPillar))
const selectedPillars = computed(() => getFourPillars(`${selectedAlmanacDate.value}T12:00:00`))
const selectedAdvice = computed(() => {
  if (remoteAlmanac.value?.yi || remoteAlmanac.value?.ji) {
    return {
      good: splitAlmanacList(remoteAlmanac.value.yi),
      avoid: splitAlmanacList(remoteAlmanac.value.ji),
      goodText: splitAlmanacList(remoteAlmanac.value.yi).join('、') || '接口暂未返回宜事。',
      avoidText: splitAlmanacList(remoteAlmanac.value.ji).join('、') || '接口暂未返回忌事。'
    }
  }
  return getDailyAdvice(selectedPillars.value.dayPillar)
})
const selectedDisplay = computed(() => remoteAlmanac.value || {
  lunar: '',
  jieqi: '',
  chong: '',
  pengzu: '',
  tianShen: '',
  tianShenType: '',
  xiu: '',
  xunKong: '',
  naYin: '',
  yearGanzhi: selectedPillars.value.yearPillar,
  monthGanzhi: selectedPillars.value.monthPillar,
  dayGanzhi: selectedPillars.value.dayPillar
})
const selectedDateText = computed(() => {
  const date = new Date(`${selectedAlmanacDate.value}T12:00:00`)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
})
const calendarTitle = computed(() => {
  const [year, month] = calendarMonth.value.split('-').map(Number)
  return `${year}年${month}月`
})
const calendarCells = computed(() => buildCalendarCells(calendarMonth.value, selectedAlmanacDate.value))

function getDailyAdvice(dayPillar) {
  const branch = dayPillar?.slice(1, 2) || ''
  const adviceMap = {
    子: { good: ['求学', '整理', '沟通'], avoid: ['冲动决策', '熬夜'], goodText: '适合学习、整理信息、沟通确认。', avoidText: '不宜临时冲动拍板，晚上少熬夜。' },
    丑: { good: ['储蓄', '修整', '复盘'], avoid: ['急进', '争执'], goodText: '适合盘点账目、修补细节、做复盘。', avoidText: '不宜强行推进，少卷入口舌。' },
    寅: { good: ['开局', '拜访', '学习'], avoid: ['拖延', '过劳'], goodText: '适合启动新计划、拜访沟通、吸收新知识。', avoidText: '不宜拖延，也别把日程塞太满。' },
    卯: { good: ['协商', '创作', '养护'], avoid: ['口舌', '散漫'], goodText: '适合协商关系、创作表达、照顾身心。', avoidText: '不宜争辩细枝末节，注意专注。' },
    辰: { good: ['规划', '签订', '整理资料'], avoid: ['冒进', '借贷'], goodText: '适合定计划、整理材料、推进文书事项。', avoidText: '不宜冒险投资或随意借贷。' },
    巳: { good: ['表达', '学习', '求财'], avoid: ['急躁', '隐瞒'], goodText: '适合表达观点、学习技能、处理收益相关事项。', avoidText: '不宜急躁催促，也别藏着关键问题。' },
    午: { good: ['行动', '见人', '推进'], avoid: ['冲突', '贪快'], goodText: '适合见人、执行、把事情向前推。', avoidText: '不宜硬碰硬，速度快也要留余地。' },
    未: { good: ['安顿', '修补', '储备'], avoid: ['犹豫', '过度承诺'], goodText: '适合安顿事务、修补关系、储备资源。', avoidText: '不宜犹豫太久，承诺前先看能力。' },
    申: { good: ['执行', '谈判', '检查'], avoid: ['轻信', '分心'], goodText: '适合执行落地、谈判沟通、检查漏洞。', avoidText: '不宜轻信口头承诺，注意别分心。' },
    酉: { good: ['收尾', '审美', '理财'], avoid: ['固执', '冷战'], goodText: '适合收尾、整理形象、处理财务细节。', avoidText: '不宜过于固执，关系中别冷处理。' },
    戌: { good: ['守成', '复核', '定计划'], avoid: ['争强', '冒险'], goodText: '适合守住成果、复核方案、制定稳妥计划。', avoidText: '不宜争强好胜或做高风险尝试。' },
    亥: { good: ['休养', '学习', '内省'], avoid: ['拖泥带水', '夜行'], goodText: '适合休养、学习、独处思考。', avoidText: '不宜拖泥带水，夜间出行多留意。' }
  }
  return adviceMap[branch] || { good: ['整理', '学习', '复盘'], avoid: ['冲动', '争执'], goodText: '适合整理、学习、复盘。', avoidText: '不宜冲动争执。' }
}

function buildCalendarCells(monthKey, selectedKey) {
  const [year, month] = monthKey.split('-').map(Number)
  const first = new Date(year, month - 1, 1)
  const startOffset = (first.getDay() + 6) % 7
  const start = new Date(year, month - 1, 1 - startOffset)
  const todayKey = toDateKey(new Date())
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    const key = toDateKey(date)
    const pillars = getFourPillars(`${key}T12:00:00`)
    return {
      key,
      date,
      day: date.getDate(),
      inMonth: date.getMonth() === month - 1,
      isToday: key === todayKey,
      isSelected: key === selectedKey,
      pillars,
      advice: getDailyAdvice(pillars.dayPillar)
    }
  })
}

function selectAlmanacDate(date) {
  selectedAlmanacDate.value = toDateKey(date)
}

function shiftCalendarMonth(offset) {
  const [year, month] = calendarMonth.value.split('-').map(Number)
  const next = new Date(year, month - 1 + offset, 1)
  calendarMonth.value = toMonthKey(next)
}

function toDateKey(value) {
  const date = new Date(value)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function toMonthKey(value) {
  const date = new Date(value)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

function resetCalendarToToday() {
  const today = new Date()
  selectedAlmanacDate.value = toDateKey(today)
  calendarMonth.value = toMonthKey(today)
}

function splitAlmanacList(value) {
  return String(value || '')
    .split(/[|、,，\s]+/)
    .map(item => item.trim())
    .filter(Boolean)
}

async function loadRemoteAlmanac(dateKey) {
  almanacLoading.value = true
  almanacError.value = ''
  try {
    remoteAlmanac.value = await getAlmanacDay(dateKey)
  } catch (error) {
    remoteAlmanac.value = null
    almanacError.value = '万年历接口暂时不可用，已显示本地干支。'
  } finally {
    almanacLoading.value = false
  }
}

watch([selectedAlmanacDate, almanacExpanded], ([dateKey, expanded]) => {
  if (expanded) loadRemoteAlmanac(dateKey)
}, { immediate: true })
</script>

<style scoped>
.home-page {
  --display-font: "STXingkai", "华文行楷", "STKaiti", "KaiTi", "FangSong", "Microsoft YaHei", serif;
  min-height: 100vh;
  padding: 74px 46px 46px;
  color: #173f35;
  background:
    linear-gradient(180deg, rgba(0, 62, 54, 0.96) 0 410px, #f8f1e2 411px),
    radial-gradient(circle at 82% 12%, rgba(232, 214, 165, 0.16), transparent 26%),
    repeating-linear-gradient(105deg, transparent 0 36px, rgba(232, 214, 165, 0.08) 36px 38px);
}

.home-hero {
  position: relative;
  min-height: 252px;
  max-width: 1480px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30px 82px 42px;
  color: #f8f0d8;
  overflow: hidden;
}

.home-hero::before {
  content: "";
  position: absolute;
  top: -74px;
  bottom: 0;
  left: 50%;
  width: 100vw;
  transform: translateX(-50%);
  opacity: 1;
  background:
    linear-gradient(90deg, rgba(4, 50, 43, 0.96) 0%, rgba(4, 50, 43, 0.76) 34%, rgba(4, 50, 43, 0.16) 100%),
    url("/images/home-ui/hero-bg.jpg") center top / cover no-repeat;
}

.home-hero::after {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  width: 240px;
  height: 100%;
  opacity: 0.22;
  background:
    linear-gradient(96deg, transparent 0 30%, rgba(202, 229, 205, 0.5) 31% 33%, transparent 34%),
    linear-gradient(104deg, transparent 0 54%, rgba(202, 229, 205, 0.38) 55% 56%, transparent 57%),
    radial-gradient(ellipse at 16% 24%, rgba(202, 229, 205, 0.36), transparent 30%);
}

.hero-copy {
  position: relative;
  z-index: 1;
}

.hero-copy h1 {
  margin: 0;
  font-family: var(--display-font);
  font-size: 68px;
  line-height: 1;
  letter-spacing: 0;
  color: #f7d88e;
  background: linear-gradient(180deg, #fff1bf 0%, #f2cb75 42%, #c89437 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 8px 18px rgba(0, 0, 0, 0.24);
}

.hero-copy p {
  margin: 14px 0 0;
  font-family: var(--display-font);
  font-size: 21px;
  color: #f4d791;
  font-weight: 800;
}

.hero-copy small {
  display: block;
  margin-top: 14px;
  color: rgba(255, 248, 221, 0.9);
  font-size: 15px;
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
  border: 1px solid rgba(218, 184, 122, 0.48);
  border-radius: 14px;
  background:
    linear-gradient(180deg, rgba(255, 252, 243, 0.97), rgba(248, 244, 230, 0.97)),
    repeating-linear-gradient(0deg, transparent 0 34px, rgba(91, 112, 95, 0.035) 34px 36px);
  box-shadow: 0 16px 30px rgba(23, 63, 53, 0.13), inset 0 0 0 2px rgba(255, 255, 255, 0.56);
}

.today-card {
  max-width: 1480px;
  margin: -32px auto 0;
  padding: 28px 32px;
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
  font-family: var(--display-font);
  font-size: 24px;
  color: #173f35;
}

.card-title small {
  margin-left: auto;
  color: #806326;
  font-size: 15px;
}

.lunar-pill {
  border: 1px solid rgba(128, 99, 38, 0.25);
  border-radius: 999px;
  padding: 6px 12px;
  background: rgba(255, 253, 246, 0.64);
  color: #9b7430;
  font-size: 13px;
  font-weight: 800;
}

.expand-button {
  border: 1px solid rgba(128, 99, 38, 0.28);
  border-radius: 999px;
  background: rgba(255, 253, 246, 0.7);
  color: #806326;
  font-size: 13px;
  font-weight: 700;
  line-height: 28px;
  padding: 0 12px;
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
  grid-template-columns: minmax(0, 1.38fr) minmax(320px, 0.72fr);
  gap: 16px;
  align-items: stretch;
}

.almanac-main,
.almanac-advice {
  min-width: 0;
  border: 1px solid rgba(176, 138, 60, 0.22);
  border-radius: 10px;
  background:
    radial-gradient(circle at 4% 86%, rgba(47, 111, 94, 0.08), transparent 25%),
    rgba(255, 253, 246, 0.6);
}

.almanac-date {
  margin: 0;
  padding: 16px 18px 0;
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
  border: 0;
  border-radius: 0;
  overflow: hidden;
}

.almanac-pillars div {
  min-height: 118px;
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
  font-size: 32px;
  font-weight: 900;
}

.almanac-advice {
  background: transparent;
  border: 0;
  padding: 0;
  display: grid;
  gap: 10px;
}

.almanac-advice div {
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr);
  gap: 16px;
  align-items: center;
  min-height: 105px;
  padding: 18px;
  border-radius: 10px;
  border: 1px solid rgba(176, 138, 60, 0.18);
  overflow: hidden;
  position: relative;
}

.almanac-advice div::after {
  content: "";
  position: absolute;
  right: 10px;
  top: 0;
  width: 150px;
  height: 100%;
  opacity: 0.18;
  background:
    linear-gradient(105deg, transparent 0 48%, currentColor 49% 51%, transparent 52%),
    radial-gradient(ellipse at 70% 22%, currentColor 0 12%, transparent 13%);
  transform: rotate(9deg);
}

.almanac-advice span {
  width: 46px;
  height: 46px;
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

.almanac-advice .good {
  color: #2f6f5e;
  background: linear-gradient(90deg, rgba(47, 111, 94, 0.13), rgba(255, 253, 246, 0.8));
}

.almanac-advice .avoid span {
  background: #a64e3d;
}

.almanac-advice .avoid {
  color: #a64e3d;
  background: linear-gradient(90deg, rgba(166, 78, 61, 0.12), rgba(255, 245, 236, 0.8));
}

.almanac-advice strong {
  color: #173f35;
  font-size: 17px;
  line-height: 1.5;
  word-break: break-word;
}

.almanac-expanded {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed rgba(176, 138, 60, 0.32);
}

.expanded-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.expanded-grid div,
.month-calendar {
  border: 1px solid rgba(176, 138, 60, 0.24);
  border-radius: 8px;
  background: rgba(255, 253, 246, 0.62);
  padding: 10px 12px;
}

.expanded-grid span {
  display: block;
  color: #806326;
  font-size: 12px;
  margin-bottom: 5px;
}

.expanded-grid strong {
  display: block;
  color: #173f35;
  font-size: 14px;
  line-height: 1.55;
  word-break: break-word;
}

.almanac-status {
  margin-top: 10px;
  border-radius: 8px;
  background: rgba(128, 99, 38, 0.08);
  color: #806326;
  font-size: 13px;
  line-height: 1.5;
  padding: 8px 10px;
}

.month-calendar {
  margin-top: 10px;
}

.calendar-head {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) 36px 36px;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
}

.calendar-head strong {
  color: #173f35;
  font-size: 16px;
  font-weight: 800;
  text-align: center;
}

.calendar-head button {
  height: 34px;
  border: 1px solid rgba(128, 99, 38, 0.24);
  border-radius: 8px;
  background: #fffdf6;
  color: #806326;
  font-size: 18px;
  font-weight: 800;
}

.calendar-head .today-link {
  font-size: 13px;
}

.weekday-row,
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 6px;
}

.weekday-row {
  margin-bottom: 6px;
}

.weekday-row span {
  color: #806326;
  font-size: 12px;
  text-align: center;
}

.calendar-grid button {
  min-height: 72px;
  border: 1px solid rgba(176, 138, 60, 0.18);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.62);
  color: #173f35;
  display: grid;
  align-content: start;
  gap: 3px;
  padding: 7px 4px;
  text-align: center;
}

.calendar-grid button.muted {
  opacity: 0.38;
}

.calendar-grid button.today {
  border-color: #2f6f5e;
}

.calendar-grid button.selected {
  color: #fff;
  border-color: #173f35;
  background: linear-gradient(180deg, #2f6f5e, #173f35);
}

.calendar-grid button span {
  font-size: 14px;
  font-weight: 800;
}

.calendar-grid button strong {
  font-size: 12px;
  line-height: 1.2;
}

.calendar-grid button small {
  color: inherit;
  opacity: 0.72;
  font-size: 11px;
  line-height: 1.2;
}

.entry-grid {
  max-width: 1480px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin: 16px auto;
}

.feature-card {
  position: relative;
  min-height: 0;
  aspect-ratio: 2.62 / 1;
  border: 1px solid rgba(232, 214, 165, 0.36);
  border-radius: 10px;
  text-align: left;
  padding: 0;
  color: #f4e6b8;
  overflow: hidden;
  isolation: isolate;
  box-shadow: 0 16px 28px rgba(19, 37, 32, 0.18);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 34px rgba(19, 37, 32, 0.22);
}

.feature-card::before,
.feature-card::after {
  content: "";
  position: absolute;
  pointer-events: none;
  z-index: -1;
}

.feature-card::before {
  display: none;
}

.feature-card::after {
  inset: 1px;
  border-radius: 9px;
  box-shadow: inset 0 0 0 1px rgba(255, 240, 192, 0.18);
}

.feature-bazi {
  background: url("/images/home-ui/card-bazi.jpg") center / cover no-repeat;
}

.feature-bazi::before {
  background:
    linear-gradient(120deg, transparent 0 58%, rgba(151, 193, 159, 0.22) 58% 59%, transparent 60%),
    radial-gradient(ellipse at 80% 18%, rgba(178, 214, 178, 0.18), transparent 30%),
    repeating-linear-gradient(150deg, transparent 0 28px, rgba(229, 213, 157, 0.09) 29px 31px),
    linear-gradient(22deg, transparent 58%, rgba(198, 218, 181, 0.12) 59% 63%, transparent 64%);
}

.feature-liuyao {
  background: url("/images/home-ui/card-liuyao.jpg") center / cover no-repeat;
}

.feature-liuyao::before {
  background:
    radial-gradient(ellipse at 86% 42%, rgba(229, 200, 130, 0.18), transparent 22%),
    radial-gradient(circle at 78% 76%, rgba(188, 137, 56, 0.2), transparent 16%),
    repeating-linear-gradient(170deg, transparent 0 30px, rgba(229, 213, 157, 0.08) 31px 33px);
}

.feature-ziwei {
  background: url("/images/home-ui/card-ziwei.jpg") center / cover no-repeat;
}

.feature-ziwei::before {
  background:
    radial-gradient(circle at 17% 28%, rgba(247, 221, 149, 0.18) 0 1px, transparent 2px),
    radial-gradient(circle at 70% 22%, rgba(247, 221, 149, 0.24) 0 1px, transparent 2px),
    radial-gradient(circle at 82% 70%, rgba(247, 221, 149, 0.22) 0 1px, transparent 2px),
    radial-gradient(ellipse at 88% 48%, rgba(91, 84, 166, 0.28), transparent 30%),
    repeating-radial-gradient(circle at 82% 48%, transparent 0 22px, rgba(229, 213, 157, 0.13) 23px 24px);
}

.feature-emblem {
  position: absolute;
  left: 24px;
  top: 50%;
  width: 86px;
  height: 86px;
  transform: translateY(-50%);
  border: 2px solid rgba(232, 214, 165, 0.78);
  border-radius: 50%;
  color: #f4e6b8;
  display: grid;
  place-items: center;
  box-shadow: inset 0 0 0 2px rgba(232, 214, 165, 0.14);
}

.feature-card .feature-emblem,
.feature-card .feature-copy,
.feature-card > i {
  opacity: 0;
  pointer-events: none;
}

.bazi-emblem {
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: repeat(3, 1fr);
  gap: 0;
  padding: 12px;
  border-radius: 50%;
}

.bazi-emblem span,
.bazi-emblem strong {
  min-width: 0;
  display: grid;
  place-items: center;
  border-right: 1px solid rgba(232, 214, 165, 0.38);
  border-bottom: 1px solid rgba(232, 214, 165, 0.38);
  font-size: 12px;
  line-height: 1;
  font-weight: 700;
}

.bazi-emblem :nth-child(4n) {
  border-right: 0;
}

.bazi-emblem :nth-last-child(-n + 4) {
  border-bottom: 0;
}

.hexagram-emblem {
  align-content: center;
  gap: 6px;
  padding: 20px 18px;
}

.hexagram-emblem b {
  width: 46px;
  height: 5px;
  border-radius: 99px;
  background: linear-gradient(90deg, #d7b66e, #fff1bb, #b2873e);
}

.hexagram-emblem b:nth-child(2),
.hexagram-emblem b:nth-child(5) {
  background:
    linear-gradient(90deg, #d7b66e 0 39%, transparent 40% 60%, #b2873e 61% 100%);
}

.astrolabe-emblem {
  background:
    radial-gradient(circle, #f4e6b8 0 4px, transparent 5px),
    repeating-radial-gradient(circle, transparent 0 16px, rgba(232, 214, 165, 0.78) 17px 18px),
    conic-gradient(from 0deg, transparent 0 23deg, rgba(232, 214, 165, 0.58) 24deg 25deg, transparent 26deg 60deg);
}

.astrolabe-emblem b {
  position: absolute;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #f4e6b8;
}

.astrolabe-emblem b:nth-child(1) {
  left: 26px;
  top: 28px;
}

.astrolabe-emblem b:nth-child(2) {
  right: 22px;
  top: 42px;
}

.astrolabe-emblem b:nth-child(3) {
  left: 42px;
  bottom: 23px;
}

.feature-copy,
.feature-copy strong,
.feature-copy small,
.feature-copy em {
  display: block;
}

.feature-copy {
  position: relative;
  z-index: 1;
}

.feature-copy strong {
  font-size: 30px;
  line-height: 1.1;
  letter-spacing: 0;
  color: #f4d791;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.24);
}

.feature-copy small {
  margin-top: 12px;
  color: rgba(255, 248, 221, 0.92);
  font-size: 15px;
  line-height: 1.35;
}

.feature-copy em {
  position: relative;
  margin-top: 22px;
  color: rgba(244, 215, 145, 0.9);
  font-size: 14px;
  font-style: normal;
  line-height: 1.4;
}

.feature-copy em::before {
  content: "";
  position: absolute;
  top: -12px;
  left: 0;
  width: min(170px, 80%);
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(232, 214, 165, 0.54), transparent);
}

.feature-card i {
  position: absolute;
  right: 20px;
  bottom: 20px;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: radial-gradient(circle at 34% 28%, #fff1bb, #d7b66e 58%, #a9782d);
  color: #0f2f29;
  font-style: normal;
  font-size: 22px;
  font-weight: 900;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.24);
}

.home-lower-grid {
  max-width: 1480px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(320px, 0.7fr);
  gap: 16px;
}

.advice-card,
.recent-card {
  padding: 20px;
}

.advice-card .card-title button,
.recent-card .card-title button {
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

.support-strip {
  width: 100%;
  margin-top: 14px;
  border: 1px solid rgba(176, 138, 60, 0.28);
  border-radius: 8px;
  background: rgba(47, 111, 94, 0.07);
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  color: #173f35;
  text-align: left;
}

.recent-empty {
  min-height: 168px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 8px;
  color: #806326;
  text-align: center;
}

.recent-empty span {
  width: 62px;
  height: 62px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: rgba(214, 169, 84, 0.14);
  color: rgba(128, 99, 38, 0.38);
  font-size: 28px;
  font-weight: 900;
}

.recent-empty strong {
  color: #4c463a;
  font-size: 16px;
}

.recent-empty p {
  margin: 0;
  color: #7b7466;
  font-size: 13px;
}

.support-strip span {
  color: #806326;
  font-weight: 800;
}

.support-strip strong {
  font-size: 14px;
  font-weight: 600;
  line-height: 1.45;
}

.support-strip i {
  font-style: normal;
  color: #806326;
  font-size: 20px;
}

@media (max-width: 980px) {
  .entry-grid,
  .home-lower-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 700px) {
  .home-page {
    min-height: 100dvh;
    padding: 0 14px 92px;
    background:
      linear-gradient(180deg, rgba(0, 62, 54, 0.97) 0 460px, #f8f1e2 461px),
      radial-gradient(circle at 86px 120px, rgba(232, 214, 165, 0.1), transparent 26%);
  }

  .home-hero {
    min-height: 346px;
    padding: 78px 20px 86px;
    align-items: flex-start;
  }

  .home-hero::before {
    top: 0;
    background-position: 55% top;
  }

  .hero-copy h1 {
    font-size: 58px;
  }

  .hero-copy p {
    font-size: 19px;
  }

  .hero-copy small {
    font-size: 14px;
    line-height: 1.5;
  }

  .wisdom-button {
    width: 48px;
    height: 48px;
    font-size: 10px;
  }

  .today-card {
    margin-top: -74px;
    padding: 18px;
    border-radius: 12px;
  }

  .almanac-layout,
  .expanded-grid,
  .entry-grid {
    grid-template-columns: 1fr;
  }

  .card-title {
    flex-wrap: wrap;
  }

  .card-title small {
    order: 4;
    width: 100%;
    margin-left: 44px;
  }

  .lunar-pill {
    margin-left: 44px;
    padding: 5px 10px;
    font-size: 12px;
  }

  .expand-button {
    margin-left: auto;
  }

  .almanac-date strong {
    font-size: 18px;
  }

  .almanac-pillars strong {
    font-size: 28px;
  }

  .almanac-pillars div {
    min-height: 106px;
  }

  .almanac-advice div {
    min-height: 84px;
    grid-template-columns: 48px minmax(0, 1fr);
    padding: 13px;
  }

  .month-calendar {
    padding: 8px;
  }

  .calendar-grid {
    gap: 4px;
  }

  .calendar-grid button {
    min-height: 64px;
    padding: 6px 2px;
  }

  .calendar-grid button strong,
  .calendar-grid button small {
    font-size: 10px;
  }

  .feature-card {
    min-height: 0;
    aspect-ratio: 2.62 / 1;
    padding: 0;
    border-radius: 8px;
  }

  .feature-emblem {
    left: 18px;
    width: 76px;
    height: 76px;
  }

  .bazi-emblem {
    padding: 10px;
  }

  .bazi-emblem span,
  .bazi-emblem strong {
    font-size: 11px;
  }

  .feature-copy strong {
    font-size: 26px;
  }

  .feature-copy small {
    margin-top: 10px;
    font-size: 14px;
  }

  .feature-copy em {
    display: inline-block;
    margin-top: 12px;
    padding: 3px 10px;
    border: 1px solid rgba(232, 214, 165, 0.24);
    border-radius: 999px;
    font-size: 13px;
  }

  .feature-copy em::before {
    display: none;
  }

  .feature-card i {
    right: 18px;
    bottom: 18px;
    width: 38px;
    height: 38px;
  }

  .home-lower-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .advice-card,
  .recent-card {
    padding: 16px;
    border-radius: 12px;
  }
}
</style>
