<template>
  <div class="custom-datetime-picker">
    <!-- 日期选择输入框 -->
    <div class="picker-input" @click="handleClick" ref="inputRef">
      <i class="input-icon">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="16" height="16">
          <path fill="currentColor" d="M19,4H17V3a1,1,0,0,0-2,0V4H9V3A1,1,0,0,0,7,3V4H5A2,2,0,0,0,3,6V20a2,2,0,0,0,2,2H19a2,2,0,0,0,2-2V6A2,2,0,0,0,19,4Zm0,16H5V10H19ZM19,8H5V6H7V7A1,1,0,0,0,9,7V6h6V7a1,1,0,0,0,2,0V6h2Z"/>
        </svg>
      </i>
      <span class="date-text" :class="{ 'placeholder': !selectedDate }">
        {{ selectedDate ? formatDate(selectedDate) : placeholder }}
      </span>
    </div>

    <!-- 日期时间选择弹窗 -->
    <Teleport to="body">
      <div v-if="showPicker" class="picker-panel" :style="panelStyle">
        <!-- 年月选择面板 -->
        <div class="panel-header" @click="toggleYearMonthPanel">
          <button class="arrow-btn" v-if="!showYearMonthPanel" @click.stop="changeMonth(-1)">«</button>
          <span class="current-date">{{ currentYear }}年 {{ currentMonth + 1 }}月</span>
          <button class="arrow-btn" v-if="!showYearMonthPanel" @click.stop="changeMonth(1)">»</button>
        </div>

        <!-- 年月选择面板 -->
        <div v-if="showYearMonthPanel" class="year-month-panel">
          <div class="year-selector">
            <button class="year-btn" @click="changeYearRange(-12)">«</button>
            <div class="year-list">
              <div
                v-for="year in yearRange"
                :key="year"
                class="year-item"
                :class="{ 'selected': year === currentYear }"
                @click="selectYear(year)"
              >
                {{ year }}
              </div>
            </div>
            <button class="year-btn" @click="changeYearRange(12)">»</button>
          </div>
          <div class="month-selector">
            <div
              v-for="(month, index) in 12"
              :key="month"
              class="month-item"
              :class="{ 'selected': index === currentMonth }"
              @click="selectMonth(index)"
            >
              {{ month }}月
            </div>
          </div>
        </div>

        <!-- 日期面板 -->
        <div v-else>
          <div class="week-header">
            <span v-for="day in ['日', '一', '二', '三', '四', '五', '六']" :key="day">{{ day }}</span>
          </div>
          <div class="date-grid">
            <div
              v-for="(date, index) in dates"
              :key="index"
              class="date-cell"
              :class="{
                'other-month': date.otherMonth,
                'current-day': isCurrentDay(date.date),
                'selected': isSelectedDate(date.date)
              }"
              @click="selectDate(date.date)"
            >
              {{ date.day }}
            </div>
          </div>
        </div>

        <!-- 时间选择部分 -->
        <div class="time-picker" @click.stop>
          <div class="time-column">
            <button class="time-btn" @click.stop="showHourSelect = true">
              <span class="time-value">{{ String(hour).padStart(2, '0') }}</span>
              <span class="time-label">时</span>
            </button>
            <!-- 小时选择面板 -->
            <div v-if="showHourSelect" class="time-select-panel" @click.stop>
              <div class="time-select-list" ref="hourListRef">
                <div
                  v-for="h in 24"
                  :key="h-1"
                  class="time-select-item"
                  :class="{ 'selected': hour === h-1 }"
                  @click.stop="selectHour(h-1)"
                >
                  {{ String(h-1).padStart(2, '0') }}
                </div>
              </div>
            </div>
          </div>

          <span class="time-separator">:</span>

          <div class="time-column">
            <button class="time-btn" @click.stop="showMinuteSelect = true">
              <span class="time-value">{{ String(minute).padStart(2, '0') }}</span>
              <span class="time-label">分</span>
            </button>
            <!-- 分钟选择面板 -->
            <div v-if="showMinuteSelect" class="time-select-panel" @click.stop>
              <div class="time-select-list" ref="minuteListRef">
                <div
                  v-for="m in minuteList"
                  :key="m"
                  class="time-select-item"
                  :class="{ 'selected': minute === m }"
                  @click.stop="selectMinute(m)"
                >
                  {{ String(m).padStart(2, '0') }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部按钮 -->
        <div class="panel-footer">
          <button class="btn-now" @click="selectNow">此刻</button>
          <button class="btn-confirm" @click="confirmSelect">确定</button>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, onBeforeUnmount } from 'vue';
import dayjs from 'dayjs';

const props = defineProps({
  modelValue: {
    type: [Date, String],
    default: null
  },
  placeholder: {
    type: String,
    default: '请选择日期'
  }
});

const emit = defineEmits(['update:modelValue']);

const showPicker = ref(false);
const selectedDate = ref(props.modelValue ? new Date(props.modelValue) : null);
const currentDate = ref(new Date());

// 计算当前年月
const currentYear = computed(() => currentDate.value.getFullYear());
const currentMonth = computed(() => currentDate.value.getMonth());

// 生成日期网格数据
const dates = computed(() => {
  const firstDay = new Date(currentYear.value, currentMonth.value, 1);
  const lastDay = new Date(currentYear.value, currentMonth.value + 1, 0);
  const dates = [];
  
  // 填充上个月的日期
  const firstDayWeek = firstDay.getDay();
  const prevMonthLastDay = new Date(currentYear.value, currentMonth.value, 0).getDate();
  for (let i = firstDayWeek - 1; i >= 0; i--) {
    dates.push({
      date: new Date(currentYear.value, currentMonth.value - 1, prevMonthLastDay - i),
      day: prevMonthLastDay - i,
      otherMonth: true
    });
  }
  
  // 当前月的日期
  for (let i = 1; i <= lastDay.getDate(); i++) {
    dates.push({
      date: new Date(currentYear.value, currentMonth.value, i),
      day: i,
      otherMonth: false
    });
  }
  
  // 填充下个月的日期
  const remainingDays = 42 - dates.length;
  for (let i = 1; i <= remainingDays; i++) {
    dates.push({
      date: new Date(currentYear.value, currentMonth.value + 1, i),
      day: i,
      otherMonth: true
    });
  }
  
  return dates;
});

// 日期格式化
const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
};

// 判断是否是当天
const isCurrentDay = (date) => {
  const today = new Date();
  return date.getDate() === today.getDate() &&
         date.getMonth() === today.getMonth() &&
         date.getFullYear() === today.getFullYear();
};

// 判断是否是选中日期
const isSelectedDate = (date) => {
  if (!selectedDate.value) return false;
  return date.getDate() === selectedDate.value.getDate() &&
         date.getMonth() === selectedDate.value.getMonth() &&
         date.getFullYear() === selectedDate.value.getFullYear();
};

// 切换月份
const changeMonth = (delta) => {
  currentDate.value = new Date(currentYear.value, currentMonth.value + delta, 1);
};

// 选择日期
const selectDate = (date) => {
  selectedDate.value = date;
};

const hour = ref(0);
const minute = ref(0);

// 调整小时
const adjustHour = (delta) => {
  hour.value = (hour.value + delta + 24) % 24;
};

// 调整分钟
const adjustMinute = (delta) => {
  minute.value = (minute.value + delta + 60) % 60;
};

// 选择当前时间
const selectNow = () => {
  const now = new Date();
  selectedDate.value = now;
  hour.value = now.getHours();
  minute.value = now.getMinutes();
};

// 确认选择
const confirmSelect = () => {
  if (selectedDate.value) {
    const date = new Date(selectedDate.value);
    date.setHours(hour.value);
    date.setMinutes(minute.value);
    emit('update:modelValue', date);
  }
  showPicker.value = false;
};

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  selectedDate.value = newVal ? new Date(newVal) : null;
});

const inputRef = ref(null);
const panelStyle = ref({});

const updatePosition = () => {
  if (!showPicker.value || !inputRef.value) return
  const inputRect = inputRef.value.getBoundingClientRect()
  const windowHeight = window.innerHeight
  const windowWidth = window.innerWidth
  const panelWidth = 280 // 弹窗宽度

  // 计算左侧位置，确保不超出屏幕
  let left = inputRect.left
  if (left + panelWidth > windowWidth) {
    left = windowWidth - panelWidth - 16 // 16px 是右侧边距
  }
  left = Math.max(8, left) // 确保左侧至少有 8px 边距

  // 计算顶部位置
  let top = inputRect.bottom + 8
  if (top + 380 > windowHeight) {
    top = inputRect.top - 380 - 8
  }

  panelStyle.value = {
    position: 'fixed',
    top: `${top}px`,
    left: `${left}px`,
    zIndex: 2000,
    width: `${panelWidth}px`, // 固定宽度
    maxWidth: `${windowWidth - 16}px` // 确保不超过屏幕宽度
  }
}

// 处理点击事件
const handleClick = () => {
  showPicker.value = !showPicker.value
  if (showPicker.value) {
    nextTick(() => {
      updatePosition()
    })
  }
}

// 添加滚动事件监听
onMounted(() => {
  window.addEventListener('scroll', updatePosition, true)
  window.addEventListener('resize', updatePosition)
})

// 移除事件监听
onBeforeUnmount(() => {
  window.removeEventListener('scroll', updatePosition, true)
  window.removeEventListener('resize', updatePosition)
})

const showYearMonthPanel = ref(false)
const yearRangeStart = ref(Math.floor(new Date().getFullYear() / 12) * 12)

// 计算年份范围
const yearRange = computed(() => {
  const years = []
  for (let i = 0; i < 12; i++) {
    years.push(yearRangeStart.value + i)
  }
  return years
})

// 切换年月选择面板
const toggleYearMonthPanel = () => {
  showYearMonthPanel.value = !showYearMonthPanel.value
}

// 选择年份
const selectYear = (year) => {
  currentDate.value = new Date(year, currentDate.value.getMonth(), 1)
}

// 选择月份
const selectMonth = (month) => {
  currentDate.value = new Date(currentDate.value.getFullYear(), month, 1)
  showYearMonthPanel.value = false
}

// 改变年份范围
const changeYearRange = (delta) => {
  yearRangeStart.value += delta
}

const showHourSelect = ref(false)
const showMinuteSelect = ref(false)
const hourListRef = ref(null)
const minuteListRef = ref(null)

// 生成分钟列表（每5分钟一个选项）
const minuteList = computed(() => {
  const list = []
  for (let i = 0; i < 60; i += 5) {
    list.push(i)
  }
  return list
})

// 选择小时
const selectHour = (h) => {
  hour.value = h
  showHourSelect.value = false
}

// 选择分钟
const selectMinute = (m) => {
  minute.value = m
  showMinuteSelect.value = false
}

// 当面板打开时，滚动到选中项
watch(showHourSelect, (val) => {
  if (val && hourListRef.value) {
    nextTick(() => {
      const selectedItem = hourListRef.value.children[hour.value]
      selectedItem?.scrollIntoView({ block: 'center' })
    })
  }
})

watch(showMinuteSelect, (val) => {
  if (val && minuteListRef.value) {
    nextTick(() => {
      const selectedItem = minuteListRef.value.children[minuteList.value.indexOf(minute.value)]
      selectedItem?.scrollIntoView({ block: 'center' })
    })
  }
})
</script>

<style scoped>
.custom-datetime-picker {
  position: relative;
  width: 100%;
}

.picker-input {
  height: 32px;
  padding: 0 12px;
  background-color: #1b1c31;
  border: 1px solid #2f3446;
  border-radius: 4px;
  display: flex;
  align-items: center;
  cursor: pointer;
  white-space: nowrap; /* 防止文本换行 */
  overflow: hidden; /* 防止内容溢出 */
}

.input-icon {
  color: #8b8c8f;
  margin-right: 8px;
  flex-shrink: 0; /* 防止图标被压缩 */
}

.date-text {
  color: #fff;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.placeholder {
  color: #8b8c8f;
}

.picker-panel {
  position: fixed;
  background-color: #1b1c31;
  border: 1px solid #2f3446;
  border-radius: 4px;
  padding: 16px;
  z-index: 2000;
  width: 280px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  box-sizing: border-box; /* 确保 padding 不会增加总宽度 */
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  color: rgb(229, 231, 235);
}

.arrow-btn {
  background: none;
  border: none;
  color: rgb(156, 163, 175);
  cursor: pointer;
  padding: 4px 8px;
  transition: color 0.3s;
}

.arrow-btn:hover {
  color: rgb(96, 165, 250);
}

.current-date {
  font-size: 14px;
  color: rgb(229, 231, 235);
}

.week-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  padding: 8px 0;
}

.week-header span {
  color: rgb(156, 163, 175);
  font-size: 12px;
  text-align: center;
}

.date-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  padding: 8px 0;
}

.date-cell {
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: rgb(229, 231, 235);
  font-size: 13px;
  border-radius: 4px;
  transition: all 0.3s;
}

.date-cell:hover {
  background-color: rgba(96, 165, 250, 0.1);
}

.date-cell.other-month {
  color: rgb(75, 85, 99);
}

.date-cell.current-day {
  color: rgb(96, 165, 250);
  font-weight: 500;
}

.date-cell.selected {
  background-color: rgb(96, 165, 250);
  color: white;
}

.panel-footer {
  display: flex;
  justify-content: space-between;
  padding: 8px;
  border-top: 1px solid rgb(55, 65, 81);
  margin-top: 8px;
}

.btn-now,
.btn-confirm {
  background: none;
  border: none;
  color: rgb(156, 163, 175);
  cursor: pointer;
  padding: 4px 8px;
  font-size: 13px;
  transition: color 0.3s;
}

.btn-now:hover,
.btn-confirm:hover {
  color: rgb(96, 165, 250);
}

.btn-confirm {
  color: rgb(96, 165, 250);
  font-weight: 500;
}

.time-picker {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  border-top: 1px solid #2f3446;
  margin-top: 12px;
}

.time-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.time-btn {
  background: none;
  border: none;
  color: #fff;
  cursor: pointer;
  padding: 4px 8px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.time-label {
  color: #8b8c8f;
  font-size: 12px;
}

.time-value {
  color: #fff;
  font-size: 16px;
  width: 32px;
  text-align: center;
}

.time-separator {
  color: #fff;
  margin: 0 12px;
  font-size: 16px;
  padding: 24px 0;
}

/* 移动端适配 */
@media screen and (max-width: 480px) {
  .picker-panel {
    width: calc(100vw - 32px); /* 屏幕宽度减去左右边距 */
    min-width: 280px;
    max-width: 100%;
    margin: 0 auto;
  }
  
  .date-grid {
    width: 100%;
  }
}

.year-month-panel {
  padding: 8px 0;
}

.year-selector {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.year-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  flex: 1;
  padding: 0 8px;
}

.year-item,
.month-item {
  padding: 4px 8px;
  text-align: center;
  color: #fff;
  cursor: pointer;
  border-radius: 4px;
}

.year-item:hover,
.month-item:hover {
  background-color: rgba(76, 175, 80, 0.1);
}

.year-item.selected,
.month-item.selected {
  background-color: #4caf50;
  color: #fff;
}

.year-btn {
  background: none;
  border: none;
  color: #8b8c8f;
  cursor: pointer;
  padding: 4px 8px;
}

.month-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  padding: 0 8px;
}

.current-date {
  cursor: pointer;
}

.current-date:hover {
  color: #4caf50;
}

.time-select-panel {
  position: absolute;
  background-color: #1b1c31;
  border: 1px solid #2f3446;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 2001;
  margin-top: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.time-select-list {
  padding: 4px 0;
}

.time-select-item {
  padding: 4px 16px;
  cursor: pointer;
  color: #fff;
  text-align: center;
}

.time-select-item:hover {
  background-color: rgba(76, 175, 80, 0.1);
}

.time-select-item.selected {
  background-color: #4caf50;
  color: #fff;
}

/* 自定义滚动条样式 */
.time-select-panel::-webkit-scrollbar {
  width: 6px;
}

.time-select-panel::-webkit-scrollbar-thumb {
  background-color: #2f3446;
  border-radius: 3px;
}

.time-select-panel::-webkit-scrollbar-track {
  background-color: transparent;
}
</style> 