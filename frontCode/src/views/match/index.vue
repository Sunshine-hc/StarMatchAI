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
                        <span>AI星座匹配测试</span>
                    </div>
                </template>

                <el-form :model="matchForm" :rules="rules" ref="matchFormRef" label-width="100px">
                    <div class="form-row">
                        <el-form-item label="生日选择" required>
                            <div class="birthday-inputs">
                                <el-form-item prop="person1Birthday" class="birthday-item" label-width="0">
                                    <el-date-picker v-model="matchForm.person1Birthday" type="date"
                                        placeholder="第一个人的生日" format="YYYY-MM-DD" value-format="YYYY-MM-DD"
                                        :clearable="false" class="date-picker" />
                                </el-form-item>

                                <span class="separator">×</span>

                                <el-form-item prop="person2Birthday" class="birthday-item" label-width="0">
                                    <el-date-picker v-model="matchForm.person2Birthday" type="date"
                                        placeholder="第二个人的生日" format="YYYY-MM-DD" value-format="YYYY-MM-DD"
                                        :clearable="false" class="date-picker" />
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
                                <div class="zodiac-icon">{{ getZodiacIcon(matchResult.person1Sign) }}</div>
                                <div class="zodiac-name">{{ matchResult.person1Sign }}</div>
                            </div>
                            <div class="match-score">
                                <div class="score-circle">
                                    <div class="water-container">
                                        <div class="water" :style="{ height: matchResult.matchScore + '%' }">
                                            <div class="water-surface"></div>
                                        </div>
                                    </div>
                                    <div class="score-text">
                                        {{ matchResult.matchScore }}
                                        <span class="score-unit">%</span>
                                    </div>
                                </div>
                            </div>
                            <div class="zodiac-item">
                                <div class="zodiac-icon">{{ getZodiacIcon(matchResult.person2Sign) }}</div>
                                <div class="zodiac-name">{{ matchResult.person2Sign }}</div>
                            </div>
                        </div>
                    </div>

                    <!-- 分析总结部分 -->
                    <div class="summary-section">
                        <h3 class="section-title">分析总结</h3>
                        <div class="summary-content">
                            <div v-if="!displayContent.analysis" class="loading-wrapper">
                                <div class="loading-container">
                                    <span class="loading-text">分析中</span>
                                    <div class="loading-dots">
                                        <span></span>
                                        <span></span>
                                        <span></span>
                                    </div>
                                </div>
                            </div>
                            <p v-else class="summary-text">{{ displayContent.analysis }}</p>
                        </div>
                    </div>

                    <div class="details-section">
                        <div class="detail-card">
                            <h4 class="detail-title">优势特点</h4>
                            <div v-if="!displayContent.advantages" class="loading-wrapper">
                                <div class="loading-container">
                                    <span class="loading-text">分析中</span>
                                    <div class="loading-dots">
                                        <span></span>
                                        <span></span>
                                        <span></span>
                                    </div>
                                </div>
                            </div>
                            <p v-else class="detail-text">{{ displayContent.advantages }}</p>
                        </div>

                        <div class="detail-card">
                            <h4 class="detail-title">潜在问题</h4>
                            <div v-if="!displayContent.disadvantages" class="loading-wrapper">
                                <div class="loading-container">
                                    <span class="loading-text">分析中</span>
                                    <div class="loading-dots">
                                        <span></span>
                                        <span></span>
                                        <span></span>
                                    </div>
                                </div>
                            </div>
                            <p v-else class="detail-text">{{ displayContent.disadvantages }}</p>
                        </div>

                        <div class="detail-card">
                            <h4 class="detail-title">相处建议</h4>
                            <div v-if="!displayContent.suggestions" class="loading-wrapper">
                                <div class="loading-container">
                                    <span class="loading-text">分析中</span>
                                    <div class="loading-dots">
                                        <span></span>
                                        <span></span>
                                        <span></span>
                                    </div>
                                </div>
                            </div>
                            <p v-else class="detail-text">{{ displayContent.suggestions }}</p>
                        </div>
                    </div>
                </div>
            </el-card>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, reactive, watch, watchEffect } from 'vue'
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
const displayContent = reactive({
    analysis: '',
    advantages: '',
    disadvantages: '',
    suggestions: ''
})

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
const typeWriter = async (text, key) => {
    let index = 0;
    
    return new Promise((resolve) => {
        const timer = setInterval(() => {
            if (index < text.length) {
                // 直接修改 displayContent 对象的对应属性
                displayContent[key] = text.substring(0, index + 1);
                index++;
            } else {
                clearInterval(timer);
                resolve();
            }
        }, 50);
    });
};

// 监听 displayContent 的变化
// watch(() => displayContent.analysis, (newVal) => {
//     console.log('分析总结内容:', newVal)
// })

// watch(() => displayContent.advantages, (newVal) => {
//     console.log('优势特点内容:', newVal)
// })

// watch(() => displayContent.disadvantages, (newVal) => {
//     console.log('潜在问题内容:', newVal)
// })

// watch(() => displayContent.suggestions, (newVal) => {
//     console.log('相处建议内容:', newVal)
// })

// 使用 watchEffect 来监听变化
// watchEffect(() => {
//     console.log('displayContent 发生变化:', {
//         analysis: displayContent.analysis,
//         advantages: displayContent.advantages,
//         disadvantages: displayContent.disadvantages,
//         suggestions: displayContent.suggestions
//     });
// });

// 修改匹配提交函数
const submitMatch = async () => {
    try {
        loading.value = true
        showResult.value = true
        console.log('开始匹配计算...')
        console.log('初始 displayContent:', displayContent)

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
        displayContent.analysis = '';
        displayContent.advantages = '';
        displayContent.disadvantages = '';
        displayContent.suggestions = '';
        
        console.log('重置后 displayContent:', displayContent)

        let hasError = false;

        await calculateMatchStream(matchForm.value, async (eventData) => {
            console.log('收到事件数据:', eventData)
            
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
                console.log('处理事件:', eventData.event)
                switch (eventData.event) {
                    case 'signs':
                        matchResult.person1Sign = eventData.data.person1Sign;
                        matchResult.person2Sign = eventData.data.person2Sign;
                        break;
                    case 'score':
                        matchResult.matchScore = parseInt(eventData.data);
                        break;
                    case 'analysis':
                        console.log('开始写入分析内容')
                        matchResult.analysis = eventData.data;
                        await typeWriter(eventData.data, 'analysis');
                        console.log('分析内容写入完成:', displayContent.analysis);
                        break;
                    case 'advantages':
                        matchResult.advantages = eventData.data;
                        await typeWriter(eventData.data, 'advantages');
                        break;
                    case 'disadvantages':
                        matchResult.disadvantages = eventData.data;
                        await typeWriter(eventData.data, 'disadvantages');
                        break;
                    case 'suggestions':
                        matchResult.suggestions = eventData.data;
                        await typeWriter(eventData.data, 'suggestions');
                        break;
                }
            }
        });

        if (!hasError) {
            console.log('匹配分析完成')
            ElMessage.success('匹配分析完成');
        }
    } catch (error) {
        console.error('匹配错误:', error);
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

// 添加星座图标映射
const zodiacIcons = {
    '白羊座': '♈',
    '金牛座': '♉',
    '双子座': '♊',
    '巨蟹座': '♋',
    '狮子座': '♌',
    '处女座': '♍',
    '天秤座': '♎',
    '天蝎座': '♏',
    '射手座': '♐',
    '摩羯座': '♑',
    '水瓶座': '♒',
    '双鱼座': '♓'
}

// 获取星座图标的方法
const getZodiacIcon = (signName) => {
    return zodiacIcons[signName] || '⭐'; // 如果找不到对应图标，返回默认星星
}
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
    background: rgba(42, 47, 79, 0.9);
    display: flex;
    justify-content: center;
    align-items: center;
    font-family: 'Roboto Mono', monospace;
    font-size: 32px;
    color: #FFFFFF;
    box-shadow: 0 0 20px rgba(0, 255, 136, 0.3);
    position: relative;
    overflow: hidden;
}

/* 水容器 */
.water-container {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    overflow: hidden;
    pointer-events: none;
}

/* 水效果 - 调整颜色更接近图片 */
.water {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    background: rgba(0, 220, 120, 0.8);
    transition: height 1.5s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 水面波浪效果 - 降低高度和波动幅度 */
.water-surface {
    position: absolute;
    top: -5px; /* 从-10px改为-5px，降低高度 */
    left: 0;
    width: 100%;
    height: 10px; /* 从20px改为10px，降低高度 */
    background: rgba(0, 220, 120, 0.8);
    border-radius: 50%;
    animation: water-ripple 2s ease-in-out infinite alternate;
}

/* 水面荡漾动画 - 减小波动幅度 */
@keyframes water-ripple {
    0% {
        transform: scale(1, 1) translateY(0);
        border-radius: 50% 50% 25% 25% / 50% 50% 25% 25%;
    }
    50% {
        transform: scale(1.03, 0.97) translateY(1px); /* 减小变形幅度 */
        border-radius: 25% 25% 50% 50% / 25% 25% 50% 50%;
    }
    100% {
        transform: scale(0.97, 1.03) translateY(-1px); /* 减小变形幅度 */
        border-radius: 50% 50% 25% 25% / 50% 50% 25% 25%;
    }
}

/* 分数文字 - 优化居中 */
.score-text {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 2;
    text-shadow: 0 0 5px rgba(0, 0, 0, 0.5);
    width: 100%;
    text-align: center;
    line-height: 1; /* 添加行高确保垂直居中 */
    margin-top: 0; /* 移除可能的边距 */
    padding: 0; /* 移除可能的内边距 */
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
}

.score-unit {
    font-size: 16px;
    margin-left: 2px;
    vertical-align: middle; /* 确保单位与数字对齐 */
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

/* 只添加加载动画相关样式 */
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

.loading-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    min-height: 60px; /* 确保有足够的高度 */
}

.loading-container {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 10px 20px;
    /* background: rgba(0, 255, 136, 0.05); 可选：添加一个微妙的背景 */
    border-radius: 4px;
}

.loading-text {
    color: #00FF88;
    font-size: 14px;
}

.loading-dots {
    display: inline-flex;
    gap: 4px;
    align-items: center;
}

.loading-dots span {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background-color: #00FF88;
    display: inline-block;
    animation: dots 1.4s infinite;
}
</style>