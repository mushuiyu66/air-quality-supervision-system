<template>
  <canvas
    ref="canvasRef"
    class="eco-particle-canvas"
    :style="{ opacity: opacity }"
  />
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'

const props = defineProps({
  // 粒子密度（每万像素的粒子数）
  density: { type: Number, default: 3 },
  // 整体透明度
  opacity: { type: Number, default: 0.7 },
  // 叶子粒子占比 (0-1)
  leafRatio: { type: Number, default: 0.3 },
  // 最大粒子数上限
  maxParticles: { type: Number, default: 80 }
})

const canvasRef = ref(null)
let ctx = null
let particles = []
let animationId = null
let width = 0
let height = 0
let resizeTimer = null

// 绿色调色板
const colors = [
  { r: 76, g: 175, b: 80 },   // #4caf50
  { r: 102, g: 187, b: 106 },  // #66bb6a
  { r: 129, g: 199, b: 132 },  // #81c784
  { r: 165, g: 214, b: 167 },  // #a5d6a7
  { r: 46, g: 125, b: 50 },    // #2e7d32
  { r: 56, g: 142, b: 60 },    // #388e3c
  { r: 0, g: 137, b: 123 },    // #00897b
  { r: 38, g: 166, b: 154 }    // #26a69a
]

// 低碳图标字符池
const ecoIcons = ['♻️', '🌿', '🍃', '🌱', '🌍', '💚', '☘️']

function randomBetween(min, max) {
  return min + Math.random() * (max - min)
}

function randomColor() {
  return colors[Math.floor(Math.random() * colors.length)]
}

// 创建叶子形状路径
function drawLeafShape(ctx, x, y, size, rotation) {
  ctx.save()
  ctx.translate(x, y)
  ctx.rotate(rotation)
  ctx.beginPath()
  ctx.moveTo(0, -size)
  ctx.bezierCurveTo(size * 0.4, -size * 0.5, size * 0.6, 0, 0, size * 0.7)
  ctx.bezierCurveTo(-size * 0.6, 0, -size * 0.4, -size * 0.5, 0, -size)
  ctx.closePath()
  ctx.fill()
  ctx.restore()
}

// 创建圆形光点
function drawDot(ctx, x, y, size) {
  ctx.beginPath()
  ctx.arc(x, y, size, 0, Math.PI * 2)
  ctx.fill()
}

function createParticle() {
  const color = randomColor()
  const isLeaf = Math.random() < props.leafRatio
  const baseSize = randomBetween(3, 10)

  return {
    x: randomBetween(0, width),
    y: randomBetween(-20, height + 20),
    size: baseSize,
    baseSize,
    color,
    isLeaf,
    speedX: randomBetween(-0.15, 0.15),
    speedY: randomBetween(0.2, 0.7),
    opacity: randomBetween(0.15, 0.6),
    rotation: randomBetween(0, Math.PI * 2),
    rotationSpeed: randomBetween(-0.01, 0.01),
    // 呼吸式闪烁
    pulseSpeed: randomBetween(0.005, 0.02),
    pulseOffset: randomBetween(0, Math.PI * 2),
    // 摆动幅度
    swayAmount: randomBetween(0, 1.5),
    swaySpeed: randomBetween(0.003, 0.01),
    swayOffset: randomBetween(0, Math.PI * 2),
    // 图标粒子（小概率）
    icon: !isLeaf && Math.random() < 0.08 ? ecoIcons[Math.floor(Math.random() * ecoIcons.length)] : null
  }
}

function initParticles() {
  if (!width || !height) return

  const area = width * height
  const count = Math.min(
    Math.floor(area / 10000 * props.density),
    props.maxParticles
  )

  particles = []
  for (let i = 0; i < count; i++) {
    particles.push(createParticle())
  }
}

function updateParticle(p) {
  const now = Date.now()

  // Y轴上升
  p.y -= p.speedY

  // X轴摆动
  p.x += p.speedX + Math.sin(now * p.swaySpeed + p.swayOffset) * p.swayAmount * 0.03

  // 旋转
  p.rotation += p.rotationSpeed

  // 超出顶部 → 从底部重生
  if (p.y < -30) {
    p.y = height + 30
    p.x = randomBetween(0, width)
    p.opacity = randomBetween(0.15, 0.6)
  }
  // 超出底部 → 从顶部重生（防止粒子堆积在底部）
  if (p.y > height + 30) {
    p.y = -30
    p.x = randomBetween(0, width)
  }
  // 水平边界循环
  if (p.x < -20) p.x = width + 20
  if (p.x > width + 20) p.x = -20
}

function renderParticle(p) {
  const now = Date.now()

  // 呼吸式闪烁：基于时间的正弦波控制透明度
  const pulse = Math.sin(now * p.pulseSpeed + p.pulseOffset) * 0.25 + 0.75
  const alpha = p.opacity * pulse

  const { r, g, b } = p.color
  ctx.fillStyle = `rgba(${r}, ${g}, ${b}, ${alpha})`

  if (p.icon) {
    // 图标粒子
    ctx.save()
    ctx.translate(p.x, p.y)
    ctx.rotate(p.rotation)
    ctx.font = `${p.size * 1.5}px sans-serif`
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(p.icon, 0, 0)
    ctx.restore()
  } else if (p.isLeaf) {
    drawLeafShape(ctx, p.x, p.y, p.size, p.rotation)
  } else {
    // 添加发光效果
    const gradient = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.size * 1.5)
    gradient.addColorStop(0, `rgba(${r}, ${g}, ${b}, ${alpha})`)
    gradient.addColorStop(0.5, `rgba(${r}, ${g}, ${b}, ${alpha * 0.5})`)
    gradient.addColorStop(1, `rgba(${r}, ${g}, ${b}, 0)`)
    ctx.fillStyle = gradient
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.size * 1.5, 0, Math.PI * 2)
    ctx.fill()

    // 实心核
    ctx.fillStyle = `rgba(${Math.min(r + 50, 255)}, ${Math.min(g + 50, 255)}, ${Math.min(b + 50, 255)}, ${alpha})`
    drawDot(ctx, p.x, p.y, p.size * 0.5)
  }
}

function animate() {
  if (!ctx) return

  ctx.clearRect(0, 0, width, height)

  for (const p of particles) {
    updateParticle(p)
    renderParticle(p)
  }

  animationId = requestAnimationFrame(animate)
}

function resize() {
  if (!canvasRef.value) return
  const canvas = canvasRef.value
  const parent = canvas.parentElement
  if (!parent) return

  width = parent.clientWidth
  height = parent.clientHeight

  const dpr = window.devicePixelRatio || 1
  canvas.width = width * dpr
  canvas.height = height * dpr
  canvas.style.width = width + 'px'
  canvas.style.height = height + 'px'

  ctx = canvas.getContext('2d')
  ctx.scale(dpr, dpr)

  initParticles()
}

function handleResize() {
  clearTimeout(resizeTimer)
  resizeTimer = setTimeout(resize, 150)
}

// 监听密度变化
watch(() => props.density, () => {
  initParticles()
})

onMounted(() => {
  resize()
  animate()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
  clearTimeout(resizeTimer)
})
</script>

<style lang="scss" scoped>
.eco-particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}
</style>
