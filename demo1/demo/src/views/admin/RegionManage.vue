<template>
  <div class="page-container">
    <el-row :gutter="20" style="height: 100%">
      <!-- 左侧:省份列表 -->
      <el-col :span="10">
        <el-card shadow="never" class="left-card eco-card card-animate">
          <template #header>
            <div class="card-header page-card-header">
              <span class="title">🌿 省份管理</span>
              <el-button type="primary" size="small" @click="handleAddProvince" class="eco-btn">
                <el-icon><Plus /></el-icon>
                新增省份
              </el-button>
            </div>
          </template>

          <el-table
            :data="provinceList"
            v-loading="provinceLoading"
            stripe
            border
            highlight-current-row
            @current-change="handleProvinceChange"
            style="width: 100%"
            ref="provinceTableRef"
            class="eco-table"
          >
            <el-table-column prop="province_id" label="编号" width="70" align="center" />
            <el-table-column prop="province_name" label="省份名称" min-width="120" />
            <el-table-column prop="province_abbr" label="简称" width="80" align="center" />
            <el-table-column prop="remarks" label="备注" min-width="120" show-overflow-tooltip />
            
            <el-table-column label="操作" width="90" fixed="right" align="center">
              <template #default="{ row }">
                <div class="eco-icon-actions compact nowrap">
                  <el-tooltip content="编辑省份" placement="top" :show-after="300">
                    <el-button
                      type="primary"
                      size="small"
                      class="eco-icon-btn edit"
                      @click="handleEditProvince(row)"
                    >
                      <el-icon><Edit /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="删除省份" placement="top" :show-after="300">
                    <el-button
                      type="danger"
                      size="small"
                      class="eco-icon-btn delete"
                      @click="handleDeleteProvince(row)"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧:城市列表 -->
      <el-col :span="14">
        <el-card shadow="never" class="right-card eco-card card-animate">
          <template #header>
            <div class="card-header page-card-header">
              <span class="title">
                🏙️ 城市管理
                <el-tag v-if="selectedProvince" type="success" effect="light" round style="margin-left: 8px">
                  {{ selectedProvince.province_name }}
                </el-tag>
              </span>
              <el-button 
                type="primary" 
                size="small" 
                @click="handleAddCity"
                :disabled="!selectedProvince"
                class="eco-btn"
              >
                <el-icon><Plus /></el-icon>
                新增城市
              </el-button>
            </div>
          </template>

          <el-alert
            v-if="!selectedProvince"
            title="🌿 请先在左侧选择一个省份"
            description="选择省份后，将显示该省份下的城市列表"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
          />

          <el-table
            :data="cityList"
            v-loading="cityLoading"
            stripe
            border
            :data-empty-text="selectedProvince ? '暂无城市数据' : '请先选择省份'"
            style="width: 100%"
            class="eco-table"
          >
            <el-table-column prop="city_id" label="编号" width="70" align="center" />
            <el-table-column prop="city_name" label="城市名称" min-width="120" />
            <el-table-column prop="province_name" label="所属省份" width="110" />
            <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
            
            <el-table-column label="操作" width="90" fixed="right" align="center">
              <template #default="{ row }">
                <div class="eco-icon-actions compact nowrap">
                  <el-tooltip content="编辑城市" placement="top" :show-after="300">
                    <el-button
                      type="primary"
                      size="small"
                      class="eco-icon-btn edit"
                      @click="handleEditCity(row)"
                    >
                      <el-icon><Edit /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="删除城市" placement="top" :show-after="300">
                    <el-button
                      type="danger"
                      size="small"
                      class="eco-icon-btn delete"
                      @click="handleDeleteCity(row)"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 省份编辑弹窗 -->
    <el-dialog
      v-model="provinceDialogVisible"
      :title="provinceDialogTitle"
      width="500px"
      :before-close="handleProvinceDialogClose"
    >
      <el-form
        ref="provinceFormRef"
        :model="provinceForm"
        :rules="provinceRules"
        label-width="100px"
      >
        <el-form-item label="省份名称" prop="province_name">
          <el-input
            v-model="provinceForm.province_name"
            placeholder="请输入省份名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="省份简称" prop="province_abbr">
          <el-input
            v-model="provinceForm.province_abbr"
            placeholder="请输入省份简称"
            maxlength="5"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="provinceForm.remarks"
            type="textarea"
            placeholder="请输入备注信息"
            maxlength="200"
            show-word-limit
            :rows="3"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleProvinceDialogClose">取消</el-button>
        <el-button type="primary" @click="handleProvinceSubmit" :loading="provinceSubmitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 城市编辑弹窗 -->
    <el-dialog
      v-model="cityDialogVisible"
      :title="cityDialogTitle"
      width="500px"
      :before-close="handleCityDialogClose"
    >
      <el-form
        ref="cityFormRef"
        :model="cityForm"
        :rules="cityRules"
        label-width="100px"
      >
        <el-form-item label="城市名称" prop="city_name">
          <el-input
            v-model="cityForm.city_name"
            placeholder="请输入城市名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="所属省份" prop="province_id" v-if="!cityForm.province_id && !selectedProvince">
          <el-select
            v-model="cityForm.province_id"
            placeholder="请选择省份"
            style="width: 100%"
          >
            <el-option
              v-for="item in provinceList"
              :key="item.province_id"
              :label="item.province_name"
              :value="item.province_id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="cityForm.remarks"
            type="textarea"
            placeholder="请输入备注信息"
            maxlength="200"
            show-word-limit
            :rows="3"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleCityDialogClose">取消</el-button>
        <el-button type="primary" @click="handleCitySubmit" :loading="citySubmitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="js">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { 
  getProvinceList,
  getCityList,
  addProvince,
  addCity,
  updateProvince,
  updateCity,
  deleteProvince,
  deleteCity
} from '@/api/province'

// ==================== 省份管理 ====================
const provinceLoading = ref(false)
const provinceList = ref([])
const provinceTableRef = ref(null)
const selectedProvince = ref(null)

const provinceDialogVisible = ref(false)
const provinceDialogTitle = ref('')
const provinceFormRef = ref(null)
const provinceSubmitLoading = ref(false)

const provinceForm = reactive({
  province_id: null,
  province_name: '',
  province_abbr: '',
  remarks: ''
})

const provinceRules = {
  province_name: [
    { required: true, message: '请输入省份名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  province_abbr: [
    { required: true, message: '请输入省份简称', trigger: 'blur' },
    { min: 1, max: 5, message: '长度在 1 到 5 个字符', trigger: 'blur' }
  ]
}

// 获取省份列表
const fetchProvinceList = async () => {
  provinceLoading.value = true
  try {
    const res = await getProvinceList()
    console.log('[获取省份列表] API原始响应:', res)
    
    // request.js拦截器已解包result.data
    // 如果res有code字段,说明是后端标准响应格式{code, data, msg}
    let provinces = []
    if (res && typeof res === 'object' && res.code !== undefined) {
      // 标准响应格式 { code: 200, data: [...] }
      provinces = res.data || []
    } else if (Array.isArray(res)) {
      // 直接返回数组
      provinces = res
    } else {
      ElMessage.error('获取省份列表失败')
      provinceList.value = []
      return
    }
    
    // 确保每个省份都有正确的字段名(处理可能的驼峰/下划线转换)
    provinceList.value = provinces.map(p => ({
      province_id: p.province_id,
      province_name: p.province_name,
      province_abbr: p.province_abbr,
      remarks: p.remarks || ''
    }))
    
    console.log('[获取省份列表] 成功,共', provinceList.value.length, '个省份')
    console.log('[获取省份列表] 第一条数据示例:', provinceList.value[0])
  } catch (error) {
    console.error('获取省份列表失败:', error)
    ElMessage.error(error.message || '获取数据失败,请稍后重试')
    provinceList.value = []
  } finally {
    provinceLoading.value = false
  }
}

// 选择省份
const handleProvinceChange = (val) => {
  selectedProvince.value = val
  if (val) {
    fetchCityList(val.province_id)
  } else {
    cityList.value = []
  }
}

// 新增省份
const handleAddProvince = () => {
  provinceDialogTitle.value = '新增省份'
  Object.assign(provinceForm, {
    province_id: null,
    province_name: '',
    province_abbr: '',
    remarks: ''
  })
  provinceDialogVisible.value = true
}

// 编辑省份
const handleEditProvince = (row) => {
  provinceDialogTitle.value = '编辑省份'
  Object.assign(provinceForm, {
    province_id: row.province_id,
    province_name: row.province_name,
    province_abbr: row.province_abbr,
    remarks: row.remarks || ''
  })
  provinceDialogVisible.value = true
}

// 删除省份
const handleDeleteProvince = async (row) => {
  console.log('[删除省份] 要删除的省份数据:', row)
  console.log('[删除省份] province_id:', row.province_id)
  
  if (!row.province_id) {
    ElMessage.error('省份ID不存在,无法删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除省份 "${row.province_name}" 吗?该省下的所有城市也将被删除!`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteProvince(row.province_id)
    console.log('[删除省份] API响应:', res)
    
    // request.js已解包,直接判断code
    if (res && res.code === 200) {
      ElMessage.success('删除成功')
      if (selectedProvince.value?.province_id === row.province_id) {
        selectedProvince.value = null
        cityList.value = []
      }
      fetchProvinceList()
    } else {
      ElMessage.error(res?.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[删除省份] 错误:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 关闭省份弹窗
const handleProvinceDialogClose = () => {
  provinceDialogVisible.value = false
  provinceFormRef.value?.resetFields()
}

// 提交省份表单
const handleProvinceSubmit = async () => {
  try {
    await provinceFormRef.value.validate()
    
    provinceSubmitLoading.value = true
    
    let res
    if (provinceForm.province_id) {
      // 编辑 - 传入整个对象,包含province_id
      const updateData = {
        province_id: provinceForm.province_id,
        province_name: provinceForm.province_name,
        province_abbr: provinceForm.province_abbr,
        remarks: provinceForm.remarks
      }
      console.log('[更新省份] 提交数据:', updateData)
      res = await updateProvince(updateData)
    } else {
      // 新增
      const newData = {
        province_name: provinceForm.province_name,
        province_abbr: provinceForm.province_abbr,
        remarks: provinceForm.remarks
      }
      console.log('[新增省份] 提交数据:', newData)
      res = await addProvince(newData)
    }
    
    console.log('[省份操作] API响应:', res)
    
    // request.js已解包,直接判断code
    if (res && res.code === 200) {
      ElMessage.success(provinceForm.province_id ? '更新成功' : '新增成功')
      handleProvinceDialogClose()
      fetchProvinceList()
    } else {
      ElMessage.error(res?.msg || '操作失败')
    }
  } catch (error) {
    console.error('[省份操作] 表单验证或API调用失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    provinceSubmitLoading.value = false
  }
}

// ==================== 城市管理 ====================
const cityLoading = ref(false)
const cityList = ref([])
const selectedCity = ref(null)

// 所有城市列表(用于编辑时显示所属省份选择)
const allCities = ref([])

const cityDialogVisible = ref(false)
const cityDialogTitle = ref('')
const cityFormRef = ref(null)
const citySubmitLoading = ref(false)

const cityForm = reactive({
  city_id: null,
  city_name: '',
  province_id: '',
  remarks: ''
})

const cityRules = {
  city_name: [
    { required: true, message: '请输入城市名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  province_id: [
    { required: true, message: '请选择所属省份', trigger: 'change' }
  ]
}

// 获取城市列表
const fetchCityList = async (provinceId) => {
  if (!provinceId) {
    cityList.value = []
    return
  }
  
  cityLoading.value = true
  try {
    // 获取所有城市
    const res = await getCityList()
    console.log('[获取城市列表] API原始响应:', res)
    
    // request.js拦截器已解包result.data
    let allCitiesData = []
    if (res && typeof res === 'object' && res.code !== undefined) {
      // 标准响应格式 { code: 200, data: [...] }
      allCitiesData = res.data || []
    } else if (Array.isArray(res)) {
      // 直接返回数组
      allCitiesData = res
    } else {
      ElMessage.error('获取城市列表失败')
      cityList.value = []
      return
    }
    
    // 过滤出指定省份的城市
    const filteredCities = allCitiesData.filter(city => city.province_id === provinceId)
    
    // 为每个城市添加所属省份名称
    cityList.value = filteredCities.map(city => {
      const province = provinceList.value.find(p => p.province_id === city.province_id)
      return {
        city_id: city.city_id,
        city_name: city.city_name,
        province_id: city.province_id,
        province_name: province ? province.province_name : '未知',
        remarks: city.remarks || ''
      }
    })
    
    console.log('[获取城市列表] 成功,共', cityList.value.length, '个城市')
    console.log('[获取城市列表] 第一条数据示例:', cityList.value[0])
  } catch (error) {
    console.error('获取城市列表失败:', error)
    ElMessage.error(error.message || '获取数据失败,请稍后重试')
    cityList.value = []
  } finally {
    cityLoading.value = false
  }
}

// 新增城市
const handleAddCity = () => {
  if (!selectedProvince.value) {
    ElMessage.warning('请先选择一个省份')
    return
  }
  
  cityDialogTitle.value = '新增城市'
  Object.assign(cityForm, {
    city_id: null,
    city_name: '',
    province_id: selectedProvince.value.province_id,
    remarks: ''
  })
  cityDialogVisible.value = true
}

// 编辑城市
const handleEditCity = (row) => {
  cityDialogTitle.value = '编辑城市'
  Object.assign(cityForm, {
    city_id: row.city_id,
    city_name: row.city_name,
    province_id: row.province_id,
    remarks: row.remarks || ''
  })
  cityDialogVisible.value = true
}

// 删除城市
const handleDeleteCity = async (row) => {
  console.log('[删除城市] 要删除的城市数据:', row)
  console.log('[删除城市] city_id:', row.city_id)
  
  if (!row.city_id) {
    ElMessage.error('城市ID不存在,无法删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除城市 "${row.city_name}" 吗?此操作不可撤销!`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteCity(row.city_id)
    console.log('[删除城市] API响应:', res)
    
    // request.js已解包,直接判断code
    if (res && res.code === 200) {
      ElMessage.success('删除成功')
      fetchCityList(selectedProvince.value.province_id)
    } else {
      ElMessage.error(res?.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[删除城市] 错误:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 关闭城市弹窗
const handleCityDialogClose = () => {
  cityDialogVisible.value = false
  cityFormRef.value?.resetFields()
}

// 提交城市表单
const handleCitySubmit = async () => {
  try {
    await cityFormRef.value.validate()
    
    citySubmitLoading.value = true
    
    let res
    if (cityForm.city_id) {
      // 编辑 - 传入整个对象,包含city_id
      const updateData = {
        city_id: cityForm.city_id,
        city_name: cityForm.city_name,
        province_id: cityForm.province_id,
        remarks: cityForm.remarks
      }
      console.log('[更新城市] 提交数据:', updateData)
      res = await updateCity(updateData)
    } else {
      // 新增 - 不包含city_id
      const newData = {
        city_name: cityForm.city_name,
        province_id: cityForm.province_id,
        remarks: cityForm.remarks
      }
      console.log('[新增城市] 提交数据:', newData)
      res = await addCity(newData)
    }
    
    console.log('[城市操作] API响应:', res)
    
    // request.js已解包,直接判断code
    if (res && res.code === 200) {
      ElMessage.success(cityForm.city_id ? '更新成功' : '新增成功')
      handleCityDialogClose()
      fetchCityList(selectedProvince.value.province_id)
    } else {
      ElMessage.error(res?.msg || '操作失败')
    }
  } catch (error) {
    console.error('[城市操作] 表单验证或API调用失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    citySubmitLoading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchProvinceList()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 0;
  height: 100%;
  animation: page-fade-in 0.4s ease-out;
}

.left-card,
.right-card {
  height: calc(100vh - 160px);
  border: 1px solid rgba(129, 199, 132, 0.4) !important;
  border-radius: 12px !important;
  box-shadow: 0 2px 12px rgba(46, 125, 50, 0.08) !important;
  transition: all 0.3s ease !important;
  animation: card-slide-up 0.5s ease-out both;

  &:hover {
    box-shadow: 0 6px 24px rgba(46, 125, 50, 0.15) !important;
  }

  :deep(.el-card__header) {
    background: linear-gradient(135deg, rgba(232, 245, 233, 0.6), rgba(241, 248, 233, 0.4));
    border-bottom: 1px solid rgba(129, 199, 132, 0.3);
    padding: 14px 20px;
  }

  :deep(.el-card__body) {
    padding: 0;
    height: 100%;

    .el-table {
      padding: 0 12px;
    }
  }

  // 左侧卡片延迟动画
  &.left-card {
    animation-delay: 0.1s;
  }

  // 右侧卡片延迟动画
  &.right-card {
    animation-delay: 0.2s;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .title {
    font-size: 16px;
    font-weight: 600;
    color: #1b5e20;
    display: flex;
    align-items: center;
    gap: 8px;

    &::before {
      content: '';
      display: inline-block;
      width: 4px;
      height: 18px;
      background: linear-gradient(180deg, #2e7d32, #4caf50);
      border-radius: 2px;
    }
  }
}

// 表格绿色主题
:deep(.el-table) {
  .el-table__header {
    th.el-table__cell {
      background: linear-gradient(180deg, #e8f5e9, #f1f8e9) !important;
      color: #1b5e20;
      font-weight: 600;
      font-size: 13px;
      border-bottom: 2px solid rgba(129, 199, 132, 0.4) !important;
    }
  }

  .el-table__row {
    transition: all 0.2s ease;

    &:hover {
      td.el-table__cell {
        background: rgba(200, 230, 201, 0.2) !important;
      }
    }

    &.el-table__row--striped {
      td.el-table__cell {
        background: rgba(232, 245, 233, 0.3) !important;
      }
    }
  }

  .el-table__cell {
    border-bottom: 1px solid rgba(129, 199, 132, 0.15) !important;
  }
}

:deep(.el-alert) {
  margin-bottom: 16px;
  background: rgba(232, 245, 233, 0.5) !important;
  border: 1px solid rgba(129, 199, 132, 0.3) !important;

  .el-alert__title {
    color: #2e7d32;
  }
}
</style>
