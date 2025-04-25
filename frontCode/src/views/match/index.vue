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
                        <span>{{ $t('match.title') }}</span>
                    </div>
                </template>

                <el-form :model="matchForm" :rules="rules" ref="formRef" label-width="100px">
                    <div class="form-item">
                        <el-form-item :label="$t('match.birthday')" prop="birthdays" :rules="birthdayRules">
                            <div class="birthday-inputs">
                                <el-form-item prop="person1Birthday">
                                    <DateTimePicker
                                        v-model="matchForm.person1Birthday"
                                        :placeholder="$t('match.person1Birthday')"
                                    />
                                </el-form-item>
                                <span class="multiply-icon">×</span>
                                <el-form-item prop="person2Birthday">
                                    <DateTimePicker
                                        v-model="matchForm.person2Birthday"
                                        :placeholder="$t('match.person2Birthday')"
                                    />
                                </el-form-item>
                            </div>
                        </el-form-item>
                    </div>

                    <div class="form-row">
                        <el-form-item :label="$t('match.aiModel')" prop="aiModel">
                            <el-select v-model="matchForm.aiModel" :placeholder="$t('match.selectModel')" class="model-select">
                                <el-option v-for="option in aiModelOptions" :key="option.value" :label="$t(`models.${option.value}`)"
                                    :value="option.value" />
                            </el-select>
                        </el-form-item>
                    </div>

                    <div class="form-row">
                        <el-form-item>
                            <el-button type="primary" @click="handleSubmit" :loading="loading" class="submit-button">
                                {{ $t('match.startMatch') }}
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
                        <p>{{ $t('match.analyzing') }}</p>
                    </div>
                </div>

                <div v-else class="result-content">
                    <div class="result-header">
                        <div class="zodiac-pair">
                            <div class="zodiac-item">
                                <div class="zodiac-icon">{{ getZodiacIcon(matchResult.person1Sign) }}</div>
                                <div class="zodiac-name">{{ $t(`zodiac.${matchResult.person1Sign}`) }}</div>
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
                                <div class="zodiac-name">{{ $t(`zodiac.${matchResult.person2Sign}`) }}</div>
                            </div>
                        </div>
                    </div>

                    <!-- 分析总结部分 -->
                    <div class="summary-section">
                        <h3 class="section-title">{{ $t('match.analysis') }}</h3>
                        <div class="summary-content">
                            <div v-if="!displayContent.analysis" class="loading-wrapper">
                                <div class="loading-container">
                                    <span class="loading-text">{{ $t('match.loading') }}</span>
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
                            <h4 class="detail-title">{{ $t('match.advantages') }}</h4>
                            <div v-if="!displayContent.advantages" class="loading-wrapper">
                                <div class="loading-container">
                                    <span class="loading-text">{{ $t('match.loading') }}</span>
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
                            <h4 class="detail-title">{{ $t('match.disadvantages') }}</h4>
                            <div v-if="!displayContent.disadvantages" class="loading-wrapper">
                                <div class="loading-container">
                                    <span class="loading-text">{{ $t('match.loading') }}</span>
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
                            <h4 class="detail-title">{{ $t('match.suggestions') }}</h4>
                            <div v-if="!displayContent.suggestions" class="loading-wrapper">
                                <div class="loading-container">
                                    <span class="loading-text">{{ $t('match.loading') }}</span>
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
import { useI18n } from 'vue-i18n'
import { calculateMatchStream } from '@/api/match'
import { ElMessage } from 'element-plus'
import DateTimePicker from '@/components/DateTimePicker/index.vue'

const { t, locale } = useI18n()
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
    // 回答的语言，默认是中文，如果网页切换语言，这里也跟着切换
    language: locale.value,
    // 默认使用 qwen-turbo 模型
    aiModel: 'qwen-turbo'
})

// 监听语言变化，更新表单中的语言参数
watch(locale, (newLocale) => {
    console.log('语言已切换为:', newLocale)
    matchForm.value.language = newLocale
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

const formRef = ref(null)

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
    if (!formRef.value) return

    try {
        // 表单验证
        await formRef.value.validate()
        // 验证通过后调用匹配
        await submitMatch()
    } catch (error) {
        console.log('表单验证失败')
    }
}

// 修改AI模型选项
const aiModelOptions = [
    { value: 'qwen-turbo' },
    { value: 'deepseek-chat' }
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
    background: linear-gradient(135deg, #1A1D2F 0%, #2A2F4F 100%);
    padding: 40px 0;
    display: flex;
    align-items: flex-start;
    justify-content: center;
    padding-top: 20px;
    position: relative;
    overflow: hidden;
}

.content-wrapper {
    width: 100%;
    max-width: 800px;
    margin: 0 20px;
    display: flex;
    flex-direction: column;
    gap: 30px;
    position: relative;
    z-index: 2;
}

.match-card {
    width: 100%;
    background: rgba(42, 47, 79, 0.8);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.match-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
}

.starry-background {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: 
        radial-gradient(circle at 25% 25%, rgba(255, 255, 255, 0.2) 1px, transparent 1px),
        radial-gradient(circle at 75% 75%, rgba(255, 255, 255, 0.2) 1px, transparent 1px);
    background-size: 50px 50px, 100px 100px;
    opacity: 0.2;
    pointer-events: none;
    animation: twinkle 8s ease-in-out infinite alternate;
}

@keyframes twinkle {
    0% { opacity: 0.1; }
    50% { opacity: 0.2; }
    100% { opacity: 0.1; }
}

.card-header {
    color: #FFFFFF;
    font-family: 'Poppins', sans-serif;
    font-size: 24px;
    font-weight: 600;
    text-align: center;
    margin-bottom: 20px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
    background: linear-gradient(90deg, #00FF88, #00CCFF);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

/* 表单样式 */
:deep(.el-form-item__label) {
    color: #E5E7EB !important;
    font-family: 'Poppins', sans-serif;
    font-weight: 500;
    letter-spacing: 0.5px;
}

.form-item {
    margin-bottom: 20px;
}

.birthday-inputs {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
}

.multiply-icon {
    color: #00FF88;
    font-size: 18px;
    padding: 0 4px;
    display: none;
}

/* 响应式布局 */
@media screen and (min-width: 768px) {
    .birthday-inputs {
        flex-wrap: nowrap;
    }
    
    .multiply-icon {
        display: block;
    }
}

:deep(.el-form-item) {
    flex: 1;
    min-width: 200px;
    margin-bottom: 8px;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper) {
    background: rgba(26, 29, 47, 0.8);
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: none !important;
    transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper:hover) {
    border-color: rgba(0, 255, 136, 0.5);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select .el-input__wrapper.is-focus) {
    border-color: #00FF88;
    box-shadow: 0 0 0 1px rgba(0, 255, 136, 0.2) !important;
}

:deep(.el-input__inner) {
    color: #FFFFFF;
}

:deep(.el-form-item__error) {
    padding-top: 4px;
    color: #FF6B6B;
}

/* 移动端优化 */
@media screen and (max-width: 767px) {
    .birthday-inputs {
        flex-direction: column;
        width: 100%;
    }

    :deep(.el-form-item) {
        width: 100%;
    }

    :deep(.picker-input) {
        width: 100%;
    }
}

.model-select {
    width: 240px !important;
}

/* 按钮样式 */
.submit-button {
    width: 240px;
    height: 44px;
    background: linear-gradient(45deg, #00FF88, #00CCFF);
    border: none;
    border-radius: 8px;
    font-family: 'Poppins', sans-serif;
    font-weight: 600;
    font-size: 16px;
    letter-spacing: 0.5px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(0, 255, 136, 0.3);
}

.submit-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 18px rgba(0, 255, 136, 0.4);
    background: linear-gradient(45deg, #00FF9A, #00D8FF);
}

.submit-button:active {
    transform: translateY(1px);
    box-shadow: 0 2px 8px rgba(0, 255, 136, 0.3);
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
    letter-spacing: 0.5px;
    text-shadow: 0 0 10px rgba(0, 255, 136, 0.5);
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
    .match-container {
        padding: 10px;
    }

    .result-card {
        margin: 20px 10px;
    }
    
    /* 移除这些样式，保持水平布局 */
    /*
    .zodiac-pair {
        flex-direction: column;
        gap: 30px;
    }

    .zodiac-pair::before {
        display: none;
    }
    */
    
    /* 添加这些样式来适应小屏幕 */
    .zodiac-item {
        padding: 15px 10px;
    }
    
    .zodiac-icon {
        font-size: 36px;
    }
    
    .zodiac-name {
        font-size: 16px;
    }
    
    .score-circle {
        width: 90px;
        height: 90px;
        font-size: 28px;
    }

    .details-section {
        grid-template-columns: 1fr;
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
    position: relative;
}

.zodiac-pair::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 25%;
    right: 25%;
    height: 2px;
    background: linear-gradient(90deg, 
        transparent, 
        rgba(0, 255, 136, 0.3) 20%, 
        rgba(0, 255, 136, 0.5) 50%, 
        rgba(0, 255, 136, 0.3) 80%, 
        transparent
    );
    z-index: 0;
}

.zodiac-item {
    text-align: center;
    background: rgba(42, 47, 79, 0.7);
    padding: 20px;
    border-radius: 12px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
    transition: transform 0.3s ease;
    position: relative;
    z-index: 1;
}

.zodiac-item:hover {
    transform: translateY(-5px);
}

.zodiac-icon {
    font-size: 48px;
    color: #FFD700;
    margin-bottom: 10px;
    text-shadow: 0 0 15px rgba(255, 215, 0, 0.5);
    animation: pulse 3s infinite alternate;
}

@keyframes pulse {
    0% { transform: scale(1); opacity: 0.8; }
    100% { transform: scale(1.1); opacity: 1; }
}

.zodiac-name {
    font-family: 'Poppins', sans-serif;
    color: #FFFFFF;
    font-size: 20px;
    font-weight: 600;
    letter-spacing: 0.5px;
}

.match-score {
    position: relative;
    z-index: 2;
}

.score-circle {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    background: rgba(26, 29, 47, 0.9);
    display: flex;
    justify-content: center;
    align-items: center;
    font-family: 'Roboto Mono', monospace;
    font-size: 36px;
    font-weight: 600;
    color: #FFFFFF;
    box-shadow: 0 0 30px rgba(0, 255, 136, 0.4);
    position: relative;
    overflow: hidden;
    border: 3px solid rgba(0, 255, 136, 0.3);
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
    background: linear-gradient(180deg, rgba(0, 220, 120, 0.7), rgba(0, 200, 255, 0.7));
    transition: height 1.5s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 水面波浪效果 - 降低高度和波动幅度 */
.water-surface {
    position: absolute;
    top: -5px;
    left: 0;
    width: 100%;
    height: 10px;
    background: rgba(0, 220, 255, 0.8);
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
        transform: scale(1.03, 0.97) translateY(1px);
        border-radius: 25% 25% 50% 50% / 25% 25% 50% 50%;
    }
    100% {
        transform: scale(0.97, 1.03) translateY(-1px);
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
    text-shadow: 0 0 10px rgba(0, 0, 0, 0.7);
    width: 100%;
    text-align: center;
    line-height: 1;
    margin-top: 0;
    padding: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
}

.score-unit {
    font-size: 18px;
    margin-left: 2px;
    vertical-align: middle;
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
    font-size: 22px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 10px;
    text-shadow: 0 0 10px rgba(0, 255, 136, 0.3);
    position: relative;
    padding-left: 15px;
}

.section-title::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 5px;
    height: 24px;
    background: linear-gradient(to bottom, #00FF88, #00CCFF);
    border-radius: 3px;
}

.summary-content {
    background: rgba(0, 255, 136, 0.1);
    border-left: 4px solid #00FF88;
    border-radius: 12px;
    padding: 25px;
    position: relative;
    backdrop-filter: blur(5px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.summary-text {
    color: #FFFFFF;
    line-height: 1.8;
    font-size: 16px;
    letter-spacing: 0.5px;
    white-space: pre-line;
    opacity: 0.95;
}

/* 添加引号装饰 */
.summary-content::before {
    content: '"';
    position: absolute;
    left: 15px;
    top: 0;
    font-size: 50px;
    color: rgba(0, 255, 136, 0.3);
    font-family: serif;
}

.summary-content::after {
    content: '"';
    position: absolute;
    right: 15px;
    bottom: -15px;
    font-size: 50px;
    color: rgba(0, 255, 136, 0.3);
    font-family: serif;
}

/* 添加微光效果 */
@keyframes glow {
    0% {
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    }

    50% {
        box-shadow: 0 4px 25px rgba(0, 255, 136, 0.2);
    }

    100% {
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    }
}

.summary-content {
    animation: glow 3s ease-in-out infinite;
}

.details-section {
    display: flex;
    flex-direction: column;
    gap: 25px;
}

.detail-card {
    background: rgba(42, 47, 79, 0.5);
    border-radius: 12px;
    padding: 25px;
    margin-bottom: 20px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    border: 1px solid rgba(255, 255, 255, 0.05);
    position: relative;
    overflow: hidden;
}

.detail-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
}

.detail-card::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 5px;
    background: linear-gradient(90deg, #FFD700, #FF6B6B);
    opacity: 0.7;
}

.detail-card:nth-child(1)::after {
    background: linear-gradient(90deg, #00FF88, #00CCFF);
}

.detail-card:nth-child(2)::after {
    background: linear-gradient(90deg, #FF6B6B, #FFD700);
}

.detail-card:nth-child(3)::after {
    background: linear-gradient(90deg, #A78BFA, #F472B6);
}

.detail-title {
    color: #FFD700;
    font-family: 'Poppins', sans-serif;
    font-size: 18px;
    margin-bottom: 15px;
    opacity: 0.9;
    display: flex;
    align-items: center;
    gap: 8px;
}

.detail-title::before {
    content: '•';
    color: #FFD700;
    font-size: 24px;
}

.detail-card:nth-child(1) .detail-title {
    color: #00FF88;
}

.detail-card:nth-child(1) .detail-title::before {
    color: #00FF88;
}

.detail-card:nth-child(2) .detail-title {
    color: #FF6B6B;
}

.detail-card:nth-child(2) .detail-title::before {
    color: #FF6B6B;
}

.detail-card:nth-child(3) .detail-title {
    color: #A78BFA;
}

.detail-card:nth-child(3) .detail-title::before {
    color: #A78BFA;
}

.detail-text {
    color: #FFFFFF;
    line-height: 1.8;
    font-size: 15px;
    opacity: 0.9;
    letter-spacing: 0.3px;
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
    width: 120px;
    height: 120px;
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
    box-shadow: 0 0 10px rgba(0, 255, 136, 0.8);
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
    0%, 100% {
        opacity: 0.3;
        transform: scale(0.8);
    }

    50% {
        opacity: 1;
        transform: scale(1.2);
    }
}

/* 流星动画 */
.shooting-star {
    position: absolute;
    width: 3px;
    height: 3px;
    background: linear-gradient(90deg, rgba(255, 255, 255, 0), #ffffff, rgba(255, 255, 255, 0));
    border-radius: 50%;
    animation: shooting 6s linear infinite;
    opacity: 0;
    box-shadow: 0 0 10px 2px rgba(255, 255, 255, 0.5);
}

.shooting-star::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100px;
    height: 1px;
    background: linear-gradient(90deg, #ffffff, rgba(255, 255, 255, 0));
    transform: translateX(-100%);
}

.shooting-star:nth-child(1) {
    top: 10%;
    left: 10%;
    animation-delay: 0s;
}

.shooting-star:nth-child(2) {
    top: 20%;
    left: 30%;
    animation-delay: 2s;
}

.shooting-star:nth-child(3) {
    top: 30%;
    left: 50%;
    animation-delay: 4s;
}

.shooting-star:nth-child(4) {
    top: 40%;
    left: 70%;
    animation-delay: 6s;
}

.shooting-star:nth-child(5) {
    top: 50%;
    left: 90%;
    animation-delay: 8s;
}

@keyframes shooting {
    0% {
        transform: translateX(0) translateY(0) rotate(45deg);
        opacity: 0;
    }
    5% {
        opacity: 1;
    }
    20% {
        transform: translateX(20vw) translateY(20vh) rotate(45deg);
        opacity: 0;
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
    box-shadow: 0 0 8px rgba(0, 255, 136, 0.5);
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
    min-height: 60px;
}

.loading-container {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 10px 20px;
    background: rgba(0, 255, 136, 0.05);
    border-radius: 8px;
    box-shadow: 0 0 15px rgba(0, 255, 136, 0.1);
}

.loading-text {
    color: #00FF88;
    font-size: 14px;
    letter-spacing: 0.5px;
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

/* 为星座图标添加特殊样式 */
.zodiac-icon {
    position: relative;
}

.zodiac-icon::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 60px;
    height: 60px;
    background: radial-gradient(circle, rgba(255, 215, 0, 0.2) 0%, rgba(255, 215, 0, 0) 70%);
    border-radius: 50%;
    z-index: -1;
}
</style>