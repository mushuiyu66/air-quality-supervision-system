<script setup lang="js">
import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, View } from '@element-plus/icons-vue'
import { getAllStatistics, getProvinceList, getCityList } from '@/api/admin'
import EcoAqiVisualizer from '@/components/EcoAqiVisualizer.vue'

// 搜索表单
const searchForm = reactive({
  province: '',
  city: '',
  startDate: '',
  endDate: ''
})

// 所有统计数据
const allStatistics = ref([])
// 筛选后的数据
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 省份和城市列表
const provinceList = ref([])
const cityOptions = ref([])

// 统一字段名：后端可能返回 snake_case 或 camelCase
const normalizeStatistics = (list) => {
  return (list || []).map(item => ({
    ...item,
    // AQI 综合等级
    aqi_id: item.aqi_id ?? item.aqiId ?? item.aqiGrade ?? item.aqi_grade ?? null,
    // 各污染物等级
    so2_level: item.so2_level ?? item.so2Level ?? null,
    co_level: item.co_level ?? item.coLevel ?? null,
    spm_level: item.spm_level ?? item.spmLevel ?? null,
    // 各污染物实测值
    so2_value: item.so2_value ?? item.so2Value ?? '-',
    co_value: item.co_value ?? item.coValue ?? '-',
    spm_value: item.spm_value ?? item.spmValue ?? '-',
    // 地址
    province_name: item.province_name ?? item.provinceName ?? '',
    city_name: item.city_name ?? item.cityName ?? '',
    // 时间
    confirm_date: item.confirm_date ?? item.confirmDate ?? '',
    confirm_time: item.confirm_time ?? item.confirmTime ?? '',
    // 反馈者电话
    tel_id: item.tel_id ?? item.telId ?? ''
  }))
}

// 获取所有统计数据
const fetchAllStatistics = async () => {
  loading.value = true
  try {
    const res = await getAllStatistics({
      page: currentPage.value,
      size: pageSize.value,
      provinceId: searchForm.province || undefined,
      cityId: searchForm.city || undefined,
      startDate: searchForm.startDate || undefined,
      endDate: searchForm.endDate || undefined
    })
    if (res.code === 200) {
      const pageData = res.data || {}
      allStatistics.value = normalizeStatistics(pageData.records || [])
      tableData.value = allStatistics.value
      total.value = pageData.total || 0
      updateLastUpdated()
    } else {
      ElMessage.error(res.msg || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 应用筛选条件
const applyFilters = () => {
  let filtered = [...allStatistics.value]
  
  // 省份筛选
  if (searchForm.province) {
    filtered = filtered.filter(item => item.province_id === searchForm.province)
  }
  
  // 城市筛选
  if (searchForm.city) {
    filtered = filtered.filter(item => item.city_id === searchForm.city)
  }
  
  // 日期范围筛选
  if (searchForm.startDate && searchForm.endDate) {
    filtered = filtered.filter(item => {
      const confirmDate = new Date(item.confirm_date)
      const startDate = new Date(searchForm.startDate)
      const endDate = new Date(searchForm.endDate)
      return confirmDate >= startDate && confirmDate <= endDate
    })
  } else if (searchForm.startDate) {
    filtered = filtered.filter(item => {
      const confirmDate = new Date(item.confirm_date)
      const startDate = new Date(searchForm.startDate)
      return confirmDate >= startDate
    })
  } else if (searchForm.endDate) {
    filtered = filtered.filter(item => {
      const confirmDate = new Date(item.confirm_date)
      const endDate = new Date(searchForm.endDate)
      return confirmDate <= endDate
    })
  }
  
  tableData.value = filtered
}

// 获取省份列表
const fetchProvinceList = async () => {
  try {
    const res = await getProvinceList()
    if (res.code === 200) {
      provinceList.value = res.data || []
    }
  } catch (error) {
    console.error('获取省份列表失败:', error)
  }
}

// 省份变更时更新城市列表
const handleProvinceChange = async (provinceId) => {
  searchForm.city = ''
  cityOptions.value = []
  
  if (provinceId) {
    try {
      const res = await getCityList(provinceId)
      if (res.code === 200) {
        cityOptions.value = res.data || []
      }
    } catch (error) {
      console.error('获取城市列表失败:', error)
    }
  }
}

// 搜索和重置
const handleSearch = () => {
  currentPage.value = 1
  fetchAllStatistics()
}

const handleReset = () => {
  searchForm.province = ''
  searchForm.city = ''
  searchForm.startDate = ''
  searchForm.endDate = ''
  cityOptions.value = []
  currentPage.value = 1
  fetchAllStatistics()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchAllStatistics()
}

// 工具函数
const getSo2LevelText = (level) => {
  const map = {
    1: '一级', 2: '二级', 3: '三级',
    4: '四级', 5: '五级', 6: '六级'
  }
  return map[level] || '未知'
}

const getCoLevelText = (level) => {
  const map = {
    1: '一级', 2: '二级', 3: '三级',
    4: '四级', 5: '五级', 6: '六级'
  }
  return map[level] || '未知'
}

const getSpmLevelText = (level) => {
  const map = {
    1: '一级', 2: '二级', 3: '三级',
    4: '四级', 5: '五级', 6: '六级'
  }
  return map[level] || '未知'
}

const getAqiText = (aqiId) => {
  const map = {
    1: '优', 2: '良', 3: '轻度污染',
    4: '中度污染', 5: '重度污染', 6: '严重污染'
  }
  return map[aqiId] || '未知'
}

const getAqiTagType = (aqiId) => {
  const map = {
    1: 'success', 2: '', 3: 'warning',
    4: 'danger', 5: 'danger', 6: 'danger'
  }
  return map[aqiId] || 'info'
}

const getAqiColor = (aqiId) => {
  const map = {
    1: '#4caf50', 2: '#8bc34a', 3: '#ff9800',
    4: '#ff5722', 5: '#f44336', 6: '#b71c1c'
  }
  return map[aqiId] || '#9e9e9e'
}

const getSo2TagType = (level) => {
  if (level <= 2) return 'success'
  if (level <= 4) return 'warning'
  return 'danger'
}

const getCoTagType = (level) => {
  if (level <= 2) return 'success'
  if (level <= 4) return 'warning'
  return 'danger'
}

const getSpmTagType = (level) => {
  if (level <= 2) return 'success'
  if (level <= 4) return 'warning'
  return 'danger'
}

// 详情弹窗
const detailDialogVisible = ref(false)
const detailInfo = ref({})

// 查看详情
const handleViewDetail = (row) => {
  detailInfo.value = { ...row }
  detailDialogVisible.value = true
}

// 自动刷新相关
const lastUpdated = ref('')
const autoRefreshEnabled = ref(true)
let refreshTimer = null

const updateLastUpdated = () => {
  const now = new Date()
  lastUpdated.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

// 处理页面可见性变化，切回页面时立即刷新
const handleVisibilityChange = () => {
  if (document.visibilityState === 'visible' && autoRefreshEnabled.value) {
    fetchAllStatistics()
  }
}

// 启动自动刷新
const startAutoRefresh = () => {
  stopAutoRefresh()
  refreshTimer = setInterval(() => {
    if (document.visibilityState === 'visible' && autoRefreshEnabled.value) {
      fetchAllStatistics()
    }
  }, 15000) // 15 秒轮询一次
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 初始化
onMounted(() => {
  fetchProvinceList()
  fetchAllStatistics()
  updateLastUpdated()
  startAutoRefresh()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  stopAutoRefresh()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<template>
  <div class="page-container page-fade-in">
    <el-card shadow="never" class="eco-card card-animate">
      <template #header>
        <div class="card-header page-card-header">
          <span class="title">📊 数据统计</span>
          <div class="header-actions">
            <span v-if="lastUpdated" class="sync-time">
              <el-icon><Refresh /></el-icon>
              已同步 {{ lastUpdated }}
            </span>
            <el-switch
              v-model="autoRefreshEnabled"
              active-text="自动刷新"
              inactive-text="手动刷新"
              inline-prompt
              size="small"
              class="auto-refresh-switch"
            />
            <el-button 
              type="primary" 
              size="small" 
              :icon="Refresh" 
              circle 
              @click="fetchAllStatistics" 
              :loading="loading"
              title="立即刷新"
            />
          </div>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="eco-filter-area">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="省份">
            <el-select 
              v-model="searchForm.province" 
              placeholder="请选择省份" 
              clearable 
              style="width: 140px"
              @change="handleProvinceChange"
            >
              <el-option
                v-for="province in provinceList"
                :key="province.province_id"
                :label="province.province_name"
                :value="province.province_id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="城市">
            <el-select 
              v-model="searchForm.city" 
              placeholder="请选择城市" 
              clearable 
              style="width: 140px"
            >
              <el-option
                v-for="city in cityOptions"
                :key="city.city_id"
                :label="city.city_name"
                :value="city.city_id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="日期范围">
            <el-date-picker
              v-model="searchForm.startDate"
              type="date"
              placeholder="开始日期"
              value-format="YYYY-MM-DD"
              style="width: 140px"
            />
          </el-form-item>

          <el-form-item>
            <el-date-picker
              v-model="searchForm.endDate"
              type="date"
              placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 140px"
            />
          </el-form-item>

          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" class="eco-btn">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        class="eco-table"
      >
        <el-table-column prop="id" label="统计ID" width="70" align="center" />
        
        <el-table-column label="省市区地址" min-width="180">
          <template #default="{ row }">
            <el-tooltip :content="(row.province_name || row.provinceName || '') + '/' + (row.city_name || row.cityName || '') + '/' + row.address" placement="top" :show-after="500">
              <span class="address-text">{{ (row.province_name || row.provinceName || '') + '/' + (row.city_name || row.cityName || '') + '/' + row.address }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="SO₂" width="95" align="center">
          <template #default="{ row }">
            <el-tag :type="getSo2TagType(row.so2_level)" size="small" effect="light">
              {{ row.so2_value }}·{{ getSo2LevelText(row.so2_level) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="CO" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getCoTagType(row.co_level)" size="small" effect="light">
              {{ row.co_value }}·{{ getCoLevelText(row.co_level) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="SPM" width="95" align="center">
          <template #default="{ row }">
            <el-tag :type="getSpmTagType(row.spm_level)" size="small" effect="light">
              {{ row.spm_value }}·{{ getSpmLevelText(row.spm_level) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="AQI等级" width="110" align="center">
          <template #default="{ row }">
            <div class="aqi-cell">
              <el-tag :type="getAqiTagType(row.aqi_id)" size="small" effect="light" round>
                {{ getAqiText(row.aqi_id) }}
              </el-tag>
              <div class="aqi-mini-bar">
                <div
                  class="aqi-mini-fill"
                  :style="{
                    width: `${Math.min((row.aqi_id || 0) / 6 * 100, 100)}%`,
                    background: getAqiColor(row.aqi_id)
                  }"
                />
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="确认时间" width="150" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.confirm_date }} {{ row.confirm_time }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="gm_id" label="网格员编号" width="90" align="center" />
        
        <el-table-column prop="tel_id" label="反馈者电话" width="120" align="center" />
        
        <el-table-column prop="information" label="反馈信息" min-width="170" show-overflow-tooltip />

        <el-table-column prop="remarks" label="检测备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.remarks || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{ row }">
            <div class="eco-icon-actions">
              <el-tooltip content="查看详情" placement="top" :show-after="300">
                <el-button type="primary" size="small" class="eco-icon-btn view" @click="handleViewDetail(row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="📊 统计详情"
      width="680px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="统计ID">{{ detailInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="确认时间">
          {{ detailInfo.confirm_date }} {{ detailInfo.confirm_time }}
        </el-descriptions-item>
        <el-descriptions-item label="省市区地址" :span="2">
          {{ (detailInfo.province_name || detailInfo.provinceName || '') + '/' + (detailInfo.city_name || detailInfo.cityName || '') + '/' + detailInfo.address }}
        </el-descriptions-item>
        <el-descriptions-item label="SO₂ 数值/等级">
          <el-tag :type="getSo2TagType(detailInfo.so2_level)" size="small" effect="light">
            {{ detailInfo.so2_value }} · {{ getSo2LevelText(detailInfo.so2_level) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="CO 数值/等级">
          <el-tag :type="getCoTagType(detailInfo.co_level)" size="small" effect="light">
            {{ detailInfo.co_value }} · {{ getCoLevelText(detailInfo.co_level) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="SPM 数值/等级">
          <el-tag :type="getSpmTagType(detailInfo.spm_level)" size="small" effect="light">
            {{ detailInfo.spm_value }} · {{ getSpmLevelText(detailInfo.spm_level) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="AQI 等级" :span="2">
          <div class="detail-aqi">
            <el-tag :type="getAqiTagType(detailInfo.aqi_id)" size="small" effect="light" round>
              {{ getAqiText(detailInfo.aqi_id) }}
            </el-tag>
            <EcoAqiVisualizer
              :so2="detailInfo.so2_level"
              :co="detailInfo.co_level"
              :spm="detailInfo.spm_level"
              :aqi="detailInfo.aqi_id"
            />
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="网格员编号">{{ detailInfo.gm_id }}</el-descriptions-item>
        <el-descriptions-item label="反馈者电话">{{ detailInfo.tel_id || detailInfo.telId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="反馈信息" :span="2">{{ detailInfo.information || '-' }}</el-descriptions-item>
        <el-descriptions-item label="检测备注" :span="2">{{ detailInfo.remarks || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.page-container {
  padding: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;

  .sync-time {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #66bb6a;
    background: rgba(200, 230, 201, 0.4);
    padding: 4px 10px;
    border-radius: 12px;

    .el-icon {
      animation: rotate-refresh 2s linear infinite;
    }
  }

  .auto-refresh-switch {
    :deep(.el-switch__label) {
      color: #558b2f;
      font-size: 12px;
    }
  }
}

@keyframes rotate-refresh {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.filter-form {
  margin-bottom: 0;
}

.address-text,
.time-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.aqi-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.aqi-mini-bar {
  width: 70px;
  height: 5px;
  background: rgba(200, 230, 201, 0.6);
  border-radius: 3px;
  overflow: hidden;
}

.aqi-mini-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.5s ease;
}

.detail-aqi {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
}
</style>
