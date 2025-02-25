<template>
  <div class="create-match-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>创建星座匹配</span>
        </div>
      </template>

      <el-form :model="matchForm" :rules="rules" ref="matchFormRef" label-width="120px">
        <el-form-item label="第一个星座" prop="person1Sign">
          <el-select v-model="matchForm.person1Sign" placeholder="请选择星座" style="width: 100%">
            <el-option v-for="sign in zodiacSigns" :key="sign" :label="sign" :value="sign" />
          </el-select>
        </el-form-item>

        <el-form-item label="第一个生日" prop="person1Birthday">
          <el-date-picker v-model="matchForm.person1Birthday" type="date" placeholder="选择日期" 
            format="MM-DD" value-format="MM-DD" style="width: 100%" />
        </el-form-item>

        <el-form-item label="第二个星座" prop="person2Sign">
          <el-select v-model="matchForm.person2Sign" placeholder="请选择星座" style="width: 100%">
            <el-option v-for="sign in zodiacSigns" :key="sign" :label="sign" :value="sign" />
          </el-select>
        </el-form-item>

        <el-form-item label="第二个生日" prop="person2Birthday">
          <el-date-picker v-model="matchForm.person2Birthday" type="date" placeholder="选择日期" 
            format="MM-DD" value-format="MM-DD" style="width: 100%" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">开始匹配</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { createMatch } from '@/api/match';

const router = useRouter();
const matchFormRef = ref(null);
const loading = ref(false);

const zodiacSigns = [
  '白羊座', '金牛座', '双子座', '巨蟹座', '狮子座', '处女座',
  '天秤座', '天蝎座', '射手座', '摩羯座', '水瓶座', '双鱼座'
];

const matchForm = reactive({
  person1Sign: '',
  person1Birthday: '',
  person2Sign: '',
  person2Birthday: ''
});

const rules = {
  person1Sign: [{ required: true, message: '请选择第一个星座', trigger: 'change' }],
  person1Birthday: [{ required: true, message: '请选择第一个生日', trigger: 'change' }],
  person2Sign: [{ required: true, message: '请选择第二个星座', trigger: 'change' }],
  person2Birthday: [{ required: true, message: '请选择第二个生日', trigger: 'change' }]
};

const handleSubmit = () => {
  matchFormRef.value.validate(valid => {
    if (valid) {
      loading.value = true;
      createMatch(matchForm)
        .then(response => {
          ElMessage.success('匹配成功');
          router.push(`/match/detail/${response.data.id}`);
        })
        .catch(() => {
          // 错误处理已在拦截器中完成
        })
        .finally(() => {
          loading.value = false;
        });
    }
  });
};

const resetForm = () => {
  matchFormRef.value.resetFields();
};
</script>

<style scoped>
.create-match-container {
  max-width: 600px;
  margin: 20px auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}
</style> 