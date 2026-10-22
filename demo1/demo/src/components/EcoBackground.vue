<template>
  <div class="eco-background">
    <!-- 底部渐变绿色山峦剪影 -->
    <div class="mountain mountain-1"></div>
    <div class="mountain mountain-2"></div>
    <div class="mountain mountain-3"></div>
    
    <!-- 角落植物装饰 -->
    <div class="plant plant-left">
      <div class="leaf leaf-1"></div>
      <div class="leaf leaf-2"></div>
      <div class="leaf leaf-3"></div>
    </div>
    <div class="plant plant-right">
      <div class="leaf leaf-1"></div>
      <div class="leaf leaf-2"></div>
      <div class="leaf leaf-3"></div>
    </div>
    
    <!-- 漂浮叶子 -->
    <div 
      v-for="n in leafCount" 
      :key="`leaf-${n}`"
      class="floating-leaf"
      :style="getLeafStyle(n)"
    >
      {{ leaves[n % leaves.length] }}
    </div>
    
    <!-- 低碳环保图标 -->
    <div 
      v-for="n in iconCount" 
      :key="`icon-${n}`"
      class="eco-icon"
      :style="getIconStyle(n)"
    >
      {{ icons[n % icons.length] }}
    </div>
  </div>
</template>

<script setup>
defineProps({
  leafCount: {
    type: Number,
    default: 8
  },
  iconCount: {
    type: Number,
    default: 4
  }
})

const leaves = ['🍃', '🌿', '🍀', '🌱']
const icons = ['♻️', '🌏', '☘️', '🌲']

const getLeafStyle = (n) => {
  const left = 5 + (n * 13) % 90
  const delay = (n * 0.7) % 5
  const duration = 12 + (n * 2) % 10
  const scale = 0.6 + (n % 5) * 0.15
  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    transform: `scale(${scale})`,
    opacity: 0.25 + (n % 3) * 0.1
  }
}

const getIconStyle = (n) => {
  const top = 15 + (n * 18) % 70
  const left = 8 + (n * 23) % 85
  const delay = (n * 1.3) % 6
  return {
    top: `${top}%`,
    left: `${left}%`,
    animationDelay: `${delay}s`
  }
}
</script>

<style scoped>
.eco-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

/* 底部山峦剪影 */
.mountain {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 180px;
  background: linear-gradient(135deg, rgba(46, 125, 50, 0.06), rgba(129, 199, 132, 0.04));
  border-radius: 50% 50% 0 0 / 100% 100% 0 0;
  transform-origin: bottom;
}

.mountain-1 {
  height: 140px;
  transform: translateX(-20%) scaleX(1.4);
  background: linear-gradient(135deg, rgba(27, 94, 32, 0.05), rgba(46, 125, 50, 0.02));
}

.mountain-2 {
  height: 100px;
  transform: translateX(30%) scaleX(1.2);
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.04), rgba(129, 199, 132, 0.02));
}

.mountain-3 {
  height: 70px;
  transform: translateX(-10%) scaleX(1.1);
  background: linear-gradient(135deg, rgba(0, 137, 123, 0.03), rgba(76, 175, 80, 0.02));
}

/* 角落植物 */
.plant {
  position: absolute;
  bottom: 0;
  width: 120px;
  height: 160px;
  opacity: 0.08;
}

.plant-left {
  left: -20px;
}

.plant-right {
  right: -20px;
  transform: scaleX(-1);
}

.leaf {
  position: absolute;
  bottom: 0;
  width: 24px;
  height: 80px;
  background: linear-gradient(180deg, #2e7d32, #1b5e20);
  border-radius: 50% 50% 50% 50% / 80% 80% 20% 20%;
  transform-origin: bottom center;
}

.leaf-1 {
  left: 30px;
  height: 100px;
  transform: rotate(-12deg);
}

.leaf-2 {
  left: 55px;
  height: 130px;
  transform: rotate(0deg);
}

.leaf-3 {
  left: 75px;
  height: 90px;
  transform: rotate(14deg);
}

/* 漂浮叶子动画 */
.floating-leaf {
  position: absolute;
  top: -40px;
  font-size: 18px;
  animation: leaf-fall linear infinite;
  filter: drop-shadow(0 2px 4px rgba(46, 125, 50, 0.1));
}

@keyframes leaf-fall {
  0% {
    transform: translateY(-40px) translateX(0) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 0.6;
  }
  90% {
    opacity: 0.4;
  }
  100% {
    transform: translateY(110vh) translateX(50px) rotate(360deg);
    opacity: 0;
  }
}

/* 环保图标浮动 */
.eco-icon {
  position: absolute;
  font-size: 22px;
  opacity: 0.12;
  animation: icon-float 8s ease-in-out infinite;
}

@keyframes icon-float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  33% {
    transform: translateY(-15px) rotate(8deg);
  }
  66% {
    transform: translateY(8px) rotate(-5deg);
  }
}

/* 页面容器需要设置 position: relative; z-index: 1 才能覆盖背景 */
</style>
