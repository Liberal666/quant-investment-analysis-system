<template>
  <LoginView v-if="!currentUser" @authenticated="currentUser = $event" />
  <StockDashboard v-else :initial-user="currentUser" @switch-user="switchUser" />
</template>

<script setup>
import { ref } from 'vue'
import { setAuthToken } from './api/stock'
import LoginView from './views/LoginView.vue'
import StockDashboard from './views/StockDashboard.vue'

const currentUser = ref(null)

function switchUser() {
  setAuthToken('')
  currentUser.value = null
}
</script>
