<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'

const props = defineProps({
  // 萤火虫数量
  count: {
    type: Number,
    default: 30
  },
  // 叶子数量
  leafCount: {
    type: Number,
    default: 6
  },
  // 是否显示叶子
  showLeaves: {
    type: Boolean,
    default: true
  }
})

const fireflies = ref([])
const leaves = ref([])

// 萤火虫随机生成
const generateFireflies = () => {
  const arr = []
  for (let i = 0; i < props.count; i++) {
    arr.push({
      id: i,
      left: Math.random() * 100,
      top: Math.random() * 100,
      size: Math.random() > 0.7 ? 'lg' : 'sm',
      blinkDuration: 2 + Math.random() * 4,      // 2~6秒 闪烁周期
      blinkDelay: Math.random() * 5,              // 随机延迟
      floatDuration: 6 + Math.random() * 8,      // 6~14秒 漂浮周期
      floatDelay: Math.random() * 3,
      hue: Math.random() > 0.5 ? 'yellow' : 'green'
    })
  }
  fireflies.value = arr
}

// 叶子随机生成
const generateLeaves = () => {
  const leafEmojis = ['🍃', '🌿', '🍂']
  const arr = []
  for (let i = 0; i < props.leafCount; i++) {
    arr.push({
      id: i,
      left: Math.random() * 100,
      emoji: leafEmojis[Math.floor(Math.random() * leafEmojis.length)],
      size: 14 + Math.random() * 10,
      duration: 12 + Math.random() * 10,
      delay: Math.random() * 15
    })
  }
  leaves.value = arr
}

onMounted(() => {
  generateFireflies()
  if (props.showLeaves) generateLeaves()
})
</script>

<template>
  <div class="firefly-layer" aria-hidden="true">
    <!-- 萤火虫 -->
    <div
      v-for="f in fireflies"
      :key="'ff-' + f.id"
      class="firefly-wrap"
      :style="{
        left: f.left + '%',
        top: f.top + '%'
      }"
    >
      <div
        class="firefly-inner"
        :class="{ 'firefly--lg': f.size === 'lg', 'firefly--green': f.hue === 'green' }"
        :style="{
          animationDuration: f.blinkDuration + 's, ' + f.floatDuration + 's',
          animationDelay: f.blinkDelay + 's, ' + f.floatDelay + 's'
        }"
      ></div>
    </div>

    <!-- 飘落叶子 -->
    <div
      v-for="l in leaves"
      :key="'leaf-' + l.id"
      class="leaf-particle"
      :style="{
        left: l.left + '%',
        fontSize: l.size + 'px',
        animationDuration: l.duration + 's',
        animationDelay: l.delay + 's'
      }"
    >
      {{ l.emoji }}
    </div>
  </div>
</template>

<style scoped>
.firefly-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}

.firefly-wrap {
  position: absolute;
}

.firefly-inner {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #e8ff5e;
  box-shadow:
    0 0 6px 2px rgba(232, 255, 94, 0.8),
    0 0 12px 4px rgba(174, 243, 89, 0.4);
  animation-name: firefly-blink, firefly-float;
  animation-iteration-count: infinite, infinite;
  animation-timing-function: ease-in-out, ease-in-out;
}

.firefly-inner.firefly--lg {
  width: 6px;
  height: 6px;
  box-shadow:
    0 0 8px 3px rgba(232, 255, 94, 0.9),
    0 0 16px 6px rgba(174, 243, 89, 0.5),
    0 0 24px 10px rgba(174, 243, 89, 0.2);
}

.firefly-inner.firefly--green {
  background: #aef359;
  box-shadow:
    0 0 6px 2px rgba(174, 243, 89, 0.8),
    0 0 12px 4px rgba(129, 199, 132, 0.4);
}

.firefly-inner.firefly--lg.firefly--green {
  box-shadow:
    0 0 8px 3px rgba(174, 243, 89, 0.9),
    0 0 16px 6px rgba(129, 199, 132, 0.5),
    0 0 24px 10px rgba(129, 199, 132, 0.2);
}
</style>
