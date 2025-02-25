<template>
  <div class="match-detail-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>匹配详情</span>
          <div>
            <el-button @click="goBack">返回</el-button>
            <el-button type="primary" @click="goToCreate">新建匹配</el-button>
          </div>
        </div>
      </template>

      <div v-if="matchDetail" class="detail-content">
        <div class="match-header">
          <div class="match-signs">
            <div class="sign-item">
              <div class="sign-icon">{{ matchDetail.person1Sign.charAt(0) }}</div>
              <div class="sign-name">{{ matchDetail.person1Sign }}</div>
              <div class="sign-date">{{ matchDetail.person1Birthday }}</div>
            </div>
            <div class="match-vs">VS</div>
            <div class="sign-item">
              <div class="sign-icon">{{ matchDetail.person2Sign.charAt(0) }}</div>
              <div class="sign-name">{{ matchDetail.person2Sign }}</div>
              <div class="sign-date">{{ matchDetail.person2Birthday }}</div>
            </div>
          </div>
          
          <div class="match-score">
            <h3>匹配分数</h3>
            <el-progress type="dashboard" :percentage="matchDetail.matchScore" :color="getProgressColor(matchDetail.matchScore)" />
            <div class="score-text">{{ getScoreText(matchDetail.matchScore) }}</div>
          </div>
        </div>

        <el-divider />

        <div class="detail-section">
          <h3>整体分析</h3>
          <p>{{ matchDetail.analysis }}</p>
        </div>

        <div class="detail-section">
          <h3>优势特点</h3>
          <p>{{ matchDetail.advantages }}</p>
        </div>

        <div class="detail-section">
          <h3>潜在问题</h3>
          <p>{{ matchDetail.disadvantages }}</p>
        </div>

        <div class="detail-section">
          <h3>相处建议</h3>
          <p>{{ matchDetail.suggestions }}</p>
        </div>

        <div class="detail-footer">
          <span>匹配时间: {{ formatDateTime(matchDetail.createdAt) }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getMatchDetail } from '@/api/match';

const router = useRouter();
const route = useRoute();
const loading = ref(true);
const matchDetail = ref(null);

const getProgressColor = (score) => {
  if (score >= 80) return '#67C23A';
  if (score >= 60) return '#E6A23C';
  return '#F56C6C';
};

const getScoreText = (score) => {
  if (score >= 90) return '极佳匹配';
  if (score >= 80) return '优秀匹配';
  if (score >= 70) return '良好匹配';
  if (score >= 60) return '一般匹配';
  if (score >= 50) return '勉强匹配';
  return '不推荐匹配';
};

const formatDateTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

const goBack = () => {
  router.back();
};

const goToCreate = () => {
  router.push('/match/create');
};

onMounted(() => {
  const id = route.params.id;
  if (id) {
    getMatchDetail(id)
      .then(response => {
        matchDetail.value = response.data;
      })
      .catch(() => {
        ElMessage.error('获取匹配详情失败');
      })
      .finally(() => {
        loading.value = false;
      });
  } else {
    ElMessage.error('参数错误');
    loading.value = false;
  }
});
</script>

<style scoped>
.match-detail-container {
  max-width: 800px;
  margin: 20px auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-content {
  padding: 20px 0;
}

.match-header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  margin-bottom: 30px;
}

.match-signs {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 300px;
}

.sign-item {
  text-align: center;
  flex: 1;
}

.sign-icon {
  width: 60px;
  height: 60px;
  line-height: 60px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  font-size: 24px;
  margin: 0 auto 10px;
}

.sign-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}

.match-vs {
  font-size: 20px;
  font-weight: bold;
  color: #909399;
  margin: 0 20px;
}

.match-score {
  text-align: center;
  flex: 1;
  min-width: 200px;
}

.score-text {
  margin-top: 10px;
  font-size: 16px;
  font-weight: bold;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h3 {
  color: #409EFF;
  margin-bottom: 10px;
}

.detail-section p {
  line-height: 1.6;
  text-align: justify;
}

.detail-footer {
  margin-top: 30px;
  text-align: right;
  color: #909399;
  font-size: 14px;
}
</style> 