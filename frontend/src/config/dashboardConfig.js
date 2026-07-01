/**
 * 实时监控页面的固定选项与名词解释配置。
 */
export const benchmarkOptions = [
  { code: '000300', name: '沪深300' },
  { code: '399001', name: '深证成指' },
  { code: '399006', name: '创业板指' }
]

export const definitionLeft = [
  { name: '基金收益', desc: '展示选中股票与比较基准在所选区间内的累计收益走势，用于观察超额收益和阶段性波动。' },
  { name: '净值变动', desc: '展示每期收益柱状图和累计收益曲线，用于判断收益来源、波动节奏和回撤变化。' },
  { name: '相关性分析', desc: '用散点图展示股票收益与基准收益的同步程度，Pearson系数越接近1表示同涨同跌越明显。' }
]

export const definitionRight = [
  { name: 'K线与MA均线', desc: 'K线展示开高低收价格，MA5/MA10/MA20均线用于观察短中期趋势方向。' },
  { name: 'MACD指标', desc: 'DIF、DEA和MACD柱用于判断趋势强弱，红柱扩张通常表示动能增强，绿柱扩张表示动能转弱。' },
  { name: 'RSI指标', desc: 'RSI反映价格强弱，70附近常视为偏热，30附近常视为偏弱，需结合趋势综合判断。' }
]
