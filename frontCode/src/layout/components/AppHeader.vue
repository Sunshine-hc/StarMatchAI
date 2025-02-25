<template>
  <header class="app-header">
    <div class="logo">
      <router-link to="/">星座匹配系统</router-link>
    </div>
    <div class="nav-menu">
      <router-link to="/home" class="nav-item">首页</router-link>
      <!-- 其他导航项 -->
    </div>
    <div class="user-info">
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="el-dropdown-link">
          {{ userInfo?.nickname || userInfo?.email || '用户' }}
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';

const store = useStore();
const router = useRouter();

const userInfo = computed(() => store.state.user.userInfo);

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile');
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      store.dispatch('user/logout').then(() => {
        ElMessage.success('已退出登录');
        router.push('/login');
      });
    }).catch(() => {});
  }
};
</script>

<style scoped>
.app-header {
  height: 60px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  margin-right: 40px;
}

.logo a {
  color: #409eff;
  text-decoration: none;
}

.nav-menu {
  flex: 1;
  display: flex;
}

.nav-item {
  margin-right: 20px;
  color: #333;
  text-decoration: none;
}

.nav-item:hover, .nav-item.router-link-active {
  color: #409eff;
}

.user-info {
  margin-left: 20px;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}
</style> 