<template>
  <div class="page-container page-fade-in">
    <el-card shadow="never" class="eco-card card-animate">
      <template #header>
        <div class="card-header page-card-header">
          <span class="title">👥 网格员管理</span>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="eco-filter-area">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="状态">
            <el-select v-model="filterForm.state" placeholder="全部状态" clearable style="width: 140px">
              <el-option label="全部" value="" />
              <el-option label="工作中" :value="0" />
              <el-option label="非工作状态" :value="1" />
              <el-option label="其它" :value="2" />
            </el-select>
          </el-form-item>

          <el-form-item label="关键词">
            <el-input
              v-model="filterForm.keyword"
              placeholder="搜索姓名或账号"
              clearable
              style="width: 180px"
              @keyup.enter="handleSearch"
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

        <div class="action-bar">
          <el-button type="primary" @click="handleAdd" class="eco-btn">
            <el-icon><Plus /></el-icon>
            新增网格员
          </el-button>
          <span class="total-info">
            共 {{ filteredTableData.length }} 条记录
          </span>
        </div>
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
        <el-table-column prop="gmId" label="编号" width="70" align="center" />
        <el-table-column prop="gmName" label="姓名" min-width="90" />
        <el-table-column prop="gmCode" label="登录账号" min-width="110" />
        <el-table-column prop="tel" label="联系电话" min-width="120" />
        
        <el-table-column label="所属省市" min-width="140">
          <template #default="{ row }">
            <span class="region-text">{{ row.provinceName || '未知省份' }} / {{ row.cityName || '未知城市' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStateTagType(row.state)" size="small" effect="light" round>
              {{ getStateText(row.state) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="remarks" label="备注" min-width="130" show-overflow-tooltip />
        <el-table-column prop="created_at" label="创建时间" width="160" align="center" />

        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <div class="eco-icon-actions compact nowrap">
              <el-tooltip content="编辑网格员" placement="top" :show-after="300">
                <el-button
                  type="primary"
                  size="small"
                  class="eco-icon-btn edit"
                  @click="handleEdit(row)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              
              <el-tooltip :content="row.state === 0 ? '停用账号' : '启用账号'" placement="top" :show-after="300">
                <el-button
                  :type="row.state === 0 ? 'warning' : 'success'"
                  size="small"
                  :class="['eco-icon-btn', row.state === 0 ? 'toggle-off' : 'toggle-on']"
                  @click="handleToggleState(row)"
                >
                  <el-icon><Switch /></el-icon>
                </el-button>
              </el-tooltip>
              
              <el-tooltip content="查看详情" placement="top" :show-after="300">
                <el-button
                  type="info"
                  size="small"
                  class="eco-icon-btn view"
                  @click="handleViewDetail(row)"
                >
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              
              <el-tooltip content="删除网格员" placement="top" :show-after="300">
                <el-button
                  type="danger"
                  size="small"
                  class="eco-icon-btn delete"
                  @click="handleDelete(row)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>


    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        inline-message
      >
        <el-form-item label="姓名" prop="gmName">
          <el-input
            v-model="form.gmName"
            placeholder="请输入姓名"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
      
        <el-form-item label="登录账号" prop="gmCode">
          <el-input
            v-model="form.gmCode"
            placeholder="请输入登录账号"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="form.gmId ? '不修改请留空' : '请输入密码'"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="联系电话" prop="tel">
          <el-input
            v-model="form.tel"
            placeholder="请输入联系电话"
            maxlength="20"
          />
        </el-form-item>
      
        <el-form-item label="所属省市" prop="provinceCity">
          <el-cascader
            v-model="form.provinceCity"
            :options="provinceOptions"
            :props="{ 
              value: 'value',
              label: 'label', 
              children: 'children',
              checkStrictly: true,
              emitPath: true
            }"
            placeholder="请选择省/市"
            clearable
            style="width: 100%"
            @change="handleFormCityChange"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="网格员详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="编号">{{ detailInfo.gm_id }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailInfo.gm_name }}</el-descriptions-item>
        <el-descriptions-item label="登录账号">{{ detailInfo.gm_code }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailInfo.tel }}</el-descriptions-item>
        <el-descriptions-item label="省份ID">{{ detailInfo.province_id }}</el-descriptions-item>
        <el-descriptions-item label="城市ID">{{ detailInfo.city_id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStateTagType(detailInfo.state)">
            {{ getStateText(detailInfo.state) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailInfo.remarks || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="js">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus, Edit, View, Delete, Switch } from '@element-plus/icons-vue'
import { 
  getGridMemberList,
  getGridMembersByName,
  addGridMember,
  deleteGridMember,
  updateGridMemberState,
  updateGridMember
} from '@/api/gridMember'
import { getProvinceList, getCityList } from '@/api/province'

// 筛选表单
const filterForm = reactive({
  state: '',
  keyword: ''
})

// 列表数据
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])

// 省份和城市列表（用于显示）
const provinceList = ref([])
const cityList = ref([])

// 弹窗控制
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

// 表单数据
const form = reactive({
  gmId: null,
  gmName: '',
  gmCode: '',
  password: '',
  tel: '',
  provinceId: '',
  cityId: '',
  provinceCity: []
})

// 验证规则(动态)
const rules = computed(() => {
  return {
    gmName: [
      { required: true, message: '请输入姓名', trigger: 'blur' },
      { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    gmCode: [
      { required: true, message: '请输入登录账号', trigger: 'blur' },
      { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
    ],
    password: [
      { 
        required: !form.gmId, // 编辑时密码非必填
        message: form.gmId ? '不修改请留空' : '请输入密码', 
        trigger: 'blur' 
      },
      { 
        min: 6, 
        max: 20, 
        message: '密码长度不能少于 6 个字符', 
        trigger: 'blur',
        validator: (rule, value, callback) => {
          // 编辑时如果密码为空则跳过长度验证
          if (form.gmId && !value) {
            callback()
          } else if (value && value.length < 6) {
            callback(new Error('密码长度不能少于 6 个字符'))
          } else {
            callback()
          }
        }
      }
    ],
    tel: [
      { required: true, message: '请输入联系电话', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
    ],
    provinceCity: [
      { 
        validator: (rule, value, callback) => {
          if (!value || value.length === 0) {
            callback(new Error('请选择所属省市'))
          } else {
            callback()
          }
        },
        trigger: 'change'
      }
    ]
  }
})

// 详情弹窗
const detailDialogVisible = ref(false)
const detailInfo = ref({})

// 筛选后的数据（前端筛选）
const filteredTableData = computed(() => {
  let data = tableData.value
  
  // 按状态筛选
  if (filterForm.state !== '' && filterForm.state !== undefined) {
    data = data.filter(item => item.state === filterForm.state)
  }
  
  return data
})

// 构建省市级联选项
const provinceOptions = computed(() => {
  const options = provinceList.value.map(province => {
    const cities = cityList.value.filter(city => {
      // 尝试多种可能的字段名
      return city.province_id === province.province_id || 
             city.provinceId === province.province_id
    })
    return {
      value: province.province_id,
      label: province.province_name,
      children: cities.map(city => ({
        value: city.city_id,
        label: city.city_name
      }))
    }
  })
  console.log('构建的级联选项:', options)
  return options
})

// 获取省份列表
const fetchProvinceList = async () => {
  try {
    const res = await getProvinceList()
    console.log('省份列表响应:', res)
    if (res.code === 200) {
      provinceList.value = res.data || []
      console.log('省份列表数据:', provinceList.value)
      // 打印第一个省份的字段结构
      if (provinceList.value.length > 0) {
        console.log('第一个省份字段:', Object.keys(provinceList.value[0]))
      }
    }
  } catch (error) {
    console.error('获取省份列表失败:', error)
  }
}

// 获取城市列表
const fetchCityList = async () => {
  try {
    const res = await getCityList()
    console.log('城市列表响应:', res)
    if (res.code === 200) {
      cityList.value = res.data || []
      console.log('城市列表数据:', cityList.value)
      // 打印第一个城市的字段结构
      if (cityList.value.length > 0) {
        console.log('第一个城市字段:', Object.keys(cityList.value[0]))
        console.log('第一个城市数据:', cityList.value[0])
      }
    }
  } catch (error) {
    console.error('获取城市列表失败:', error)
  }
}

// 城市变化时清空下级选择（已废弃，保留以防其他地方使用）
const handleCityChange = (val) => {
  // 不再需要此功能
}

// 表单中省市联动
const handleFormCityChange = (val) => {
  if (val && val.length >= 2) {
    form.provinceId = val[0]
    form.cityId = val[1]
  } else if (val && val.length === 1) {
    form.provinceId = val[0]
    form.cityId = ''
  }
}

// 获取网格员列表
const fetchGridMemberList = async () => {
  loading.value = true
  try {
    const res = await getGridMemberList()
    console.log('网格员列表响应:', res)
    if (res.code === 200) {
      tableData.value = res.data || []
      console.log('表格数据:', tableData.value)
    } else {
      ElMessage.error(res.msg || '获取网格员列表失败')
    }
  } catch (error) {
    console.error('获取网格员失败:', error)
    ElMessage.error(error.message || '获取数据失败,请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索（调用后端API）
const handleSearch = async () => {
  loading.value = true
  try {
    // 如果有姓名关键词，调用后端API搜索
    if (filterForm.keyword && filterForm.keyword.trim()) {
      const res = await getGridMembersByName(filterForm.keyword.trim())
      console.log('姓名搜索响应:', res)
      if (res.code === 200) {
        tableData.value = res.data || []
        console.log('搜索结果:', tableData.value)
      } else {
        ElMessage.error(res.msg || '搜索失败')
        tableData.value = []
      }
    } else {
      // 没有关键词，获取全部列表
      await fetchGridMemberList()
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error(error.message || '搜索失败,请稍后重试')
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 重置
const handleReset = async () => {
  Object.assign(filterForm, {
    state: '',
    keyword: ''
  })
  // 重新加载全部数据
  await fetchGridMemberList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增网格员'
  Object.assign(form, {
    gmId: null,
    gmName: '',
    gmCode: '',
    password: '',
    tel: '',
    provinceId: '',
    cityId: '',
    provinceCity: []
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑网格员'
  Object.assign(form, {
    gmId: row.gmId,
    gmName: row.gmName,
    gmCode: row.gmCode,
    password: '', // 编辑时密码留空
    tel: row.tel,
    provinceId: row.provinceId,
    cityId: row.cityId,
    provinceCity: [row.provinceId, row.cityId].filter(Boolean)
  })
  
  dialogVisible.value = true
}

// 停用/启用状态切换
const handleToggleState = async (row) => {
  const newState = row.state === 0 ? 1 : 0
  const actionText = newState === 0 ? '启用' : '停用'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}该网格员吗?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 直接传递整个row对象,后端会自动提取gmId和state
    const res = await updateGridMemberState({
      gmId: row.gmId,
      state: newState
    })
    if (res.code === 200) {
      ElMessage.success(`${actionText}成功`)
      fetchGridMemberList()
    } else {
      ElMessage.error(res.msg || `${actionText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态切换失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 查看详情
const handleViewDetail = (row) => {
  detailInfo.value = { ...row }
  detailDialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除网格员 "${row.gmName}" 吗?此操作不可撤销!`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 直接传递整个row对象,后端会自动提取gmId
    const res = await deleteGridMember(row)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchGridMemberList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  dialogVisible.value = false
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    
    // 打印完整的表单数据以便调试
    console.log('=== 表单数据 ===')
    console.log('form.gmId:', form.gmId)
    console.log('form.gmName:', form.gmName)
    console.log('form.gmCode:', form.gmCode)
    console.log('form.password:', form.password)
    console.log('form.tel:', form.tel)
    console.log('form.provinceId:', form.provinceId, typeof form.provinceId)
    console.log('form.cityId:', form.cityId, typeof form.cityId)
    console.log('form.provinceCity:', form.provinceCity)
    
    const data = {
      gmId: form.gmId,
      gmName: form.gmName,
      gmCode: form.gmCode,
      password: form.password || undefined, // 如果为空则不传递
      tel: form.tel,
      provinceId: form.provinceId,
      cityId: form.cityId
    }
    
    console.log('准备发送的数据:', data)
    
    if (form.gmId) {
      // 编辑 - 调用编辑API
      // 注意: 后端需要提供 PUT /gridMember/update 或 POST /gridMember/update 接口
      const res = await updateGridMember(data)
      if (res.code === 200) {
        ElMessage.success('编辑成功')
        handleDialogClose()
        fetchGridMemberList()
      } else {
        ElMessage.error(res.msg || '编辑失败,请稍后重试')
      }
    } else {
      // 新增
      console.log('调用addGridMember API, 数据:', data)
      const res = await addGridMember(data)
      console.log('新增响应:', res)
      if (res.code === 200) {
        ElMessage.success('新增成功')
        handleDialogClose()
        fetchGridMemberList()
      } else {
        ElMessage.error(res.msg || '新增失败')
      }
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 工具函数:获取状态文本
const getStateText = (state) => {
  const map = { 0: '工作中', 1: '非工作状态', 2: '其它' }
  return map[state] || '未知'
}

// 工具函数:获取状态标签类型
const getStateTagType = (state) => {
  const map = { 0: 'success', 1: 'warning', 2: 'info' }
  return map[state] || 'info'
}

// 工具函数:获取省份名称
const getProvinceName = (provinceId) => {
  const province = provinceList.value.find(p => p.province_id === provinceId)
  return province ? province.province_name : '未知省份'
}

// 工具函数:获取城市名称
const getCityName = (cityId) => {
  const city = cityList.value.find(c => c.city_id === cityId)
  return city ? city.city_name : '未知城市'
}

// 初始化
onMounted(async () => {
  await fetchProvinceList()
  await fetchCityList()
  await fetchGridMemberList()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 0;
}

.filter-form {
  margin-bottom: 0;
}

.action-bar {
  margin-left: auto;
}

.total-info {
  margin-left: 12px;
  font-size: 14px;
  color: #4caf50;
  font-weight: 500;
}

.region-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
