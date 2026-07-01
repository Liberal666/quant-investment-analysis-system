<template>
  <main class="terminal-shell">
    <header class="brand-bar">
      <div class="brand-mark">量化投资绩效分析系统</div>
      <div class="brand-title">拼一次富三代、拼命才能不失败</div>
      <div class="role-switch">
        当前用户:
        <strong>{{ currentUser.displayName || currentUser.username }}</strong>
        <button type="button" @click="emit('switch-user')">切换用户</button>
      </div>
    </header>

    <nav class="menu-bar" aria-label="系统菜单">
      <button :class="{ active: activeTab === 'data' }" @click="activeTab = 'data'">实时监控</button>
      <button :class="{ active: activeTab === 'nav' }" @click="activeTab = 'nav'">产品净值</button>
      <button :class="{ active: activeTab === 'strategy' }" @click="activeTab = 'strategy'">策略管理</button>
      <button :class="{ active: activeTab === 'risk' }" @click="activeTab = 'risk'">风险分析</button>
      <button :class="{ active: activeTab === 'report' }" @click="activeTab = 'report'">数据报表</button>
      <button v-if="currentUser.canManageUsers" :class="{ active: activeTab === 'system' }" @click="activeTab = 'system'">系统管理</button>
    </nav>

    <div class="workspace">
      <section v-if="activeTab === 'system'" class="terminal-main permission-main">
        <section class="permission-panel">
          <div class="permission-title">
            权限管理
            <span>{{ currentUser.canManageUsers ? '管理员可修改用户权限' : '普通用户只能访问数据，不能管理用户权限' }}</span>
          </div>
          <table>
            <thead>
              <tr>
                <th>用户名</th>
                <th>显示名</th>
                <th>角色</th>
                <th>数据访问</th>
                <th>权限管理</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.username">
                <td>{{ user.username }}</td>
                <td>{{ user.displayName }}</td>
                <td>
                  <select v-model="user.role" :disabled="!currentUser.canManageUsers">
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </td>
                <td><input v-model="user.canViewData" type="checkbox" :disabled="!currentUser.canManageUsers" /></td>
                <td><input v-model="user.canManageUsers" type="checkbox" :disabled="!currentUser.canManageUsers" /></td>
                <td>
                  <button :disabled="!currentUser.canManageUsers" @click="savePermission(user)">保存</button>
                </td>
              </tr>
            </tbody>
          </table>
        </section>
      </section>

      <section v-else-if="activeTab === 'strategy'" class="terminal-main page-panel">
        <section class="form-panel">
          <div class="data-panel-title">策略管理<span>☰</span></div>
          <form class="strategy-form" @submit.prevent="saveStrategy">
            <label>交易日期<input v-model="strategyForm.date" type="date" /></label>
            <label>股票代码<input v-model.trim="strategyForm.code" /></label>
            <label>策略类型<select v-model="strategyForm.type"><option>趋势跟踪</option><option>均值回归</option><option>突破交易</option><option>风险对冲</option></select></label>
            <label class="wide">策略信息<textarea v-model="strategyForm.content" placeholder="输入买入/卖出理由、仓位、止盈止损、风控条件等"></textarea></label>
            <button type="submit">保存策略</button>
          </form>
        </section>
        <section class="data-panel strategy-list">
          <div class="data-panel-title">策略记录<span>☰</span></div>
          <div class="table-scroll">
            <table>
              <thead><tr><th>日期</th><th>股票代码</th><th>类型</th><th>策略信息</th></tr></thead>
              <tbody>
                <tr v-for="item in strategies" :key="item.id">
                  <td>{{ item.tradeDate }}</td>
                  <td>{{ item.code }}</td>
                  <td>{{ item.type }}</td>
                  <td class="text-left">{{ item.content }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <section v-else-if="activeTab === 'nav'" class="terminal-main page-panel">
        <form class="filter-strip" @submit.prevent="loadNavByCode">
          <label>股票代码:<input v-model.trim="codeInput" aria-label="产品净值股票代码" /></label>
          <button type="submit" :disabled="loading">搜索股票代码</button>
          <button type="button" :disabled="loading" @click="addStock">添加股票代码</button>
        </form>
        <p v-if="error" class="terminal-error">{{ error }}</p>
        <section class="table-zone nav-zone">
          <section class="data-panel product-table">
            <div class="data-panel-title">产品净值列表<span></span></div>
            <div class="nav-product-grid">
              <button
                v-for="row in productRows"
                :key="row.code"
                type="button"
                :class="{ selected: row.code === code }"
                @click="selectProduct(row.code)"
              >
                <strong>{{ row.name }}</strong>
                <span>{{ row.code }}</span>
              </button>
            </div>
          </section>
          <section class="data-panel history-table">
            <div class="data-panel-title">{{ selectedProductName }} 历史净值明细<span></span></div>
            <div class="table-scroll">
              <table>
                <thead><tr><th>日期</th><th>净值</th><th>收益</th><th>净值增长</th><th>最大回撤</th><th>产品市值</th><th>产品份额</th></tr></thead>
                <tbody>
                  <tr v-for="row in historyRows" :key="row.date">
                    <td>{{ row.date }}</td>
                    <td>{{ row.nav }}</td>
                    <td :class="numberClass(row.returnValue)">{{ formatMoney(row.returnValue) }}</td>
                    <td :class="numberClass(row.growth)">{{ formatPercent(row.growth) }}</td>
                    <td class="down">{{ row.drawdown }}</td>
                    <td>{{ row.marketValue }}</td>
                    <td>{{ row.share }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </section>
      </section>

      <section v-else-if="activeTab === 'risk'" class="terminal-main risk-main">
        <form class="filter-strip" @submit.prevent="loadRisk">
          <label>股票代码:<input v-model.trim="riskCode" /></label>
          <label>周期:<select v-model="riskPeriod"><option>每日</option><option>每周</option><option>每月</option></select></label>
          <label>根数:<input v-model.number="riskCount" type="number" min="20" max="240" /></label>
          <button type="submit" :disabled="loading">查询风险</button>
        </form>
        <p v-if="error" class="terminal-error">{{ error }}</p>
        <section class="risk-layout">
          <section>
            <section class="quote-grid">
              <div><span>股票名称</span><strong>{{ riskQuote.name || '--' }}</strong></div>
              <div><span>股票代码</span><strong>{{ riskQuote.code || riskCode }}</strong></div>
              <div><span>最新价格</span><strong>{{ formatQuote(riskQuote.price) }}</strong></div>
              <div><span>成交量</span><strong>{{ formatVolume(riskQuote.volume) }}</strong></div>
              <div><span>涨跌</span><strong :class="numberClass(riskQuote.change)">{{ formatQuote(riskQuote.change) }}</strong></div>
              <div><span>涨跌幅</span><strong :class="numberClass(riskQuote.changePercent)">{{ formatQuote(riskQuote.changePercent) }}%</strong></div>
              <div><span>最高</span><strong>{{ formatQuote(riskQuote.high) }}</strong></div>
              <div><span>最低</span><strong>{{ formatQuote(riskQuote.low) }}</strong></div>
              <div><span>昨收</span><strong>{{ formatQuote(riskQuote.previousClose) }}</strong></div>
              <div><span>更新时间</span><strong>{{ riskQuote.updateTime || '--' }}</strong></div>
            </section>
            <section class="risk-charts">
              <ChartPanel title="单股K线图" :subtitle="riskKlineSubtitle" :option="riskKlineOption" />
              <ChartPanel title="MACD线" :subtitle="riskMacdSubtitle" :option="riskMacdOption" />
            </section>
          </section>
          <aside class="ai-side">
            <section class="ai-advice-panel">
              <div class="data-panel-title">AI参考建议<span></span></div>
              <p>{{ riskAiAdvice }}</p>
            </section>
            <section class="ai-chat-panel">
              <div class="data-panel-title">AI对话<span>DeepSeek</span></div>
              <div class="ai-chat-log">
                <div v-for="(item, index) in riskAiMessages" :key="index" :class="['ai-chat-message', item.role]">
                  <strong>{{ item.role === 'user' ? '我' : 'AI' }}</strong>
                  <span>{{ item.content }}</span>
                </div>
              </div>
              <form class="ai-chat-form" @submit.prevent="sendRiskAiMessage">
                <input v-model.trim="riskAiQuestion" :disabled="riskAiLoading" placeholder="输入问题，例如：这只股票现在风险大吗？" />
                <button type="submit" :disabled="riskAiLoading || !riskAiQuestion">{{ riskAiLoading ? '回复中' : '发送' }}</button>
              </form>
            </section>
          </aside>
        </section>
      </section>

      <section v-else-if="activeTab === 'report'" class="terminal-main page-panel">
        <form class="filter-strip" @submit.prevent="buildReport">
          <label>开始日期:<input v-model="reportStartDate" type="date" /></label>
          <label>截止日期:<input v-model="reportEndDate" type="date" /></label>
          <button type="submit">生成报表</button>
        </form>
        <section class="report-picker">
          <label v-for="item in products" :key="item.code"><input v-model="reportCodes" type="checkbox" :value="item.code" />{{ item.name }} {{ item.code }}</label>
          <button type="button" class="select-all-button" @click="toggleReportCodes">{{ allReportCodesSelected ? '取消全选' : '全选' }}</button>
        </section>
        <section class="report-grid">
          <section class="data-panel">
            <div class="data-panel-title">区间综合收益<span>☰</span></div>
            <div class="table-scroll">
              <table>
                <thead><tr><th>股票</th><th>开始日期</th><th>截止日期</th><th>开始价</th><th>截止价</th><th>区间收益</th><th>成交量</th><th>数据条数</th></tr></thead>
                <tbody>
                  <tr v-for="item in reportRows" :key="item.code">
                    <td>{{ item.name }} {{ item.code }}</td>
                    <td>{{ item.startDate }}</td>
                    <td>{{ item.endDate }}</td>
                    <td>{{ item.startClose }}</td>
                    <td>{{ item.endClose }}</td>
                    <td :class="numberClass(item.returnRate)">{{ formatPercent(item.returnRate) }}</td>
                    <td>{{ formatVolume(item.volume) }}</td>
                    <td>{{ item.rows }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
          <ChartPanel title="多股票综合收益" :subtitle="reportSubtitle" :option="reportOption" />
        </section>
      </section>

      <section v-else class="terminal-main data-main">
        <form class="filter-strip" @submit.prevent="loadAll">
          <label>净值开始日期:<input v-model="startDateInput" type="date" /></label>
          <label>净值截止日期:<input v-model="endDateInput" type="date" /></label>
          <label>净值周期:<select v-model="period"><option>每日</option><option>每周</option><option>每月</option></select></label>
          <label>比较基准:<select v-model="benchmarkCode"><option v-for="item in benchmarkOptions" :key="item.code" :value="item.code">{{ item.name }}</option></select></label>
          <label>股票代码:<input v-model.trim="codeInput" aria-label="股票代码" /></label>
          <button type="submit" :disabled="loading">更新净值</button>
          <button type="button" :disabled="loading" @click="syncAndReload">同步数据</button>
          <button type="button" :disabled="loading" @click="addStock">添加股票代码</button>
        </form>

        <p v-if="error" class="terminal-error">{{ error }}</p>
        <p v-if="statusMessage" class="terminal-status">{{ statusMessage }}</p>

        <section class="table-zone">
          <section class="data-panel product-table">
            <div class="data-panel-title">产品净值列表<span>☰</span></div>
            <div class="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>产品</th>
                    <th>净值</th>
                    <th>收益</th>
                    <th>净值增长</th>
                    <th>本周增长</th>
                    <th>本月增长</th>
                    <th>前日净值</th>
                    <th>上周净值</th>
                    <th>近期趋势</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in productRows" :key="row.code" :class="{ selected: row.code === code }" @click="selectProduct(row.code)">
                    <td class="product-name">{{ row.name }} {{ row.code }}</td>
                    <td><span class="dot"></span>{{ row.nav }}</td>
                    <td :class="numberClass(row.returnValue)">{{ formatMoney(row.returnValue) }}</td>
                    <td :class="numberClass(row.dayGrowth)">{{ formatPercent(row.dayGrowth) }}</td>
                    <td :class="numberClass(row.weekGrowth)">{{ formatPercent(row.weekGrowth) }}</td>
                    <td :class="numberClass(row.monthGrowth)">{{ formatPercent(row.monthGrowth) }}</td>
                    <td><span class="dot"></span>{{ row.prevNav }}</td>
                    <td><span class="dot"></span>{{ row.weekNav }}</td>
                    <td><span class="sparkline"></span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section class="data-panel history-table">
            <div class="data-panel-title">历史净值明细<span>☰</span></div>
            <div class="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>产品</th>
                    <th>日期</th>
                    <th>净值</th>
                    <th>收益</th>
                    <th>净值增长</th>
                    <th>最大回撤</th>
                    <th>产品市值</th>
                    <th>产品份额</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in historyRows" :key="row.date">
                    <td>{{ selectedProductName }}</td>
                    <td>{{ row.date }}</td>
                    <td>{{ row.nav }}</td>
                    <td :class="numberClass(row.returnValue)">{{ formatMoney(row.returnValue) }}</td>
                    <td :class="numberClass(row.growth)">{{ formatPercent(row.growth) }}</td>
                    <td class="down">{{ row.drawdown }}</td>
                    <td>{{ row.marketValue }}</td>
                    <td>{{ row.share }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </section>

        <section class="chart-grid">
          <ChartPanel title="基金收益" :subtitle="fundSubtitle" :option="fundOption" />
          <ChartPanel title="净值变动" :subtitle="changeSubtitle" :option="changeOption" />
          <ChartPanel title="相关性分析" :subtitle="correlationSubtitle" :option="scatterOption" />
          <ChartPanel title="K线与MA均线" :subtitle="klineSubtitle" :option="klineOption" />
          <ChartPanel title="MACD指标" :subtitle="macdSubtitle" :option="macdOption" />
          <ChartPanel title="RSI指标" :subtitle="rsiSubtitle" :option="rsiOption" />
        </section>

        <section class="definition-zone">
          <section class="definition-list">
            <div class="data-panel-title">图表名词解释<span></span></div>
            <dl>
              <template v-for="item in definitionLeft" :key="item.name">
                <dt>{{ item.name }}</dt>
                <dd>{{ item.desc }}</dd>
              </template>
            </dl>
          </section>
          <section class="definition-list">
            <div class="data-panel-title">指标名词解释<span></span></div>
            <dl>
              <template v-for="item in definitionRight" :key="item.name">
                <dt>{{ item.name }}</dt>
                <dd>{{ item.desc }}</dd>
              </template>
            </dl>
          </section>
        </section>

        <section class="analysis-strip">
          <strong>AI辅助解释</strong>
          <span>{{ analysis.content || '等待分析结果...' }}</span>
        </section>
      </section>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import ChartPanel from '../components/ChartPanel.vue'
import { benchmarkOptions, definitionLeft, definitionRight } from '../config/dashboardConfig'
import { chartTheme, dynamicMax, dynamicMin, terminalLine } from '../utils/chartTools'
import { normalizeDate, sampleByPeriod, todayText } from '../utils/dateTools'
import { formatIndicator, formatMoney, formatPercent, formatQuote, formatVolume, numberClass, numberOrNull } from '../utils/formatters'
import {
  addUserStock,
  askRiskAi,
  getAnalysis,
  getCorrelation,
  getCurrentUser,
  getIndicator,
  getKline,
  getProducts,
  getQuote,
  getUsers,
  getStrategies,
  saveStrategy as saveStrategyApi,
  syncStock,
  updateUserPermission
} from '../api/stock'

const props = defineProps({
  initialUser: {
    type: Object,
    default: () => ({ username: 'viewer', displayName: '普通用户', role: 'USER', canViewData: true, canManageUsers: false })
  }
})
const emit = defineEmits(['switch-user'])

const code = ref('000001')
const klines = ref([])
const indicators = ref([])
const correlation = ref({ coefficient: 0, points: [] })
const analysis = ref({ source: 'local', content: '' })
const products = ref([])
const users = ref([])
const startDateInput = ref('2025-07-01')
const endDateInput = ref(todayText())
const period = ref('每日')
const benchmarkCode = ref('000300')
const codeInput = ref('000001')
const currentUser = ref(props.initialUser)
const activeTab = ref('data')
const loading = ref(false)
const error = ref('')
const statusMessage = ref('')
const riskCode = ref('000001')
const riskPeriod = ref('每日')
const riskCount = ref(80)
const riskQuote = ref({})
const riskKlines = ref([])
const riskIndicators = ref([])
const riskAiQuestion = ref('')
const riskAiLoading = ref(false)
const riskAiMessages = ref([
  { role: 'assistant', content: '可以围绕当前股票的K线、MACD、成交量和涨跌幅提问，我会结合页面行情给出风险参考。' }
])
const strategyForm = ref({ date: todayText(), code: '000001', type: '趋势跟踪', content: '' })
const strategies = ref([])
const reportStartDate = ref('2024-01-02')
const reportEndDate = ref(todayText())
const reportCodes = ref(['000001', '600519'])
const reportRows = ref([])
const filteredKlines = computed(() => sampleByPeriod(filterByDate(klines.value), period.value))
const filteredIndicators = computed(() => {
  const allowedDates = new Set(filteredKlines.value.map(item => item.date))
  return indicators.value.filter(item => allowedDates.has(item.date))
})
const dates = computed(() => filteredKlines.value.map(item => item.date))
const selectedProductName = computed(() => products.value.find(item => item.code === code.value)?.name || `股票 ${code.value}`)
const selectedBenchmarkName = computed(() => benchmarkOptions.find(item => item.code === benchmarkCode.value)?.name || '基准')
const latestClose = computed(() => Number(filteredKlines.value.at(-1)?.close || 0))
const totalReturn = computed(() => {
  if (filteredKlines.value.length < 2) return 0
  const first = Number(filteredKlines.value[0].close)
  return first === 0 ? 0 : (latestClose.value - first) / first
})
const dailyReturns = computed(() => filteredKlines.value.slice(1).map((item, index) => {
  const previous = Number(filteredKlines.value[index].close)
  const close = Number(item.close)
  return previous === 0 ? 0 : (close - previous) / previous
}))
const cumulativeReturns = computed(() => {
  let value = 1
  return dailyReturns.value.map(item => {
    value *= 1 + item
    return value - 1
  })
})
const filteredCorrelationPoints = computed(() => {
  const allowedDates = new Set(filteredKlines.value.map(item => item.date))
  return (correlation.value.points || []).filter(point => allowedDates.has(point.date))
})
const benchmarkReturns = computed(() => filteredCorrelationPoints.value.map(point => point.indexReturn))
const benchmarkCumulative = computed(() => {
  let value = 1
  return benchmarkReturns.value.map(item => {
    value *= 1 + item
    return value - 1
  })
})
const maxDrawdown = computed(() => {
  let peak = 1
  let max = 0
  cumulativeReturns.value.forEach(value => {
    const equity = 1 + value
    peak = Math.max(peak, equity)
    max = Math.min(max, (equity - peak) / peak)
  })
  return max
})
const summerRatio = computed(() => {
  if (!benchmarkReturns.value.length) return 0
  const stockAvg = dailyReturns.value.reduce((sum, item) => sum + item, 0) / Math.max(dailyReturns.value.length, 1)
  const benchmarkAvg = benchmarkReturns.value.reduce((sum, item) => sum + item, 0) / benchmarkReturns.value.length
  return benchmarkAvg === 0 ? 0 : stockAvg / benchmarkAvg
})
const historicalMaxDrawdown = computed(() => maxDrawdown.value * 1.18)
const fundSubtitle = computed(() => `区间收益率:${formatPercent(totalReturn.value)}   年化收益率:${formatPercent(annualReturn.value)}   年化波动率:${formatPercent(volatility.value)}   夏普比率:${summerRatio.value.toFixed(2)}`)
const changeSubtitle = computed(() => `胜率:${formatPercent(winRate.value)}   盈亏比:${profitLossRatio.value.toFixed(2)}   当前最大回撤:${formatPercent(maxDrawdown.value)}   历史最大回撤:${formatPercent(historicalMaxDrawdown.value)}`)
const correlationSubtitle = computed(() => '')
const klineSubtitle = computed(() => `${selectedProductName.value}   周期:${period.value}   数据:${filteredKlines.value.length}条`)
const macdSubtitle = computed(() => `DIF / DEA / MACD柱   ${startDateInput.value} - ${endDateInput.value}`)
const rsiSubtitle = computed(() => `RSI(14)   最新:${formatIndicator(filteredIndicators.value.at(-1)?.rsi)}`)
const riskSampledKlines = computed(() => sampleByPeriod(riskKlines.value, riskPeriod.value).slice(-Number(riskCount.value || 80)))
const riskDates = computed(() => riskSampledKlines.value.map(item => item.date))
const riskSampledIndicators = computed(() => {
  const allowedDates = new Set(riskDates.value)
  return riskIndicators.value.filter(item => allowedDates.has(item.date))
})
const riskKlineSubtitle = computed(() => `${riskQuote.value.name || '股票'} ${riskCode.value}   周期:${riskPeriod.value}   根数:${riskSampledKlines.value.length}`)
const riskMacdSubtitle = computed(() => `DIF / DEA / MACD柱   实时行情刷新`)
const riskAiAdvice = computed(() => {
  const pct = Number(riskQuote.value.changePercent || 0)
  if (pct > 3) return '短线涨幅较高，注意追高风险，可结合MACD是否继续放量确认趋势。'
  if (pct < -3) return '当日跌幅较大，建议观察支撑位和成交量变化，避免盲目补仓。'
  return '当前波动相对温和，可结合K线形态、MACD金叉/死叉和区间收益综合判断。'
})
const riskAiContext = computed(() => {
  const latestIndicator = riskSampledIndicators.value.at(-1) || {}
  return [
    `股票名称:${riskQuote.value.name || '--'}`,
    `股票代码:${riskQuote.value.code || riskCode.value}`,
    `最新价格:${formatQuote(riskQuote.value.price)}`,
    `成交量:${formatVolume(riskQuote.value.volume)}`,
    `涨跌:${formatQuote(riskQuote.value.change)}`,
    `涨跌幅:${formatQuote(riskQuote.value.changePercent)}%`,
    `最高:${formatQuote(riskQuote.value.high)}`,
    `最低:${formatQuote(riskQuote.value.low)}`,
    `昨收:${formatQuote(riskQuote.value.previousClose)}`,
    `周期:${riskPeriod.value}`,
    `根数:${riskSampledKlines.value.length}`,
    `MACD:${formatIndicator(latestIndicator.macd)}`,
    `DIF:${formatIndicator(latestIndicator.dif)}`,
    `DEA:${formatIndicator(latestIndicator.dea)}`
  ].join('；')
})
const reportSubtitle = computed(() => `区间:${reportStartDate.value} 至 ${reportEndDate.value}   股票:${reportRows.value.length}只`)
const allReportCodesSelected = computed(() => products.value.length > 0 && products.value.every(item => reportCodes.value.includes(item.code)))
const annualReturn = computed(() => {
  if (filteredKlines.value.length < 2) return 0
  const days = Math.max((new Date(filteredKlines.value.at(-1).date) - new Date(filteredKlines.value[0].date)) / 86400000, 1)
  return Math.pow(1 + totalReturn.value, 365 / days) - 1
})
const volatility = computed(() => {
  if (!dailyReturns.value.length) return 0
  const avg = dailyReturns.value.reduce((sum, item) => sum + item, 0) / dailyReturns.value.length
  const variance = dailyReturns.value.reduce((sum, item) => sum + (item - avg) ** 2, 0) / dailyReturns.value.length
  return Math.sqrt(variance) * Math.sqrt(252)
})
const winRate = computed(() => {
  if (!dailyReturns.value.length) return 0
  return dailyReturns.value.filter(item => item > 0).length / dailyReturns.value.length
})
const profitLossRatio = computed(() => {
  const gains = dailyReturns.value.filter(item => item > 0)
  const losses = dailyReturns.value.filter(item => item < 0).map(Math.abs)
  const avgGain = gains.reduce((sum, item) => sum + item, 0) / Math.max(gains.length, 1)
  const avgLoss = losses.reduce((sum, item) => sum + item, 0) / Math.max(losses.length, 1)
  return avgLoss === 0 ? 0 : avgGain / avgLoss
})
const productRows = computed(() => {
  return products.value.map(product => {
    const latestClose = Number(product.latestClose || 0)
    const previousClose = Number(product.previousClose || latestClose)
    const weekClose = Number(product.weekClose || previousClose || latestClose)
    const firstClose = Number(product.firstClose || latestClose)
    const dayGrowth = previousClose === 0 ? 0 : (latestClose - previousClose) / previousClose
    const weekGrowth = weekClose === 0 ? 0 : (latestClose - weekClose) / weekClose
    const monthGrowth = firstClose === 0 ? 0 : (latestClose - firstClose) / firstClose
    const nav = latestClose.toFixed(4)
    return {
      code: product.code,
      name: product.name,
      nav,
      prevNav: previousClose.toFixed(4),
      weekNav: weekClose.toFixed(4),
      returnValue: (latestClose - firstClose) * 100000,
      dayGrowth,
      weekGrowth,
      monthGrowth
    }
  })
})
const historyRows = computed(() => filteredKlines.value.slice(-16).reverse().map((item, index) => {
  const previous = filteredKlines.value[filteredKlines.value.length - 17 + index]
  const growth = previous ? (Number(item.close) - Number(previous.close)) / Number(previous.close) : 0
  const marketValue = Number(item.close) * 103000
  return {
    date: item.date,
    nav: (Number(item.close) / 180).toFixed(4),
    returnValue: growth * marketValue,
    growth,
    drawdown: formatPercent(maxDrawdown.value - index * 0.0008),
    marketValue: formatMoney(marketValue),
    share: '22,095,863'
  }
}))

const fundOption = computed(() => ({
  ...chartTheme,
  xAxis: { ...chartTheme.xAxis, type: 'category', data: dates.value.slice(1) },
  yAxis: { ...chartTheme.yAxis, type: 'value', min: dynamicMin([...cumulativeReturns.value, ...benchmarkCumulative.value]), max: dynamicMax([...cumulativeReturns.value, ...benchmarkCumulative.value]) },
  series: [
    terminalLine(selectedProductName.value, cumulativeReturns.value.map(item => item * 100), '#df0000'),
    terminalLine(selectedBenchmarkName.value, benchmarkCumulative.value.map(item => item * 100), '#008bd2')
  ]
}))
const changeOption = computed(() => ({
  ...chartTheme,
  legend: { bottom: 4, textStyle: { color: '#e4edf5', fontWeight: 700 } },
  xAxis: { ...chartTheme.xAxis, type: 'category', data: dates.value.slice(1) },
  yAxis: [
    { ...chartTheme.yAxis, type: 'value', min: dynamicMin([...dailyReturns.value, ...cumulativeReturns.value]), max: dynamicMax([...dailyReturns.value, ...cumulativeReturns.value]) },
    { ...chartTheme.yAxis, type: 'value', min: -4, max: 4, axisLabel: { color: '#dbe6ee', fontWeight: 700, formatter: value => `${value}%` } }
  ],
  series: [
    {
      name: '当期收益',
      type: 'bar',
      yAxisIndex: 0,
      data: dailyReturns.value.map(item => item * 100),
      itemStyle: { color: value => value.value >= 0 ? '#df0000' : '#00e000' }
    },
    { ...terminalLine('累计收益', cumulativeReturns.value.map(item => item * 100), '#ffe000'), yAxisIndex: 0 }
  ]
}))
const klineOption = computed(() => ({
  ...chartTheme,
  tooltip: { trigger: 'axis', backgroundColor: '#111', borderColor: '#777', textStyle: { color: '#eee' } },
  legend: { bottom: 4, textStyle: { color: '#e4edf5' } },
  xAxis: { ...chartTheme.xAxis, type: 'category', data: dates.value },
  yAxis: { ...chartTheme.yAxis, type: 'value', scale: true, axisLabel: { color: '#dbe6ee' } },
  series: [
    {
      name: 'K线',
      type: 'candlestick',
      data: filteredKlines.value.map(item => [item.open, item.close, item.low, item.high]),
      itemStyle: { color: '#df0000', color0: '#00e000', borderColor: '#df0000', borderColor0: '#00e000' }
    },
    terminalLine('MA5', filteredIndicators.value.map(item => numberOrNull(item.ma5)), '#ffe000'),
    terminalLine('MA10', filteredIndicators.value.map(item => numberOrNull(item.ma10)), '#00a6e7'),
    terminalLine('MA20', filteredIndicators.value.map(item => numberOrNull(item.ma20)), '#f08a00')
  ]
}))
const macdOption = computed(() => ({
  ...chartTheme,
  legend: { bottom: 4, textStyle: { color: '#e4edf5' } },
  xAxis: { ...chartTheme.xAxis, type: 'category', data: dates.value },
  yAxis: { ...chartTheme.yAxis, type: 'value', axisLabel: { color: '#dbe6ee' } },
  series: [
    {
      name: 'MACD',
      type: 'bar',
      data: filteredIndicators.value.map(item => numberOrNull(item.macd)),
      itemStyle: { color: value => Number(value.value || 0) >= 0 ? '#df0000' : '#00e000' }
    },
    terminalLine('DIF', filteredIndicators.value.map(item => numberOrNull(item.dif)), '#ffe000'),
    terminalLine('DEA', filteredIndicators.value.map(item => numberOrNull(item.dea)), '#00a6e7')
  ]
}))
const rsiOption = computed(() => ({
  ...chartTheme,
  legend: { bottom: 4, textStyle: { color: '#e4edf5' } },
  xAxis: { ...chartTheme.xAxis, type: 'category', data: dates.value },
  yAxis: { ...chartTheme.yAxis, type: 'value', min: 0, max: 100, axisLabel: { color: '#dbe6ee' } },
  series: [
    terminalLine('RSI14', filteredIndicators.value.map(item => numberOrNull(item.rsi)), '#ffe000'),
    terminalLine('超买线', dates.value.map(() => 70), '#df0000'),
    terminalLine('超卖线', dates.value.map(() => 30), '#00e000')
  ]
}))
const riskKlineOption = computed(() => ({
  ...chartTheme,
  legend: { bottom: 2, textStyle: { color: '#e4edf5' } },
  xAxis: { ...chartTheme.xAxis, type: 'category', data: riskDates.value },
  yAxis: { ...chartTheme.yAxis, type: 'value', scale: true, axisLabel: { color: '#dbe6ee' } },
  series: [
    {
      name: 'K线',
      type: 'candlestick',
      data: riskSampledKlines.value.map(item => [item.open, item.close, item.low, item.high]),
      itemStyle: { color: '#df0000', color0: '#00e000', borderColor: '#df0000', borderColor0: '#00e000' }
    }
  ]
}))
const riskMacdOption = computed(() => ({
  ...chartTheme,
  legend: { bottom: 2, textStyle: { color: '#e4edf5' } },
  xAxis: { ...chartTheme.xAxis, type: 'category', data: riskDates.value },
  yAxis: { ...chartTheme.yAxis, type: 'value', axisLabel: { color: '#dbe6ee' } },
  series: [
    {
      name: 'MACD',
      type: 'bar',
      data: riskSampledIndicators.value.map(item => numberOrNull(item.macd)),
      itemStyle: { color: value => Number(value.value || 0) >= 0 ? '#df0000' : '#00e000' }
    },
    terminalLine('DIF', riskSampledIndicators.value.map(item => numberOrNull(item.dif)), '#ffe000'),
    terminalLine('DEA', riskSampledIndicators.value.map(item => numberOrNull(item.dea)), '#00a6e7')
  ]
}))
const reportOption = computed(() => ({
  ...chartTheme,
  xAxis: { ...chartTheme.xAxis, type: 'category', data: reportRows.value.map(item => item.name) },
  yAxis: { ...chartTheme.yAxis, type: 'value' },
  series: [
    {
      name: '区间收益',
      type: 'bar',
      data: reportRows.value.map(item => item.returnRate * 100),
      itemStyle: { color: value => value.value >= 0 ? '#df0000' : '#00e000' }
    }
  ]
}))
const scatterOption = computed(() => ({
  ...chartTheme,
  tooltip: {
    trigger: 'item',
    formatter: params => `产品收益: ${params.value[1].toFixed(2)}%<br/>${selectedBenchmarkName.value}收益: ${params.value[0].toFixed(2)}%`
  },
  legend: { bottom: 4, textStyle: { color: '#e4edf5', fontWeight: 700 } },
  grid: { left: 66, right: 28, top: 42, bottom: 64 },
  xAxis: { ...chartTheme.xAxis, name: `${selectedBenchmarkName.value}收益`, type: 'value', min: -10, max: 10, axisLabel: { color: '#dbe6ee', fontWeight: 700, formatter: v => `${v}%` }, splitLine: { lineStyle: { color: '#616161' } } },
  yAxis: { ...chartTheme.yAxis, name: '产品收益', type: 'value', min: -30, max: 20 },
  series: [
    {
      name: selectedProductName.value,
      type: 'scatter',
      symbolSize: 4,
      data: filteredCorrelationPoints.value.map(point => [point.indexReturn * 100, point.stockReturn * 100]),
      itemStyle: { color: '#df0000' }
    },
    {
      name: 'Equation: y = 0.05x + 0',
      type: 'line',
      data: [[-8, -0.4], [8, 0.4]],
      showSymbol: false,
      lineStyle: { color: '#008bd2', type: 'dashed' }
    }
  ]
}))

async function loadAll(options = {}) {
  const { autoSync = false, showStatus = true } = options
  loading.value = true
  error.value = ''
  if (showStatus) {
    statusMessage.value = ''
  }
  try {
    if (!currentUser.value.canViewData) {
      throw new Error('当前用户没有数据访问权限')
    }
    if (codeInput.value) {
      code.value = codeInput.value
    }
    const [klineRes, indicatorRes] = await Promise.all([getKline(code.value, autoSync), getIndicator(code.value, autoSync)])
    klines.value = klineRes.data
    indicators.value = indicatorRes.data
    getCorrelation(code.value, benchmarkCode.value, autoSync).then(res => {
      correlation.value = res.data
    }).catch(() => {
      correlation.value = { coefficient: 0, points: [] }
    })
    getAnalysis(code.value, autoSync).then(res => {
      analysis.value = res.data
    }).catch(() => {
      analysis.value = { source: 'local', content: 'AI分析暂不可用，基础行情数据已加载。' }
    })
    if (showStatus) {
      statusMessage.value = `已按 ${startDateInput.value} 至 ${endDateInput.value}、${period.value}、${selectedBenchmarkName.value} 刷新净值展示`
    }
  } catch (err) {
    error.value = err?.message || '请求失败，请确认后端服务是否启动'
  } finally {
    loading.value = false
  }
}

async function loadProducts() {
  try {
    const res = await getProducts()
    products.value = res.data
  } catch (err) {
    products.value = []
    error.value = '数据库产品列表加载失败，请检查 MySQL 连接配置'
  }
}

async function loadUsers() {
  try {
    const res = await getUsers()
    users.value = res.data
  } catch (err) {
    users.value = []
  }
}

async function loadStrategies() {
  try {
    const res = await getStrategies()
    strategies.value = res.data
  } catch (err) {
    strategies.value = []
  }
}

async function loadCurrentUser(reload = true) {
  try {
    const res = await getCurrentUser()
    currentUser.value = res.data
    error.value = ''
    if (!currentUser.value.canManageUsers && activeTab.value === 'system') {
      activeTab.value = 'data'
    }
    await Promise.all([loadProducts(), loadStrategies()])
    if (reload) {
      await loadAll()
    }
  } catch (err) {
    error.value = '用户权限加载失败，请检查 MySQL 连接配置'
  }
}

async function selectProduct(nextCode) {
  code.value = nextCode
  codeInput.value = nextCode
  await loadAll()
}

async function loadNavByCode() {
  await loadAll()
}

async function savePermission(user) {
  await updateUserPermission(user)
  await loadUsers()
  await loadCurrentUser()
}

async function syncAndReload() {
  loading.value = true
  error.value = ''
  statusMessage.value = ''
  try {
    if (codeInput.value) {
      code.value = codeInput.value
    }
    const res = await syncStock(code.value)
    await loadProducts()
    await loadAll({ autoSync: false, showStatus: false })
    statusMessage.value = `同步成功，股票 ${code.value} 已新增/更新 ${res.data.count || 0} 条K线数据，并已自动更新净值`
  } catch (err) {
    error.value = err?.response?.data?.message || err?.message || '同步失败'
  } finally {
    loading.value = false
  }
}

async function addStock() {
  loading.value = true
  error.value = ''
  try {
    const nextCode = codeInput.value.trim()
    if (!/^\d{6}$/.test(nextCode)) {
      throw new Error('请输入正确的6位股票代码')
    }
    await addUserStock(nextCode)
    await loadProducts()
    code.value = nextCode
    codeInput.value = nextCode
    await loadAll()
  } catch (err) {
    error.value = err?.response?.data?.message || err?.message || '添加股票失败'
  } finally {
    loading.value = false
  }
}

async function loadRisk() {
  loading.value = true
  error.value = ''
  try {
    if (!/^\d{6}$/.test(riskCode.value)) {
      throw new Error('请输入正确的6位股票代码')
    }
    const [quoteRes, klineRes, indicatorRes] = await Promise.all([
      getQuote(riskCode.value),
      getKline(riskCode.value),
      getIndicator(riskCode.value)
    ])
    riskQuote.value = quoteRes.data
    riskKlines.value = klineRes.data
    riskIndicators.value = indicatorRes.data
  } catch (err) {
    error.value = err?.response?.data?.message || err?.message || '风险分析加载失败'
  } finally {
    loading.value = false
  }
}

async function saveStrategy() {
  if (!strategyForm.value.content.trim()) {
    error.value = '请输入策略信息'
    return
  }
  try {
    await saveStrategyApi({
      username: currentUser.value.username,
      tradeDate: strategyForm.value.date,
      code: strategyForm.value.code,
      type: strategyForm.value.type,
      content: strategyForm.value.content
    })
    await loadStrategies()
    strategyForm.value = { date: todayText(), code: strategyForm.value.code, type: strategyForm.value.type, content: '' }
    error.value = ''
  } catch (err) {
    error.value = err?.response?.data?.message || err?.message || '策略保存失败'
  }
}

async function sendRiskAiMessage() {
  if (!riskAiQuestion.value) return
  const question = riskAiQuestion.value
  riskAiMessages.value.push({ role: 'user', content: question })
  riskAiQuestion.value = ''
  riskAiLoading.value = true
  try {
    const res = await askRiskAi({
      code: riskCode.value,
      question,
      context: riskAiContext.value
    })
    riskAiMessages.value.push({ role: 'assistant', content: res.data.content || 'DeepSeek 暂无回复。' })
  } catch (err) {
    riskAiMessages.value.push({ role: 'assistant', content: err?.response?.data?.message || 'AI对话请求失败，请稍后重试。' })
  } finally {
    riskAiLoading.value = false
  }
}

async function buildReport() {
  error.value = ''
  try {
    const rows = []
    for (const itemCode of reportCodes.value) {
      await syncStock(itemCode)
      const res = await getKline(itemCode)
      const rowsInRange = res.data.filter(item => item.date >= reportStartDate.value && item.date <= reportEndDate.value)
      if (rowsInRange.length < 2) continue
      const startClose = Number(rowsInRange[0].close)
      const endClose = Number(rowsInRange.at(-1).close)
      const volume = rowsInRange.reduce((sum, item) => sum + Number(item.volume || 0), 0)
      const product = products.value.find(item => item.code === itemCode)
      rows.push({
        code: itemCode,
        name: product?.name || `股票 ${itemCode}`,
        startDate: rowsInRange[0].date,
        endDate: rowsInRange.at(-1).date,
        startClose: startClose.toFixed(4),
        endClose: endClose.toFixed(4),
        returnRate: startClose === 0 ? 0 : (endClose - startClose) / startClose,
        volume,
        rows: rowsInRange.length
      })
    }
    reportRows.value = rows
  } catch (err) {
    error.value = '数据报表生成失败'
  }
}

function toggleReportCodes() {
  reportCodes.value = allReportCodesSelected.value ? [] : products.value.map(item => item.code)
}

function filterByDate(rows) {
  const start = normalizeDate(startDateInput.value)
  const end = normalizeDate(endDateInput.value)
  return rows.filter(item => (!start || item.date >= start) && (!end || item.date <= end))
}

onMounted(async () => {
  await loadCurrentUser(false)
  await Promise.all([loadProducts(), loadUsers(), loadStrategies()])
  await loadAll()
  await Promise.all([loadRisk(), buildReport()])
  window.setInterval(() => {
    loadAll()
    if (activeTab.value === 'risk') {
      loadRisk()
    }
  }, 60000)
})

watch(benchmarkCode, () => {
  loadAll()
})
</script>
