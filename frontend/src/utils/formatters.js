/**
 * 页面展示层的数字、金额、百分比格式化工具。
 */
export function formatPercent(value) {
  return `${(Number(value || 0) * 100).toFixed(2)}%`
}

export function formatMoney(value) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export function formatQuote(value) {
  return value === null || value === undefined || value === '' ? '--' : Number(value).toFixed(2)
}

export function formatVolume(value) {
  return Number(value || 0).toLocaleString('zh-CN')
}

export function formatIndicator(value) {
  return value === null || value === undefined ? '--' : Number(value).toFixed(2)
}

export function numberClass(value) {
  return Number(value) >= 0 ? 'up' : 'down'
}

export function numberOrNull(value) {
  return value === null || value === undefined ? null : Number(value)
}
