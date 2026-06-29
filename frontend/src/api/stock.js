import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000
})

export function getKline(code) {
  return request.get('/api/stock/kline', { params: { code } })
}

export function login(payload) {
  return request.post('/api/auth/login', payload)
}

export function register(payload) {
  return request.post('/api/auth/register', payload)
}

export function getProducts(username) {
  return request.get('/api/stock/products', { params: { username } })
}

export function getIndicator(code) {
  return request.get('/api/stock/indicator', { params: { code } })
}

export function getQuote(code) {
  return request.get('/api/stock/quote', { params: { code } })
}

export function getCorrelation(code, benchmarkCode) {
  return request.get('/api/stock/correlation', { params: { code, benchmarkCode } })
}

export function syncStock(code) {
  return request.post('/api/stock/sync', null, { params: { code } })
}

export function addUserStock(username, code) {
  return request.post('/api/stock/user-stock', null, { params: { username, code } })
}

export function getAnalysis(code) {
  return request.get('/api/stock/analysis', { params: { code } })
}

export function askRiskAi(payload) {
  return request.post('/api/stock/chat', payload)
}

export function getUsers() {
  return request.get('/api/users')
}

export function getCurrentUser(username) {
  return request.get('/api/users/current', { params: { username } })
}

export function updateUserPermission(currentUser, user) {
  return request.post('/api/users/permission', null, {
    params: {
      currentUser,
      username: user.username,
      role: user.role,
      canViewData: user.canViewData,
      canManageUsers: user.canManageUsers
    }
  })
}

export function getStrategies(username) {
  return request.get('/api/strategies', { params: { username } })
}

export function saveStrategy(strategy) {
  return request.post('/api/strategies', strategy)
}
