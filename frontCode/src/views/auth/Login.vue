<template>
  <div class="login-container">
    <div class="starry-background">
      <div class="shooting-star" v-for="n in 5" :key="n"></div>
    </div>

    <div class="content-wrapper">
      <el-card class="login-card">
        <template #header>
          <div class="card-header">
            <span>AI星座匹配 - 登录</span>
          </div>
        </template>

        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0">
          <div class="form-row">
            <el-form-item prop="email">
              <el-input 
                v-model="loginForm.email" 
                placeholder="邮箱" 
                class="custom-input"
              ></el-input>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="密码" 
                show-password 
                class="custom-input"
              ></el-input>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item>
              <el-button type="primary" @click="handleLogin" :loading="loading" class="submit-button">
                登录
              </el-button>
            </el-form-item>
          </div>

          <div class="form-footer">
            <span>还没有账号？</span>
            <router-link to="/register" class="register-link">立即注册</router-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useStore } from 'vuex';
import { ElMessage } from 'element-plus';

const store = useStore();
const router = useRouter();
const route = useRoute();
const loginFormRef = ref(null);
const loading = ref(false);

const loginForm = reactive({
  email: '',
  password: ''
});

const rules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
};

const handleLogin = async () => {
  try {
    loginFormRef.value.validate(valid => {
      if (valid) {
        loading.value = true;
        console.log('Submitting login form:', loginForm);
        
        fetch(`${import.meta.env.VITE_API_BASE_URL || '/api'}/user/login`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            email: loginForm.email,
            password: loginForm.password
          })
        })
          .then(response => {
            console.log('Login response status:', response.status);
            if (!response.ok) {
              const errorData = response.json();
              console.error('Login error data:', errorData);
              ElMessage.error(errorData.message || '登录失败');
              return;
            }
            return response.json();
          })
          .then(data => {
            console.log('Login success data:', data);
            let token = null;
            if (typeof data === 'object') {
              if (data.token) {
                token = data.token;
              } else if (data.data && data.data.token) {
                token = data.data.token;
              } else if (data.access_token) {
                token = data.access_token;
              }
            }
            
            if (!token) {
              console.error('No token in response:', data);
              ElMessage.error('登录失败：无法获取认证令牌');
              return;
            }
            
            console.log('Extracted token:', token);
            
            localStorage.setItem('token', token);
            console.log('Token stored in localStorage:', localStorage.getItem('token'));
            
            const userInfo = data.user || data.userInfo || data.data || {};
            localStorage.setItem('userInfo', JSON.stringify(userInfo));
            console.log('User info stored in localStorage:', userInfo);
            
            store.commit('user/SET_TOKEN', token);
            store.commit('user/SET_USER_INFO', userInfo);
            
            console.log('Token in Vuex after login:', store.state.user.token);
            console.log('User info in Vuex after login:', store.state.user.userInfo);
            
            ElMessage.success('登录成功');
            
            const redirectPath = route.query.redirect || '/';
            router.push(redirectPath);
          })
          .catch(() => {
            // 错误处理已在请求拦截器中完成
          })
          .finally(() => {
            loading.value = false;
          });
      }
    });
  } catch (error) {
    console.error('Login error:', error);
    ElMessage.error('登录失败，请稍后重试');
  }
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
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
}

.login-card {
  width: 400px;
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

.form-row {
  margin-bottom: 20px;
}

.custom-input :deep(.el-input__inner) {
  background-color: #1e2235 !important;
  border: 1px solid rgba(0, 255, 136, 0.2) !important;
  color: #ffffff !important;
  height: 44px !important;
  border-radius: 22px !important;
  box-shadow: none !important;
  text-align: center !important;
}

.custom-input :deep(.el-input__inner::placeholder) {
  text-align: center !important;
  color: rgba(255, 255, 255, 0.6) !important;
}

.custom-input :deep(.el-input__prefix) {
  color: #00FF88 !important;
}

/* 确保输入框占满整个宽度 */
.custom-input {
  width: 100% !important;
}

/* 修复可能的内边距问题 */
.custom-input :deep(.el-input__wrapper) {
  padding: 0 !important;
  background-color: transparent !important;
  box-shadow: none !important;
}

.submit-button {
  width: 100%;
  height: 44px;
  background: linear-gradient(90deg, rgba(0, 255, 136, 0.8) 0%, rgba(0, 210, 255, 0.8) 100%);
  border: none;
  border-radius: 22px;
  font-size: 16px;
  font-weight: bold;
  color: #1a1d2f;
}

.submit-button:hover {
  background: linear-gradient(90deg, rgba(0, 255, 136, 1) 0%, rgba(0, 210, 255, 1) 100%);
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.register-link {
  color: #00FF88;
  margin-left: 5px;
  text-decoration: none;
}

.register-link:hover {
  text-decoration: underline;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .login-card {
    width: 90%;
  }
}
</style> 