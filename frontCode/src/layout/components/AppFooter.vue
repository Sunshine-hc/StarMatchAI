<template>
  <footer class="app-footer">
    <div class="footer-content">
      <p>© {{ currentYear }}</p>
      <p class="icp-info">
        <a href="https://beian.miit.gov.cn/#/Integrated/index" target="_blank" rel="noopener noreferrer">浙ICP备2025166029号</a>
      </p>
    </div>
  </footer>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const currentYear = ref(new Date().getFullYear());
</script>

<style scoped>
.app-footer {
  background-color: #1a1d2f;
  padding: 15px 0;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

.icp-info {
  margin-top: 5px;
  font-size: 12px;
}

.icp-info a {
  color: #999;
  text-decoration: none;
}

.icp-info a:hover {
  text-decoration: underline;
}
</style> 