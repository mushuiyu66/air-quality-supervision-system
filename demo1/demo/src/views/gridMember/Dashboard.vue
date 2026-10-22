<script setup lang="js">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { 
  Document, 
  Position
} from '@element-plus/icons-vue'
import { getGridMemberList, getGmById } from '@/api/gridMember'
import { getAssignedFeedbacks } from '@/api/gridMember'
import { getProvinceList, getCityList } from '@/api/province'
import EcoSloganBanner from '@/components/EcoSloganBanner.vue'

const userStore = useUserStore()

// 网格员专属标语
const gridSlogans = [
  {
    icon: '🌍',
    text: '低碳生活，绿色未来',
    subtitle: '网格巡查，守护每一寸蓝天'
  },
  {
    icon: '🔧',
    text: '及时响应，高效处理',
    subtitle: '工单就是命令，责任重于泰山'
  },
  {
    icon: '🌿',
    text: '绿水青山就是金山银山',
    subtitle: '深入一线，精准排查每一处环境问题'
  },
  {
    icon: '💚',
    text: '环保监督，共建美好家园',
    subtitle: '您的每一次巡查，都让城市更清新'
  },
  {
    icon: '🌱',
    text: '节能减排，人人有责',
    subtitle: '从网格开始，推动绿色低碳发展'
  }
]

// 网格员信息
const memberInfo = ref({})
const provinceName = ref('')
const cityName = ref('')

// 统计数据
const stats = ref({
  assignedTasks: 0,  // 已指派工单数(state=1)
  completedTasks: 0  // 已完成工单数(state=2)
})

// 获取网格员详细信息
const fetchMemberInfo = async () => {
  // 优先从 Pinia store 获取
  if (userStore.info && userStore.info.gmId) {
    memberInfo.value = userStore.info
    await resolveLocationNames(memberInfo.value.provinceId, memberInfo.value.cityId)
    console.log('[Dashboard] 从Pinia store获取信息:', memberInfo.value)
    return
  }
  
  // 如果 store 中没有,尝试从 localStorage 获取 gmId
  const gmId = localStorage.getItem('gmId')
  if (!gmId) {
    console.warn('[Dashboard] 警告:localStorage中没有gmId,但角色为:', localStorage.getItem('role'))
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const res = await getGmById(gmId)
    if (res.code === 200) {
      // 处理嵌套结构:res.data.data才是实际用户数据
      const userData = res.data.data || res.data
      memberInfo.value = userData
      await resolveLocationNames(memberInfo.value.provinceId, memberInfo.value.cityId)
      console.log('[Dashboard] 从API获取信息:', memberInfo.value)
    }
  } catch (error) {
    console.error('获取网格员信息失败:', error)
  }
}

// 根据ID解析省/市名称
const resolveLocationNames = async (provinceId, cityId) => {
  if (!provinceId && !cityId) {
    provinceName.value = '-'
    cityName.value = '-'
    return
  }

  try {
    const [provinceRes, cityRes] = await Promise.all([
      getProvinceList(),
      getCityList()
    ])

    let province = null
    let city = null

    if (provinceRes && provinceRes.code === 200) {
      const provinces = provinceRes.data || []
      province = provinces.find(p => p.province_id === provinceId || p.provinceId === provinceId)
    }

    if (cityRes && cityRes.code === 200) {
      const cities = cityRes.data || []
      city = cities.find(c => c.city_id === cityId || c.cityId === cityId)
    }

    provinceName.value = province?.province_name || province?.provinceName || `省编号: ${provinceId}`
    cityName.value = city?.city_name || city?.cityName || `市编号: ${cityId}`
  } catch (error) {
    console.error('解析省/市名称失败:', error)
    provinceName.value = `省编号: ${provinceId}`
    cityName.value = `市编号: ${cityId}`
  }
}

// 获取工单统计数据
const fetchTaskStats = async () => {
  const gmId = localStorage.getItem('gmId')
  if (!gmId) {
    console.warn('[Dashboard] 未找到gmId,无法获取工单统计')
    return
  }
  
  try {
    const res = await getAssignedFeedbacks(gmId)
    if (res.code === 200) {
      const tasks = res.data || []
      // state=1: 已指派(待处理), state=2: 已完成
      stats.value.assignedTasks = tasks.filter(task => task.state === 1).length
      stats.value.completedTasks = tasks.filter(task => task.state === 2).length
      console.log('[Dashboard] 工单统计:', stats.value)
    }
  } catch (error) {
    console.error('获取工单统计失败:', error)
  }
}

// 初始化数据
onMounted(() => {
  fetchMemberInfo()
  fetchTaskStats() // 加载工单统计
})
</script>

<template>
  <div class="dashboard-page page-fade-in">
    <!-- 环保标语横幅 -->
    <EcoSloganBanner
      :slogans="gridSlogans"
      :interval="5000"
      :height="180"
    />

    <el-row :gutter="20">
      <!-- 网格员基本信息 -->
      <el-col :span="24">
        <el-card shadow="hover" class="eco-card card-animate info-card">
          <template #header>
            <div class="card-header page-card-header">
              <span class="title">👤 个人信息</span>
            </div>
          </template>
          <el-descriptions :column="3" border class="eco-descriptions">
            <el-descriptions-item label="网格员编号">
              {{ memberInfo.gmCode || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="姓名">
              {{ memberInfo.gmName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ memberInfo.tel || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="省份">
              {{ provinceName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="城市">
              {{ cityName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="memberInfo.state === 0 ? 'success' : 'warning'" effect="light" round>
                {{ memberInfo.state === 0 ? '工作状态' : '非工作状态' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 统计卡片 -->
      <el-col :span="8">
        <el-card shadow="hover" class="eco-card stat-card card-animate">
          <div class="stat-item">
            <div class="stat-icon pending">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.assignedTasks }}</div>
              <div class="stat-label">待处理工单</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="eco-card stat-card card-animate">
          <div class="stat-item">
            <div class="stat-icon completed">
              <el-icon><Position /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completedTasks }}</div>
              <div class="stat-label">已完成工单</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover" class="eco-card card-animate">
          <template #header>
            <div class="card-header page-card-header">
              <span class="title">🚀 快捷入口</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-button class="quick-btn task" @click="$router.push('/gridmember/tasks')">
                <el-icon><Document /></el-icon>
                <span>查看工单</span>
              </el-button>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover" class="eco-card card-animate eco-gallery-card">
          <template #header>
            <div class="card-header page-card-header">
              <span class="title">🌿 生态空气保护</span>
            </div>
          </template>
          <div class="eco-gallery">
            <div class="eco-image-item">
              <svg viewBox="0 0 200 140" xmlns="http://www.w3.org/2000/svg">
                <defs>
                  <linearGradient id="sky1" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="#e3f2fd"/>
                    <stop offset="100%" stop-color="#b3e5fc"/>
                  </linearGradient>
                </defs>
                <rect width="200" height="140" fill="url(#sky1)" rx="12"/>
                <circle cx="160" cy="30" r="16" fill="#ffd54f"/>
                <g fill="#fff" opacity="0.9">
                  <ellipse cx="50" cy="40" rx="18" ry="10"/>
                  <ellipse cx="65" cy="42" rx="14" ry="8"/>
                  <ellipse cx="120" cy="28" rx="16" ry="9"/>
                  <ellipse cx="135" cy="30" rx="12" ry="7"/>
                </g>
                <path d="M0 110 Q 50 95 100 110 T 200 110 L 200 140 L 0 140 Z" fill="#81c784"/>
                <path d="M0 120 Q 60 108 120 120 T 200 120 L 200 140 L 0 140 Z" fill="#66bb6a"/>
                <g transform="translate(30, 85)">
                  <rect x="9" y="25" width="4" height="20" fill="#5d4037"/>
                  <circle cx="11" cy="20" r="16" fill="#4caf50"/>
                  <circle cx="11" cy="12" r="12" fill="#66bb6a"/>
                </g>
                <g transform="translate(150, 80)">
                  <rect x="9" y="30" width="4" height="25" fill="#5d4037"/>
                  <ellipse cx="11" cy="22" rx="18" ry="14" fill="#43a047"/>
                </g>
                <text x="100" y="128" text-anchor="middle" font-size="10" fill="#1b5e20" font-weight="bold">清新蓝天</text>
              </svg>
            </div>

            <div class="eco-image-item">
              <svg viewBox="0 0 200 140" xmlns="http://www.w3.org/2000/svg">
                <defs>
                  <linearGradient id="sky2" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="#e8f5e9"/>
                    <stop offset="100%" stop-color="#c8e6c9"/>
                  </linearGradient>
                </defs>
                <rect width="200" height="140" fill="url(#sky2)" rx="12"/>
                <circle cx="170" cy="28" r="14" fill="#fff9c4"/>
                <path d="M0 115 Q 50 100 100 115 T 200 115 L 200 140 L 0 140 Z" fill="#a5d6a7"/>
                <g transform="translate(40, 55)">
                  <polygon points="0,60 30,0 60,60" fill="#e0e0e0"/>
                  <polygon points="10,60 30,15 50,60" fill="#f5f5f5"/>
                  <rect x="28" y="60" width="4" height="20" fill="#9e9e9e"/>
                  <circle cx="30" cy="0" r="4" fill="#ef5350"/>
                </g>
                <g transform="translate(120, 80)">
                  <rect x="0" y="25" width="40" height="4" fill="#42a5f5"/>
                  <rect x="2" y="20" width="36" height="10" fill="#64b5f6"/>
                  <rect x="0" y="29" width="40" height="15" fill="#bdbdbd"/>
                </g>
                <text x="100" y="128" text-anchor="middle" font-size="10" fill="#1b5e20" font-weight="bold">绿色能源</text>
              </svg>
            </div>

            <div class="eco-image-item">
              <svg viewBox="0 0 200 140" xmlns="http://www.w3.org/2000/svg">
                <defs>
                  <linearGradient id="sky3" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="#e0f7fa"/>
                    <stop offset="100%" stop-color="#b2ebf2"/>
                  </linearGradient>
                </defs>
                <rect width="200" height="140" fill="url(#sky3)" rx="12"/>
                <path d="M20 100 L 60 50 L 100 100 Z" fill="#90a4ae"/>
                <path d="M70 100 L 120 40 L 170 100 Z" fill="#78909c"/>
                <path d="M130 100 L 165 60 L 200 100 Z" fill="#b0bec5"/>
                <path d="M0 105 Q 50 95 100 105 T 200 105 L 200 140 L 0 140 Z" fill="#66bb6a"/>
                <g transform="translate(30, 78)">
                  <rect x="8" y="20" width="4" height="18" fill="#5d4037"/>
                  <circle cx="10" cy="15" r="14" fill="#2e7d32"/>
                </g>
                <g transform="translate(150, 75)">
                  <rect x="8" y="25" width="4" height="20" fill="#5d4037"/>
                  <circle cx="10" cy="18" r="16" fill="#388e3c"/>
                </g>
                <g fill="#546e7a">
                  <path d="M80 55 Q 85 50 90 55"/>
                  <path d="M95 48 Q 100 43 105 48"/>
                </g>
                <text x="100" y="128" text-anchor="middle" font-size="10" fill="#1b5e20" font-weight="bold">森林氧吧</text>
              </svg>
            </div>

            <div class="eco-image-item">
              <svg viewBox="0 0 200 140" xmlns="http://www.w3.org/2000/svg">
                <defs>
                  <linearGradient id="sky4" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="#f1f8e9"/>
                    <stop offset="100%" stop-color="#dcedc8"/>
                  </linearGradient>
                </defs>
                <rect width="200" height="140" fill="url(#sky4)" rx="12"/>
                <path d="M0 120 Q 50 105 100 120 T 200 120 L 200 140 L 0 140 Z" fill="#aed581"/>
                <rect x="30" y="60" width="35" height="55" fill="#aed581"/>
                <rect x="35" y="70" width="8" height="10" fill="#e6ee9c"/>
                <rect x="50" y="70" width="8" height="10" fill="#e6ee9c"/>
                <rect x="35" y="90" width="8" height="10" fill="#e6ee9c"/>
                <rect x="50" y="90" width="8" height="10" fill="#e6ee9c"/>
                <polygon points="30,60 47,35 65,60" fill="#66bb6a"/>
                <rect x="80" y="85" width="30" height="30" fill="#8d6e63" rx="4"/>
                <circle cx="95" cy="100" r="8" fill="#42a5f5"/>
                <circle cx="90" cy="95" r="5" fill="#42a5f5"/>
                <circle cx="100" cy="95" r="5" fill="#42a5f5"/>
                <g transform="translate(140, 80)">
                  <circle cx="15" cy="15" r="10" fill="#43a047"/>
                  <rect x="13" y="25" width="4" height="15" fill="#5d4037"/>
                </g>
                <text x="100" y="128" text-anchor="middle" font-size="10" fill="#1b5e20" font-weight="bold">低碳城市</text>
              </svg>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
.dashboard-page {
  padding: 0;
}

.info-card {
  animation-delay: 0s;
}

.stat-card {
  animation-delay: 0.1s;

  &:nth-of-type(2) {
    animation-delay: 0.2s;
  }
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 20px;

  .stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 28px;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

    &:hover {
      transform: scale(1.08) rotate(-3deg);
    }

    &.pending {
      background: linear-gradient(135deg, #00acc1, #00838f);
    }

    &.completed {
      background: linear-gradient(135deg, #43a047, #2e7d32);
    }
  }

  .stat-info {
    flex: 1;

    .stat-value {
      font-size: 32px;
      font-weight: bold;
      color: #1b5e20;
      line-height: 1;
      margin-bottom: 8px;
    }

    .stat-label {
      font-size: 14px;
      color: #4caf50;
      font-weight: 500;
    }
  }
}

.quick-btn {
  width: 100%;
  height: 100px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 12px;
  border: none !important;
  color: #fff !important;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) !important;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.18);
  }

  .el-icon {
    font-size: 26px;
    margin-right: 10px;
  }

  &.task {
    background: linear-gradient(135deg, #42a5f5, #1976d2) !important;
  }
}

.eco-descriptions {
  :deep(.el-descriptions__cell) {
    padding: 12px 16px;
  }

  :deep(.el-descriptions__label) {
    background: rgba(232, 245, 233, 0.5);
    color: #1b5e20;
    font-weight: 500;
  }
}

.eco-gallery-card {
  animation-delay: 0.3s;

  .eco-gallery {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    padding: 10px 0;
  }

  .eco-image-item {
    background: #fff;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    border: 1px solid rgba(129, 199, 132, 0.2);

    &:hover {
      transform: translateY(-6px) scale(1.02);
      box-shadow: 0 12px 28px rgba(76, 175, 80, 0.18);
    }

    svg {
      display: block;
      width: 100%;
      height: auto;
    }
  }
}

@media (max-width: 1200px) {
  .eco-gallery {
    grid-template-columns: repeat(2, 1fr) !important;
  }
}

@media (max-width: 768px) {
  .eco-gallery {
    grid-template-columns: 1fr !important;
  }
}
</style>
