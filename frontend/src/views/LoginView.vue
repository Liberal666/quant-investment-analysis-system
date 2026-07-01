<template>
  <main class="auth-shell">
    <section class="market-hero" aria-label="行情概览">
      <div class="hero-copy">
        <h1>量化投资绩效分析系统</h1>
        <p>拼一次富三代、拼命才能不失败</p>
        <div class="hero-features">
          <div>
            <i class="feature-icon chart-icon"></i>
            <strong>实时净值</strong>
            <span>多维数据实时更新</span>
          </div>
          <div>
            <i class="feature-icon shield-icon"></i>
            <strong>风险分析</strong>
            <span>全面评估投资风险</span>
          </div>
          <div>
            <i class="feature-icon ai-icon">AI</i>
            <strong>AI辅助</strong>
            <span>智能策略辅助决策</span>
          </div>
        </div>
      </div>
      <img class="market-preview-image" src="../assets/market-preview.svg" alt="市场行情预览" />
      <p class="risk-note">投资有风险，入市需谨慎</p>
    </section>

    <section class="auth-panel">
      <div class="auth-tabs">
        <button type="button" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
        <button type="button" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
      </div>
      <form class="auth-form" @submit.prevent="submit">
        <label class="auth-input">
          <span class="input-icon user-icon"></span>
          <input v-model.trim="form.username" autocomplete="username" placeholder="请输入用户名" />
        </label>
        <label v-if="mode === 'register'" class="auth-input">
          <span class="input-icon user-icon"></span>
          <input v-model.trim="form.displayName" placeholder="用于系统内展示" />
        </label>
        <label class="auth-input">
          <span class="input-icon lock-icon"></span>
          <input v-model="form.password" autocomplete="current-password" placeholder="请输入密码" :type="showPassword ? 'text' : 'password'" />
          <button class="password-toggle" type="button" :aria-label="showPassword ? '隐藏密码' : '查看密码'" @click="showPassword = !showPassword">
            <span :class="['input-icon', 'eye-icon', { hidden: !showPassword }]"></span>
          </button>
        </label>
        <div class="auth-options">
          <label><input v-model="rememberMe" type="checkbox" />记住我</label>
          <button type="button">忘记密码?</button>
        </div>
        <p v-if="message" class="auth-message">{{ message }}</p>
        <button class="auth-submit" type="submit" :disabled="loading">{{ loading ? '处理中...' : mode === 'login' ? '登录' : '注册' }}</button>
      </form>
      <button class="register-link" type="button" @click="mode = mode === 'login' ? 'register' : 'login'">
        <span>{{ mode === 'login' ? '还没有账号？' : '已有账号？' }}</span>
        <strong>{{ mode === 'login' ? '立即注册' : '返回登录' }}</strong>
      </button>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { login, register, setAuthToken } from '../api/stock'

const emit = defineEmits(['authenticated'])

const mode = ref('login')
const loading = ref(false)
const message = ref('')
const rememberMe = ref(false)
const showPassword = ref(false)
const form = ref({ username: '', displayName: '', password: '' })

async function submit() {
  message.value = ''
  if (!form.value.username || !form.value.password) {
    message.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  try {
    const payload = {
      username: form.value.username,
      displayName: form.value.displayName,
      password: form.value.password
    }
    const res = mode.value === 'login' ? await login(payload) : await register(payload)
    setAuthToken(res.data.token)
    emit('authenticated', res.data.user)
  } catch (err) {
    if (err?.response?.status === 401) {
      message.value = '用户名或密码错误'
    } else {
      message.value = err?.response?.data?.message || err?.response?.data?.error || err?.message || '操作失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

</script>
