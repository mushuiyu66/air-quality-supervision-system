<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  Document,
  Edit, 
  Location,
  MapLocation,
  Plus,
  SwitchButton
} from '@element-plus/icons-vue'
import { 
  submitFeedback, 
  getMyFeedbacks,
  getStatisticsByAfId
} from '@/api/supervisor'
import { getProvinceList, getCityList } from '@/api/province'
import EcoSloganBanner from '@/components/EcoSloganBanner.vue'
import EcoParticleBackground from '@/components/EcoParticleBackground.vue'
import MapLocationPicker from '@/components/MapLocationPicker.vue'


const userStore = useUserStore()
const router = useRouter()

// 市民监督员专属标语
const supervisorSlogans = [
  {
    icon: '🌍',
    text: '低碳生活，绿色未来',
    subtitle: '您的每一次反馈，都是对环境的守护'
  },
  {
    icon: '📢',
    text: '环境监督，人人参与',
    subtitle: '发现问题随手拍，共建清新好家园'
  },
  {
    icon: '🌿',
    text: '绿水青山就是金山银山',
    subtitle: '市民监督，让环保监管无死角'
  },
  {
    icon: '💚',
    text: '守护蓝天，从我做起',
    subtitle: '每一位市民都是空气质量的守护者'
  },
  {
    icon: '🌱',
    text: '清新空气，健康生活',
    subtitle: '你反馈，我处理，共创绿色城市'
  }
]

// 表单引用
const feedbackFormRef = ref()

// 反馈表单数据（与后端AqiFeedBack实体对应）
const feedbackForm = reactive({
  telId: '', // 市民ID（电话号码）
  provinceId: 0, // 省份ID
  cityId: 0, // 城市ID
  address: '', // 详细地址
  information: '', // 反馈内容
  estimatedGrade: 0, // 预估等级：0-优，1-良，2-轻度污染，3-中度污染，4-重度污染，5-严重污染
  afDate: '', // 反馈日期
  afTime: '', // 反馈时间
  gmId: 0, // 网格员ID
  state: 0 // 状态：0-待处理，1-处理中，2-已完成
})

// 预估等级选项
const gradeOptions = [
  { label: '优（AQI 0-50）', value: 0 },
  { label: '良（AQI 51-100）', value: 1 },
  { label: '轻度污染（AQI 101-150）', value: 2 },
  { label: '中度污染（AQI 151-200）', value: 3 },
  { label: '重度污染（AQI 201-300）', value: 4 },
  { label: '严重污染（AQI >300）', value: 5 }
]

// 省份和城市列表
const provinceList = ref([])
const cityList = ref([])
const provinceLoading = ref(false)
const cityLoading = ref(false)

// 表单验证规则
const formRules = {
  provinceId: [
    { required: true, message: '请选择省份', trigger: 'change' }
  ],
  cityId: [
    { required: true, message: '请选择城市', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入问题发生地点', trigger: 'blur' }
  ],
  information: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' },
    { min: 10, max: 500, message: '内容长度应在10-500个字符之间', trigger: 'blur' }
  ]
}

// 反馈类型选项（仅用于展示）
const categories = [
  { label: '环境污染', value: 'pollution' },
  { label: '基础设施损坏', value: 'infrastructure' },
  { label: '公共设施问题', value: 'public_facility' },
  { label: '社区管理', value: 'community_management' },
  { label: '其他问题', value: 'other' }
]

// 已提交的反馈列表
const feedbackList = ref([])

// 加载状态
const loading = ref(false)

// 对话框可见性
const dialogVisible = ref(false)

// 详情弹窗
const detailDialogVisible = ref(false)
const detailInfo = ref({})
const detailStatistics = ref(null)
const detailLoading = ref(false)

// 地图定位选择器
const mapPickerVisible = ref(false)

// 当前已选中的省份/城市名称，传给地图定位器作为默认区域
const selectedRegionForMap = computed(() => {
  const province = provinceList.value.find(
    (p) => p.province_id === feedbackForm.provinceId
  )
  const city = cityList.value.find(
    (c) => c.city_id === feedbackForm.cityId
  )
  return {
    province: province ? province.province_name : '',
    city: city ? city.city_name : ''
  }
})

const openMapPicker = () => {
  mapPickerVisible.value = true
}
const handleLocationSelect = async (data) => {
  let address = data.address || ''
  const street = data.street || ''
  // 如果街道存在但详细地址中未包含，追加到末尾
  if (street && address && !address.includes(street)) {
    address = `${address} ${street}`
  }
  if (address) {
    feedbackForm.address = address
  }

  // 尝试根据解析结果自动匹配省份/城市
  const provinceName = normalizeRegionName(data.province)
  const cityName = normalizeRegionName(data.city)

  if (provinceName && provinceList.value.length) {
    const province = provinceList.value.find((p) =>
      normalizeRegionName(p.province_name).includes(provinceName) ||
      provinceName.includes(normalizeRegionName(p.province_name))
    )
    if (province) {
      feedbackForm.provinceId = province.province_id
      // 等待城市列表异步加载完成后再匹配城市
      await handleProvinceChange(province.province_id)

      if (cityName && cityList.value.length) {
        const city = cityList.value.find((c) =>
          normalizeRegionName(c.city_name).includes(cityName) ||
          cityName.includes(normalizeRegionName(c.city_name))
        )
        if (city) {
          feedbackForm.cityId = city.city_id
        }
      }
    }
  }
}

// 地区名称标准化：去除常见后缀便于模糊匹配
const normalizeRegionName = (name) => {
  if (!name) return ''
  return String(name)
    .replace(/(省|市|自治区|自治州|地区|特别行政区|回族自治区|维吾尔自治区|壮族自治区)$/g, '')
    .trim()
}

// 编辑模式(市民端禁用)
const isEdit = ref(false)
const editId = ref(null)

// 提交反馈（市民只能添加自己的反馈）
const handleSubmit = async () => {
  if (!feedbackFormRef.value) return
  
  await feedbackFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      // 获取当前登录用户的电话号码
      const phone = userStore.info.tel_id || localStorage.getItem('phone')
      if (!phone) {
        ElMessage.warning('请先登录')
        return
      }
      
      // 构建与后端对应的数据结构
      // 必须传递的字段:telId, provinceId, cityId, address, information, estimatedGrade
      // 自动生成的字段:afDate, afTime, state(默认为0)
      const now = new Date()
      const afDate = now.toISOString().split('T')[0] // yyyy-MM-dd
      const afTime = now.toTimeString().split(' ')[0].substring(0, 8) // HH:mm:ss
            
      const formData = {
        telId: phone, // 当前市民ID(电话号码)
        provinceId: feedbackForm.provinceId || null, // 省份ID
        cityId: feedbackForm.cityId || null, // 城市ID
        address: feedbackForm.address || '', // 详细地址
        information: feedbackForm.information || '', // 反馈内容
        estimatedGrade: feedbackForm.estimatedGrade !== undefined ? feedbackForm.estimatedGrade : 0, // 预估等级
        afDate: afDate, // 反馈日期(自动生成)
        afTime: afTime, // 反馈时间(自动生成)
        state: 0 // 状态:待处理
      }
      
      console.log('[提交反馈] 请求数据:', formData)
      console.log('[提交反馈] 表单原始数据:', {
        provinceId: feedbackForm.provinceId,
        cityId: feedbackForm.cityId,
        address: feedbackForm.address,
        information: feedbackForm.information,
        estimatedGrade: feedbackForm.estimatedGrade,
        afDate: afDate,
        afTime: afTime
      })
      const result = await submitFeedback(formData)
      console.log('[提交反馈] 响应数据:', result)
      
      // request.js已解包一层,result就是后端的ResponseData对象
      if (result && result.code === 200) {
        ElMessage.success('反馈提交成功')
        resetForm()
        dialogVisible.value = false
        loadMyFeedbacks()
      } else {
        ElMessage.error(result?.msg || '提交失败')
      }
    } catch (error) {
      console.error('[提交反馈] 错误:', error)
      console.error('[提交反馈] 错误详情:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      
      let errorMsg = '提交失败，请稍后重试'
      if (error.response?.status === 500) {
        // 服务器内部错误，显示后端返回的具体错误信息
        const backendMsg = error.response?.data?.msg || error.response?.data?.message || '服务器内部错误'
        errorMsg = `服务器错误: ${backendMsg}`
        console.error('[提交反馈] 后端错误信息:', backendMsg)
      } else if (error.response?.status === 404) {
        errorMsg = '接口不存在，请联系管理员确认后端服务配置'
      } else if (error.code === 'ERR_NETWORK' || !error.response) {
        errorMsg = '网络连接失败，请检查后端服务是否启动及CORS配置'
      } else if (error.message) {
        errorMsg = error.message
      }
      ElMessage.error(errorMsg)
    }
  })
}

// 重置表单
const resetForm = () => {
  feedbackForm.telId = ''
  feedbackForm.provinceId = 0
  feedbackForm.cityId = 0
  feedbackForm.address = ''
  feedbackForm.information = ''
  feedbackForm.estimatedGrade = 0
  feedbackForm.afDate = ''
  feedbackForm.afTime = ''
  feedbackForm.gmId = 0
  feedbackForm.state = 0
  cityList.value = [] // 清空城市列表
}



// 获取省份列表
const fetchProvinceList = async () => {
  provinceLoading.value = true
  try {
    const res = await getProvinceList()
    console.log('[获取省份列表] API原始响应:', res)
    
    // 根据request.js拦截器,res已经是result.data
    // 如果res有code字段,说明是后端标准响应格式
    if (res && res.code === 200) {
      provinceList.value = res.data || []
      console.log('[获取省份列表] 成功,共', provinceList.value.length, '个省份')
    } else if (Array.isArray(res)) {
      // 如果直接返回数组(某些情况)
      provinceList.value = res
      console.log('[获取省份列表] 直接返回数组,共', provinceList.value.length, '个省份')
    } else {
      ElMessage.error(res?.msg || '获取省份列表失败')
      provinceList.value = []
    }
  } catch (error) {
    console.error('获取省份列表失败:', error)
    ElMessage.error(error.message || '获取数据失败，请稍后重试')
    provinceList.value = []
  } finally {
    provinceLoading.value = false
  }
}

// 省份改变时获取城市列表
const handleProvinceChange = async (provinceId) => {
  feedbackForm.cityId = 0 // 重置城市选择
  cityList.value = [] // 清空城市列表

  if (!provinceId) {
    return
  }

  cityLoading.value = true
  try {
    const data = await getCityList()
    console.log('[获取城市列表] API原始响应:', data)

    // 根据request.js拦截器,data已经是result.data
    if (data && data.code === 200) {
      const allCitiesData = data.data || []
      // 过滤出指定省份的城市
      cityList.value = allCitiesData.filter(city => city.province_id === provinceId)
      console.log('[获取城市列表] 成功,共', cityList.value.length, '个城市')
    } else if (Array.isArray(data)) {
      // 如果直接返回数组(某些情况)
      cityList.value = data.filter(city => city.province_id === provinceId)
      console.log('[获取城市列表] 直接返回数组,共', cityList.value.length, '个城市')
    } else {
      ElMessage.error(data?.msg || '获取城市列表失败')
      cityList.value = []
    }
  } catch (error) {
    console.error('获取城市列表失败:', error)
    ElMessage.error(error.message || '获取数据失败，请稍后重试')
    cityList.value = []
  } finally {
    cityLoading.value = false
  }
}
// 统一反馈字段：后端可能返回 snake_case 或 camelCase
const normalizeFeedback = (item) => {
  const estimated = item.estimatedGrade ?? item.estimated_grade ?? null
  return {
    ...item,
    // 主键/电话
    afId: item.afId ?? item.af_id ?? null,
    telId: item.telId ?? item.tel_id ?? '',
    // 地址
    provinceId: item.provinceId ?? item.province_id ?? null,
    cityId: item.cityId ?? item.city_id ?? null,
    provinceName: item.provinceName ?? item.province_name ?? '',
    cityName: item.cityName ?? item.city_name ?? '',
    address: item.address ?? '',
    information: item.information ?? '',
    // 等级（兼容 0-5 与 1-6 两种约定）
    estimatedGrade: estimated,
    estimated_grade: estimated,
    // 状态
    state: item.state ?? 0,
    // 时间
    afDate: item.afDate ?? item.af_date ?? '',
    afTime: item.afTime ?? item.af_time ?? '',
    // 网格员
    gmId: item.gmId ?? item.gm_id ?? null
  }
}

// 获取预估等级文本与颜色（同时兼容前端 0-5 与后端 1-6 两种约定）
const levelTextMap = {
  0: '优（AQI 0-50）',
  1: '良（AQI 51-100）',
  2: '轻度污染（AQI 101-150）',
  3: '中度污染（AQI 151-200）',
  4: '重度污染（AQI 201-300）',
  5: '严重污染（AQI >300）'
}

const backendLevelTextMap = {
  1: '优（AQI 0-50）',
  2: '良（AQI 51-100）',
  3: '轻度污染（AQI 101-150）',
  4: '中度污染（AQI 151-200）',
  5: '重度污染（AQI 201-300）',
  6: '严重污染（AQI >300）'
}

const getGradeLabel = (grade) => {
  if (grade === null || grade === undefined || grade === '') return '未选择'
  const val = Number(grade)
  if (val >= 0 && val <= 5) return levelTextMap[val]
  if (val >= 1 && val <= 6) return backendLevelTextMap[val]
  return '未知'
}

const getGradeType = (grade) => {
  const val = Number(grade)
  // 0-5 范围
  if (val >= 0 && val <= 1) return 'success'
  if (val >= 2 && val <= 3) return 'warning'
  if (val >= 4 && val <= 5) return 'danger'
  // 1-6 范围
  if (val === 6) return 'danger'
  return 'info'
}

// 加载我的反馈列表（市民只能查看自己的反馈）
const loadMyFeedbacks = async () => {
  loading.value = true
  try {
    const tel = userStore.info.tel_id || localStorage.getItem('phone')
    if (!tel) {
      ElMessage.warning('请先登录')
      return
    }
    
    console.log('[获取反馈列表] 当前用户手机号:', tel)
    const result = await getMyFeedbacks() // 后端findAll()不需要参数
    console.log('[获取反馈列表] 响应数据:', result)
    
    // request.js已解包一层,result就是后端的ResponseData对象 {code: 200, data: [...]}
    if (result && result.code === 200) {
      const dataList = Array.isArray(result.data) ? result.data : []
      console.log('[获取反馈列表] 第一条数据结构示例:', dataList.length > 0 ? dataList[0] : '无数据')
      
      // 注意:后端返回的字段可能是 tel_id 或 telId,需要兼容处理
      feedbackList.value = dataList
        .map(normalizeFeedback)
        .filter(item => String(item.telId) === String(tel))
      console.log('[获取反馈列表] 总数:', dataList.length, '条, 过滤后:', feedbackList.value.length, '条')
    } else if (Array.isArray(result)) {
      // 某些情况可能直接返回数组(无包装)
      feedbackList.value = result
        .map(normalizeFeedback)
        .filter(item => String(item.telId) === String(tel))
      console.log('[获取反馈列表] 直接数组,过滤后:', feedbackList.value.length, '条')
    } else {
      console.warn('[获取反馈列表] 响应格式异常:', result)
      feedbackList.value = []
    }
  } catch (error) {
    console.error('[获取反馈列表] 错误:', error)
    let errorMsg = '加载失败，请稍后重试'
    if (error.response?.status === 404) {
      errorMsg = '接口不存在，请联系管理员确认后端服务配置'
    } else if (error.code === 'ERR_NETWORK' || !error.response) {
      errorMsg = '网络连接失败，请检查后端服务是否启动及CORS配置'
    } else if (error.message) {
      errorMsg = error.message
    }
    ElMessage.error(errorMsg)
    feedbackList.value = []
  } finally {
    loading.value = false
  }
}

// 市民只能查看自己的反馈，不允许编辑和删除
const handleView = async (row) => {
  detailInfo.value = normalizeFeedback(row)
  detailStatistics.value = null
  detailDialogVisible.value = true
  detailLoading.value = true
  
  const afId = detailInfo.value.afId
  if (afId) {
    try {
      const res = await getStatisticsByAfId(afId)
      if (res.code === 200) {
        detailStatistics.value = res.data || null
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

// 市民不能删除其他人的反馈
const handleDelete = async (row) => {
  ElMessage.warning('市民只能查看自己的反馈，请联系管理员删除')
}



// 获取状态文本
const getStatusText = (state) => {
  const map = {
    0: '待处理',
    1: '处理中',
    2: '已完成'
  }
  return map[state] || '未知'
}

// 获取状态标签类型
const getStatusType = (state) => {
  const map = {
    0: 'warning',
    1: 'primary',
    2: 'success'
  }
  return map[state] || 'info'
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    // 用户取消操作，静默处理
    return
  }

  try {
    console.log('[退出登录] 开始清理登录状态')

    // 清除所有登录信息（与后端/路由守卫保持一致）
    localStorage.removeItem('role')
    localStorage.removeItem('token')
    localStorage.removeItem('phone')
    localStorage.removeItem('gmId')
    localStorage.removeItem('adminCode')
    userStore.clearInfo()

    console.log('[退出登录] localStorage 清理完成，role:', localStorage.getItem('role'))

    ElMessage.success('退出登录成功')

    // 直接替换到登录页并刷新，确保路由守卫重新判定未登录状态
    setTimeout(() => {
      router.replace('/').then(() => {
        window.location.reload()
      }).catch((err) => {
        console.error('[退出登录] 路由跳转失败，直接刷新:', err)
        window.location.href = '/'
      })
    }, 300)
  } catch (error) {
    console.error('[退出登录] 退出过程出错:', error)
    ElMessage.error('退出失败，请稍后重试')
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadMyFeedbacks()
  fetchProvinceList() // 加载省份列表
})
</script>

<template>
  <div class="feedback-container">
    <!-- 粒子背景 -->
    <EcoParticleBackground :density="3" :opacity="0.5" :max-particles="50" />

    <!-- 环保标语横幅 -->
    <EcoSloganBanner
      :slogans="supervisorSlogans"
      :interval="5000"
      :height="180"
    />

    <el-card class="feedback-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="24" color="#409eff">
              <Document />
            </el-icon>
            <span class="title">我的反馈</span>
          </div>
          <div class="header-right">
            <el-button 
              type="danger" 
              :icon="SwitchButton" 
              @click="handleLogout"
            >
              退出登录
            </el-button>
            <el-button type="primary" :icon="Plus" @click="dialogVisible = true">
              提交新反馈
            </el-button>
          </div>
        </div>
      </template>

      <!-- 反馈列表 -->
      <el-table 
        :data="feedbackList" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        
        <el-table-column prop="provinceName" label="省份" width="120">
          <template #default="{ row }">
            {{ row.provinceName || row.province_name || '未设置' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="cityName" label="城市" width="120">
          <template #default="{ row }">
            {{ row.cityName || row.city_name || '未设置' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="address" label="详细地址" min-width="200">
          <template #default="{ row }">
            <el-icon>
              <Location />
            </el-icon>
            <span>{{ row.address || '未填写' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="information" label="反馈内容" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.information }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="state" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusType(row.state)" 
              size="small"
              effect="light"
              round
            >
              {{ getStatusText(row.state) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="estimatedGrade" label="预估等级" width="150">
          <template #default="{ row }">
            <el-tag size="small" :type="getGradeType(row.estimatedGrade)" effect="light" round>
              {{ getGradeLabel(row.estimatedGrade) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="afDate" label="反馈日期" width="120">
          <template #default="{ row }">
            {{ row.afDate || '-' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="afTime" label="反馈时间" width="100">
          <template #default="{ row }">
            {{ row.afTime || '-' }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              :icon="Edit" 
              circle 
              size="small"
              @click="handleView(row)"
              class="view-btn"
              title="查看详情"
            />
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-if="!loading && feedbackList.length === 0" description="暂无反馈记录" />
    </el-card>

    <!-- 提交/编辑反馈对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="提交新反馈"
      width="600px"
      :before-close="resetForm"
    >
      <el-form 
        ref="feedbackFormRef"
        :model="feedbackForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="省份" prop="provinceId">
          <el-select 
            v-model="feedbackForm.provinceId" 
            placeholder="请选择省份"
            style="width: 100%"
            :loading="provinceLoading"
            @change="handleProvinceChange"
          >
            <el-option
              v-for="item in provinceList"
              :key="item.province_id"
              :label="item.province_name"
              :value="item.province_id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="城市" prop="cityId">
          <el-select 
            v-model="feedbackForm.cityId" 
            placeholder="请先选择省份，再选择城市"
            style="width: 100%"
            :loading="cityLoading"
            :disabled="!feedbackForm.provinceId"
          >
            <el-option
              v-for="item in cityList"
              :key="item.city_id"
              :label="item.city_name"
              :value="item.city_id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="地点" prop="address">
          <el-input 
            v-model="feedbackForm.address" 
            placeholder="请输入详细地址（街道、门牌号等）"
            :prefix-icon="Location"
            clearable
          >
            <template #append>
              <el-button :icon="MapLocation" @click="openMapPicker">
                地图定位
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="反馈内容" prop="information">
          <el-input 
            v-model="feedbackForm.information" 
            type="textarea"
            :rows="4"
            placeholder="请详细描述您遇到的问题（10-500字）"
            maxlength="500"
            show-word-limit
            clearable
          />
        </el-form-item>
        
        <el-form-item label="预估等级" prop="estimatedGrade">
          <el-select
            v-model="feedbackForm.estimatedGrade"
            placeholder="请选择预估等级"
            style="width: 100%"
          >
            <el-option
              v-for="item in gradeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        

      </el-form>
      
      <template #footer>
        <el-button @click="resetForm; dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          提交反馈
        </el-button>
      </template>
    </el-dialog>
    <!-- 反馈详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="反馈详情"
      width="620px"
    >
      <el-skeleton v-if="detailLoading" :rows="6" animated />
      
      <template v-else>
        <el-descriptions :column="1" border class="eco-descriptions">
          <el-descriptions-item label="反馈ID">{{ detailInfo.afId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="详细地址">
            {{ detailInfo.provinceName || '' }}{{ detailInfo.cityName ? ' / ' + detailInfo.cityName : '' }}{{ detailInfo.address ? ' / ' + detailInfo.address : '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="反馈内容">{{ detailInfo.information || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预估等级">
            <el-tag
              size="small"
              :type="getGradeType(detailInfo.estimatedGrade)"
              effect="light"
              round
            >
              {{ getGradeLabel(detailInfo.estimatedGrade) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag
              :type="getStatusType(detailInfo.state)"
              size="small"
              effect="light"
              round
            >
              {{ getStatusText(detailInfo.state) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="反馈时间">{{ detailInfo.afDate || '-' }} {{ detailInfo.afTime || '' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 网格员处理结果 -->
        <template v-if="detailStatistics">
          <el-divider content-position="left">🌱 网格员处理结果</el-divider>
          
          <el-descriptions :column="2" border class="eco-descriptions">
            <el-descriptions-item label="SO₂ 实测值">
              <span class="detail-value">{{ detailStatistics.so2_value ?? detailStatistics.so2Value ?? '-' }} μg/m³</span>
            </el-descriptions-item>
            <el-descriptions-item label="SO₂ 等级">
              <el-tag
                :type="getGradeType(detailStatistics.so2_level ?? detailStatistics.so2Level)"
                size="small"
                effect="light"
                round
              >
                {{ getGradeLabel(detailStatistics.so2_level ?? detailStatistics.so2Level) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="CO 实测值">
              <span class="detail-value">{{ detailStatistics.co_value ?? detailStatistics.coValue ?? '-' }} mg/m³</span>
            </el-descriptions-item>
            <el-descriptions-item label="CO 等级">
              <el-tag
                :type="getGradeType(detailStatistics.co_level ?? detailStatistics.coLevel)"
                size="small"
                effect="light"
                round
              >
                {{ getGradeLabel(detailStatistics.co_level ?? detailStatistics.coLevel) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="SPM 实测值">
              <span class="detail-value">{{ detailStatistics.spm_value ?? detailStatistics.spmValue ?? '-' }} μg/m³</span>
            </el-descriptions-item>
            <el-descriptions-item label="SPM 等级">
              <el-tag
                :type="getGradeType(detailStatistics.spm_level ?? detailStatistics.spmLevel)"
                size="small"
                effect="light"
                round
              >
                {{ getGradeLabel(detailStatistics.spm_level ?? detailStatistics.spmLevel) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="综合 AQI 等级" :span="2">
              <el-tag
                :type="getGradeType(detailStatistics.aqi_id ?? detailStatistics.aqiId)"
                size="small"
                effect="dark"
                round
              >
                {{ getGradeLabel(detailStatistics.aqi_id ?? detailStatistics.aqiId) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="检测备注" :span="2">
              <span class="detail-remarks">{{ detailStatistics.remarks || '暂无备注' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="处理时间" :span="2">
              {{ detailStatistics.confirm_date || detailStatistics.confirmDate || '-' }} 
              {{ detailStatistics.confirm_time || detailStatistics.confirmTime || '' }}
            </el-descriptions-item>
          </el-descriptions>
        </template>

        <el-alert
          v-else-if="detailInfo.state === 2"
          title="未找到处理记录"
          type="warning"
          :closable="false"
          description="该反馈已处理完成，但未查询到检测统计记录。"
          style="margin-top: 16px;"
        />
      </template>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 地图定位选择器 -->
    <MapLocationPicker
      v-model="mapPickerVisible"
      :region="selectedRegionForMap"
      @select="handleLocationSelect"
    />
  </div>
</template>

<style lang="scss" scoped>
.feedback-container {
  padding: 20px;
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 50%, #e0f2f1 100%);
  min-height: calc(100vh - 120px);
  position: relative;
  overflow: hidden;

  .feedback-card {
    border: 1px solid rgba(129, 199, 132, 0.3);
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(46, 125, 50, 0.1);
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);

    :deep(.el-card__header) {
      background: linear-gradient(135deg, rgba(232, 245, 233, 0.8), rgba(241, 248, 233, 0.8));
      border-bottom: 1px solid #c8e6c9;
      border-radius: 16px 16px 0 0;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-left {
        display: flex;
        align-items: center;
        gap: 10px;

        .title {
          font-size: 18px;
          font-weight: 600;
          color: #1b5e20;
        }
      }

      .header-right {
        display: flex;
        align-items: center;
        gap: 12px;
      }
    }

    .feedback-title {
      font-weight: 500;
      color: #1b5e20;
    }

    .view-btn {
      color: #2e7d32;
    }
  }
}

.detail-value {
  font-weight: 600;
  color: #2e7d32;
}

.detail-remarks {
  color: #555;
  line-height: 1.6;
  white-space: pre-wrap;
}

// 响应式适配
@media (max-width: 768px) {
  .feedback-container {
    padding: 10px;

    .feedback-card {
      .card-header {
        flex-direction: column;
        gap: 12px;
        align-items: flex-start;
      }
    }
  }
}
</style>
