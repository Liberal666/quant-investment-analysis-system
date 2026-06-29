<template>
  <main class="auth-shell">
    <section class="market-hero" aria-label="行情概览">
      <div class="auth-logo">
        <span class="logo-mark"></span>
        <div>
          <strong>智投财经</strong>
          <small>SMART INVEST</small>
        </div>
      </div>
      <div class="hero-copy">
        <h1>洞察市场&nbsp;&nbsp;把握机会</h1>
        <p>专业的股票分析平台 助您决策每一步</p>
        <div class="hero-features">
          <div><i>▟</i><strong>实时行情</strong><span>全面覆盖市场数据</span></div>
          <div><i>◔</i><strong>深度分析</strong><span>专业工具助力决策</span></div>
          <div><i>♢</i><strong>安全可靠</strong><span>资金数据多重保障</span></div>
        </div>
      </div>
      <div class="quote-cloud">
        <span>3286.62<small>+1.35%</small></span>
        <span>2867.71<small>+0.92%</small></span>
        <span>2356.57<small>+0.63%</small></span>
        <span>1634.18<small>-0.23%</small></span>
      </div>
      <div class="market-scene">
        <div class="candle-field">
          <i v-for="item in candleItems" :key="item.left" :style="{ left: item.left, height: item.height, bottom: item.bottom }" :class="{ red: item.red }"></i>
        </div>
        <div class="growth-arrow"></div>
        <div class="skyline">
          <i v-for="item in skylineItems" :key="item.left" :style="{ left: item.left, height: item.height, width: item.width }"></i>
        </div>
      </div>
      <div class="index-strip">
        <div v-for="item in indexItems" :key="item.name">
          <span>{{ item.name }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.change }}</small>
        </div>
      </div>
      <p class="risk-note">投资有风险，入市需谨慎</p>
    </section>

    <section class="auth-panel">
      <div class="auth-tabs">
        <button type="button" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
        <button type="button" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
      </div>
      <form class="auth-form" @submit.prevent="submit">
        <label class="auth-input">
          <span>♙</span>
          <input v-model.trim="form.username" autocomplete="username" placeholder="请输入手机号/邮箱/用户名" />
        </label>
        <label v-if="mode === 'register'" class="auth-input">
          <span>✦</span>
          <input v-model.trim="form.displayName" placeholder="用于系统内展示" />
        </label>
        <label class="auth-input">
          <span>▣</span>
          <input v-model="form.password" autocomplete="current-password" placeholder="默认账号密码 123456" type="password" />
        </label>
        <div class="auth-options">
          <label><input v-model="rememberMe" type="checkbox" />记住我</label>
          <button type="button">忘记密码?</button>
        </div>
        <p v-if="message" class="auth-message">{{ message }}</p>
        <button class="auth-submit" type="submit" :disabled="loading">{{ loading ? '处理中...' : mode === 'login' ? '登录' : '注册' }}</button>
      </form>
      <div class="demo-account">
        <span>{{ mode === 'login' ? '演示账号' : '已有账号?' }}</span>
        <button type="button" @click="fillDemo('viewer')">普通用户</button>
        <button type="button" @click="fillDemo('admin')">管理员</button>
      </div>
      <button class="register-link" type="button" @click="mode = mode === 'login' ? 'register' : 'login'">
        {{ mode === 'login' ? '还没有账号？ 立即注册' : '已有账号？ 返回登录' }}
      </button>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { login, register } from '../api/stock'

const emit = defineEmits(['authenticated'])

const mode = ref('login')
const loading = ref(false)
const message = ref('')
const rememberMe = ref(false)
const form = ref({ username: 'viewer', displayName: '', password: '123456' })
const indexItems = [
  { name: '上证指数', value: '3,286.62', change: '+43.72   +1.35%' },
  { name: '深证成指', value: '10,502.33', change: '+231.09   +2.25%' },
  { name: '创业板指', value: '2,041.67', change: '+46.31   +2.32%' },
  { name: '沪深300', value: '3,842.21', change: '+32.16   +0.84%' }
]
const skylineItems = [
  { left: '2%', width: '28px', height: '92px' },
  { left: '11%', width: '34px', height: '150px' },
  { left: '20%', width: '42px', height: '118px' },
  { left: '32%', width: '30px', height: '84px' },
  { left: '46%', width: '36px', height: '130px' },
  { left: '61%', width: '44px', height: '108px' },
  { left: '75%', width: '32px', height: '156px' },
  { left: '88%', width: '40px', height: '124px' }
]
const candleItems = [
  { left: '3%', height: '42px', bottom: '38%', red: false },
  { left: '9%', height: '72px', bottom: '44%', red: false },
  { left: '15%', height: '54px', bottom: '50%', red: true },
  { left: '23%', height: '88px', bottom: '46%', red: false },
  { left: '31%', height: '70px', bottom: '53%', red: true },
  { left: '40%', height: '64px', bottom: '44%', red: false },
  { left: '49%', height: '92px', bottom: '49%', red: false },
  { left: '58%', height: '56px', bottom: '58%', red: true },
  { left: '68%', height: '112px', bottom: '52%', red: false },
  { left: '78%', height: '84px', bottom: '61%', red: true },
  { left: '88%', height: '108px', bottom: '58%', red: false }
]

async function submit() {
  loading.value = true
  message.value = ''
  try {
    const payload = {
      username: form.value.username,
      displayName: form.value.displayName,
      password: form.value.password
    }
    const res = mode.value === 'login' ? await login(payload) : await register(payload)
    emit('authenticated', res.data)
  } catch (err) {
    message.value = err?.response?.data?.message || err?.message || '操作失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function fillDemo(username) {
  mode.value = 'login'
  form.value.username = username
  form.value.password = '123456'
}
</script>
