<template>
  <div class="app-wrapper">
    <div class="main-container">
      <app-header />
      <div class="app-main">
        <router-view />
      </div>
      <app-footer />
    </div>
  </div>
</template>

<script setup>
import AppHeader from './components/AppHeader.vue';
import AppFooter from './components/AppFooter.vue';
</script>

<style scoped>
.app-wrapper {
  position: relative;
  height: 100vh;
  width: 100%;
}

.main-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
  overflow: auto;
  padding: 20px;
}
</style> 