<template>
    <div class="match-container">
        <div class="starry-background">
            <div class="shooting-star" v-for="n in 5" :key="n"></div>
        </div>

        <div class="content-wrapper">
            <!-- 表单卡片 -->
            <el-card class="match-card form-card">
                <template #header>
                    <div class="card-header">
                        <span>星座匹配测试</span>
                    </div>
                </template>

                <el-form :model="matchForm" :rules="rules" ref="matchFormRef" label-width="100px">
                    <div class="form-row">
                        <el-form-item label="生日选择" required>
                            <div class="birthday-inputs">
                                <el-form-item prop="person1Birthday" class="birthday-item" label-width="0">
                                    <el-date-picker v-model="matchForm.person1Birthday" type="date" placeholder="选择日期"
                                        format="YYYY-MM-DD" value-format="YYYY-MM-DD" :clearable="false"
                                        class="date-picker" />
                                </el-form-item>

                                <span class="separator">×</span>

                                <el-form-item prop="person2Birthday" class="birthday-item" label-width="0">
                                    <el-date-picker v-model="matchForm.person2Birthday" type="date" placeholder="选择日期"
                                        format="YYYY-MM-DD" value-format="YYYY-MM-DD" :clearable="false"
                                        class="date-picker" />
                                </el-form-item>
                            </div>
                        </el-form-item>
                    </div>

                    <div class="form-row">
                        <el-form-item label="AI模型" prop="aiModel">
                            <el-select v-model="matchForm.aiModel" placeholder="请选择AI模型" class="model-select">
                                <el-option v-for="option in aiModelOptions" :key="option.value" :label="option.label"
                                    :value="option.value" />
                            </el-select>
                        </el-form-item>
                    </div>

                    <div class="form-row">
                        <el-form-item>
                            <el-button type="primary" @click="handleSubmit" :loading="loading" class="submit-button">
                                开始匹配
                            </el-button>
                        </el-form-item>
                    </div>
                </el-form>
            </el-card>

            <!-- 结果卡片 -->
            <el-card v-if="showResult" class="match-card result-card">
                <div v-if="loading" class="loading-card">
                    <div class="loading-animation">
                        <div class="galaxy">
                            <div v-for="n in 12" :key="n" class="star"></div>
                        </div>
                        <p>正在进行星座匹配分析...</p>
                    </div>
                </div>

                <div v-else class="result-content">
                    <div class="result-header">
                        <div class="zodiac-pair">
                            <div class="zodiac-item">
                                <div class="zodiac-icon">♈</div>
                                <div class="zodiac-name">{{ matchResult.person1Sign }}</div>
                            </div>
                            <div class="match-score">
                                <div class="score-circle">
                                    {{ matchResult.matchScore }}
                                    <span class="score-unit">%</span>
                                </div>
                            </div>
                            <div class="zodiac-item">
                                <div class="zodiac-icon">♊</div>
                                <div class="zodiac-name">{{ matchResult.person2Sign }}</div>
                            </div>
                        </div>
                    </div>

                    <!-- 分析总结部分 -->
                    <div class="summary-section">
                        <h3 class="section-title">分析总结</h3>
                        <div class="summary-content">
                            <div class="summary-icon">
                                <i class="el-icon-document"></i>
                            </div>
                            <p class="summary-text">{{ displayContent.analysis }}</p>
                        </div>
                    </div>

                    <div class="details-section">
                        <div class="detail-card">
                            <h4 class="detail-title">优势特点</h4>
                            <p class="detail-text">{{ displayContent.advantages }}</p>
                        </div>

                        <div class="detail-card">
                            <h4 class="detail-title">潜在问题</h4>
                            <p class="detail-text">{{ displayContent.disadvantages }}</p>
                        </div>

                        <div class="detail-card">
                            <h4 class="detail-title">相处建议</h4>
                            <p class="detail-text">{{ displayContent.suggestions }}</p>
                        </div>
                    </div>
                </div>
            </el-card>
        </div>
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

        let hasError = false;

        await calculateMatchStream(matchForm.value, async (eventData) => {
            // 收到响应就关闭加载状态
            loading.value = false;

            // 检查是否是错误事件
            if (eventData.event === 'error') {
                hasError = true;
                ElMessage.error(eventData.data);
                showResult.value = false;
                return;
            }

            if (!hasError) {
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

        if (!hasError) {
            ElMessage.success('匹配分析完成');
        }
    } catch (error) {
        console.error('Match error:', error);
        ElMessage.error(error.message || '匹配分析失败');
        showResult.value = false;
    } finally {
        loading.value = false;
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
/* 导入字体 */
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@500;600&family=Montserrat:wght@400;500&family=Roboto+Mono&display=swap');

.match-container {
    min-height: 100vh;
    background: #1A1D2F;
    padding: 40px 0;
    /* 移除左右padding */
    display: flex;
    align-items: flex-start;
    /* 改为顶部对齐 */
    justify-content: center;
}

.content-wrapper {
    width: 100%;
    max-width: 800px;
    /* 统一宽度 */
    margin: 0 20px;
    /* 添加左右边距 */
    display: flex;
    flex-direction: column;
    gap: 30px;
}

.match-card {
    width: 100%;
    background: rgba(42, 47, 79, 0.8);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 16px;
}

.starry-background {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: radial-gradient(circle at center, #ffffff 1px, transparent 1px);
    background-size: 50px 50px;
    opacity: 0.1;
    pointer-events: none;
}

.card-header {
    color: #FFFFFF;
    font-family: 'Poppins', sans-serif;
    font-size: 24px;
    font-weight: 600;
    text-align: center;
    margin-bottom: 20px;
}

/* 表单样式 */
:deep(.el-form-item__label) {
    color: #FFFFFF !important;
    font-family: 'Poppins', sans-serif;
    font-weight: 500;
}

.birthday-inputs {
    display: flex;
    align-items: center;
    gap: 15px;
}

.date-picker {
    width: 240px !important;
}

.model-select {
    width: 240px !important;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper) {
    background: rgba(255, 255, 255, 0.1);
    box-shadow: none;
    border: 1px solid rgba(255, 255, 255, 0.1);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper:hover) {
    border-color: #00FF88;
}

:deep(.el-input__inner) {
    color: #FFFFFF;
}

.separator {
    color: #00FF88;
    font-size: 20px;
}

/* 按钮样式 */
.submit-button {
    width: 240px;
    height: 40px;
    background: linear-gradient(45deg, #00FF88, #00CC88);
    border: none;
    border-radius: 8px;
    font-family: 'Poppins', sans-serif;
    font-weight: 600;
    transition: all 0.3s ease;
}

.submit-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(0, 255, 136, 0.3);
}

/* 加载动画卡片 */
.loading-card {
    width: 100%;
    max-width: 800px;
    margin: 0 auto;
    height: 200px;
    background: rgba(42, 47, 79, 0.8);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 16px;
    display: flex;
    justify-content: center;
    align-items: center;
}

.loading-animation {
    text-align: center;
}

.loading-animation p {
    color: #00FF88;
    font-family: 'Poppins', sans-serif;
    margin-top: 20px;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
    .match-container {
        padding: 20px 10px;
    }

    .birthday-inputs {
        flex-direction: column;
    }

    .date-picker,
    .model-select,
    .submit-button {
        width: 100% !important;
    }

    .separator {
        display: none;
    }
}

.result-card {
    width: 100%;
    max-width: 800px;
    margin-top: 30px;
    background: rgba(26, 29, 47, 0.9);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(0, 255, 136, 0.1);
    border-radius: 16px;
}

.result-header {
    padding: 20px;
    text-align: center;
}

.zodiac-pair {
    display: flex;
    justify-content: space-around;
    align-items: center;
    margin: 20px 0;
}

.zodiac-item {
    text-align: center;
}

.zodiac-icon {
    font-size: 48px;
    color: #FFD700;
    margin-bottom: 10px;
}

.zodiac-name {
    font-family: 'Poppins', sans-serif;
    color: #FFFFFF;
    font-size: 20px;
}

.match-score {
    position: relative;
}

.score-circle {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    background: linear-gradient(45deg, #00FF88, #00CC88);
    display: flex;
    justify-content: center;
    align-items: center;
    font-family: 'Roboto Mono', monospace;
    font-size: 32px;
    color: #FFFFFF;
    box-shadow: 0 0 20px rgba(0, 255, 136, 0.3);
}

.score-unit {
    font-size: 16px;
    margin-left: 2px;
}

.result-content {
    padding: 30px;
}

.summary-section {
    margin-bottom: 40px;
}

.section-title {
    color: #00FF88;
    font-family: 'Poppins', sans-serif;
    font-size: 20px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 10px;
}

.summary-content {
    background: rgba(0, 255, 136, 0.1);
    border-left: 4px solid #00FF88;
    border-radius: 8px;
    padding: 20px;
    position: relative;
    backdrop-filter: blur(5px);
}

.summary-text {
    color: #FFFFFF;
    line-height: 1.8;
    font-size: 15px;
    letter-spacing: 0.5px;
    white-space: pre-line;
    opacity: 0.95;
}

/* 添加引号装饰 */
.summary-content::before {
    content: '"';
    position: absolute;
    left: 10px;
    top: 0;
    font-size: 40px;
    color: rgba(0, 255, 136, 0.3);
    font-family: serif;
}

.summary-content::after {
    content: '"';
    position: absolute;
    right: 10px;
    bottom: -10px;
    font-size: 40px;
    color: rgba(0, 255, 136, 0.3);
    font-family: serif;
}

/* 添加微光效果 */
@keyframes glow {
    0% {
        box-shadow: 0 0 5px rgba(0, 255, 136, 0.1);
    }

    50% {
        box-shadow: 0 0 15px rgba(0, 255, 136, 0.2);
    }

    100% {
        box-shadow: 0 0 5px rgba(0, 255, 136, 0.1);
    }
}

.summary-content {
    animation: glow 3s ease-in-out infinite;
}

.details-section {
    display: grid;
    gap: 20px;
}

.detail-card {
    background: rgba(42, 47, 79, 0.5);
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
}

.detail-title {
    color: #FFD700;
    font-family: 'Poppins', sans-serif;
    font-size: 18px;
    margin-bottom: 15px;
    opacity: 0.9;
}

.detail-text {
    color: #FFFFFF;
    line-height: 1.8;
    font-size: 15px;
    opacity: 0.85;
}

/* 加载动画 */
.loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(26, 29, 47, 0.9);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.loading-animation {
    text-align: center;
}

.galaxy {
    width: 100px;
    height: 100px;
    position: relative;
    animation: rotate 4s linear infinite;
}

.star {
    position: absolute;
    width: 4px;
    height: 4px;
    background: #00FF88;
    border-radius: 50%;
    animation: twinkle 1s ease-in-out infinite;
}

@keyframes rotate {
    from {
        transform: rotate(0deg);
    }

    to {
        transform: rotate(360deg);
    }
}

@keyframes twinkle {

    0%,
    100% {
        opacity: 0.3;
    }

    50% {
        opacity: 1;
    }
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
    .match-container {
        padding: 10px;
    }

    .result-card {
        margin: 20px 10px;
    }

    .zodiac-pair {
        flex-direction: column;
        gap: 20px;
    }

    .details-section {
        grid-template-columns: 1fr;
    }
}

/* 流星动画 */
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
</style>