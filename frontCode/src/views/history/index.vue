<template>
  <div class="history-container">
    <div class="starry-background">
      <div class="shooting-star" v-for="n in 5" :key="n"></div>
    </div>
    
    <div class="content-wrapper">
      <el-card class="history-card">
        <template #header>
          <div class="card-header">
            <span>匹配历史记录</span>
          </div>
        </template>
        
        <div v-if="loading" class="loading-container">
          <div class="loading-dots">
            <span></span>
            <span></span>
            <span></span>
          </div>
          <p>加载中...</p>
        </div>
        
        <div v-else-if="historyList.length === 0" class="empty-container">
          <el-empty description="暂无匹配记录" :image-size="200">
            <el-button type="primary" @click="$router.push('/')" class="action-button">
              去匹配
            </el-button>
          </el-empty>
        </div>
        
        <div v-else class="history-list">
          <div v-for="(item, index) in historyList" :key="index" class="history-item">
            <el-card class="match-card">
              <div class="match-info">
                <div class="match-date">{{ formatDate(item.createTime) }}</div>
                <div class="match-signs">
                  <span class="sign">{{ item.userSign }}</span>
                  <span class="connector">与</span>
                  <span class="sign">{{ item.targetSign }}</span>
                </div>
                <div class="match-score">
                  <div class="score-circle" :style="{ background: getScoreColor(item.score) }">
                    {{ item.score }}%
                  </div>
                </div>
              </div>
              
              <div class="match-content">
                <p>{{ item.content }}</p>
              </div>
              
              <div class="match-actions">
                <el-button type="primary" size="small" @click="viewDetail(item)" class="action-button">
                  查看详情
                </el-button>
              </div>
            </el-card>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getMatchHistory } from '@/api/match'; // 假设您有这个API

const loading = ref(true);
const historyList = ref([]);

onMounted(async () => {
  try {
    // 模拟API调用，实际项目中应该调用真实API
    // const response = await getMatchHistory();
    // historyList.value = response.data;
    
    // 模拟数据
    setTimeout(() => {
      historyList.value = [
        {
          id: 1,
          userSign: '白羊座',
          targetSign: '天秤座',
          score: 75,
          content: '白羊座和天秤座是相对的星座，可能会有一些挑战，但也有互补的特质。白羊座的热情和天秤座的平衡感可以创造有趣的关系。',
          createTime: new Date().getTime() - 86400000 // 昨天
        },
        {
          id: 2,
          userSign: '白羊座',
          targetSign: '狮子座',
          score: 92,
          content: '白羊座和狮子座都是火象星座，彼此非常合拍。两者都充满活力和热情，可以激发对方的创造力和冒险精神。',
          createTime: new Date().getTime() - 172800000 // 前天
        }
      ];
      loading.value = false;
    }, 1500);
  } catch (error) {
    ElMessage.error('获取历史记录失败');
    loading.value = false;
  }
});

const formatDate = (timestamp) => {
  const date = new Date(timestamp);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

const getScoreColor = (score) => {
  if (score >= 90) return 'linear-gradient(135deg, #00FF88 0%, #00D2FF 100%)';
  if (score >= 70) return 'linear-gradient(135deg, #00D2FF 0%, #00BFFF 100%)';
  if (score >= 50) return 'linear-gradient(135deg, #FFD700 0%, #FFA500 100%)';
  return 'linear-gradient(135deg, #FF6B6B 0%, #FF0000 100%)';
};

const viewDetail = (item) => {
  // 查看详情的逻辑，可以跳转到详情页或显示弹窗
  ElMessage.info('查看详情功能开发中...');
};
</script>

<style scoped>
.history-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  background-color: #1a1d2f;
  position: relative;
  overflow: hidden;
}

.starry-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(circle at 50% 50%, rgba(0, 255, 136, 0.1) 0%, transparent 80%);
}

.shooting-star {
  position: absolute;
  width: 2px;
  height: 2px;
  background: #ffffff;
  animation: shooting 3s linear infinite;
}

.shooting-star:nth-child(1) {
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shooting-star:nth-child(2) {
  top: 20%;
  left: 30%;
  animation-delay: 1s;
}

.shooting-star:nth-child(3) {
  top: 30%;
  left: 50%;
  animation-delay: 2s;
}

.shooting-star:nth-child(4) {
  top: 40%;
  left: 70%;
  animation-delay: 3s;
}

.shooting-star:nth-child(5) {
  top: 50%;
  left: 90%;
  animation-delay: 4s;
}

@keyframes shooting {
  0% {
    transform: translateX(0) translateY(0) rotate(45deg);
    opacity: 1;
  }

  100% {
    transform: translateX(100vw) translateY(100vh) rotate(45deg);
    opacity: 0;
  }
}

.content-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1200px;
  display: flex;
  justify-content: center;
  padding: 20px;
  margin-top: 40px;
}

.history-card {
  width: 800px;
  max-width: 90%;
  border-radius: 12px;
  overflow: hidden;
  background-color: rgba(30, 34, 53, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 255, 136, 0.1);
}

.card-header {
  display: flex;
  justify-content: center;
  align-items: center;
  color: #00FF88;
  font-size: 20px;
  font-weight: bold;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
}

.loading-dots {
  display: inline-flex;
  gap: 4px;
  height: 20px;
  margin-bottom: 10px;
}

.loading-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #00FF88;
  display: inline-block;
  animation: dots 1.4s infinite;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes dots {
  0%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

.empty-container {
  padding: 50px 0;
}

.history-list {
  padding: 20px;
}

.history-item {
  margin-bottom: 20px;
}

.match-card {
  background-color: rgba(255, 255, 255, 0.05);
  border: none;
}

.match-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.match-date {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.match-signs {
  display: flex;
  align-items: center;
}

.sign {
  color: #00FF88;
  font-weight: bold;
}

.connector {
  margin: 0 10px;
  color: rgba(255, 255, 255, 0.7);
}

.match-score {
  display: flex;
  align-items: center;
}

.score-circle {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #ffffff;
  font-weight: bold;
}

.match-content {
  margin-bottom: 15px;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

.match-actions {
  display: flex;
  justify-content: flex-end;
}

.action-button {
  background: linear-gradient(90deg, rgba(0, 255, 136, 0.8) 0%, rgba(0, 210, 255, 0.8) 100%);
  border: none;
}

.action-button:hover {
  background: linear-gradient(90deg, rgba(0, 255, 136, 1) 0%, rgba(0, 210, 255, 1) 100%);
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .history-card {
    width: 95%;
  }
  
  .match-info {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .match-signs {
    margin: 10px 0;
  }
  
  .match-score {
    align-self: center;
    margin-top: 10px;
  }
}
</style> 