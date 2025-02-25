<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <h2>欢迎使用星座匹配系统</h2>
      <p>在这里，您可以通过星座分析了解您与他人的匹配程度</p>
      
      <el-button type="primary" @click="startMatch" class="start-btn">开始匹配</el-button>
    </el-card>
    
    <el-card class="history-card" v-if="matchHistory.length > 0">
      <template #header>
        <div class="card-header">
          <h3>最近匹配记录</h3>
          <router-link to="/history">查看全部</router-link>
        </div>
      </template>
      
      <el-table :data="matchHistory" style="width: 100%">
        <el-table-column prop="person1Sign" label="星座一" width="120"></el-table-column>
        <el-table-column prop="person2Sign" label="星座二" width="120"></el-table-column>
        <el-table-column prop="matchScore" label="匹配分数" width="120">
          <template #default="scope">
            <el-tag :type="getScoreType(scope.row.matchScore)">{{ scope.row.matchScore }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="匹配时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="text" @click="viewDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
// 假设您有一个获取匹配历史的API
// import { getMatchHistory } from '@/api/match';

const router = useRouter();
const matchHistory = ref([]);
const loading = ref(false);

onMounted(() => {
  fetchMatchHistory();
});

const fetchMatchHistory = () => {
  loading.value = true;
  // 这里应该调用实际的API
  // getMatchHistory({ pageNum: 1, pageSize: 5 })
  //   .then(response => {
  //     matchHistory.value = response.data;
  //   })
  //   .catch(() => {
  //     ElMessage.error('获取匹配历史失败');
  //   })
  //   .finally(() => {
  //     loading.value = false;
  //   });
  
  // 模拟数据
  setTimeout(() => {
    matchHistory.value = [
      {
        id: 1,
        person1Sign: '白羊座',
        person2Sign: '天秤座',
        matchScore: 75,
        createdAt: '2023-05-20 14:30:00'
      },
      {
        id: 2,
        person1Sign: '双子座',
        person2Sign: '水瓶座',
        matchScore: 90,
        createdAt: '2023-05-18 10:15:00'
      }
    ];
    loading.value = false;
  }, 500);
};

const startMatch = () => {
  router.push('/match');
};

const viewDetail = (row) => {
  router.push(`/match/detail/${row.id}`);
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

const getScoreType = (score) => {
  if (score >= 85) return 'success';
  if (score >= 70) return '';
  if (score >= 50) return 'warning';
  return 'danger';
};
</script>

<style scoped>
.home-container {
  max-width: 1000px;
  margin: 0 auto;
}

.welcome-card {
  text-align: center;
  padding: 30px;
  margin-bottom: 30px;
}

.welcome-card h2 {
  font-size: 24px;
  margin-bottom: 15px;
}

.welcome-card p {
  color: #666;
  margin-bottom: 30px;
}

.start-btn {
  padding: 12px 30px;
  font-size: 16px;
}

.history-card {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header a {
  color: #409eff;
  font-size: 14px;
}
</style> 