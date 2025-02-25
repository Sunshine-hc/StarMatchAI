<template>
  <div class="app">
    <AppHeader />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useStore } from 'vuex';
import AppHeader from '@/components/AppHeader.vue';

const store = useStore();

onMounted(() => {
  console.log('App mounted');
  
  // 如果有token，尝试获取用户信息
  const token = localStorage.getItem('token');
  console.log('Real token from localStorage:', token);
  
  if (token) {
    // 确保token已设置到store
    store.commit('user/SET_TOKEN', token);
    
    // 验证token是否正确设置到store
    console.log('Token set to store, checking store token:', store.state.user.token);
    
    // 获取用户信息
    setTimeout(() => {
      store.dispatch('user/fetchUserInfo');
    }, 100); // 添加短暂延迟确保token已设置
  } else {
    console.log('App mounted, no token found');
  }
});
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Arial', sans-serif;
  background-color: #1a1d2f;
  color: #ffffff;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding-top: 60px; /* 为固定的header留出空间 */
}
</style>
