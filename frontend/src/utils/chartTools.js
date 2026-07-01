/**
 * ECharts 公共主题和常用图表序列构造工具。
 */
export const chartTheme = {
  backgroundColor: 'transparent',
  textStyle: { color: '#dbe6ee' },
  color: ['#df0000', '#00a6e7', '#ffe000', '#00e000'],
  tooltip: { trigger: 'axis', backgroundColor: '#111', borderColor: '#777', textStyle: { color: '#eee' } },
  legend: { bottom: 4, textStyle: { color: '#e4edf5', fontWeight: 700 } },
  grid: { left: 52, right: 18, top: 36, bottom: 64 },
  xAxis: {
    axisLine: { lineStyle: { color: '#6a6f74' } },
    axisLabel: { color: '#dbe6ee', fontWeight: 700 },
    splitLine: { show: false }
  },
  yAxis: {
    axisLine: { lineStyle: { color: '#6a6f74' } },
    axisLabel: { color: '#dbe6ee', fontWeight: 700, formatter: value => `${value}%` },
    splitLine: { lineStyle: { color: '#616161' } }
  }
}

export function terminalLine(name, data, color) {
  return {
    name,
    type: 'line',
    data,
    showSymbol: false,
    smooth: true,
    lineStyle: { width: 2, color }
  }
}

export function dynamicMin(values) {
  const numbers = values.map(item => Number(item) * 100).filter(Number.isFinite)
  if (!numbers.length) return -50
  return Math.floor((Math.min(...numbers, 0) - 8) / 10) * 10
}

export function dynamicMax(values) {
  const numbers = values.map(item => Number(item) * 100).filter(Number.isFinite)
  if (!numbers.length) return 50
  return Math.ceil((Math.max(...numbers, 0) + 8) / 10) * 10
}
