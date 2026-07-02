import { createApp } from 'vue'
import {
  ElAlert,
  ElButton,
  ElCard,
  ElCheckbox,
  ElCol,
  ElContainer,
  ElForm,
  ElFormItem,
  ElImage,
  ElInput,
  ElRow,
  ElTabPane,
  ElTabs,
  ElTag
} from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import './styles.css'

const app = createApp(App)

app.use(ElAlert)
app.use(ElButton)
app.use(ElCard)
app.use(ElCheckbox)
app.use(ElCol)
app.use(ElContainer)
app.use(ElForm)
app.use(ElFormItem)
app.use(ElImage)
app.use(ElInput)
app.use(ElRow)
app.use(ElTabPane)
app.use(ElTabs)
app.use(ElTag)

app.mount('#app')
