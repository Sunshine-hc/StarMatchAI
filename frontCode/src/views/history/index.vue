<template>
  <div class="history-container">
    <div class="starry-background">
      <div class="shooting-star" v-for="n in 5" :key="n"></div>
    </div>
    
    <div class="content-wrapper">
      <el-card class="history-card">
        <template #header>
          <div class="card-header">
            <span class="history-title">匹配历史记录</span>
            <div 
              class="refresh-button" 
              :class="{ 'refreshing': isRefreshing }" 
              @click="refreshHistory"
              title="刷新"
            >
              <el-icon class="refresh-icon">
                <Refresh />
              </el-icon>
            </div>
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
        
        <div v-else-if="Object.keys(groupedHistory).length === 0" class="empty-container">
          <el-empty description="暂无匹配记录" :image-size="200">
            <el-button type="primary" @click="$router.push('/')" class="action-button">
              去匹配
            </el-button>
          </el-empty>
        </div>
        
        <div v-else class="history-list">
          <!-- 按时间线分组展示 -->
          <div v-for="(group, groupName) in groupedHistory" :key="groupName" class="history-group">
            <div class="time-divider">
              <span class="time-label">{{ groupName }}</span>
              <div class="divider-line"></div>
            </div>
            
            <div v-for="(item, index) in group" :key="index" class="history-item">
              <el-card class="match-card">
                <div class="match-info">
                  <div class="match-date">{{ formatDate(item.createdAt) }}</div>
                  <div class="match-signs">
                    <span class="sign">{{ item.person1Sign }}({{ formatBirthday(item.person1Birthday) }})</span>
                    <span class="connector">与</span>
                    <span class="sign">{{ item.person2Sign }}({{ formatBirthday(item.person2Birthday) }})</span>
                  </div>
                  <div class="match-score">
                    <div class="score-circle" :style="{ background: getScoreColor(item.matchScore) }">
                      {{ item.matchScore }}%
                    </div>
                  </div>
                </div>
                
                <div class="match-content">
                  <p>{{ getDisplayContent(item) }}</p>
                </div>
                
                <div class="match-actions">
                  <el-button type="primary" size="small" @click="viewDetail(item)" class="action-button">
                    查看详情
                  </el-button>
                  <el-button type="danger" size="small" @click="handleDelete(item.id)" class="delete-button">
                    删除
                  </el-button>
                </div>
              </el-card>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
  
  <!-- 匹配详情对话框 -->
  <el-dialog
    v-model="detailVisible"
    title="匹配详情"
    width="90%"
    :close-on-click-modal="false"
    custom-class="match-detail-dialog"
    :before-close="handleCloseDetail"
  >
    <div class="match-detail-container">
      <h2 class="match-title">{{ detailData.person1Sign }} 与 {{ detailData.person2Sign }} 的匹配分析</h2>
      
      <div class="score-display">
        <div class="score-circle" :style="{ background: getScoreColor(detailData.matchScore) }">
          {{ detailData.matchScore }}%
        </div>
      </div>
      
      <div class="detail-content">
        <div class="detail-section">
          <h3 class="section-title">分析:</h3>
          <p class="section-text">{{ detailData.analysis }}</p>
        </div>
        
        <div class="detail-section">
          <h3 class="section-title">优势:</h3>
          <div class="advantage-list">
            <p class="section-text">{{ detailData.advantages }}</p>
          </div>
        </div>
        
        <div class="detail-section">
          <h3 class="section-title">劣势:</h3>
          <div class="disadvantage-list">
            <p class="section-text">{{ detailData.disadvantages }}</p>
          </div>
        </div>
        
        <div class="detail-section">
          <h3 class="section-title">建议:</h3>
          <div class="suggestion-list">
            <p class="section-text">{{ detailData.suggestions }}</p>
          </div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="handleCloseDetail">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getMatchRecords, getMatchRecordDetail, deleteMatchRecord } from '@/api/matchRecord';
import { Refresh } from '@element-plus/icons-vue'

const loading = ref(true);
const historyList = ref([]);
const today = new Date();
today.setHours(0, 0, 0, 0);

// 计算时间分组
const groupedHistory = computed(() => {
  const groups = {
    '今日': [],
    '昨天': [],
    '7天内': [],
    '30天内': []
  };
  
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  
  const sevenDaysAgo = new Date(today);
  sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
  
  const thirtyDaysAgo = new Date(today);
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
  
  historyList.value.forEach(item => {
    const itemDate = new Date(item.createdAt); // 使用createdAt字段
    
    if (itemDate >= today) {
      groups['今日'].push(item);
    } else if (itemDate >= yesterday) {
      groups['昨天'].push(item);
    } else if (itemDate >= sevenDaysAgo) {
      groups['7天内'].push(item);
    } else if (itemDate >= thirtyDaysAgo) {
      groups['30天内'].push(item);
    }
  });
  
  // 移除空分组
  Object.keys(groups).forEach(key => {
    if (groups[key].length === 0) {
      delete groups[key];
    }
  });
  
  return groups;
});

// 加载历史数据
const loadHistoryData = async () => {
  try {
    loading.value = true;
    
    const response = await getMatchRecords({
      page: 1,
      size: 100
    });
    
    if (response.code === 200) {
      console.log('获取到的历史记录:', response.data);
      
      // 直接使用后端返回的数据
      historyList.value = response.data || [];
    } else {
      ElMessage.error(response.message || '获取历史记录失败');
    }
  } catch (error) {
    console.error('获取历史记录失败:', error);
    ElMessage.error('获取历史记录失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 获取显示内容
const getDisplayContent = (item) => {
  // 优先使用analysis字段，如果没有则组合其他字段
  if (item.analysis) {
    // 截取前100个字符，避免内容过长
    return item.analysis.length > 100 
      ? item.analysis.substring(0, 100) + '...' 
      : item.analysis;
  }
  
  // 组合优势和劣势
  let content = '';
  if (item.advantages) {
    content += '优势: ' + item.advantages + ' ';
  }
  if (item.disadvantages) {
    content += '劣势: ' + item.disadvantages;
  }
  
  // 如果内容太长，截取
  return content.length > 100 
    ? content.substring(0, 100) + '...' 
    : content || '查看详情了解更多';
};

// 查看详情
const viewDetail = async (item) => {
  try {
    const response = await getMatchRecordDetail(item.id);
    if (response.code === 200) {
      // 构建详情内容HTML
      const detailContent = `
        <div class="match-detail-content" style="max-height: 60vh; overflow-y: auto; padding: 10px;">
          <h3 style="
            text-align: center;
            color: #40E0D0;
            font-size: 1.2rem;
            margin-bottom: 20px;
            padding: 10px;
            background: rgba(64, 224, 208, 0.1);
            border-radius: 8px;
          ">${item.person1Sign} 与 ${item.person2Sign} 的匹配分析</h3>
          
          <div style="text-align: center; margin-bottom: 20px;">
            <span style="
              display: inline-block;
              width: 80px;
              height: 80px;
              line-height: 80px;
              border-radius: 50%;
              background: ${getScoreColor(item.matchScore)};
              color: white;
              font-size: 1.5rem;
              font-weight: bold;
              text-align: center;
              box-shadow: 0 0 15px rgba(64, 224, 208, 0.3);
            ">
              ${item.matchScore}%
            </span>
          </div>
          
          ${item.analysis ? `
            <div style="
              margin-bottom: 15px;
              padding: 15px;
              border-radius: 8px;
              background: rgba(64, 224, 208, 0.05);
              border: 1px solid rgba(64, 224, 208, 0.1);
            ">
              <strong style="
                color: #40E0D0;
                display: block;
                margin-bottom: 8px;
                font-size: 1.1rem;
              ">分析：</strong>
              <div style="color: #E0FFFF; line-height: 1.6;">${item.analysis}</div>
            </div>
          ` : ''}
          
          ${item.advantages ? `
            <div style="
              margin-bottom: 15px;
              padding: 15px;
              border-radius: 8px;
              background: rgba(64, 224, 208, 0.05);
              border: 1px solid rgba(64, 224, 208, 0.1);
            ">
              <strong style="
                color: #40E0D0;
                display: block;
                margin-bottom: 8px;
                font-size: 1.1rem;
              ">优势：</strong>
              <div style="color: #E0FFFF; line-height: 1.6;">${item.advantages}</div>
            </div>
          ` : ''}
          
          ${item.disadvantages ? `
            <div style="
              margin-bottom: 15px;
              padding: 15px;
              border-radius: 8px;
              background: rgba(64, 224, 208, 0.05);
              border: 1px solid rgba(64, 224, 208, 0.1);
            ">
              <strong style="
                color: #40E0D0;
                display: block;
                margin-bottom: 8px;
                font-size: 1.1rem;
              ">劣势：</strong>
              <div style="color: #E0FFFF; line-height: 1.6;">${item.disadvantages}</div>
            </div>
          ` : ''}
          
          ${item.suggestions ? `
            <div style="
              margin-bottom: 15px;
              padding: 15px;
              border-radius: 8px;
              background: rgba(64, 224, 208, 0.05);
              border: 1px solid rgba(64, 224, 208, 0.1);
            ">
              <strong style="
                color: #40E0D0;
                display: block;
                margin-bottom: 8px;
                font-size: 1.1rem;
              ">建议：</strong>
              <div style="color: #E0FFFF; line-height: 1.6;">${item.suggestions}</div>
            </div>
          ` : ''}
        </div>
      `;
      
      // 使用自定义样式的消息框
      ElMessageBox.alert(detailContent, '匹配详情', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '关闭',
        customClass: 'match-detail-dialog',
        showClose: true,
        closeOnClickModal: false
      });
    } else {
      ElMessage.error(response.message || '获取详情失败');
    }
  } catch (error) {
    console.error('获取详情失败:', error);
    ElMessage.error('获取详情失败，请稍后重试');
  }
};

// 修改删除记录函数
const handleDelete = (id) => {
  // 使用自定义样式的确认对话框
  ElMessageBox.confirm(
    '<i class="el-icon-warning" style="color: #FFA500; margin-right: 8px;"></i>确定要删除这条匹配记录吗？',
    '提示',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      customClass: 'starry-confirm-box',
      confirmButtonClass: 'starry-confirm-button',
      cancelButtonClass: 'starry-cancel-button',
      closeOnClickModal: false
    }
  ).then(async () => {
    try {
      const response = await deleteMatchRecord(id);
      if (response.code === 200) {
        ElMessage({
          message: '删除成功',
          type: 'success',
          customClass: 'starry-message'
        });
        loadHistoryData();
      } else {
        ElMessage({
          message: response.message || '删除失败',
          type: 'error',
          customClass: 'starry-message'
        });
      }
    } catch (error) {
      console.error('删除失败:', error);
      ElMessage({
        message: '删除失败，请稍后重试',
        type: 'error',
        customClass: 'starry-message'
      });
    }
  }).catch(() => {
    // 用户取消删除
  });
};

const formatDate = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

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

// 添加刷新状态控制
const isRefreshing = ref(false);

// 刷新历史记录
const refreshHistory = async () => {
  if (isRefreshing.value) return;
  
  isRefreshing.value = true;
  try {
    await loadHistoryData();
  } catch (error) {
    console.error('刷新失败:', error);
  } finally {
    setTimeout(() => {
      isRefreshing.value = false;
    }, 1000); // 保持动画至少运行1秒
  }
};

// 只添加这一个格式化生日的函数
const formatBirthday = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  loadHistoryData();
});
</script>

<style>
/* 全局样式，用于详情对话框 */
.match-detail-dialog .el-message-box__content {
  padding: 10px 15px;
}

/* 全局样式，用于自定义对话框 */
.starry-confirm-box {
  background: linear-gradient(135deg, #1a1d2f 0%, #2a2d3f 100%) !important;
  border: 1px solid rgba(0, 210, 255, 0.3) !important;
  border-radius: 12px !important;
  box-shadow: 0 0 20px rgba(0, 210, 255, 0.2) !important;
}

.starry-confirm-box .el-message-box__title {
  color: #ffffff !important;
  font-weight: bold !important;
}

.starry-confirm-box .el-message-box__content {
  color: #e0e0e0 !important;
  padding: 20px !important;
}

.starry-confirm-box .el-message-box__header {
  background: linear-gradient(90deg, rgba(0, 0, 0, 0) 0%, rgba(0, 210, 255, 0.1) 50%, rgba(0, 0, 0, 0) 100%) !important;
  padding: 15px 20px !important;
}

.starry-confirm-box .el-message-box__headerbtn .el-message-box__close {
  color: rgba(0, 210, 255, 0.8) !important;
}

.starry-confirm-box .el-message-box__btns {
  padding: 10px 20px 20px !important;
}

.starry-confirm-button {
  background: linear-gradient(90deg, rgba(0, 210, 255, 0.8) 0%, rgba(0, 255, 136, 0.8) 100%) !important;
  border: none !important;
  color: #ffffff !important;
  padding: 10px 20px !important;
  border-radius: 20px !important;
  font-weight: bold !important;
  transition: all 0.3s ease !important;
}

.starry-confirm-button:hover {
  background: linear-gradient(90deg, rgba(0, 210, 255, 1) 0%, rgba(0, 255, 136, 1) 100%) !important;
  box-shadow: 0 0 15px rgba(0, 210, 255, 0.5) !important;
}

.starry-cancel-button {
  background: transparent !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  color: #e0e0e0 !important;
  padding: 10px 20px !important;
  border-radius: 20px !important;
  margin-right: 15px !important;
  transition: all 0.3s ease !important;
}

.starry-cancel-button:hover {
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.5) !important;
}

/* 自定义消息提示样式 */
.starry-message {
  background: linear-gradient(135deg, #1a1d2f 0%, #2a2d3f 100%) !important;
  border: 1px solid rgba(0, 210, 255, 0.3) !important;
  border-radius: 12px !important;
  box-shadow: 0 0 20px rgba(0, 210, 255, 0.2) !important;
  color: #ffffff !important;
}

.starry-message .el-message__icon {
  color: rgba(0, 210, 255, 0.8) !important;
}

.starry-message.el-message--success .el-message__icon {
  color: rgba(0, 255, 136, 0.8) !important;
}

.starry-message.el-message--error .el-message__icon {
  color: rgba(255, 107, 107, 0.8) !important;
}

/* 匹配详情对话框样式 */
.match-detail-dialog {
  background: #1e2c2c !important;  /* 使用深青色背景 */
  border: 1px solid rgba(64, 224, 208, 0.3) !important;
  border-radius: 8px !important;
  box-shadow: 0 0 20px rgba(64, 224, 208, 0.2) !important;
}

.match-detail-dialog .el-message-box__header {
  background: #1e2c2c !important;  /* 保持一致的背景色 */
  padding: 15px 20px !important;
  border-bottom: 1px solid rgba(64, 224, 208, 0.2) !important;
}

.match-detail-dialog .el-message-box__title {
  color: #40E0D0 !important;
  font-weight: bold !important;
}

.match-detail-dialog .el-message-box__headerbtn .el-message-box__close {
  color: #40E0D0 !important;
}

.match-detail-dialog .el-message-box__content {
  background: #1e2c2c !important;
  color: #E0FFFF !important;
  padding: 20px !important;
}

.match-detail-dialog .el-message-box__btns {
  background: #1e2c2c !important;
  padding: 10px 20px 20px !important;
  border-top: 1px solid rgba(64, 224, 208, 0.2) !important;
}

/* 内容区域的样式 */
.match-detail-content > div {
  background: #243333 !important;  /* 稍微浅一点的深青色用于内容块 */
  border: 1px solid rgba(64, 224, 208, 0.2) !important;
  margin-bottom: 15px !important;
  padding: 15px !important;
  border-radius: 8px !important;
}

.match-detail-dialog .el-button {
  background: #40E0D0 !important;
  border: none !important;
  color: #1e2c2c !important;
  padding: 8px 20px !important;
  border-radius: 4px !important;
  font-weight: bold !important;
  transition: all 0.3s ease !important;
}

.match-detail-dialog .el-button:hover {
  background: #48D1CC !important;
  box-shadow: 0 0 10px rgba(64, 224, 208, 0.5) !important;
}

/* 自定义滚动条样式 */
.match-detail-content::-webkit-scrollbar {
  width: 6px;
}

.match-detail-content::-webkit-scrollbar-track {
  background: #1e2c2c;
  border-radius: 3px;
}

.match-detail-content::-webkit-scrollbar-thumb {
  background: rgba(64, 224, 208, 0.5);
  border-radius: 3px;
}

/* 修改标题颜色为绿色 */
.history-title {
  color: #40ff98 !important;  /* 使用亮绿色 */
  font-size: 1.2rem;
  font-weight: bold;
}

/* 修改刷新按钮样式 */
.refresh-button {
  background: rgba(64, 255, 152, 0.1) !important;
  border: 1px solid rgba(64, 255, 152, 0.3) !important;
  border-radius: 50% !important;
  width: 36px !important;
  height: 36px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  cursor: pointer !important;
  transition: all 0.3s ease !important;
  position: relative !important;
}

.refresh-button .refresh-icon {
  color: #40ff98 !important;
  transition: transform 0.3s ease !important;
}

.refresh-button:hover {
  background: rgba(64, 255, 152, 0.2) !important;
  border-color: rgba(64, 255, 152, 0.5) !important;
  box-shadow: 0 0 10px rgba(64, 255, 152, 0.3) !important;
}

.refresh-button:hover .refresh-icon {
  transform: rotate(180deg) !important;
}

/* 刷新按钮激活时的动画 */
.refresh-button.refreshing .refresh-icon {
  animation: rotating 1s linear infinite !important;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>

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
  justify-content: space-between;
  align-items: center;
}

.refresh-button {
  background: linear-gradient(90deg, rgba(0, 210, 255, 0.8) 0%, rgba(0, 255, 136, 0.8) 100%);
  border: none;
}

.refresh-button:hover {
  background: linear-gradient(90deg, rgba(0, 210, 255, 1) 0%, rgba(0, 255, 136, 1) 100%);
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

/* 新增：时间分组样式 */
.history-group {
  margin-bottom: 30px;
}

.time-divider {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.time-label {
  color: #00FF88;
  font-weight: bold;
  margin-right: 15px;
  white-space: nowrap;
}

.divider-line {
  flex-grow: 1;
  height: 1px;
  background: linear-gradient(90deg, rgba(0, 255, 136, 0.5), transparent);
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

/* 新增删除按钮样式 */
.delete-button {
  background: linear-gradient(90deg, rgba(255, 107, 107, 0.8) 0%, rgba(255, 0, 0, 0.8) 100%);
  border: none;
  margin-left: 10px;
}

.delete-button:hover {
  background: linear-gradient(90deg, rgba(255, 107, 107, 1) 0%, rgba(255, 0, 0, 1) 100%);
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