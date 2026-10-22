<script setup>
/**
 * 环保空气质量预测结果可视化组件
 * 用于网格员现场检测弹窗，把 SO2/CO/SPM 等级和综合 AQI 等级图形化展示
 */
const props = defineProps({
  so2: { type: Number, default: null },
  co: { type: Number, default: null },
  spm: { type: Number, default: null },
  aqi: { type: Number, default: null },
  aqiText: { type: String, default: '' }
})

// 等级颜色映射（1-6 级，越严重颜色越重）
const levelColors = {
  1: '#4caf50',   // 优 - 翠绿
  2: '#8bc34a',   // 良 - 浅绿
  3: '#ff9800',   // 轻度污染 - 橙
  4: '#ff5722',   // 中度污染 - 深橙
  5: '#f44336',   // 重度污染 - 红
  6: '#b71c1c'    // 严重污染 - 暗红
}

const levelNames = {
  1: '优',
  2: '良',
  3: '轻度污染',
  4: '中度污染',
  5: '重度污染',
  6: '严重污染'
}

// 污染物名称与图标
const indicators = [
  { key: 'so2', label: 'SO₂', unit: 'μg/m³', icon: '🌫️' },
  { key: 'co', label: 'CO', unit: 'mg/m³', icon: '🏭' },
  { key: 'spm', label: 'SPM', unit: 'μg/m³', icon: '💨' }
]

const getLevelColor = (level) => levelColors[level] || '#9e9e9e'
const getLevelName = (level) => levelNames[level] || '未知'

// SVG 圆环参数
const radius = 36
const stroke = 8
const circumference = 2 * Math.PI * radius

const getRingOffset = (level) => {
  if (!level) return circumference
  const percent = Math.min(Math.max(level / 6, 0), 1)
  return circumference * (1 - percent)
}
</script>

<template>
  <div class="aqi-visualizer">
    <!-- 三个污染物等级圆环 -->
    <div class="indicator-rings">
      <div v-for="item in indicators" :key="item.key" class="ring-item">
        <div class="ring-svg-wrap">
          <svg class="ring-svg" :width="radius * 2 + stroke * 2" :height="radius * 2 + stroke * 2">
            <!-- 背景轨道 -->
            <circle
              class="ring-track"
              :cx="radius + stroke"
              :cy="radius + stroke"
              :r="radius"
              :stroke-width="stroke"
              fill="none"
            />
            <!-- 进度弧 -->
            <circle
              class="ring-progress"
              :cx="radius + stroke"
              :cy="radius + stroke"
              :r="radius"
              :stroke-width="stroke"
              fill="none"
              :stroke="getLevelColor(props[item.key])"
              :stroke-dasharray="circumference"
              :stroke-dashoffset="getRingOffset(props[item.key])"
              stroke-linecap="round"
            />
          </svg>
          <div class="ring-center">
            <span class="ring-icon">{{ item.icon }}</span>
            <span class="ring-level" :style="{ color: getLevelColor(props[item.key]) }">
              {{ props[item.key] || '-' }}
            </span>
          </div>
        </div>
        <div class="ring-label">{{ item.label }}</div>
        <div class="ring-grade" :style="{ color: getLevelColor(props[item.key]) }">
          {{ props[item.key] ? getLevelName(props[item.key]) : '未输入' }}
        </div>
      </div>
    </div>

    <!-- 综合 AQI 等级仪表盘 -->
    <div class="aqi-gauge-panel" v-if="aqi">
      <div class="gauge-header">
        <span class="gauge-icon">🌍</span>
        <span class="gauge-title">综合 AQI 等级</span>
        <span class="gauge-grade" :style="{ color: getLevelColor(aqi) }">
          {{ aqiText || getLevelName(aqi) }}
        </span>
      </div>

      <div class="gauge-scale">
        <div
          v-for="n in 6"
          :key="n"
          class="scale-segment"
          :style="{ background: levelColors[n] }"
        >
          <span class="segment-number">{{ n }}</span>
        </div>
        <div class="gauge-marker" :style="{ left: `calc(${((aqi - 0.5) / 6) * 100}% - 8px)` }">
          ▲
        </div>
      </div>

      <div class="gauge-legend">
        <span
          v-for="n in 6"
          :key="n"
          class="legend-dot"
          :style="{ background: levelColors[n] }"
        />
        <span class="legend-text">{{ getLevelName(aqi) }} ({{ aqi }}级)</span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.aqi-visualizer {
  padding: 16px;
  background: linear-gradient(135deg, rgba(232, 245, 233, 0.6), rgba(241, 248, 233, 0.6));
  border: 1px solid rgba(129, 199, 132, 0.3);
  border-radius: 14px;
  margin-bottom: 8px;
}

.indicator-rings {
  display: flex;
  justify-content: space-around;
  gap: 12px;
  margin-bottom: 20px;
}

.ring-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.ring-svg-wrap {
  position: relative;
  width: 88px;
  height: 88px;
}

.ring-svg {
  transform: rotate(-90deg);
  overflow: visible;
}

.ring-track {
  stroke: rgba(200, 230, 201, 0.6);
}

.ring-progress {
  transition: stroke-dashoffset 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.ring-center {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.ring-icon {
  font-size: 16px;
  line-height: 1;
  margin-bottom: 2px;
}

.ring-level {
  font-size: 22px;
  font-weight: 700;
  line-height: 1;
}

.ring-label {
  margin-top: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #1b5e20;
}

.ring-grade {
  margin-top: 4px;
  font-size: 12px;
  font-weight: 500;
}

.aqi-gauge-panel {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  padding: 14px 16px;
  border: 1px solid rgba(129, 199, 132, 0.25);
}

.gauge-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.gauge-icon {
  font-size: 18px;
}

.gauge-title {
  font-size: 15px;
  font-weight: 600;
  color: #1b5e20;
  flex: 1;
}

.gauge-grade {
  font-size: 16px;
  font-weight: 700;
}

.gauge-scale {
  position: relative;
  display: flex;
  height: 22px;
  border-radius: 11px;
  overflow: hidden;
  margin-bottom: 10px;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}

.scale-segment {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.segment-number {
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25);
}

.gauge-marker {
  position: absolute;
  top: -10px;
  font-size: 16px;
  color: #1b5e20;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.9);
  transition: left 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.gauge-legend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: #555;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

@media (max-width: 480px) {
  .indicator-rings {
    gap: 6px;
  }

  .ring-svg-wrap {
    width: 72px;
    height: 72px;
  }

  .ring-level {
    font-size: 18px;
  }
}
</style>
