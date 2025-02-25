<template>
  <div class="register-container">
    <div class="starry-background">
      <div class="shooting-star" v-for="n in 5" :key="n"></div>
    </div>

    <div class="content-wrapper">
      <el-card class="register-card">
        <template #header>
          <div class="card-header">
            <span>AI星座匹配 - 注册</span>
          </div>
        </template>

        <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="0">
          <div class="form-row">
            <el-form-item prop="email">
              <el-input 
                v-model="registerForm.email" 
                placeholder="邮箱" 
                class="custom-input has-append"
              >
                <template #append>
                  <el-button 
                    :disabled="cooldown > 0" 
                    @click="sendCode" 
                    :loading="sendingCode"
                    class="code-button"
                  >
                    {{ cooldown > 0 ? `${cooldown}秒` : '获取验证码' }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item prop="code">
              <el-input 
                v-model="registerForm.code" 
                placeholder="验证码" 
                class="custom-input"
              ></el-input>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="密码" show-password class="custom-input"></el-input>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" show-password class="custom-input"></el-input>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item prop="nickname">
              <el-input v-model="registerForm.nickname" placeholder="昵称（选填）" class="custom-input"></el-input>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item>
              <el-button type="primary" @click="handleRegister" :loading="loading" class="submit-button">
                注册
              </el-button>
            </el-form-item>
          </div>

          <div class="form-footer">
            <span>已有账号？</span>
            <router-link to="/login" class="login-link">立即登录</router-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { register, sendVerificationCode } from '@/api/user';

const router = useRouter();
const registerFormRef = ref(null);
const loading = ref(false);
const sendingCode = ref(false);
const cooldown = ref(0);
let timer = null;

const registerForm = reactive({
  email: '',
  code: '',
  password: '',
  confirmPassword: '',
  nickname: ''
});

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'));
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword');
    }
    callback();
  }
};

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'));
  } else {
    callback();
  }
};

const rules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 6, max: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  nickname: [
    { required: false, message: '请输入昵称', trigger: 'blur' }
  ]
};

// 发送验证码
const sendCode = async () => {
  try {
    // 验证邮箱
    await registerFormRef.value.validateField('email');
    
    sendingCode.value = true;
    console.log('发送验证码到邮箱:', registerForm.email);
    
    // 调用发送验证码API
    const response = await sendVerificationCode({
      email: registerForm.email,
      type: 1  // 使用数字1表示注册类型
    });
    
    console.log('验证码发送响应:', response);
    
    ElMessage.success('验证码已发送，请查收邮箱');
    
    // 开始倒计时
    cooldown.value = 60;
    timer = setInterval(() => {
      cooldown.value--;
      if (cooldown.value <= 0) {
        clearInterval(timer);
      }
    }, 1000);
  } catch (error) {
    console.error('发送验证码失败:', error);
    ElMessage.error(error.message || '发送验证码失败，请稍后重试');
  } finally {
    sendingCode.value = false;
  }
};

// 注册
const handleRegister = () => {
  registerFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true;
        
        // 调用注册API
        await register({
          email: registerForm.email,
          code: registerForm.code,
          password: registerForm.password,
          nickname: registerForm.nickname || undefined
        });
        
        ElMessage.success('注册成功，请登录');
        router.push('/login');
      } catch (error) {
        console.error('注册失败:', error);
        // 错误处理已在请求拦截器中完成
      } finally {
        loading.value = false;
      }
    }
  });
};

// 组件卸载时清除定时器
onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer);
  }
});
</script>

<style scoped>
.register-container {
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

.register-card {
  width: 450px;
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

/* 更全面的输入框样式覆盖 */
.custom-input {
  width: 100% !important;
}

.custom-input :deep(.el-input__wrapper) {
  background-color: #1e2235 !important;
  box-shadow: none !important;
  padding: 0 !important;
  border-radius: 22px !important;
}

.custom-input :deep(.el-input__inner) {
  background-color: #1e2235 !important;
  border: none !important;
  color: #ffffff !important;
  height: 44px !important;
  text-align: center !important;
}

.custom-input :deep(.el-input__inner::placeholder) {
  text-align: center !important;
  color: rgba(255, 255, 255, 0.6) !important;
}

/* 对于带有验证码按钮的输入框，文本左对齐 */
.custom-input.has-append :deep(.el-input__inner) {
  text-align: left !important;
  padding-left: 15px !important;
}

.custom-input.has-append :deep(.el-input__inner::placeholder) {
  text-align: left !important;
}

/* 添加自定义边框，避免使用Element Plus的box-shadow */
.form-row {
  position: relative;
}

.form-row::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 22px;
  border: 1px solid rgba(0, 255, 136, 0.2);
  pointer-events: none;
}

/* 获取验证码按钮样式 */
.code-button {
  background: linear-gradient(90deg, rgba(0, 255, 136, 0.8) 0%, rgba(0, 210, 255, 0.8) 100%) !important;
  color: #1a1d2f !important;
  border: none !important;
  border-radius: 0 22px 22px 0 !important;
  height: 44px !important;
  font-weight: bold !important;
  z-index: 2 !important;
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

.login-link {
  color: #00FF88;
  margin-left: 5px;
  text-decoration: none;
}

.login-link:hover {
  text-decoration: underline;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .register-card {
    width: 90%;
  }
}

/* 加载动画 */
.loading-dots {
  display: inline-flex;
  gap: 4px;
  height: 20px;
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
</style> 