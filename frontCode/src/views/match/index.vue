<template>
    <div class="match-container">
        <el-card class="match-form">
            <template #header>
                <div class="card-header">
                    <span>星座匹配测试</span>
                </div>
            </template>

            <el-form :model="matchForm" :rules="rules" ref="matchFormRef" label-width="120px">
                <el-form-item label="第一个人生日" prop="person1Birthday">
                    <el-date-picker v-model="matchForm.person1Birthday" type="date" placeholder="选择日期"
                        format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
                </el-form-item>

                <el-form-item label="第二个人生日" prop="person2Birthday">
                    <el-date-picker v-model="matchForm.person2Birthday" type="date" placeholder="选择日期"
                        format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="submitMatch" :loading="loading">
                        开始匹配
                    </el-button>
                </el-form-item>
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
    person1Birthday: [{ required: true, message: '请选择第一个人的生日', trigger: 'change' }],
    person2Birthday: [{ required: true, message: '请选择第二个人的生日', trigger: 'change' }]
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

const submitMatch = async () => {
    if (!matchFormRef.value) return

    try {
        await matchFormRef.value.validate()
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

        await calculateMatchStream(matchForm.value, async (eventData) => {

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
        });

        ElMessage.success('匹配分析完成')
    } catch (error) {
        console.error('Match error:', error);
        ElMessage.error(error.message || '匹配分析失败')
    } finally {
        loading.value = false
    }
}
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