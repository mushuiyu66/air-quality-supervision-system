<script setup lang="js">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, LocationFilled, View, DocumentChecked } from '@element-plus/icons-vue'
import { 
  getAssignedFeedbacks,
  confirmStatistics,
  getAqiList,
  getStatisticsByAfId
} from '@/api/gridMember'
import EcoAqiVisualizer from '@/components/EcoAqiVisualizer.vue'

// 列表数据
const loading = ref(false)
const tableData = ref([])
const aqiList = ref([])

// 工单统计
const taskStats = computed(() => {
  const total = tableData.value.length
  const pending = tableData.value.filter(item => item.state === 1).length
  const completed = tableData.value.filter(item => item.state === 2).length
  return { total, pending, completed }
})

// 分页后的展示数据
const displayData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return tableData.value.slice(start, end)
})

// 查看详情弹窗
const detailDialogVisible = ref(false)
const detailInfo = ref({})
const detailLoading = ref(false)
const statisticsInfo = ref(null)

// 预测等级信息
const predictedLevel = ref({
  so2Level: null,
  coLevel: null,
  spmLevel: null,
  aqiId: null,
  aqiText: ''
})

// 搜索条件
const searchKey = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 现场检测弹窗
const detectDialogVisible = ref(false)
const currentFeedback = ref(null)
const detectFormRef = ref(null)
const detectForm = reactive({
  so2Value: null,
  coValue: null,
  spmValue: null,
  information: '',  // 反馈原始描述
  remarks: ''       // 检测备注
})

// 验证规则
const detectRules = {
  so2Value: [
    { required: true, message: '请输入二氧化硫浓度', trigger: 'blur' },
    { type: 'number', min: 0, message: '数值必须大于等于0', trigger: 'blur' }
  ],
  coValue: [
    { required: true, message: '请输入一氧化碳浓度', trigger: 'blur' },
    { type: 'number', min: 0, message: '数值必须大于等于0', trigger: 'blur' }
  ],
  spmValue: [
    { required: true, message: '请输入悬浮颗粒物浓度', trigger: 'blur' },
    { type: 'number', min: 0, message: '数值必须大于等于0', trigger: 'blur' }
  ]
}

// 获取AQI标准列表
const fetchAqiList = async () => {
  try {
    const res = await getAqiList()
    if (res.code === 200) {
      aqiList.value = res.data || []
      console.log('AQI标准列表:', aqiList.value)
    }
  } catch (error) {
    console.error('获取AQI标准失败:', error)
  }
}

// 计算污染物等级
const calculateLevel = (value, min, max) => {
  if (value === null || value === undefined) return null
  return value >= min && value <= max
}

// 实时预测AQI等级
const predictAqiLevel = () => {
  // 重置预测结果
  predictedLevel.value = {
    so2Level: null,
    coLevel: null,
    spmLevel: null,
    aqiId: null,
    aqiText: ''
  }
  
  // 如果三个值都为空,不预测
  if (detectForm.so2Value === null && detectForm.coValue === null && detectForm.spmValue === null) {
    return
  }
  
  // 分别计算每个污染物的等级
  for (const standard of aqiList.value) {
    // 检查SO2是否在范围内
    if (detectForm.so2Value !== null && predictedLevel.value.so2Level === null) {
      if (detectForm.so2Value >= standard.so2_min && detectForm.so2Value <= standard.so2_max) {
        predictedLevel.value.so2Level = standard.aqi_id
      }
    }
    
    // 检查CO是否在范围内
    if (detectForm.coValue !== null && predictedLevel.value.coLevel === null) {
      if (detectForm.coValue >= standard.co_min && detectForm.coValue <= standard.co_max) {
        predictedLevel.value.coLevel = standard.aqi_id
      }
    }
    
    // 检查SPM是否在范围内
    if (detectForm.spmValue !== null && predictedLevel.value.spmLevel === null) {
      if (detectForm.spmValue >= standard.spm_min && detectForm.spmValue <= standard.spm_max) {
        predictedLevel.value.spmLevel = standard.aqi_id
      }
    }
  }
  
  // 计算综合AQI等级：取所有污染物等级中的最大值
  const levels = [
    predictedLevel.value.so2Level,
    predictedLevel.value.coLevel,
    predictedLevel.value.spmLevel
  ].filter(level => level !== null)
  
  if (levels.length > 0) {
    // 找到最大等级作为综合AQI
    const maxLevel = Math.max(...levels)
    predictedLevel.value.aqiId = maxLevel
    
    // 从AQI标准表中获取对应的描述文本
    const matchedStandard = aqiList.value.find(s => s.aqi_id === maxLevel)
    if (matchedStandard) {
      predictedLevel.value.aqiText = matchedStandard.chinese_explain || matchedStandard.aqi_explain || ''
    }
  }
  
  console.log('预测等级:', predictedLevel.value)
}

// 获取分配给当前网格员的工单
const fetchTasks = async () => {
  const gmId = localStorage.getItem('gmId')
  if (!gmId) {
    ElMessage.warning('请先登录')
    return
  }
  
  loading.value = true
  try {
    const res = await getAssignedFeedbacks(gmId)
    
    if (res.code === 200) {
      let data = res.data || []
      
      // 前端搜索过滤
      if (searchKey.value) {
        data = data.filter(item => 
          item.information && item.information.includes(searchKey.value)
        )
      }
      
      tableData.value = data
    } else {
      ElMessage.error(res.msg || '获取工单失败')
    }
  } catch (error) {
    console.error('获取工单异常:', error)
    ElMessage.error(error.message || '获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchTasks()
}

// 重置
const handleReset = () => {
  searchKey.value = ''
  handleSearch()
}

// 打开现场检测弹窗
const handleDetect = (row) => {
  currentFeedback.value = row
  // 重置表单
  detectForm.so2Value = null
  detectForm.coValue = null
  detectForm.spmValue = null
  detectForm.information = row.information || ''  // 保留原始反馈描述
  detectForm.remarks = ''                         // 清空检测备注
  // 重置预测等级
  predictedLevel.value = {
    so2Level: null,
    coLevel: null,
    spmLevel: null,
    aqiId: null,
    aqiText: ''
  }
  detectDialogVisible.value = true
}

// 打开查看详情弹窗
const handleViewDetail = async (row) => {
  detailInfo.value = { ...row }
  statisticsInfo.value = null
  detailDialogVisible.value = true
  detailLoading.value = true
  
  const afId = row.af_id || row.afId
  if (afId) {
    try {
      const res = await getStatisticsByAfId(afId)
      if (res.code === 200) {
        statisticsInfo.value = res.data || null
      }
    } catch (error) {
      console.error('获取检测记录失败:', error)
    } finally {
      detailLoading.value = false
    }
  } else {
    detailLoading.value = false
  }
}

// 获取等级文本
const getLevelText = (level) => {
  const map = {
    1: '优',
    2: '良',
    3: '轻度污染',
    4: '中度污染',
    5: '重度污染',
    6: '严重污染'
  }
  return map[level] || `未知 (${level}级)`
}

// 获取等级标签类型
const getLevelTagType = (level) => {
  if (level === 1) return 'success'
  if (level === 2) return ''
  if (level === 3) return 'warning'
  if (level >= 4 && level <= 6) return 'danger'
  return 'info' // 未知等级用灰色
}

// 获取AQI标签类型
const getAqiTagType = (aqiId) => {
  if (aqiId === 1) return 'success'
  if (aqiId === 2) return ''
  if (aqiId === 3) return 'warning'
  return 'danger'
}

// 提交现场检测数据并确认工单
const handleSubmitDetect = async () => {
  try {
    await detectFormRef.value.validate()
    
    const gmId = localStorage.getItem('gmId')
    if (!gmId) {
      ElMessage.warning('请先登录')
      return
    }
    
    // 获取当前反馈信息
    const feedback = currentFeedback.value
    if (!feedback) {
      ElMessage.error('未找到反馈信息')
      return
    }
    
    // 构建AqiFeedBack对象，传递给后端 /statistics/confirm 接口
    // 后端会根据实测值自动计算等级并生成statistics记录
    const confirmData = {
      afId: Number(feedback.af_id || feedback.afId),  // 反馈ID
      telId: feedback.tel_id || feedback.telId,       // 监督员电话
      provinceId: feedback.province_id || feedback.provinceId,
      cityId: feedback.city_id || feedback.cityId,
      address: feedback.address,
      information: feedback.information,              // 原始反馈描述
      estimatedGrade: feedback.estimated_grade || feedback.estimatedGrade,
      afDate: feedback.af_date || feedback.afDate,
      afTime: feedback.af_time || feedback.afTime,
      gmId: Number(gmId),                             // 网格员ID
      // 实测值（后端会根据这些值计算等级）
      so2Value: detectForm.so2Value,
      coValue: detectForm.coValue,
      spmValue: detectForm.spmValue,
      remarks: detectForm.remarks                     // 检测备注
    }
    
    console.log('提交的确认数据:', confirmData)
    
    // 调用 /statistics/confirm 接口
    const res = await confirmStatistics(confirmData)
    if (res.code === 200) {
      ElMessage.success('现场检测完成，工单已确认')
      detectDialogVisible.value = false
      fetchTasks()
    } else {
      ElMessage.error(res.msg || '确认失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '录入失败')
    }
  }
}

// 初始化
onMounted(() => {
  fetchAqiList()
  fetchTasks()
})
</script>

<template>
  <div class="page-container page-fade-in">
    <el-card shadow="never" class="eco-card card-animate">
      <template #header>
        <div class="card-header page-card-header">
          <span class="title">🔧 工单处理</span>
        </div>
      </template>

      <!-- 工单统计卡片 -->
      <el-row :gutter="16" class="task-stats-row">
        <el-col :span="8">
          <div class="stat-card total">
            <div class="stat-icon"><el-icon :size="28"><DocumentChecked /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ taskStats.total }}</div>
              <div class="stat-label">工单总数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card pending">
            <div class="stat-icon"><el-icon :size="28"><LocationFilled /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ taskStats.pending }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card completed">
            <div class="stat-icon"><el-icon :size="28"><View /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ taskStats.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 搜索区域 -->
      <div class="eco-filter-area">
        <div class="filter-left">
          <el-input
            v-model="searchKey"
            placeholder="请输入反馈描述搜索"
            clearable
            style="width: 260px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </div>

        <div class="filter-right">
          <el-tag type="success" effect="light" round>🌿 待处理工单</el-tag>
          <el-tag type="warning" effect="light" round class="ml-8">{{ taskStats.pending }} 个</el-tag>
        </div>
      </div>

      <el-table 
        :data="displayData" 
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        class="eco-table"
      >
        <el-table-column prop="af_id" label="工单ID" width="70" align="center" />
        <el-table-column prop="tel_id" label="监督员电话" width="120" />
        <el-table-column prop="address" label="详细地址" min-width="160" show-overflow-tooltip />
        <el-table-column prop="information" label="反馈描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="预估等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              v-if="row.estimated_grade !== null && row.estimated_grade !== undefined"
              :type="getLevelTagType(row.estimated_grade)"
              size="small"
              effect="light"
              round
            >
              {{ getLevelText(row.estimated_grade) }}
            </el-tag>
            <el-tag v-else type="info" size="small" effect="light" round>-</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="工单状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.state === 1 ? 'primary' : row.state === 2 ? 'success' : 'info'"
              size="small"
              effect="light"
              round
            >
              {{ row.state === 1 ? '待处理' : row.state === 2 ? '已完成' : '待指派' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="af_date" label="反馈日期" width="105" />
        <el-table-column prop="af_time" label="反馈时间" width="100" />
        <el-table-column label="操作" width="90" fixed="right" align="center">
          <template #default="{ row }">
            <div class="eco-icon-actions compact nowrap">
              <el-tooltip v-if="row.state === 1" content="现场检测" placement="top" :show-after="300">
                <el-button 
                  type="primary" 
                  size="small" 
                  class="eco-icon-btn edit"
                  @click="handleDetect(row)"
                >
                  <el-icon><LocationFilled /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip v-else content="查看详情" placement="top" :show-after="300">
                <el-button 
                  type="primary" 
                  size="small" 
                  class="eco-icon-btn view"
                  @click="handleViewDetail(row)"
                >
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="tableData.length > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="tableData.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="(val) => { currentPage = 1; pageSize = val; fetchTasks() }"
          @current-change="() => fetchTasks()"
        />
      </div>
      <div class="pagination-summary" v-else-if="tableData.length > 0">
        <el-tag type="success" effect="light" round>
          共 {{ tableData.length }} 条工单记录
        </el-tag>
      </div>
    </el-card>

    <!-- 现场检测弹窗 -->
    <el-dialog
      v-model="detectDialogVisible"
      title="现场检测数据录入"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="detectFormRef"
        :model="detectForm"
        :rules="detectRules"
        label-width="140px"
        size="large"
      >
        <el-divider content-position="left">基础信息</el-divider>
        
        <el-form-item label="反馈地址">
          <el-input :value="currentFeedback?.address || '-'" disabled />
        </el-form-item>
        
        <el-form-item label="反馈描述">
          <el-input :value="currentFeedback?.information || '-'" disabled />
        </el-form-item>
        
        <el-divider content-position="left">污染物实测数据</el-divider>
        
        <el-form-item label="二氧化硫(SO₂)浓度" prop="so2Value">
          <el-input-number
            v-model="detectForm.so2Value"
            :min="0"
            :precision="2"
            :step="0.1"
            placeholder="请输入实测值"
            style="width: 100%"
            @input="predictAqiLevel"
          />
          <span style="margin-left: 8px; color: #909399">单位: μg/m³</span>
        </el-form-item>
        
        <el-form-item label="一氧化碳(CO)浓度" prop="coValue">
          <el-input-number
            v-model="detectForm.coValue"
            :min="0"
            :precision="2"
            :step="0.1"
            placeholder="请输入实测值"
            style="width: 100%"
            @input="predictAqiLevel"
          />
          <span style="margin-left: 8px; color: #909399">单位: mg/m³</span>
        </el-form-item>
        
        <el-form-item label="悬浮颗粒物(SPM)浓度" prop="spmValue">
          <el-input-number
            v-model="detectForm.spmValue"
            :min="0"
            :precision="2"
            :step="0.1"
            placeholder="请输入实测值"
            style="width: 100%"
            @input="predictAqiLevel"
          />
          <span style="margin-left: 8px; color: #909399">单位: μg/m³</span>
        </el-form-item>
        
        <!-- 实时预测等级可视化 -->
        <el-divider content-position="left">🌱 预测结果</el-divider>
        
        <el-form-item label="空气质量预测">
          <EcoAqiVisualizer
            :so2="predictedLevel.so2Level"
            :co="predictedLevel.coLevel"
            :spm="predictedLevel.spmLevel"
            :aqi="predictedLevel.aqiId"
            :aqi-text="predictedLevel.aqiText"
          />
        </el-form-item>
        
        <el-divider content-position="left">补充说明</el-divider>
        
        <el-form-item label="检测备注" prop="remarks">
          <el-input
            v-model="detectForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入检测过程中的其他情况说明"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="detectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitDetect">提交检测结果</el-button>
      </template>
    </el-dialog>

    <!-- 工单详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="工单详情"
      width="620px"
    >
      <el-skeleton v-if="detailLoading" :rows="6" animated />
      
      <template v-else>
        <!-- 基础信息 -->
        <el-descriptions :column="1" border class="eco-descriptions">
          <el-descriptions-item label="工单ID">{{ detailInfo.af_id || detailInfo.afId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="监督员电话">{{ detailInfo.tel_id || detailInfo.telId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="详细地址">{{ detailInfo.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="反馈描述">{{ detailInfo.information || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预估等级">
            <el-tag
              v-if="detailInfo.estimated_grade !== null && detailInfo.estimated_grade !== undefined"
              :type="getLevelTagType(detailInfo.estimated_grade)"
              size="small"
              effect="light"
              round
            >
              {{ getLevelText(detailInfo.estimated_grade) }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="工单状态">
            <el-tag
              :type="detailInfo.state === 1 ? 'primary' : detailInfo.state === 2 ? 'success' : 'info'"
              size="small"
              effect="light"
              round
            >
              {{ detailInfo.state === 1 ? '待处理' : detailInfo.state === 2 ? '已完成' : '待指派' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="反馈日期">{{ detailInfo.af_date || detailInfo.afDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="反馈时间">{{ detailInfo.af_time || detailInfo.afTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 实测检测数据 -->
        <template v-if="statisticsInfo">
          <el-divider content-position="left">🌱 网格员现场检测结果</el-divider>
          
          <el-descriptions :column="2" border class="eco-descriptions">
            <el-descriptions-item label="SO₂ 实测值">
              <span class="value-text">{{ statisticsInfo.so2_value ?? statisticsInfo.so2Value ?? '-' }} μg/m³</span>
            </el-descriptions-item>
            <el-descriptions-item label="SO₂ 等级">
              <el-tag
                :type="getLevelTagType(statisticsInfo.so2_level ?? statisticsInfo.so2Level)"
                size="small"
                effect="light"
                round
              >
                {{ getLevelText(statisticsInfo.so2_level ?? statisticsInfo.so2Level) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="CO 实测值">
              <span class="value-text">{{ statisticsInfo.co_value ?? statisticsInfo.coValue ?? '-' }} mg/m³</span>
            </el-descriptions-item>
            <el-descriptions-item label="CO 等级">
              <el-tag
                :type="getLevelTagType(statisticsInfo.co_level ?? statisticsInfo.coLevel)"
                size="small"
                effect="light"
                round
              >
                {{ getLevelText(statisticsInfo.co_level ?? statisticsInfo.coLevel) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="SPM 实测值">
              <span class="value-text">{{ statisticsInfo.spm_value ?? statisticsInfo.spmValue ?? '-' }} μg/m³</span>
            </el-descriptions-item>
            <el-descriptions-item label="SPM 等级">
              <el-tag
                :type="getLevelTagType(statisticsInfo.spm_level ?? statisticsInfo.spmLevel)"
                size="small"
                effect="light"
                round
              >
                {{ getLevelText(statisticsInfo.spm_level ?? statisticsInfo.spmLevel) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="综合 AQI 等级" :span="2">
              <el-tag
                :type="getAqiTagType(statisticsInfo.aqi_id ?? statisticsInfo.aqiId)"
                size="small"
                effect="dark"
                round
              >
                {{ getLevelText(statisticsInfo.aqi_id ?? statisticsInfo.aqiId) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="检测备注" :span="2">
              <span class="remarks-text">{{ statisticsInfo.remarks || '暂无备注' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="确认时间" :span="2">
              {{ statisticsInfo.confirm_date || statisticsInfo.confirmDate || '-' }} 
              {{ statisticsInfo.confirm_time || statisticsInfo.confirmTime || '' }}
            </el-descriptions-item>
          </el-descriptions>
        </template>

        <el-alert
          v-else-if="detailInfo.state === 2"
          title="未找到检测记录"
          type="warning"
          :closable="false"
          description="该工单标记为已完成，但未查询到对应的统计检测记录，可能是旧数据。"
          style="margin-top: 16px;"
        />
      </template>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.page-container {
  padding: 0;
}

.search-bar {
  margin-bottom: 0;
}

.action-bar {
  margin-left: auto;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding: 12px 0;

  :deep(.el-pagination) {
    --el-pagination-bg-color: rgba(255, 255, 255, 0.6);
    --el-pagination-hover-color: #2e7d32;
    --el-pagination-button-disabled-color: #b0bec5;
    --el-pagination-button-color: #1b5e20;

    .el-pagination__total,
    .el-pagination__jump {
      color: #558b2f;
      font-weight: 500;
    }

    .el-pagination__sizes {
      .el-input__wrapper {
        background: rgba(255, 255, 255, 0.7);
        box-shadow: 0 0 0 1px rgba(129, 199, 132, 0.4) inset;
      }
    }

    .btn-prev,
    .btn-next,
    .el-pager li {
      background: rgba(255, 255, 255, 0.7);
      border: 1px solid rgba(129, 199, 132, 0.25);
      border-radius: 8px;
      margin: 0 4px;
      font-weight: 500;
      transition: all 0.25s ease;

      &:hover {
        background: rgba(200, 230, 201, 0.5);
        border-color: rgba(76, 175, 80, 0.4);
      }

      &.is-active {
        background: linear-gradient(135deg, #66bb6a, #43a047);
        color: #fff;
        border-color: transparent;
        box-shadow: 0 4px 12px rgba(67, 160, 71, 0.3);
      }
    }

    .el-pagination__jump {
      .el-input__wrapper {
        background: rgba(255, 255, 255, 0.7);
        box-shadow: 0 0 0 1px rgba(129, 199, 132, 0.4) inset;
      }
    }
  }
}

.pagination-summary {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding: 12px 0;
}

// 工单统计卡片
.task-stats-row {
  margin-bottom: 16px;

  .stat-card {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 16px 20px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(8px);
    border: 1px solid rgba(129, 199, 132, 0.25);
    box-shadow: 0 4px 16px rgba(27, 94, 32, 0.08);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(27, 94, 32, 0.14);
    }

    .stat-icon {
      width: 52px;
      height: 52px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;
    }

    .stat-info {
      flex: 1;
      min-width: 0;
    }

    .stat-value {
      font-size: 26px;
      font-weight: 700;
      line-height: 1.1;
      color: #1b5e20;
    }

    .stat-label {
      font-size: 13px;
      color: #666;
      margin-top: 4px;
    }

    &.total .stat-icon {
      background: linear-gradient(135deg, #66bb6a, #43a047);
    }

    &.pending .stat-icon {
      background: linear-gradient(135deg, #ffa726, #fb8c00);
    }

    &.completed .stat-icon {
      background: linear-gradient(135deg, #42a5f5, #1976d2);
    }
  }
}

// 筛选区左右布局
.eco-filter-area {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;

  .filter-left {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
  }

  .filter-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.ml-8 {
  margin-left: 8px;
}

.value-text {
  font-weight: 600;
  color: #2e7d32;
}

.remarks-text {
  color: #555;
  line-height: 1.6;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .task-stats-row {
    .stat-card {
      padding: 12px 14px;
      gap: 10px;

      .stat-icon {
        width: 40px;
        height: 40px;
      }

      .stat-value {
        font-size: 20px;
      }
    }
  }

  .eco-filter-area {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
