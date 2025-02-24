<template>
    <div class="match-container">
        <el-card class="match-form">
            <template #header>
                <div class="card-header">
                    <span>星座匹配测试</span>
                </div>
            </template>

            <el-form :model="matchForm" :rules="rules" ref="matchFormRef" label-width="80px">
                <div class="form-row">
                    <el-form-item label="生日选择" prop="person1Birthday" class="birthday-item">
                        <el-date-picker v-model="matchForm.person1Birthday" type="date" placeholder="选择日期"
                            format="YYYY-MM-DD" value-format="YYYY-MM-DD" :clearable="false" style="width: 240px" />
                        <span class="separator">×</span>
                        <el-date-picker v-model="matchForm.person2Birthday" type="date" placeholder="选择日期"
                            format="YYYY-MM-DD" value-format="YYYY-MM-DD" :clearable="false" style="width: 240px" />
                    </el-form-item>
                </div>

                <div class="form-row actions">
                    <el-form-item label="AI模型" prop="aiModel" class="model-item">
                        <el-select v-model="matchForm.aiModel" placeholder="请选择AI模型" style="width: 240px">
                            <el-option v-for="option in aiModelOptions" :key="option.value" :label="option.label"
                                :value="option.value" />
                        </el-select>
                    </el-form-item>

                    <el-button type="primary" @click="handleSubmit" :loading="loading" class="submit-button">
                        开始匹配
                    </el-button>
                </div>
            </el-form>
        </el-card>

        <!-- 匹配结果展示 -->
        <el-card class="match-result" v-if="showResult">
            <template #header>
                <div class="card-header">
                    <span>匹配结果</span>
                </div>
            </template>

            <div class="result-content">
                <!-- 匹配分数 -->
                <div class="score-section">
                    <el-progress type="circle" :percentage="Number(matchResult.matchScore) || 0"
                        :color="getScoreColor" />
                    <div class="score-text">
                        匹配度: {{ matchResult.matchScore || 0 }}%
                    </div>
                </div>

                <div class="detail-section">
                    <!-- 星座组合 -->
                    <div class="section">
                        <h3>星座组合</h3>
                        <div v-if="matchResult.person1Sign && matchResult.person2Sign">
                            {{ matchResult.person1Sign }} × {{ matchResult.person2Sign }}
                        </div>
                        <div v-else>加载中...</div>
                    </div>

                    <!-- 整体分析 -->
                    <div class="section">
                        <h3>整体分析</h3>
                        <div class="content">{{ displayContent.analysis.value || '分析中...' }}</div>
                    </div>

                    <!-- 优势特点 -->
                    <div class="section">
                        <h3>优势特点</h3>
                        <div class="content">{{ displayContent.advantages.value || '分析中...' }}</div>
                    </div>

                    <!-- 潜在问题 -->
                    <div class="section">
                        <h3>潜在问题</h3>
                        <div class="content">{{ displayContent.disadvantages.value || '分析中...' }}</div>
                    </div>

                    <!-- 相处建议 -->
                    <div class="section">
                        <h3>相处建议</h3>
                        <div class="content">{{ displayContent.suggestions.value || '分析中...' }}</div>
                    </div>
                </div>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, computed, reactive, watch } from 'vue'
import { calculateMatchStream } from '@/api/match'
import { ElMessage } from 'element-plus'

const matchFormRef = ref(null)
const loading = ref(false)
const showResult = ref(false)

// 使用 reactive 而不是 ref 来存储结果
const matchResult = reactive({
    person1Sign: '',
    person2Sign: '',
    matchScore: 0,
    analysis: '',
    advantages: '',
    disadvantages: '',
    suggestions: '',
    loading: {
        score: true,
        analysis: true,
        advantages: true,
        disadvantages: true,
        suggestions: true
    }
})

// 使用独立的 ref 来存储显示内容
const displayContent = {
    analysis: ref(''),
    advantages: ref(''),
    disadvantages: ref(''),
    suggestions: ref('')
};

const matchForm = ref({
    person1Birthday: '',
    person2Birthday: '',
    aiModel: 'deepseek'
})

const rules = {
    person1Birthday: [
        { required: true, message: '请选择第一个人的生日', trigger: 'change' }
    ],
    person2Birthday: [
        { required: true, message: '请选择第二个人的生日', trigger: 'change' }
    ],
    aiModel: [
        { required: true, message: '请选择AI模型', trigger: 'change' }
    ]
}

const getScoreColor = computed(() => {
    const score = matchResult.matchScore || 0
    if (score >= 80) return '#67C23A'
    if (score >= 60) return '#E6A23C'
    return '#F56C6C'
})

// 修改 typeWriter 函数
const typeWriter = async (text, target, speed = 50) => {
    let index = 0;
    target.value = '';

    return new Promise((resolve) => {
        const timer = setInterval(() => {
            if (index < text.length) {
                target.value += text[index];
                index++;
            } else {
                clearInterval(timer);
                resolve();
            }
        }, speed);
    });
};

// 修改匹配提交函数
const submitMatch = async () => {
    try {
        loading.value = true
        showResult.value = true

        // 重置状态
        Object.assign(matchResult, {
            person1Sign: '',
            person2Sign: '',
            matchScore: 0,
            analysis: '',
            advantages: '',
            disadvantages: '',
            suggestions: ''
        })

        // 重置显示内容
        displayContent.analysis.value = '';
        displayContent.advantages.value = '';
        displayContent.disadvantages.value = '';
        displayContent.suggestions.value = '';

        let hasError = false;  // 添加错误标记

        await calculateMatchStream(matchForm.value, async (eventData) => {
            // 检查是否是错误事件
            if (eventData.event === 'error') {
                hasError = true;  // 设置错误标记
                ElMessage.error(eventData.data);
                showResult.value = false;
                return;
            }

            if (!hasError) {  // 只有在没有错误时才处理数据
                switch (eventData.event) {
                    case 'signs':
                        matchResult.person1Sign = eventData.data.person1Sign;
                        matchResult.person2Sign = eventData.data.person2Sign;
                        break;
                    case 'score':
                        matchResult.matchScore = parseInt(eventData.data);
                        break;
                    case 'analysis':
                        matchResult.analysis = eventData.data;
                        await typeWriter(eventData.data, displayContent.analysis);
                        break;
                    case 'advantages':
                        matchResult.advantages = eventData.data;
                        await typeWriter(eventData.data, displayContent.advantages);
                        break;
                    case 'disadvantages':
                        matchResult.disadvantages = eventData.data;
                        await typeWriter(eventData.data, displayContent.disadvantages);
                        break;
                    case 'suggestions':
                        matchResult.suggestions = eventData.data;
                        await typeWriter(eventData.data, displayContent.suggestions);
                        break;
                }
            }
        });

        // 只有在没有错误时才显示成功消息
        if (!hasError) {
            ElMessage.success('匹配分析完成');
        }
    } catch (error) {
        console.error('Match error:', error);
        ElMessage.error(error.message || '匹配分析失败');
        showResult.value = false;
    } finally {
        loading.value = false
    }
}

// 修改提交处理函数
const handleSubmit = async () => {
    if (!matchFormRef.value) return

    try {
        // 表单验证
        await matchFormRef.value.validate()
        // 验证通过后调用匹配
        await submitMatch()
    } catch (error) {
        console.log('表单验证失败')
    }
}

// 修改AI模型选项，只保留支持的模型
const aiModelOptions = [
    { label: 'DeepSeek', value: 'deepseek' },
    { label: '文心一言', value: 'wenxin' }
]
</script>

<style scoped>
.match-container {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
}

.match-form {
    margin-bottom: 20px;
}

.form-row {
    margin-bottom: 18px;
    display: flex;
    align-items: center;
}

.birthday-item {
    margin-bottom: 0;
    display: flex;
    align-items: center;
}

.separator {
    margin: 0 15px;
    color: #909399;
    font-size: 16px;
}

.actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.model-item {
    margin-bottom: 0;
}

.submit-button {
    margin-right: 20px;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
    .birthday-item {
        flex-direction: column;
        align-items: flex-start;
    }

    .separator {
        margin: 10px 0;
    }

    .actions {
        flex-direction: column;
        align-items: stretch;
    }

    .submit-button {
        margin-top: 20px;
        margin-right: 0;
        width: 100%;
    }

    .el-date-picker,
    .el-select {
        width: 100% !important;
    }
}

.card-header {
    font-size: 18px;
    font-weight: bold;
}

.result-content {
    display: flex;
    gap: 40px;
}

.score-section {
    text-align: center;
    min-width: 200px;
}

.score-text {
    margin-top: 10px;
    font-size: 16px;
    font-weight: bold;
}

.detail-section {
    flex: 1;
}

.section {
    margin-bottom: 20px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 8px;
}

.section h3 {
    margin-bottom: 10px;
    color: #409EFF;
}

.content {
    white-space: pre-wrap;
    line-height: 1.6;
}

.loading {
    color: #999;
    font-style: italic;
}
</style>