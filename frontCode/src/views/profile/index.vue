<template>
  <div class="profile-container">
    <div class="starry-background">
      <div class="shooting-star" v-for="n in 5" :key="n"></div>
    </div>
    
    <div class="content-wrapper">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span>{{ $t('profile.title') }}</span>
          </div>
        </template>
        
        <div class="profile-content">
          <div class="avatar-container">
            <div class="profile-avatar">
              {{ userNameFirstChar }}
            </div>
          </div>
          
          <div class="user-details">
            <h2 class="user-nickname">{{ userName }}</h2>
            <p class="join-date">{{ $t('profile.memberSince') }}: {{ formattedJoinDate }}</p>
          </div>
          
          <div class="profile-section">
            <h3 class="section-title">{{ $t('profile.accountInfo') }}</h3>
            <el-divider />
            <div class="info-row">
              <span class="info-label">{{ $t('profile.email') }}:</span>
              <span class="info-value">{{ userEmail }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const store = useStore();

// 获取用户信息
const userName = computed(() => {
  const userInfo = store.state.user.userInfo;
  return userInfo.nickname || userInfo.username || userInfo.email || t('header.user');
});

const userEmail = computed(() => {
  return store.state.user.userInfo.email || t('profile.noEmail');
});

// 获取用户名的第一个字符作为默认头像
const userNameFirstChar = computed(() => {
  return userName.value ? userName.value.charAt(0) : t('header.userInitial');
});

// 格式化加入日期（使用当前日期作为示例）
const formattedJoinDate = computed(() => {
  // 如果有创建时间，使用它，否则使用当前日期
  const createdAt = store.state.user.userInfo.createTime;
  const date = createdAt ? new Date(createdAt) : new Date();
  return date.toLocaleDateString();
});

// 初始化时检查登录状态并获取用户信息
onMounted(() => {
  console.log('Profile page mounted, checking user info');
  if (store.getters['user/isLoggedIn']) {
    store.dispatch('user/fetchUserInfo');
  }
});
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #1a1d2f;
  position: relative;
  overflow: hidden;
  padding-top: 60px; /* 为顶部导航栏留出空间 */
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
  animation: shooting 8s linear infinite;
}

.shooting-star:nth-child(1) {
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shooting-star:nth-child(2) {
  top: 20%;
  left: 30%;
  animation-delay: 1.5s;
}

.shooting-star:nth-child(3) {
  top: 30%;
  left: 50%;
  animation-delay: 3s;
}

.shooting-star:nth-child(4) {
  top: 40%;
  left: 70%;
  animation-delay: 4.5s;
}

.shooting-star:nth-child(5) {
  top: 50%;
  left: 90%;
  animation-delay: 6s;
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
}

.profile-card {
  width: 600px;
  max-width: 90%;
  border-radius: 12px;
  overflow: hidden;
  background-color: rgba(30, 34, 53, 0.8);
  border: 1px solid rgba(0, 255, 136, 0.2);
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  justify-content: center;
  font-size: 20px;
  color: #00FF88;
  padding: 15px 0;
}

.profile-content {
  padding: 30px;
}

.avatar-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.profile-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: #00FF88;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #1a1d2f;
  font-weight: bold;
  font-size: 40px;
  box-shadow: 0 0 20px rgba(0, 255, 136, 0.5);
}

.user-details {
  text-align: center;
  margin-bottom: 30px;
}

.user-nickname {
  color: #ffffff;
  font-size: 24px;
  margin-bottom: 8px;
}

.join-date {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.profile-section {
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 20px;
  margin-top: 20px;
}

.section-title {
  color: #00FF88;
  font-size: 18px;
  margin-bottom: 10px;
}

.info-row {
  display: flex;
  margin: 10px 0;
  align-items: center;
}

.info-label {
  color: rgba(255, 255, 255, 0.6);
  width: 100px;
}

.info-value {
  color: #ffffff;
  flex: 1;
}

:deep(.el-divider) {
  background-color: rgba(0, 255, 136, 0.2);
  margin: 12px 0;
}
</style> 