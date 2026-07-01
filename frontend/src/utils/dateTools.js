/**
 * 日期归一化、周期抽样和默认日期生成工具。
 */
export function normalizeDate(value) {
  return value ? value.replaceAll('/', '-') : ''
}

export function todayText() {
  const date = new Date()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

export function sampleByPeriod(rows, value) {
  if (value === '每日') {
    return rows
  }
  const result = []
  const seen = new Set()
  for (let i = rows.length - 1; i >= 0; i--) {
    const date = rows[i].date
    const key = value === '每周' ? `${date.slice(0, 4)}-W${weekNumber(date)}` : date.slice(0, 7)
    if (!seen.has(key)) {
      seen.add(key)
      result.unshift(rows[i])
    }
  }
  return result
}

function weekNumber(dateText) {
  const date = new Date(dateText)
  const first = new Date(date.getFullYear(), 0, 1)
  return Math.ceil((((date - first) / 86400000) + first.getDay() + 1) / 7)
}
