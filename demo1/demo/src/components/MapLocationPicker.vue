<template>
  <el-dialog
    v-model="dialogVisible"
    title="地图定位"
    width="85vw"
    class="map-location-dialog"
    :close-on-click-modal="false"
    destroy-on-close
    @opened="handleOpened"
    @closed="handleClosed"
  >
    <div class="map-picker-body">
      <!-- 顶部工具栏：搜索 + 地图切换 -->
      <div class="map-toolbar">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索地点（如：北京市朝阳区）"
            clearable
            @keyup.enter="searchLocation"
          >
            <template #append>
              <el-button :icon="Search" :loading="searching" @click="searchLocation" />
            </template>
          </el-input>
        </div>

        <el-radio-group v-model="tileProvider" size="small" @change="switchTileLayer">
          <el-radio-button :value="'amap'">高德地图</el-radio-button>
          <el-radio-button :value="'osm'">OpenStreetMap</el-radio-button>
        </el-radio-group>

        <span v-if="selectedCoord" class="coord-tip">
          已选坐标：{{ selectedCoord.lat.toFixed(5) }}, {{ selectedCoord.lng.toFixed(5) }}
        </span>
        <span v-else class="coord-tip hint">点击地图选择位置，或搜索地点</span>
      </div>

      <div class="map-picker-content">
        <!-- 地图 -->
        <div ref="mapContainerRef" v-loading="mapLoading" element-loading-text="加载地图中..." class="map-chart-wrap"></div>

        <!-- 地图下方操作栏：定位当前位置 -->
        <div class="map-action-bar">
          <el-button type="primary" :icon="Position" :loading="locating" @click="locateCurrentPosition">
            {{ locating ? '定位中...' : '定位到当前位置' }}
          </el-button>
          <span class="action-hint">优先使用已填写的省份/城市作为默认区域</span>
        </div>

        <!-- 地址信息 -->
        <div v-if="selectedCoord" class="address-card">
          <div class="address-title">
            <el-icon><Location /></el-icon>
            <span>已选位置信息</span>
          </div>
          <el-form label-width="70px" class="address-form">
            <el-form-item label="详细地址">
              <el-input v-model="addressForm.address" type="textarea" :rows="1" placeholder="可手动编辑详细地址" />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item label="省/直辖市">
                  <el-input v-model="addressForm.province" placeholder="省" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="城市">
                  <el-input v-model="addressForm.city" placeholder="市" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="区/县">
                  <el-input v-model="addressForm.district" placeholder="区/县" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="街道">
              <el-input v-model="addressForm.street" placeholder="街道/道路/门牌号" />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :disabled="!selectedCoord" :loading="confirmLoading" @click="confirmSelection">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Position, Search, Location } from '@element-plus/icons-vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import OpenCC from 'opencc-js'

// 繁体（台湾/香港）转大陆简体中文
const toSimplified = OpenCC.Converter({ from: 'tw', to: 'cn' })
const toSimplifiedCN = (text) => {
  if (!text) return ''
  try {
    return toSimplified(String(text))
  } catch (e) {
    console.warn('[地图定位] 简繁转换失败:', e)
    return String(text)
  }
}

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  initialCoord: { type: Object, default: null },
  region: { type: Object, default: null } // { province: '辽宁省', city: '大连市' }
})

const emit = defineEmits(['update:modelValue', 'select'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const mapContainerRef = ref()
let mapInstance = null
let markerInstance = null
let tileLayerInstance = null
let reverseGeocodeController = null
let reverseGeocodeTimer = null

const mapLoading = ref(true)
const locating = ref(false)
const searching = ref(false)
const confirmLoading = ref(false)
const searchKeyword = ref('')
const tileProvider = ref('amap') // 默认高德，国内访问更快

const selectedCoord = ref(props.initialCoord ? { ...props.initialCoord } : null)
const addressForm = ref({
  address: '',
  province: '',
  city: '',
  district: '',
  street: ''
})

watch(
  () => props.initialCoord,
  (val) => {
    if (val) {
      selectedCoord.value = { ...val }
      if (mapInstance) {
        updateMarker(val.lat, val.lng, false, true)
      }
    }
  },
  { immediate: true }
)

// 街道输入变化时，自动同步到详细地址末尾
watch(
  () => addressForm.value.street,
  (val, oldVal) => {
    if (!val || val === oldVal) return
    const current = addressForm.value.address || ''
    // 如果当前地址已经包含该街道，不再追加
    if (current.includes(val)) return
    // 去除旧街道后缀
    const oldStreet = oldVal || ''
    let base = oldStreet ? current.replace(new RegExp(`[\\s\\-]*${oldStreet.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}$`), '').trim() : current
    // 如果 address 仍为空或为坐标兜底，用省市+街道重建
    if (!base || /^-?\d+\.\d+,\s*-?\d+\.\d+$/.test(base)) {
      base = [addressForm.value.province, addressForm.value.city, addressForm.value.district].filter(Boolean).join(' ')
    }
    addressForm.value.address = [base, val].filter(Boolean).join(' ')
  }
)

// ============== 坐标系转换：WGS84 <-> GCJ02（火星坐标） ==============
const PI = 3.1415926535897932384626
const X_PI = (PI * 3000.0) / 180.0
const A = 6378245.0
const EE = 0.00669342162296594323

const outOfChina = (lng, lat) => {
  return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271
}

const transformlat = (lng, lat) => {
  let ret =
    -100.0 +
    2.0 * lng +
    3.0 * lat +
    0.2 * lat * lat +
    0.1 * lng * lat +
    0.2 * Math.sqrt(Math.abs(lng))
  ret +=
    ((20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0) / 3.0
  ret +=
    ((20.0 * Math.sin(lat * PI) + 40.0 * Math.sin((lat / 3.0) * PI)) * 2.0) / 3.0
  ret +=
    ((160.0 * Math.sin((lat / 12.0) * PI) + 320 * Math.sin((lat * PI) / 30.0)) * 2.0) / 3.0
  return ret
}

const transformlng = (lng, lat) => {
  let ret =
    300.0 +
    lng +
    2.0 * lat +
    0.1 * lng * lng +
    0.1 * lng * lat +
    0.1 * Math.sqrt(Math.abs(lng))
  ret +=
    ((20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0) / 3.0
  ret +=
    ((20.0 * Math.sin(lng * PI) + 40.0 * Math.sin((lng / 3.0) * PI)) * 2.0) / 3.0
  ret +=
    ((150.0 * Math.sin((lng / 12.0) * PI) + 300 * Math.sin((lng / 30.0) * PI)) * 2.0) / 3.0
  return ret
}

// WGS84 -> GCJ02，返回 [lat, lng]
const wgs84ToGcj02 = (lng, lat) => {
  if (outOfChina(lng, lat)) return [lat, lng]
  let dlat = transformlat(lng - 105.0, lat - 35.0)
  let dlng = transformlng(lng - 105.0, lat - 35.0)
  const radlat = (lat / 180.0) * PI
  let magic = Math.sin(radlat)
  magic = 1 - EE * magic * magic
  const sqrtmagic = Math.sqrt(magic)
  dlat = (dlat * 180.0) / (((A * (1 - EE)) / (magic * sqrtmagic)) * PI)
  dlng = (dlng * 180.0) / ((A / sqrtmagic) * Math.cos(radlat) * PI)
  return [lat + dlat, lng + dlng]
}

// GCJ02 -> WGS84，返回 [lat, lng]
const gcj02ToWgs84 = (lng, lat) => {
  if (outOfChina(lng, lat)) return [lat, lng]
  let dlat = transformlat(lng - 105.0, lat - 35.0)
  let dlng = transformlng(lng - 105.0, lat - 35.0)
  const radlat = (lat / 180.0) * PI
  let magic = Math.sin(radlat)
  magic = 1 - EE * magic * magic
  const sqrtmagic = Math.sqrt(magic)
  dlat = (dlat * 180.0) / (((A * (1 - EE)) / (magic * sqrtmagic)) * PI)
  dlng = (dlng * 180.0) / ((A / sqrtmagic) * Math.cos(radlat) * PI)
  const mglat = lat + dlat
  const mglng = lng + dlng
  return [lat * 2 - mglat, lng * 2 - mglng]
}

// ============== 地图初始化 ==============
const tileProviders = {
  amap: {
    url: 'https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}',
    subdomains: '1234',
    attribution: '&copy; 高德地图',
    maxZoom: 18
  },
  osm: {
    url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    subdomains: 'abc',
    attribution: '&copy; OpenStreetMap contributors',
    maxZoom: 19
  }
}

const initMap = () => {
  if (mapInstance || !mapContainerRef.value) return

  mapInstance = L.map(mapContainerRef.value, {
    center: [39.9042, 116.4074], // 默认北京
    zoom: 12,
    zoomControl: true,
    attributionControl: true
  })

  addTileLayer(tileProvider.value)

  // 点击地图选点：得到 GCJ02，转 WGS84 存储
  mapInstance.on('click', (e) => {
    const { lat, lng } = e.latlng
    const [wgsLat, wgsLng] = gcj02ToWgs84(lng, lat)
    selectedCoord.value = { lat: wgsLat, lng: wgsLng }
    // 点击只平移地图，不改变缩放级别，避免乱跳
    updateMarker(wgsLat, wgsLng, true, false)
  })

  mapLoading.value = false

  // 如果有初始坐标，定位到该位置
  if (selectedCoord.value) {
    updateMarker(selectedCoord.value.lat, selectedCoord.value.lng, false, true)
  }
}

const addTileLayer = (providerKey) => {
  if (!mapInstance) return
  const cfg = tileProviders[providerKey]
  if (tileLayerInstance) {
    mapInstance.removeLayer(tileLayerInstance)
  }
  tileLayerInstance = L.tileLayer(cfg.url, {
    subdomains: cfg.subdomains,
    maxZoom: cfg.maxZoom,
    attribution: cfg.attribution,
    crossOrigin: true
  }).addTo(mapInstance)
}

const switchTileLayer = (providerKey) => {
  addTileLayer(providerKey)
  // 切换图层后，如果已有标记，按新坐标系重新定位
  if (selectedCoord.value && markerInstance) {
    const { lat, lng } = selectedCoord.value
    const [mapLat, mapLng] = tileProvider.value === 'amap' ? wgs84ToGcj02(lng, lat) : [lat, lng]
    markerInstance.setLatLng([mapLat, mapLng])
  }
}

// 自定义标记图标
const createMarkerIcon = () => {
  return L.divIcon({
    className: 'custom-map-marker',
    html: '<div class="marker-pin"><div class="marker-dot"></div></div>',
    iconSize: [28, 36],
    iconAnchor: [14, 34]
  })
}

// 更新标记（入参为 WGS84）
// center: false 只移动标记，不平移地图；true 时平移并缩放到合适级别
const updateMarker = async (lat, lng, reverse = true, center = false) => {
  if (!mapInstance) return

  // 在地图上显示需转为 GCJ02
  const [mapLat, mapLng] = tileProvider.value === 'amap' ? wgs84ToGcj02(lng, lat) : [lat, lng]

  if (markerInstance) {
    markerInstance.setLatLng([mapLat, mapLng])
  } else {
    markerInstance = L.marker([mapLat, mapLng], { draggable: true, icon: createMarkerIcon() }).addTo(mapInstance)
    markerInstance.on('dragend', async (e) => {
      const { lat: mapLat2, lng: mapLng2 } = e.target.getLatLng()
      const [wgsLat, wgsLng] = tileProvider.value === 'amap' ? gcj02ToWgs84(mapLng2, mapLat2) : [mapLat2, mapLng2]
      selectedCoord.value = { lat: wgsLat, lng: wgsLng }
      await reverseGeocode(wgsLat, wgsLng)
    })
  }

  if (center) {
    mapInstance.setView([mapLat, mapLng], 16)
  }
  // 普通点击不再强制 setView，只移动标记，保持当前缩放，避免乱跳

  if (reverse) {
    await reverseGeocode(lat, lng)
  }
}

// 取消正在进行的反地理编码请求
const cancelPendingReverseGeocode = () => {
  if (reverseGeocodeTimer) {
    clearTimeout(reverseGeocodeTimer)
    reverseGeocodeTimer = null
  }
  if (reverseGeocodeController) {
    try {
      reverseGeocodeController.abort()
    } catch (e) {
      // ignore
    }
    reverseGeocodeController = null
  }
}

// 反地理编码：优先 BigDataCloud（国内友好），超时则 fallback 到 Nominatim
const reverseGeocode = async (lat, lng) => {
  cancelPendingReverseGeocode()

  let result = null

  // 先尝试 BigDataCloud（国内访问更稳定）
  try {
    const controller = new AbortController()
    reverseGeocodeController = controller
    const timeoutId = setTimeout(() => controller.abort(), 4000)
    reverseGeocodeTimer = timeoutId
    const res = await fetch(
      `https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${lat}&longitude=${lng}&localityLanguage=zh`,
      { signal: controller.signal }
    )
    clearTimeout(timeoutId)
    reverseGeocodeTimer = null
    reverseGeocodeController = null
    if (res.ok) {
      const data = await res.json()
      if (data) {
        result = parseBigDataCloudAddress(data)
      }
    }
  } catch (e) {
    if (e.name !== 'AbortError') {
      console.warn('[地图定位] BigDataCloud 反地理编码失败:', e)
    }
  }

  // BigDataCloud 失败，或没有解析出街道，则 fallback Nominatim
  if (!result || !result.address || !result.street) {
    try {
      const controller = new AbortController()
      reverseGeocodeController = controller
      const timeoutId = setTimeout(() => controller.abort(), 4000)
      reverseGeocodeTimer = timeoutId
      const res = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1&accept-language=zh-CN`,
        { signal: controller.signal, headers: { 'Accept-Language': 'zh-CN' } }
      )
      clearTimeout(timeoutId)
      reverseGeocodeTimer = null
      reverseGeocodeController = null
      if (res.ok) {
        const data = await res.json()
        if (data && data.address) {
          result = parseNominatimAddress(data.address, data.display_name)
        }
      }
    } catch (e) {
      if (e.name !== 'AbortError') {
        console.warn('[地图定位] Nominatim 反地理编码失败:', e)
      }
    }
  }

  if (result && result.address) {
    addressForm.value = result
  } else {
    // 解析失败时保留已有字段，只补充坐标兜底
    addressForm.value.address = addressForm.value.address || `${lat.toFixed(5)}, ${lng.toFixed(5)}`
  }
}

const parseBigDataCloudAddress = (data) => {
  const province = toSimplifiedCN(data.principalSubdivision || '')
  const rawCity = data.city || data.locality || ''
  const city = toSimplifiedCN(rawCity)
  const district = toSimplifiedCN(data.localityInfo?.administrative?.find((a) => a.adminLevel === 6)?.name || '')
  // 尝试从街道级（adminLevel 8/9/10）提取街道名，若 locality 本身较细也纳入
  let street = toSimplifiedCN(data.localityInfo?.administrative?.find((a) => a.adminLevel >= 8 && a.adminLevel <= 10)?.name || '')
  if (!street && data.locality && data.locality !== rawCity) {
    street = toSimplifiedCN(data.locality)
  }
  const address = [province, city, district, street].filter(Boolean).join(' ')
  return { province, city, district, street, address }
}

const parseNominatimAddress = (addr, displayName) => {
  const province = toSimplifiedCN(addr.state || addr.province || addr.region || '')
  const city = toSimplifiedCN(addr.city || addr.city_district || addr.town || addr.county || '')
  const district = toSimplifiedCN(addr.district || addr.suburb || addr.county || addr.borough || '')
  const street = toSimplifiedCN([addr.road, addr.street, addr.pedestrian, addr.path, addr.house_number, addr.neighbourhood, addr.quarter].filter(Boolean).join(' '))
  const address = toSimplifiedCN(displayName || [province, city, district, street].filter(Boolean).join(' '))
  return { province, city, district, street, address }
}

const handleOpened = () => {
  mapLoading.value = true
  nextTick(() => {
    initMap()
    if (selectedCoord.value) {
      reverseGeocode(selectedCoord.value.lat, selectedCoord.value.lng)
    } else if (props.region) {
      locateByRegion(props.region)
    }
    // 对话框动画结束后通知 Leaflet 重新计算容器尺寸
    setTimeout(() => {
      if (mapInstance) mapInstance.invalidateSize()
    }, 260)
  })
}

// 根据已选省市定位到对应区域
const locateByRegion = async (region) => {
  const keyword = [region.province, region.city].filter(Boolean).join(' ')
  if (!keyword) return
  searching.value = true
  try {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 6000)
    const res = await fetch(
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(keyword)}&limit=1&accept-language=zh-CN`,
      { signal: controller.signal, headers: { 'Accept-Language': 'zh-CN' } }
    )
    clearTimeout(timeoutId)
    const data = await res.json()
    if (Array.isArray(data) && data.length > 0) {
      const first = data[0]
      const lat = parseFloat(first.lat)
      const lng = parseFloat(first.lon)
      selectedCoord.value = { lat, lng }
      await updateMarker(lat, lng, true, true)
    }
  } catch (e) {
    if (e.name !== 'AbortError') {
      console.error('[地图定位] 省市定位失败:', e)
    }
  } finally {
    searching.value = false
  }
}

const handleClosed = () => {
  cancelPendingReverseGeocode()
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
    markerInstance = null
    tileLayerInstance = null
  }
}

// 定位当前位置（浏览器返回 WGS84）
const locateCurrentPosition = () => {
  if (!navigator.geolocation) {
    ElMessage.warning('您的浏览器不支持地理定位')
    return
  }
  locating.value = true
  navigator.geolocation.getCurrentPosition(
    async (position) => {
      locating.value = false
      const lng = position.coords.longitude
      const lat = position.coords.latitude
      selectedCoord.value = { lat, lng }
      // 定位成功时地图移动到当前位置并放大到街道级
      await updateMarker(lat, lng, true, true)
      ElMessage.success('定位成功')
    },
    (err) => {
      locating.value = false
      console.error('[地图定位] 定位失败:', err)
      ElMessage.error('定位失败：' + (err.message || '请检查定位权限'))
    },
    { enableHighAccuracy: false, timeout: 10000, maximumAge: 60000 }
  )
}

// 搜索地点（Nominatim 返回 WGS84）
const searchLocation = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    ElMessage.warning('请输入搜索地点')
    return
  }
  searching.value = true
  try {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 6000)
    const res = await fetch(
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(keyword)}&limit=5&accept-language=zh-CN`,
      { signal: controller.signal, headers: { 'Accept-Language': 'zh-CN' } }
    )
    clearTimeout(timeoutId)
    const data = await res.json()
    if (Array.isArray(data) && data.length > 0) {
      const first = data[0]
      const lat = parseFloat(first.lat)
      const lng = parseFloat(first.lon)
      selectedCoord.value = { lat, lng }
      await updateMarker(lat, lng, true, true)
    } else {
      ElMessage.warning('未找到该地点，请尝试其他关键词')
    }
  } catch (e) {
    if (e.name !== 'AbortError') {
      console.error('[地图定位] 搜索失败:', e)
    }
    ElMessage.error('地点搜索失败，请检查网络')
  } finally {
    searching.value = false
  }
}

const confirmSelection = async () => {
  if (!selectedCoord.value) return
  confirmLoading.value = true
  try {
    if (!addressForm.value.address) {
      await reverseGeocode(selectedCoord.value.lat, selectedCoord.value.lng)
    }

    // 确保街道信息被合并到详细地址中
    const street = addressForm.value.street || ''
    let address = addressForm.value.address || ''
    if (street && !address.includes(street)) {
      const base = [addressForm.value.province, addressForm.value.city, addressForm.value.district].filter(Boolean).join(' ')
      address = [base, street].filter(Boolean).join(' ')
    }
    addressForm.value.address = address

    emit('select', {
      lat: selectedCoord.value.lat,
      lng: selectedCoord.value.lng,
      ...addressForm.value
    })
    dialogVisible.value = false
  } finally {
    confirmLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.map-location-dialog {
  max-width: 900px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  margin-top: 5vh !important;
  font-family: "PingFang SC", "Microsoft YaHei", "Hiragino Sans GB", "Helvetica Neue", Arial, sans-serif;

  :deep(.el-dialog__body) {
    padding: 12px 20px;
    flex: 1;
    overflow: hidden;
    min-height: 0;
  }
}

:deep(.leaflet-container) {
  font-family: "PingFang SC", "Microsoft YaHei", "Hiragino Sans GB", "Helvetica Neue", Arial, sans-serif;
}

:deep(.leaflet-control-attribution) {
  font-size: 10px;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
}

.map-picker-body {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 10px;
}

.map-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  flex-shrink: 0;

  .search-box {
    width: 260px;
    flex-shrink: 0;
  }

  .coord-tip {
    font-size: 13px;
    color: #2e7d32;
    font-weight: 500;

    &.hint {
      color: #888;
      font-weight: normal;
    }
  }
}

.map-picker-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.map-chart-wrap {
  height: 280px;
  min-height: 200px;
  border: 1px solid #c8e6c9;
  border-radius: 12px;
  overflow: hidden;
  background: #f9fdf9;
  z-index: 1;
  flex-shrink: 0;
  position: relative;
  isolation: isolate;
}

.map-action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  flex-shrink: 0;
  padding: 6px 4px;
  position: relative;
  z-index: 2;
  background: #fff;
  border-radius: 8px;

  .action-hint {
    font-size: 13px;
    color: #888;
  }
}

.address-card {
  background: linear-gradient(135deg, #f1f8e9 0%, #ffffff 100%);
  border: 1px solid #c8e6c9;
  border-radius: 12px;
  padding: 12px;
  flex-shrink: 0;
  position: relative;
  z-index: 2;

  .address-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 600;
    color: #1b5e20;
    margin-bottom: 8px;
  }
}

.address-form {
  :deep(.el-form-item) {
    margin-bottom: 8px;
  }
  :deep(.el-form-item__label) {
    color: #2e7d32;
    font-weight: 500;
    padding-right: 8px;
  }
}

:deep(.custom-map-marker) {
  background: transparent;
  border: none;
}

:deep(.marker-pin) {
  width: 28px;
  height: 36px;
  position: relative;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.25));
}

:deep(.marker-pin::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #ef5350 0%, #c62828 100%);
  border-radius: 50% 50% 50% 0;
  transform: translateX(-50%) rotate(-45deg);
  border: 2px solid #fff;
}

:deep(.marker-dot) {
  position: absolute;
  top: 7px;
  left: 50%;
  transform: translateX(-50%);
  width: 8px;
  height: 8px;
  background: #fff;
  border-radius: 50%;
  z-index: 1;
}

:deep(.leaflet-control-attribution) {
  font-size: 10px;
}
</style>
