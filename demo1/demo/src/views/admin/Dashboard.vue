<template>
  <div class="dashboard-container">
    <!-- 环保标语横幅 -->
    <EcoSloganBanner
      :slogans="adminSlogans"
      :interval="5000"
      :height="180"
    />

    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon blue">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">1,234</div>
              <div class="stat-label">总工单数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon orange">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">56</div>
              <div class="stat-label">待指派</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon blue">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">89</div>
              <div class="stat-label">已指派</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon green">
              <el-icon><Select /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">1,089</div>
              <div class="stat-label">已确认</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 省市污染热力图 -->
    <el-row style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover" v-loading="mapLoading" element-loading-text="加载地图数据中...">
          <template #header>
            <div class="card-header">
              <span class="card-title-icon">🔥</span>
              <span>省市污染热力图 — 区域污染分布特征</span>
              <div class="card-legend">
                <span class="legend-dot low"></span>优
                <span class="legend-dot moderate"></span>良
                <span class="legend-dot light"></span>轻度
                <span class="legend-dot mid"></span>中度
                <span class="legend-dot heavy"></span>重度
                <span class="legend-dot severe"></span>严重
              </div>
            </div>
          </template>
          <v-chart
            v-if="mapLoaded"
            :option="heatmapChartOption"
            autoresize
            style="height: 420px;"
            @zr:click="handleMapClick"
          />
          <div v-else class="map-fallback">
            <el-empty description="地图数据加载失败，请刷新重试" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：工单状态分布 + AQI等级趋势 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>工单状态分布</span>
            </div>
          </template>
          <v-chart :option="statusChartOption" autoresize style="height: 320px;" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📈 AQI等级趋势 — 近30天空气质量变化</span>
            </div>
          </template>
          <v-chart :option="aqiTrendOption" autoresize style="height: 320px;" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 第三行：AQI等级分布统计 -->
    <el-row style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📊 AQI等级统计分布 — 近30天汇总</span>
            </div>
          </template>
          <v-chart :option="aqiDistOption" autoresize style="height: 300px;" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="js">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Warning, Clock, Select } from '@element-plus/icons-vue'
import EcoSloganBanner from '@/components/EcoSloganBanner.vue'
import VChart from 'vue-echarts'
import * as echarts from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart, MapChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  GeoComponent,
  VisualMapComponent
} from 'echarts/components'

// 注册 ECharts 组件
echarts.use([
  CanvasRenderer,
  PieChart,
  BarChart,
  MapChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  GeoComponent,
  VisualMapComponent
])

const router = useRouter()

const mapLoading = ref(true)
const mapLoaded = ref(false)

// 管理员专属标语
const adminSlogans = [
  {
    icon: '🌍',
    text: '低碳生活，绿色未来',
    subtitle: '每一次监督，都是对蓝天的守护'
  },
  {
    icon: '🏛️',
    text: '科学管理，精准治污',
    subtitle: '用数据驱动空气质量持续改善'
  },
  {
    icon: '🌿',
    text: '绿水青山就是金山银山',
    subtitle: '共建生态文明，共享美丽家园'
  },
  {
    icon: '💚',
    text: '节能减排，人人有责',
    subtitle: '空气监督系统 — 让每一次呼吸都清新'
  },
  {
    icon: '🌱',
    text: '守护蓝天，从管理做起',
    subtitle: '统筹调度，高效响应，持续优化'
  }
]

// 模拟统计数据
const statusStats = ref({
  unassigned: 56,
  assigned: 89,
  confirmed: 1089
})

// ---- 省市污染热力图数据 ----
// grade: 0-优 1-良 2-轻度污染 3-中度污染 4-重度污染 5-严重污染
const pollutionData = ref([
  { name: '北京', value: 3.5 },
  { name: '天津', value: 3.0 },
  { name: '河北', value: 3.8 },
  { name: '山西', value: 4.2 },
  { name: '内蒙古', value: 2.0 },
  { name: '辽宁', value: 3.0 },
  { name: '吉林', value: 2.2 },
  { name: '黑龙江', value: 1.8 },
  { name: '上海', value: 2.5 },
  { name: '江苏', value: 2.8 },
  { name: '浙江', value: 2.0 },
  { name: '安徽', value: 2.6 },
  { name: '福建', value: 1.5 },
  { name: '江西', value: 2.0 },
  { name: '山东', value: 3.5 },
  { name: '河南', value: 4.0 },
  { name: '湖北', value: 3.0 },
  { name: '湖南', value: 2.8 },
  { name: '广东', value: 1.8 },
  { name: '广西', value: 1.5 },
  { name: '海南', value: 0.8 },
  { name: '重庆', value: 2.5 },
  { name: '四川', value: 2.6 },
  { name: '贵州', value: 1.8 },
  { name: '云南', value: 1.2 },
  { name: '西藏', value: 0.5 },
  { name: '陕西', value: 3.2 },
  { name: '甘肃', value: 2.8 },
  { name: '青海', value: 1.5 },
  { name: '宁夏', value: 2.5 },
  { name: '新疆', value: 2.0 },
  { name: '台湾', value: 1.5 },
  { name: '香港', value: 2.0 },
  { name: '澳门', value: 1.8 }
])

// 加载中国地图 GeoJSON
const loadChinaMap = async () => {
  try {
    const res = await fetch('/china.json')
    if (!res.ok) throw new Error('HTTP ' + res.status)
    const geoJson = await res.json()
    echarts.registerMap('china', geoJson)
    mapLoaded.value = true
    console.log('[热力图] 中国地图注册成功')
  } catch (e) {
    console.error('[热力图] 地图加载失败:', e)
  } finally {
    mapLoading.value = false
  }
}

// 热力图 ECharts 配置
const heatmapChartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderColor: '#c8e6c9',
    textStyle: { color: '#1b5e20' },
    formatter: (params) => {
      if (!params.data) return ''
      const level = params.data.value
      const grades = ['优', '良', '轻度污染', '中度污染', '重度污染', '严重污染']
      const idx = Math.min(Math.floor(level), 5)
      return `<strong>${params.name}</strong><br/>
        污染指数：${level.toFixed(1)}<br/>
        空气质量：<span style="color:${gradeColorsHex[idx]}">${grades[idx]}</span>`
    }
  },
  visualMap: {
    min: 0,
    max: 5,
    left: 20,
    bottom: 20,
    calculable: true,
    inRange: {
      color: ['#c8e6c9', '#a5d6a7', '#ffeb3b', '#ff9800', '#ef6c00', '#c62828']
    },
    text: ['严重', '优'],
    textStyle: { color: '#555' }
  },
  series: [
    {
      name: '污染等级',
      type: 'map',
      map: 'china',
      roam: true,
      zoom: 1.2,
      center: [105, 36],
      label: {
        show: true,
        fontSize: 9,
        color: '#333'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 13,
          fontWeight: 'bold'
        },
        itemStyle: {
          areaColor: '#81c784',
          borderColor: '#2e7d32',
          borderWidth: 2
        }
      },
      itemStyle: {
        borderColor: '#fff',
        borderWidth: 1,
        areaColor: '#e8f5e9'
      },
      data: pollutionData.value
    }
  ]
}))

const gradeColorsHex = ['#4caf50', '#aed581', '#ffeb3b', '#ff9800', '#ef6c00', '#c62828']

// 点击地图省份
const handleMapClick = (evt) => {
  if (evt.topTarget?.__ecComponentInfo?.mainType !== 'series') return
  console.log('[热力图] 点击省份:', evt)
}

// ---- 工单状态分布饼图 ----
const statusChartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    top: 'middle'
  },
  series: [
    {
      name: '工单状态',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{c}'
      },
      emphasis: {
        label: { show: true, fontSize: 16, fontWeight: 'bold' }
      },
      data: [
        { value: statusStats.value.unassigned, name: '未指派', itemStyle: { color: '#ff9800' } },
        { value: statusStats.value.assigned, name: '已指派', itemStyle: { color: '#26a69a' } },
        { value: statusStats.value.confirmed, name: '已确认', itemStyle: { color: '#4caf50' } }
      ]
    }
  ]
}))

// ---- AQI等级趋势：近30天折线图 ----
const recent30Days = (() => {
  const days = []
  const now = new Date()
  for (let i = 29; i >= 0; i--) {
    const d = new Date(now)
    d.setDate(d.getDate() - i)
    days.push(`${d.getMonth() + 1}/${d.getDate()}`)
  }
  return days
})()

// 模拟30天AQI等级数据 (0-5)
const mock30DayLevels = [
  1, 1, 2, 1, 0, 1, 2, 3, 2, 1,
  2, 3, 4, 3, 2, 1, 2, 2, 3, 2,
  1, 0, 1, 1, 2, 1, 0, 1, 1, 2
]

const aqiTrendOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderColor: '#c8e6c9',
    textStyle: { color: '#1b5e20' },
    formatter: (params) => {
      const p = params[0]
      const levels = ['优', '良', '轻度污染', '中度污染', '重度污染', '严重污染']
      return `${p.axisValue}<br/>AQI等级：<b>${levels[p.value] || '未知'}</b>`
    }
  },
  grid: {
    left: '5%', right: '5%', bottom: '10%', top: '10%'
  },
  xAxis: {
    type: 'category',
    data: recent30Days,
    axisLine: { lineStyle: { color: '#a5d6a7' } },
    axisLabel: { rotate: 45, fontSize: 10, color: '#555' }
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 5,
    interval: 1,
    name: 'AQI等级',
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: {
      formatter: (v) => {
        const labels = ['优', '良', '轻度', '中度', '重度', '严重']
        return labels[v] || v
      }
    },
    splitLine: { lineStyle: { color: '#e0e0e0', type: 'dashed' } }
  },
  series: [
    {
      name: 'AQI等级',
      type: 'line',
      data: mock30DayLevels,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 3,
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#4caf50' },
          { offset: 0.5, color: '#ff9800' },
          { offset: 1, color: '#c62828' }
        ])
      },
      itemStyle: {
        color: (params) => {
          const colors = ['#4caf50', '#aed581', '#ffeb3b', '#ff9800', '#ef6c00', '#c62828']
          return colors[params.value] || '#999'
        }
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(76, 175, 80, 0.3)' },
          { offset: 1, color: 'rgba(76, 175, 80, 0.02)' }
        ])
      },
      markLine: {
        silent: true,
        data: [
          { yAxis: 1, name: '良-优分界线', lineStyle: { color: '#4caf50', type: 'dashed' } }
        ],
        label: { formatter: '优/良分界' }
      }
    }
  ]
}))

// ---- AQI等级分布统计（柱状图+占比） ----
const aqiDistRaw = [
  { level: '优', count: 8, color: '#4caf50' },
  { level: '良', count: 12, color: '#aed581' },
  { level: '轻度污染', count: 6, color: '#ffeb3b' },
  { level: '中度污染', count: 3, color: '#ff9800' },
  { level: '重度污染', count: 1, color: '#ef6c00' },
  { level: '严重污染', count: 0, color: '#c62828' }
]

const aqiDistOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    formatter: (params) => {
      const p = params[0]
      const total = aqiDistRaw.reduce((s, i) => s + i.count, 0)
      const pct = total > 0 ? ((p.value / total) * 100).toFixed(1) : 0
      return `${p.name}<br/>天数：<b>${p.value}</b> 天<br/>占比：<b>${pct}%</b>`
    }
  },
  grid: {
    left: '5%', right: '5%', bottom: '10%', top: '10%'
  },
  xAxis: {
    type: 'category',
    data: aqiDistRaw.map(i => i.level),
    axisLine: { lineStyle: { color: '#a5d6a7' } },
    axisLabel: { fontSize: 12, color: '#555' }
  },
  yAxis: {
    type: 'value',
    name: '天数',
    splitLine: { lineStyle: { color: '#e0e0e0', type: 'dashed' } }
  },
  series: [
    {
      name: '天数',
      type: 'bar',
      barWidth: '50%',
      data: aqiDistRaw.map((i, idx) => ({
        value: i.count,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: i.color },
            { offset: 1, color: i.color + '88' }
          ]),
          borderRadius: [8, 8, 0, 0]
        }
      })),
      label: {
        show: true,
        position: 'top',
        fontSize: 13,
        fontWeight: 'bold',
        color: '#333'
      }
    }
  ]
}))

onMounted(() => {
  loadChinaMap()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 0;
}

.stat-cards {
  .stat-card {
    margin-bottom: 20px;
    border: 1px solid rgba(129, 199, 132, 0.3) !important;
    border-radius: 16px !important;
    box-shadow: 0 4px 20px rgba(46, 125, 50, 0.1) !important;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 30px rgba(46, 125, 50, 0.2) !important;
    }

    .stat-item {
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        color: #fff;

        &.blue {
          background: linear-gradient(135deg, #00897b 0%, #00695c 100%);
        }

        &.orange {
          background: linear-gradient(135deg, #ffa726 0%, #ef6c00 100%);
        }

        &.green {
          background: linear-gradient(135deg, #66bb6a 0%, #2e7d32 100%);
        }
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #1b5e20;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 14px;
          color: #4caf50;
          margin-top: 4px;
        }
      }
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  color: #1b5e20;

  .card-title-icon {
    margin-right: 8px;
  }

  .card-legend {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #666;

    .legend-dot {
      display: inline-block;
      width: 12px;
      height: 12px;
      border-radius: 3px;
      margin-right: 2px;

      &.low { background: #c8e6c9; }
      &.moderate { background: #a5d6a7; }
      &.light { background: #ffeb3b; }
      &.mid { background: #ff9800; }
      &.heavy { background: #ef6c00; }
      &.severe { background: #c62828; }
    }
  }
}

.chart-placeholder {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.map-fallback {
  height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fdf9;
  border-radius: 12px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .stat-cards {
    .el-col {
      margin-bottom: 10px;
    }
  }
}
</style>
