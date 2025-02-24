<template>
    <div class="history-container">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span>匹配历史记录</span>
                </div>
            </template>

            <!-- 搜索表单 -->
            <el-form :model="queryForm" inline>
                <el-form-item label="第一个星座">
                    <el-select v-model="queryForm.person1Sign" placeholder="请选择" clearable>
                        <el-option v-for="sign in zodiacSigns" :key="sign" :label="sign" :value="sign" />
                    </el-select>
                </el-form-item>

                <el-form-item label="第二个星座">
                    <el-select v-model="queryForm.person2Sign" placeholder="请选择" clearable>
                        <el-option v-for="sign in zodiacSigns" :key="sign" :label="sign" :value="sign" />
                    </el-select>
                </el-form-item>

                <el-form-item label="匹配分数">
                    <el-input-number v-model="queryForm.minScore" :min="0" :max="100" placeholder="最小分数" />
                    <span class="mx-2">-</span>
                    <el-input-number v-model="queryForm.maxScore" :min="0" :max="100" placeholder="最大分数" />
                </el-form-item>

                <el-form-item label="匹配时间">
                    <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期"
                        end-placeholder="结束日期" value-format="YYYY-MM-DD" @change="handleDateRangeChange" />
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="handleSearch">查询</el-button>
                    <el-button @click="handleReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 数据表格 -->
            <el-table v-loading="loading" :data="tableData" style="width: 100%" border>
                <el-table-column prop="person1Sign" label="第一个星座" width="120" />
                <el-table-column prop="person2Sign" label="第二个星座" width="120" />
                <el-table-column prop="matchScore" label="匹配分数" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getScoreTagType(row.matchScore)">
                            {{ row.matchScore }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="analysis" label="整体分析" show-overflow-tooltip />
                <el-table-column prop="createdAt" label="匹配时间" width="180" />
                <el-table-column label="操作" width="120" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="handleViewDetail(row)">
                            查看详情
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
                <el-pagination v-model:current-page="queryForm.page" v-model:page-size="queryForm.size" :total="total"
                    :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleSizeChange" @current-change="handleCurrentChange" />
            </div>
        </el-card>

        <!-- 详情弹窗 -->
        <el-dialog v-model="dialogVisible" title="匹配详情" width="60%">
            <div v-if="currentDetail" class="detail-content">
                <div class="detail-item">
                    <h4>星座组合</h4>
                    <p>{{ currentDetail.person1Sign }} × {{ currentDetail.person2Sign }}</p>
                </div>
                <div class="detail-item">
                    <h4>匹配分数</h4>
                    <el-progress :percentage="currentDetail.matchScore"
                        :color="getProgressColor(currentDetail.matchScore)" />
                </div>
                <div class="detail-item">
                    <h4>整体分析</h4>
                    <p>{{ currentDetail.analysis }}</p>
                </div>
                <div class="detail-item">
                    <h4>优势特点</h4>
                    <p>{{ currentDetail.advantages }}</p>
                </div>
                <div class="detail-item">
                    <h4>潜在问题</h4>
                    <p>{{ currentDetail.disadvantages }}</p>
                </div>
                <div class="detail-item">
                    <h4>相处建议</h4>
                    <p>{{ currentDetail.suggestions }}</p>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMatchHistory } from '@/api/match'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentDetail = ref(null)
const dateRange = ref([])

const zodiacSigns = [
    '白羊座', '金牛座', '双子座', '巨蟹座', '狮子座', '处女座',
    '天秤座', '天蝎座', '射手座', '摩羯座', '水瓶座', '双鱼座'
]

const queryForm = ref({
    page: 1,
    size: 10,
    person1Sign: '',
    person2Sign: '',
    minScore: null,
    maxScore: null,
    startTime: '',
    endTime: ''
})

const getScoreTagType = (score) => {
    if (score >= 80) return 'success'
    if (score >= 60) return 'warning'
    return 'danger'
}

const getProgressColor = (score) => {
    if (score >= 80) return '#67C23A'
    if (score >= 60) return '#E6A23C'
    return '#F56C6C'
}

const handleDateRangeChange = (val) => {
    queryForm.value.startTime = val ? val[0] : ''
    queryForm.value.endTime = val ? val[1] : ''
}

const loadData = async () => {
    try {
        loading.value = true
        const result = await getMatchHistory(queryForm.value)
        tableData.value = result.records
        total.value = result.total
    } catch (error) {
        ElMessage.error(error.message || '获取数据失败')
    } finally {
        loading.value = false
    }
}

const handleSearch = () => {
    queryForm.value.page = 1
    loadData()
}

const handleReset = () => {
    queryForm.value = {
        page: 1,
        size: 10,
        person1Sign: '',
        person2Sign: '',
        minScore: null,
        maxScore: null,
        startTime: '',
        endTime: ''
    }
    dateRange.value = []
    loadData()
}

const handleSizeChange = (val) => {
    queryForm.value.size = val
    loadData()
}

const handleCurrentChange = (val) => {
    queryForm.value.page = val
    loadData()
}

const handleViewDetail = (row) => {
    currentDetail.value = row
    dialogVisible.value = true
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.history-container {
    padding: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.pagination-container {
    margin-top: 20px;
    text-align: right;
}

.detail-content {
    padding: 20px;
}

.detail-item {
    margin-bottom: 20px;
}

.detail-item h4 {
    margin: 0 0 10px;
    color: #409EFF;
}

.detail-item p {
    margin: 0;
    line-height: 1.6;
}

.mx-2 {
    margin: 0 8px;
}
</style>