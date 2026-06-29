<template>
  <LoginView v-if="!currentUser" @authenticated="currentUser = $event" />
  <StockDashboard v-else :initial-username="currentUser.username" />
</template>

<script setup>
import { ref } from 'vue'
import LoginView from './views/LoginView.vue'
import StockDashboard from './views/StockDashboard.vue'

const currentUser = ref(null)
</script>
