<script setup lang="js">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Position, CircleCheck, View } from '@element-plus/icons-vue'
import { getFeedbackList, assignFeedback } from '@/api/feedback'
import { getAllGridMembers, getGridMembersByCityId } from '@/api/gridMember'

// 筛选表单
const filterForm = reactive({
  keyword: ''
})

// 列表数据
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const filteredTableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 网格员列表
const gridMemberList = ref([])

// 指派弹窗
const assignDialogVisible = ref(false)
const currentFeedback = ref(null)
const assignForm = reactive({
  gmId: null
})

// 详情弹窗
const detailDialogVisible = ref(false)
const detailInfo = ref({})

// 获取工单列表
const fetchFeedbackList = async () => {
  loading.value = true
  try {
    const res = await getFeedbackList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: filterForm.keyword || undefined
    })
    if (res.code === 200) {
      const pageData = res.data || {}
      tableData.value = pageData.records || []
      filteredTableData.value = tableData.value
      total.value = pageData.total || 0
    } else {
      ElMessage.error(res.msg || res.message || '获取工单列表失败')
    }
  } catch (error) {
    console.error('获取工单失败:', error)
    ElMessage.error(error.message || '获取数据失败,请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取所有网格员列表（保留用于其他场景）
const fetchGridMemberList = async () => {
  try {
    const res = await getAllGridMembers()
    console.log('网格员列表响应:', res)
    if (res.code === 200) {
      let data = res.data || []
      
      // 标准化字段名:将驼峰命名转换为下划线命名,确保一致性
      gridMemberList.value = data.map(member => ({
        gm_id: member.gm_id || member.gmId,
        gm_name: member.gm_name || member.gmName,
        tel: member.tel,
        province_id: member.province_id || member.provinceId,
        city_id: member.city_id || member.cityId,
        state: member.state,
        ...member // 保留其他字段
      }))
      
      console.log('标准化后的网格员列表数据:', gridMemberList.value)
      // 打印第一个元素的字段名,用于调试
      if (gridMemberList.value.length > 0) {
        console.log('第一个网格员的字段:', Object.keys(gridMemberList.value[0]))
      }
    }
  } catch (error) {
    console.error('获取网格员列表失败:', error)
  }
}

// 根据城市ID获取网格员列表（用于指派工单）
const fetchGridMembersByCity = async (cityId) => {
  if (!cityId) {
    console.warn('未提供城市ID')
    return []
  }
  
  try {
    const res = await getGridMembersByCityId(cityId)
    console.log(`获取城市${cityId}的网格员列表响应:`, res)
    if (res.code === 200) {
      let data = res.data || []
      
      // 标准化字段名
      const members = data.map(member => ({
        gm_id: member.gm_id || member.gmId,
        gm_name: member.gm_name || member.gmName,
        tel: member.tel,
        province_id: member.province_id || member.provinceId,
        city_id: member.city_id || member.cityId,
        state: member.state,
        ...member
      }))
      
      console.log(`城市${cityId}的网格员列表:`, members)
      return members
    } else {
      console.error('获取网格员列表失败:', res.msg)
      return []
    }
  } catch (error) {
    console.error(`获取城市${cityId}的网格员列表失败:`, error)
    return []
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchFeedbackList()
}

// 重置
const handleReset = () => {
  filterForm.keyword = ''
  currentPage.value = 1
  fetchFeedbackList()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchFeedbackList()
}

// 指派工单
const handleAssign = async (row) => {
  currentFeedback.value = row
  assignForm.gmId = null
  
  // 打开弹窗前先加载对应城市的网格员列表
  assignDialogVisible.value = true
  
  // 根据工单的city_id获取该城市的网格员
  const cityId = row.city_id || row.cityId
  if (cityId) {
    loading.value = true
    try {
      const members = await fetchGridMembersByCity(cityId)
      gridMemberList.value = members
      
      if (members.length === 0) {
        ElMessage.warning(`该地区暂无可用网格员`)
      }
    } finally {
      loading.value = false
    }
  } else {
    ElMessage.warning('工单缺少城市信息')
    gridMemberList.value = []
  }
}

// 提交指派
const handleSubmitAssign = async () => {
  if (!assignForm.gmId) {
    return ElMessage.warning('请选择网格员')
  }

  submitLoading.value = true
  try {
    // 构造完整的工单对象,包含所有字段
    const updateData = {
      ...currentFeedback.value,
      gm_id: assignForm.gmId,
      state: 1, // 已指派状态
      assign_date: new Date().toISOString().split('T')[0], // 当前日期
      assign_time: new Date().toTimeString().split(' ')[0] // 当前时间
    }
    
    const res = await assignFeedback(updateData)
    if (res.code === 200) {
      ElMessage.success('指派成功')
      assignDialogVisible.value = false
      fetchFeedbackList()
    } else {
      ElMessage.error(res.msg || '指派失败')
    }
  } catch (error) {
    console.error('指派失败:', error)
    ElMessage.error(error.message || '指派失败')
  } finally {
    submitLoading.value = false
  }
}

// 确认工单
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确认该工单已完成?', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    // 构造更新数据
    const updateData = {
      ...row,
      state: 2 // 已确认状态
    }
    
    const res = await assignFeedback(updateData)
    if (res.code === 200) {
      ElMessage.success('工单已确认')
      fetchFeedbackList()
    } else {
      ElMessage.error(res.msg || '确认失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认失败:', error)
    }
  }
}

// 查看详情
const handleDetail = (row) => {
  detailInfo.value = { ...row }
  detailDialogVisible.value = true
}



// 工具函数
const getFullAddress = (row) => {
  return `${row.province_name || row.provinceName || ''}/${row.city_name || row.cityName || ''}/${row.address}`
}

const getStateText = (state) => {
  const map = { 0: '未指派', 1: '已指派', 2: '已确认' }
  return map[state] || '未知'
}

const getStateTagType = (state) => {
  const map = { 0: 'danger', 1: 'primary', 2: 'success' }
  return map[state] || 'info'
}

const getAqiText = (grade) => {
  const map = {
    1: '优', 2: '良', 3: '轻度污染',
    4: '中度污染', 5: '重度污染', 6: '严重污染'
  }
  return map[grade] || '未知'
}

const getAqiTagType = (grade) => {
  const map = {
    1: 'success', 2: '', 3: 'warning',
    4: 'danger', 5: 'danger', 6: 'danger'
  }
  return map[grade] || 'info'
}

// 初始化
onMounted(() => {
  fetchFeedbackList()
  fetchGridMemberList()
})
</script>
<template>
  <div class="page-container page-fade-in">
    <el-card shadow="never" class="eco-card card-animate">
      <template #header>
        <div class="card-header page-card-header">
          <span class="title">📋 工单管理</span>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="eco-filter-area">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="关键词">
            <el-input
              v-model="filterForm.keyword"
              placeholder="搜索地址、反馈内容或手机号"
              clearable
              style="width: 280px"
            />
          </el-form-item>

          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="filteredTableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        class="eco-table"
      >
        <el-table-column prop="af_id" label="工单ID" width="70" align="center" />
        <el-table-column prop="tel_id" label="手机号码" width="125" align="center" />
        
        <el-table-column label="省市区地址" min-width="180">
          <template #default="{ row }">
            <el-tooltip :content="getFullAddress(row)" placement="top" :show-after="500">
              <span class="address-text">{{ getFullAddress(row) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column prop="information" label="反馈信息" min-width="170" show-overflow-tooltip />

        <el-table-column label="预估等级" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getAqiTagType(row.estimated_grade)" size="small" effect="light">
              {{ getAqiText(row.estimated_grade) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="指派网格员" width="110" align="center">
          <template #default="{ row }">
            <span v-if="row.gm_id && row.gm_id > 0" class="assigned-name">{{ row.gm_name || '网格员' }}</span>
            <span v-else class="not-assigned">未指派</span>
          </template>
        </el-table-column>

        <el-table-column label="指派时间" width="150" align="center">
          <template #default="{ row }">
            <span v-if="row.assign_date && row.assign_time" class="time-text">{{ row.assign_date }} {{ row.assign_time }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStateTagType(row.state)" size="small" effect="light" round>
              {{ getStateText(row.state) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="af_date" label="反馈日期" width="95" align="center" />

        <el-table-column label="操作" width="110" fixed="right" align="center">
          <template #default="{ row }">
            <div class="eco-icon-actions compact nowrap">
              <el-tooltip v-if="row.state === 0" content="指派网格员" placement="top" :show-after="300">
                <el-button
                  type="primary"
                  size="small"
                  class="eco-icon-btn assign"
                  @click="handleAssign(row)"
                >
                  <el-icon><Position /></el-icon>
                </el-button>
              </el-tooltip>

              <el-tooltip v-if="row.state === 1" content="确认完成" placement="top" :show-after="300">
                <el-button
                  type="success"
                  size="small"
                  class="eco-icon-btn confirm"
                  @click="handleConfirm(row)"
                >
                  <el-icon><CircleCheck /></el-icon>
                </el-button>
              </el-tooltip>

              <el-tooltip content="查看详情" placement="top" :show-after="300">
                <el-button
                  type="info"
                  size="small"
                  class="eco-icon-btn view"
                  @click="handleDetail(row)"
                >
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

    <!-- 指派弹窗 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="指派工单"
      width="500px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单编号">{{ currentFeedback?.af_id }}</el-descriptions-item>
        <el-descriptions-item label="手机号码">{{ currentFeedback?.tel_id }}</el-descriptions-item>
        <el-descriptions-item label="省市区地址" :span="2">
          {{ getFullAddress(currentFeedback) }}
        </el-descriptions-item>
        <el-descriptions-item label="反馈信息" :span="2">{{ currentFeedback?.information }}</el-descriptions-item>
      </el-descriptions>

      <el-form label-width="100px" style="margin-top: 20px;">
        <el-form-item label="分配给">
          <el-select
            v-model="assignForm.gmId"
            placeholder="请选择网格员"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="member in gridMemberList"
              :key="member.gm_id"
              :label="`${member.gm_name} (${member.tel})`"
              :value="member.gm_id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAssign" :loading="submitLoading">
          确定指派
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="工单详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单编号">{{ detailInfo.af_id }}</el-descriptions-item>
        <el-descriptions-item label="手机号码">{{ detailInfo.tel_id }}</el-descriptions-item>
        <el-descriptions-item label="省市区地址" :span="2">
          {{ getFullAddress(detailInfo) }}
        </el-descriptions-item>
        <el-descriptions-item label="反馈信息" :span="2">{{ detailInfo.information }}</el-descriptions-item>
        <el-descriptions-item label="预估等级">
          <el-tag :type="getAqiTagType(detailInfo.estimated_grade)">
            {{ getAqiText(detailInfo.estimated_grade) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="反馈时间" :span="2">
          {{ detailInfo.af_date }} {{ detailInfo.af_time }}
        </el-descriptions-item>
        <el-descriptions-item label="指派网格员">
          {{ detailInfo.gm_name || '未指派' }}
        </el-descriptions-item>
        <el-descriptions-item label="指派时间" :span="2">
          {{ detailInfo.assign_date && detailInfo.assign_time ? `${detailInfo.assign_date} ${detailInfo.assign_time}` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="工单状态">
          <el-tag :type="getStateTagType(detailInfo.state)">
            {{ getStateText(detailInfo.state) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailInfo.remarks || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>



<style lang="scss" scoped>
.page-container {
  padding: 0;
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

.assigned-name {
  color: #2e7d32;
  font-weight: 500;
}

.not-assigned {
  color: #909399;
  font-size: 12px;
}
</style>
