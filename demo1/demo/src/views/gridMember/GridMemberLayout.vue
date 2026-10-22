<script setup lang="js">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import EcoBackground from '@/components/EcoBackground.vue'
import { 
  Monitor, 
  OfficeBuilding, 
  Document, 
  TrendCharts,
  ArrowDown,
  RefreshRight,
  Check,
  Plus
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = ref('/gridmember/home')

// 侧边栏折叠状态
const isCollapse = ref(false)

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 处理菜单点击
const handleMenuSelect = (key, keyPath) => {
  activeMenu.value = key
  if (key.startsWith('/gridmember')) {
    router.push(key)
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 清除本地存储
    localStorage.removeItem('role')
    localStorage.removeItem('token')
    localStorage.removeItem('gmId')
    userStore.clearInfo()
    
    ElMessage.success('已退出登录')
    router.push('/')
  } catch {
    // 用户取消退出
  }
}
</script>

<template>
  <div class="layout-wrapper">
    <EcoBackground :leaf-count="6" :icon-count="3" />
    <el-container class="gridmember-layout">
      <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <el-button 
          :icon="isCollapse ? 'Expand' : 'Fold'" 
          @click="toggleSidebar"
          class="toggle-btn"
          circle
        />
        <h2 class="system-title"><span class="title-icon">🌿</span> 空气质量监督系统 - 网格员端</h2>
      </div>
      
      <div class="header-right">
        <el-dropdown>
          <span class="user-info">
            <el-icon><OfficeBuilding /></el-icon>
            <span class="username">{{ userStore.info.gmName || '网格员' }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="main-container">
      <!-- 左侧导航菜单 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          @select="handleMenuSelect"
          class="menu"
        >
          <!-- 首页仪表盘 -->
          <el-menu-item index="/gridmember/home">
            <el-icon><Monitor /></el-icon>
            <template #title>首页仪表盘</template>
          </el-menu-item>

          <!-- 工单处理 -->
          <el-menu-item index="/gridmember/tasks">
            <el-icon><Document /></el-icon>
            <template #title>工单处理</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区域 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
  </div>
</template>

<style lang="scss" scoped>
.layout-wrapper {
  position: relative;
  min-height: 100vh;
}

.gridmember-layout {
  position: relative;
  z-index: 1;
  height: 100vh;
  width: 100%;
}

.header {
  background: linear-gradient(135deg, #1b5e20 0%, #2e7d32 100%);
  border-bottom: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 12px rgba(27, 94, 32, 0.3);
  z-index: 10;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .toggle-btn {
      background: rgba(255, 255, 255, 0.15) !important;
      border: none !important;
      color: #fff !important;

      &:hover {
        background: rgba(255, 255, 255, 0.25) !important;
      }
    }

    .system-title {
      margin: 0;
      font-size: 18px;
      color: #fff;
      font-weight: 600;
      letter-spacing: 1px;

      .title-icon {
        font-size: 20px;
        animation: leaf-sway 3s ease-in-out infinite;
        display: inline-block;
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      color: rgba(255, 255, 255, 0.9);
      transition: color 0.3s;
      padding: 6px 12px;
      border-radius: 20px;

      &:hover {
        color: #fff;
        background: rgba(255, 255, 255, 0.15);
      }

      .username {
        font-size: 14px;
      }
    }
  }
}

.main-container {
  height: calc(100vh - 60px);
}

.aside {
  background: linear-gradient(180deg, #ffffff 0%, #f1f8e9 100%);
  border-right: 1px solid #c8e6c9;
  box-shadow: 2px 0 12px rgba(46, 125, 50, 0.08);
  transition: width 0.3s;
  overflow-x: hidden;

  .menu {
    border-right: none;
    height: 100%;
    background: transparent;

    &.el-menu--collapse {
      width: 64px;
    }

    :deep(.el-menu-item) {
      height: 50px;
      line-height: 50px;
      margin: 4px 10px;
      border-radius: 10px;
      transition: all 0.3s;
      color: #2e7d32;

      &:hover {
        background-color: #e8f5e9;
        color: #1b5e20;
      }

      &.is-active {
        background: linear-gradient(135deg, #2e7d32, #4caf50);
        color: #fff;
        box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3);

        .el-icon {
          color: #fff;
        }
      }
    }
  }
}

.main {
  background: #e8f5e9;
  padding: 20px;
  overflow-y: auto;

  :deep(.router-view) {
    height: 100%;
  }
}

// 滚动条样式
.main::-webkit-scrollbar {
  width: 6px;
}

.main::-webkit-scrollbar-track {
  background: #e8f5e9;
  border-radius: 3px;
}

.main::-webkit-scrollbar-thumb {
  background: #81c784;
  border-radius: 3px;

  &:hover {
    background: #4caf50;
  }
}

@keyframes leaf-sway {
  0%, 100% {
    transform: rotate(-5deg);
  }
  50% {
    transform: rotate(5deg);
  }
}
</style>
