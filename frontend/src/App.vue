<script setup lang="ts">
import Layout from './components/Layout.vue'
</script>

<template>
  <Layout />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: var(--font-sans, 'Plus Jakarta Sans', 'PingFang SC', sans-serif);
  background: #ffffff;
  color: #0a1128;
}

#app {
  height: 100vh;
}
</style>
