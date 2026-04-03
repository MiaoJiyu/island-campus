<template>
  <router-view />
</template>

<script setup lang="ts">
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'

const app = createApp(App)
app.use(createPinia()).use(router).mount('#app')
</script>
