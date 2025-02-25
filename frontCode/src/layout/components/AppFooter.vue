<template>
  <footer class="app-footer">
    <div class="footer-content">
      <p>© {{ currentYear }} 星座匹配系统 - 版权所有</p>
    </div>
  </footer>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const currentYear = ref(new Date().getFullYear());
</script>

<style scoped>
.app-footer {
  background-color: #f5f7fa;
  padding: 15px 0;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}
</style> 