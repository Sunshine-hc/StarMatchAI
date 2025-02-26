<template>
  <el-dialog
    v-model="visible"
    title="匹配详情"
    width="90%"
    :close-on-click-modal="false"
    custom-class="match-detail-dialog"
    :before-close="handleClose"
  >
    <div class="match-detail-container">
      <h2 class="match-title">{{ data.person1Sign }} 与 {{ data.person2Sign }} 的匹配分析</h2>
      
      <div class="score-display">
        <div class="score-circle" :style="{ background: getScoreColor(data.matchScore) }">
          {{ data.matchScore }}%
        </div>
      </div>
      
      <div class="detail-content">
        <div class="detail-section">
          <h3 class="section-title">分析:</h3>
          <p class="section-text">{{ data.analysis }}</p>
        </div>
        
        <div class="detail-section">
          <h3 class="section-title">优势:</h3>
          <div class="advantage-list">
            <p class="section-text">{{ data.advantages }}</p>
          </div>
        </div>
        
        <div class="detail-section">
          <h3 class="section-title">劣势:</h3>
          <div class="disadvantage-list">
            <p class="section-text">{{ data.disadvantages }}</p>
          </div>
        </div>
        
        <div class="detail-section">
          <h3 class="section-title">建议:</h3>
          <div class="suggestion-list">
            <p class="section-text">{{ data.suggestions }}</p>
          </div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  data: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['update:visible', 'close']);

// 根据分数获取颜色
const getScoreColor = (score) => {
  // 优秀：90-100分
  if (score >= 90) return 'linear-gradient(135deg, #00FF88 0%, #00D2FF 100%)';
  
  // 良好：80-89分
  if (score >= 80) return 'linear-gradient(135deg, #00D2FF 0%, #00BFFF 100%)';
  
  // 中等：70-79分
  if (score >= 70) return 'linear-gradient(135deg, #00BFFF 0%, #87CEFA 100%)';
  
  // 及格：60-69分
  if (score >= 60) return 'linear-gradient(135deg, #FFD700 0%, #FFA500 100%)';
  
  // 不及格：60分以下
  return 'linear-gradient(135deg, #FF6B6B 0%, #FF0000 100%)';
};

// 关闭详情对话框
const handleClose = () => {
  emit('update:visible', false);
  emit('close');
};
</script>

<style scoped>
.match-detail-dialog {
  background-color: #1a1d2f !important;
  border: 1px solid rgba(0, 210, 255, 0.3) !important;
  border-radius: 12px !important;
  box-shadow: 0 0 20px rgba(0, 210, 255, 0.2) !important;
}

.match-detail-dialog :deep(.el-dialog__header) {
  background: linear-gradient(90deg, rgba(0, 0, 0, 0) 0%, rgba(0, 210, 255, 0.1) 50%, rgba(0, 0, 0, 0) 100%);
  padding: 15px 20px;
  border-bottom: 1px solid rgba(0, 210, 255, 0.2);
}

.match-detail-dialog :deep(.el-dialog__title) {
  color: #ffffff;
  font-weight: bold;
  font-size: 1.2rem;
}

.match-detail-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(0, 210, 255, 0.8);
}

.match-detail-dialog :deep(.el-dialog__body) {
  color: #e0e0e0;
  padding: 20px;
  background-color: #1a1d2f;
}

.match-detail-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid rgba(0, 210, 255, 0.2);
  padding: 15px 20px;
  background-color: #1a1d2f;
}

.match-detail-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px;
}

.match-title {
  color: #ffffff;
  font-size: 1.5rem;
  margin-bottom: 20px;
  text-align: center;
  background: linear-gradient(90deg, rgba(0, 0, 0, 0) 0%, rgba(0, 210, 255, 0.1) 50%, rgba(0, 0, 0, 0) 100%);
  padding: 10px;
  border-radius: 30px;
  width: 100%;
}

.score-display {
  margin-bottom: 30px;
}

.score-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #ffffff;
  font-size: 1.8rem;
  font-weight: bold;
  box-shadow: 0 0 20px rgba(0, 210, 255, 0.4);
}

.detail-content {
  width: 100%;
  max-height: 50vh;
  overflow-y: auto;
  padding: 0 10px;
  /* 自定义滚动条 */
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 210, 255, 0.5) rgba(26, 29, 47, 0.5);
}

.detail-content::-webkit-scrollbar {
  width: 8px;
}

.detail-content::-webkit-scrollbar-track {
  background: rgba(26, 29, 47, 0.5);
  border-radius: 4px;
}

.detail-content::-webkit-scrollbar-thumb {
  background: rgba(0, 210, 255, 0.5);
  border-radius: 4px;
}

.detail-section {
  margin-bottom: 25px;
}

.section-title {
  color: rgba(0, 210, 255, 0.9);
  font-size: 1.2rem;
  margin-bottom: 10px;
  border-bottom: 1px solid rgba(0, 210, 255, 0.3);
  padding-bottom: 5px;
}

.section-text {
  color: #e0e0e0;
  line-height: 1.6;
  text-align: justify;
  white-space: pre-line;
}

.dialog-footer {
  display: flex;
  justify-content: center;
}

.close-button {
  background: linear-gradient(90deg, rgba(0, 210, 255, 0.8) 0%, rgba(0, 255, 136, 0.8) 100%);
  border: none;
  color: #ffffff;
  padding: 10px 30px;
  border-radius: 20px;
  font-weight: bold;
  transition: all 0.3s ease;
}

.close-button:hover {
  background: linear-gradient(90deg, rgba(0, 210, 255, 1) 0%, rgba(0, 255, 136, 1) 100%);
  box-shadow: 0 0 15px rgba(0, 210, 255, 0.5);
}
</style> 