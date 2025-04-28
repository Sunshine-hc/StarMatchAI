<template>
  <header class="app-header">
    <div class="header-container">
      <div class="header-left">
        <router-link to="/" class="nav-item">{{ $t('header.starMatch') }}</router-link>
        <router-link v-if="isLoggedIn" to="/history" class="nav-item">{{ $t('header.history') }}</router-link>
      </div>
      
      <div class="header-right">
        <LanguageSwitch />
        <template v-if="isLoggedIn">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <div class="avatar">
                {{ userNameFirstChar }}
              </div>
              <span class="user-name">{{ userName }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">{{ $t('header.profile') }}</el-dropdown-item>
                <el-dropdown-item command="logout">{{ $t('header.logout') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login" class="login-btn">{{ $t('header.login') }}</router-link>
          <span class="divider">/</span>
          <router-link to="/register" class="register-btn">{{ $t('header.register') }}</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ArrowDown } from '@element-plus/icons-vue';
import LanguageSwitch from '@/components/LanguageSwitch.vue';

const { t } = useI18n();
const store = useStore();
const router = useRouter();

// 从Vuex和localStorage双重检查登录状态
const isLoggedIn = computed(() => {
  const hasToken = !!localStorage.getItem('token');
  const storeLoggedIn = store.getters['user/isLoggedIn'];
  console.log('Login status check - localStorage:', hasToken, 'Vuex:', storeLoggedIn);
  return hasToken || storeLoggedIn;
});

// 从Vuex和localStorage获取用户名
const userName = computed(() => {
  let name = store.getters['user/userName'];
  
  // 如果Vuex中没有用户名，尝试从localStorage获取
  if (!name || name === '用户') {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      name = userInfo.nickname || userInfo.username || userInfo.email || t('header.user');
    } catch (e) {
      console.error('Error parsing user info from localStorage:', e);
    }
  }
  
  console.log('User name computed:', name);
  return name;
});

// 获取用户名的第一个字符作为默认头像
const userNameFirstChar = computed(() => {
  return userName.value ? userName.value.charAt(0) : t('header.userInitial');
});

// 监听登录状态变化
watch(() => isLoggedIn.value, (newVal) => {
  console.log('Login status changed:', newVal);
  if (newVal) {
    // 登录状态变为true时，尝试获取用户信息
    store.dispatch('user/fetchUserInfo');
  }
}, { immediate: true });

// 监听用户信息变化
watch(() => store.state.user.userInfo, (newVal) => {
  console.log('User info changed:', newVal);
}, { deep: true });

const handleCommand = (command) => {
  if (command === 'logout') {
    console.log('Logging out...');
    store.dispatch('user/logout');
    router.push('/login');
  } else if (command === 'profile') {
    // 跳转到个人中心页面
    router.push('/profile');
  }
};

// 初始化时检查登录状态
onMounted(() => {
  console.log('AppHeader mounted, checking login status');
  if (isLoggedIn.value) {
    console.log('User is logged in, fetching user info');
    store.dispatch('user/fetchUserInfo');
  }
});
</script>

<style scoped>
.app-header {
  width: 100%;
  height: 60px;
  background-color: rgba(30, 34, 53, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 255, 136, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
}

.header-container {
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left, .header-right {
  display: flex;
  align-items: center;
}

.nav-item {
  color: #ffffff;
  text-decoration: none;
  font-size: 16px;
  margin-right: 20px;
  transition: color 0.3s;
}

.nav-item:hover, .nav-item.router-link-active {
  color: #00FF88;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 8px;
  background: #00FF88;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #1a1d2f;
  font-weight: bold;
  font-size: 16px;
}

.user-name {
  color: #ffffff;
  font-size: 14px;
  margin-right: 5px;
}

.login-btn, .register-btn {
  color: #00FF88;
  text-decoration: none;
  cursor: pointer;
  transition: opacity 0.3s;
  font-size: 14px;
}

.login-btn:hover, .register-btn:hover {
  opacity: 0.8;
}

.divider {
  color: rgba(255, 255, 255, 0.5);
  margin: 0 8px;
}

:deep(.el-dropdown-menu) {
  background-color: rgba(42, 47, 79, 0.9) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
}

:deep(.el-dropdown-menu__item) {
  color: #FFFFFF !important;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  color: #00FF88 !important;
}

:deep(.el-icon--right) {
  color: #FFFFFF;
  font-size: 12px;
  margin-left: 5px;
}
</style> 