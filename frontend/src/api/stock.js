import axios from 'axios'

/**
 * 后端接口模块：集中维护请求地址、登录 token 和 REST API 调用。
 */
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000
})

/**
 * 保存当前登录会话 token，所有受保护接口都会自动携带它。
 */
export function setAuthToken(token) {
  if (token) {
    request.defaults.headers.common.Authorization = `Bearer ${token}`
  } else {
    delete request.defaults.headers.common.Authorization
  }
}

export function getKline(code, sync = true) {
  return request.get('/api/stock/kline', { params: { code, sync } })
}

export function login(payload) {
  return request.post('/api/auth/login', payload)
}

export function register(payload) {
  return request.post('/api/auth/register', payload)
}

export function getProducts() {
  return request.get('/api/stock/products')
}

export function getIndicator(code, sync = true) {
  return request.get('/api/stock/indicator', { params: { code, sync } })
}

export function getQuote(code) {
  return request.get('/api/stock/quote', { params: { code } })
}

export function getCorrelation(code, benchmarkCode, sync = true) {
  return request.get('/api/stock/correlation', { params: { code, benchmarkCode, sync } })
}

export function syncStock(code) {
  return request.post('/api/stock/sync', null, { params: { code } })
}

export function addUserStock(code) {
  return request.post('/api/stock/user-stock', null, { params: { code } })
}

export function getAnalysis(code, sync = true) {
  return request.get('/api/stock/analysis', { params: { code, sync } })
}

export function askRiskAi(payload) {
  return request.post('/api/stock/chat', payload)
}

export function getUsers() {
  return request.get('/api/users')
}

export function getCurrentUser() {
  return request.get('/api/users/current')
}

export function updateUserPermission(user) {
  return request.post('/api/users/permission', null, {
    params: {
      username: user.username,
      role: user.role,
      canViewData: user.canViewData,
      canManageUsers: user.canManageUsers
    }
  })
}

export function getStrategies() {
  return request.get('/api/strategies')
}

export function saveStrategy(strategy) {
  return request.post('/api/strategies', strategy)
}
