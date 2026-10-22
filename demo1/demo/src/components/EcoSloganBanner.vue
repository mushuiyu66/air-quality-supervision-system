<template>
  <div class="eco-slogan-banner" :style="{ height: height + 'px' }">
    <!-- 粒子画布层 -->
    <EcoParticleBackground
      :density="5"
      :opacity="0.85"
      :leaf-ratio="0.4"
      :max-particles="60"
    />

    <!-- 装饰元素 -->
    <div class="banner-decorations">
      <div class="decor-mountain mountain-left"></div>
      <div class="decor-mountain mountain-right"></div>
      <div class="decor-leaf leaf-1">🌿</div>
      <div class="decor-leaf leaf-2">🍃</div>
      <div class="decor-leaf leaf-3">🌱</div>
      <div class="decor-sun"></div>
    </div>

    <!-- 标语内容 -->
    <div class="banner-content">
      <div class="slogan-wrapper">
        <Transition name="slogan-fade" mode="out-in">
          <h1 class="slogan-text" :key="currentIndex">
            <span class="slogan-icon">{{ slogans[currentIndex]?.icon || '🌍' }}</span>
            {{ slogans[currentIndex]?.text || '' }}
          </h1>
        </Transition>
        <Transition name="subtitle-fade" mode="out-in">
          <p class="slogan-subtitle" :key="currentIndex">
            {{ slogans[currentIndex]?.subtitle || '' }}
          </p>
        </Transition>
      </div>

      <!-- 标语切换指示器 -->
      <div class="slogan-dots">
        <span
          v-for="(item, idx) in slogans"
          :key="idx"
          class="slogan-dot"
          :class="{ active: idx === currentIndex }"
          @click="switchTo(idx)"
        ></span>
      </div>
    </div>

    <!-- 底部波浪装饰 -->
    <div class="banner-wave">
      <svg viewBox="0 0 1440 120" preserveAspectRatio="none">
        <path
          class="wave-path wave-1"
          d="M0,40 C240,80 480,0 720,40 C960,80 1200,0 1440,40 L1440,120 L0,120 Z"
        />
        <path
          class="wave-path wave-2"
          d="M0,60 C320,20 640,80 960,50 C1120,30 1280,70 1440,50 L1440,120 L0,120 Z"
        />
        <path
          class="wave-path wave-3"
          d="M0,80 C200,60 400,100 720,80 C1040,60 1200,100 1440,80 L1440,120 L0,120 Z"
        />
      </svg>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import EcoParticleBackground from './EcoParticleBackground.vue'

const props = defineProps({
  // 自定义标语列表
  slogans: {
    type: Array,
    default: () => [
      {
        icon: '🌍',
        text: '低碳生活，绿色未来',
        subtitle: '每一次监督，都是对蓝天的守护'
      },
      {
        icon: '🌿',
        text: '绿水青山就是金山银山',
        subtitle: '共建生态文明，共享美丽家园'
      },
      {
        icon: '💚',
        text: '节能减排，人人有责',
        subtitle: '空气质量监督，让每一次呼吸都清新'
      },
      {
        icon: '🌱',
        text: '守护蓝天，从我做起',
        subtitle: '环保监督，共建美好生活环境'
      },
      {
        icon: '🍃',
        text: '清新空气，健康生活',
        subtitle: '科学监测，精准治理，持续改善'
      }
    ]
  },
  // 切换间隔（毫秒）
  interval: { type: Number, default: 5000 },
  // 横幅高度（px）
  height: { type: Number, default: 200 }
})

const currentIndex = ref(0)
let timer = null

function nextSlogan() {
  currentIndex.value = (currentIndex.value + 1) % props.slogans.length
}

function switchTo(index) {
  currentIndex.value = index
  resetTimer()
}

function resetTimer() {
  clearInterval(timer)
  timer = setInterval(nextSlogan, props.interval)
}

onMounted(() => {
  timer = setInterval(nextSlogan, props.interval)
})

onBeforeUnmount(() => {
  clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.eco-slogan-banner {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg,
    #1b5e20 0%,
    #2e7d32 15%,
    #388e3c 30%,
    #43a047 45%,
    #4caf50 60%,
    #00897b 80%,
    #00695c 100%
  );
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  box-shadow:
    0 8px 32px rgba(27, 94, 32, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

// 装饰元素
.banner-decorations {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.decor-mountain {
  position: absolute;
  bottom: 0;

  &.mountain-left {
    left: -10%;
    width: 30%;
    height: 60%;
    background: linear-gradient(135deg,
      rgba(255, 255, 255, 0.04) 0%,
      rgba(255, 255, 255, 0.01) 100%
    );
    clip-path: polygon(0% 100%, 20% 20%, 50% 45%, 80% 30%, 100% 100%);
  }

  &.mountain-right {
    right: -5%;
    width: 25%;
    height: 50%;
    background: linear-gradient(135deg,
      rgba(255, 255, 255, 0.05) 0%,
      rgba(255, 255, 255, 0.01) 100%
    );
    clip-path: polygon(0% 50%, 30% 25%, 60% 40%, 100% 15%, 100% 100%, 0% 100%);
  }
}

.decor-leaf {
  position: absolute;
  font-size: 28px;
  opacity: 0.3;
  animation: leaf-drift 8s ease-in-out infinite;

  &.leaf-1 {
    top: 15%;
    left: 8%;
    animation-delay: 0s;
  }

  &.leaf-2 {
    top: 60%;
    right: 12%;
    font-size: 22px;
    animation-delay: -3s;
  }

  &.leaf-3 {
    top: 25%;
    right: 35%;
    font-size: 20px;
    animation-delay: -6s;
  }
}

.decor-sun {
  position: absolute;
  top: -40px;
  right: -40px;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: radial-gradient(circle,
    rgba(255, 255, 255, 0.12) 0%,
    rgba(255, 255, 255, 0.05) 40%,
    transparent 70%
  );
  animation: sun-pulse 4s ease-in-out infinite;
}

@keyframes leaf-drift {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-8px) rotate(5deg); }
  50% { transform: translateY(-3px) rotate(-3deg); }
  75% { transform: translateY(-10px) rotate(2deg); }
}

@keyframes sun-pulse {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.08); opacity: 1; }
}

// 标语内容
.banner-content {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 0 40px;
}

.slogan-wrapper {
  margin-bottom: 16px;
}

.slogan-text {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
  text-shadow:
    0 2px 8px rgba(0, 0, 0, 0.3),
    0 1px 3px rgba(0, 0, 0, 0.2);
  letter-spacing: 0.05em;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  line-height: 1.4;

  .slogan-icon {
    font-size: 32px;
    display: inline-block;
    animation: icon-bounce 2s ease-in-out infinite;
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
  }
}

.slogan-subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 400;
  letter-spacing: 0.08em;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
}

// 指示器
.slogan-dots {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 4px;
}

.slogan-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.35);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:hover {
    background: rgba(255, 255, 255, 0.7);
  }

  &.active {
    width: 24px;
    border-radius: 4px;
    background: #ffffff;
    box-shadow: 0 0 8px rgba(255, 255, 255, 0.4);
  }
}

// 标语切换过渡
.slogan-fade-enter-active,
.slogan-fade-leave-active {
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.slogan-fade-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slogan-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.subtitle-fade-enter-active,
.subtitle-fade-leave-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.subtitle-fade-enter-from {
  opacity: 0;
  transform: translateX(10px);
}

.subtitle-fade-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

@keyframes icon-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

// 波浪装饰
.banner-wave {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  z-index: 1;
  pointer-events: none;

  svg {
    width: 100%;
    height: 100%;
  }

  .wave-path {
    fill: rgba(255, 255, 255, 0.06);
  }

  .wave-1 {
    animation: wave-move 6s ease-in-out infinite;
  }
  .wave-2 {
    animation: wave-move 8s ease-in-out infinite reverse;
    fill: rgba(255, 255, 255, 0.08);
  }
  .wave-3 {
    animation: wave-move 10s ease-in-out infinite;
    fill: rgba(255, 255, 255, 0.04);
  }
}

@keyframes wave-move {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(-2%); }
}

// 响应式
@media (max-width: 768px) {
  .eco-slogan-banner {
    height: 160px !important;
  }
  .slogan-text {
    font-size: 20px;

    .slogan-icon {
      font-size: 24px;
    }
  }
  .slogan-subtitle {
    font-size: 12px;
  }
}
</style>
