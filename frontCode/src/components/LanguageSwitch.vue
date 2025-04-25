<template>
  <div class="language-switch">
    <el-dropdown trigger="click" @command="handleCommand" class="custom-dropdown">
      <span class="el-dropdown-link">
        <span class="language-text">{{ currentLanguage === 'zh-CN' ? '中文' : 'English' }}</span>
        <el-icon class="el-icon--right desktop-only"><arrow-down /></el-icon>
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="zh-CN">中文</el-dropdown-item>
          <el-dropdown-item command="en-US">English</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const { locale } = useI18n()
const currentLanguage = ref(localStorage.getItem('language') || 'zh-CN')

onMounted(() => {
  locale.value = currentLanguage.value
})

const handleCommand = (command) => {
  currentLanguage.value = command
  locale.value = command
  localStorage.setItem('language', command)
}
</script>

<style>
/* 重置所有相关的 Element Plus 样式 */
.el-popper.el-dropdown__popper {
  --el-dropdown-menu-bg-color: rgba(42, 47, 79, 0.9) !important;
  --el-dropdown-menuItem-hover-fill: rgba(255, 255, 255, 0.1) !important;
  --el-dropdown-menu-box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.3) !important;
  background-color: rgba(42, 47, 79, 0.9) !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
}

.el-popper.el-dropdown__popper .el-popper__arrow::before {
  background-color: rgba(42, 47, 79, 0.9) !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
}

.el-dropdown-menu {
  background-color: rgba(42, 47, 79, 0.9) !important;
  border: none !important;
}

.el-dropdown-menu__item {
  color: #ffffff !important;
  background-color: transparent !important;
}

.el-dropdown-menu__item:hover, 
.el-dropdown-menu__item:focus {
  background-color: rgba(255, 255, 255, 0.1) !important;
  color: #00FF88 !important;
}

/* 移除 popper 的亮色主题 */
.el-popper.is-light {
  border: none !important;
  background: rgba(42, 47, 79, 0.9) !important;
}

.el-popper.is-light .el-popper__arrow::before {
  border: none !important;
  background: rgba(42, 47, 79, 0.9) !important;
}

/* 确保下拉菜单内容区域也是深色的 */
.el-scrollbar {
  background-color: rgba(42, 47, 79, 0.9) !important;
}

.el-scrollbar__view {
  background-color: rgba(42, 47, 79, 0.9) !important;
}
</style>

<style scoped>
.language-switch {
  display: inline-flex;
  align-items: center;
  margin-right: 15px;
}

.el-dropdown-link {
  cursor: pointer;
  color: #ffffff;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  background: rgba(42, 47, 79, 0.9);
  border-radius: 4px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.el-dropdown-link:hover {
  background: rgba(255, 255, 255, 0.1);
}

.desktop-only {
  display: inline-flex;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .language-switch {
    margin-right: 10px;
  }
  
  .el-dropdown-link {
    background: rgba(42, 47, 79, 0.9);
    border-radius: 4px;
    padding: 6px 8px;
    min-width: auto;
  }
  
  .language-text {
    font-size: 13px;
  }
}

@media screen and (max-width: 480px) {
  .language-switch {
    margin-right: 8px;
  }
  
  .el-dropdown-link {
    padding: 4px 8px;
  }
  
  .language-text {
    font-size: 12px;
  }
  
  .desktop-only {
    display: none;
  }
}
</style> 