<template>
  <el-container class="auth-shell">
    <el-row class="auth-layout" :gutter="72">
      <el-col :xs="24" :lg="14" class="market-hero" aria-label="行情概览">
        <el-card class="hero-card" shadow="never">
          <div class="hero-copy">
            <h1>量化投资绩效分析系统</h1>
            <p>拼一次富三代、拼命才能不失败</p>
            <el-row class="hero-features" :gutter="18">
              <el-col v-for="item in heroFeatures" :key="item.title" :span="8">
                <el-card class="feature-card" shadow="never">
                  <component :is="item.icon" class="feature-el-icon" />
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.desc }}</span>
                </el-card>
              </el-col>
            </el-row>
          </div>
          <el-image class="market-preview-image" :src="marketPreview" fit="contain" alt="市场行情预览" />
          <el-alert class="risk-note" title="投资有风险，入市需谨慎" type="info" :closable="false" show-icon />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card class="auth-panel" shadow="never">
          <el-tabs v-model="mode" class="auth-tabs" stretch>
            <el-tab-pane label="登录" name="login" />
            <el-tab-pane label="注册" name="register" />
          </el-tabs>

          <el-form ref="authFormRef" :model="form" :rules="rules" class="auth-form" size="large" @submit.prevent>
            <el-form-item prop="username">
              <el-input v-model.trim="form.username" autocomplete="username" placeholder="请输入用户名" :prefix-icon="User" clearable />
            </el-form-item>
            <el-form-item v-if="mode === 'register'" prop="displayName">
              <el-input v-model.trim="form.displayName" placeholder="用于系统内展示" :prefix-icon="UserFilled" clearable />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                autocomplete="current-password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                type="password"
              />
            </el-form-item>

            <div class="auth-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <el-button link type="primary">忘记密码?</el-button>
            </div>
            <el-alert v-if="message" class="auth-message" :title="message" type="error" :closable="false" show-icon />
            <el-button class="auth-submit" type="primary" :loading="loading" @click="submit">
              {{ mode === 'login' ? '登录' : '注册' }}
            </el-button>
          </el-form>
          <el-button class="register-link" link @click="mode = mode === 'login' ? 'register' : 'login'">
            <span>{{ mode === 'login' ? '还没有账号？' : '已有账号？' }}</span>
            <strong>{{ mode === 'login' ? '立即注册' : '返回登录' }}</strong>
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { Lock, MagicStick, TrendCharts, User, UserFilled, WarningFilled } from '@element-plus/icons-vue'
import { login, register, setAuthToken } from '../api/stock'
import marketPreview from '../assets/market-preview.svg'

const emit = defineEmits(['authenticated'])

const mode = ref('login')
const loading = ref(false)
const message = ref('')
const rememberMe = ref(false)
const authFormRef = ref(null)
const form = ref({ username: '', displayName: '', password: '' })
const heroFeatures = [
  { title: '实时净值', desc: '多维数据实时更新', icon: TrendCharts },
  { title: '风险分析', desc: '全面评估投资风险', icon: WarningFilled },
  { title: 'AI辅助', desc: '智能策略辅助决策', icon: MagicStick }
]
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  displayName: [{ required: false, trigger: 'blur' }]
}

async function submit() {
  message.value = ''
  if (authFormRef.value) {
    const valid = await authFormRef.value.validate().catch(() => false)
    if (!valid) return
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
