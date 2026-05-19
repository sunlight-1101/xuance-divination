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
      <button class="support-strip" type="button" @click="$router.push('/quota')">
        <span>支持开发</span>
        <strong>制作不易，觉得好用可以打赏；建议反馈 QQ 1559087584</strong>
        <i>›</i>
      </button>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { CalendarDays, ScrollText, Sparkles } from 'lucide-vue-next'
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

.advice-card .card-title button {
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

  .expand-button {
    margin-left: auto;
  }

  .almanac-date strong {
    font-size: 18px;
  }

  .almanac-pillars strong {
    font-size: 22px;
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
    min-height: 138px;
  }
}
</style>
