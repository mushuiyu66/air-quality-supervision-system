<script setup>
import { ref, computed } from 'vue'
import { User, Monitor, Setting } from '@element-plus/icons-vue'

// 身份选项
const identityOptions = [
    { value: 'admin', label: '系统管理员', icon: Setting },
    { value: 'gridMember', label: '网格员', icon: User },
    { value: 'supervisor', label: '公众监督员', icon: Monitor }
]

// 当前选中的身份
const selectedIdentity = ref('admin')

// 控制显示
const showLogin = ref(true)

// 事件发送
const emit = defineEmits(['login', 'register'])

// 处理登录
const handleLogin = () => {
    emit('login', {
        identity: selectedIdentity.value,
        isRegister: false
    })
}

// 处理注册
const handleRegister = () => {
    emit('register', {
        identity: selectedIdentity.value,
        isRegister: true
    })
}

// 切换身份
const changeIdentity = (val) => {
    selectedIdentity.value = val
    showLogin.value = true
}
</script>

<template>
    <div class="identity-selector">
        <!-- 身份选择按钮 -->
        <div class="identity-tabs">
            <el-button
                v-for="option in identityOptions"
                :key="option.value"
                :type="selectedIdentity === option.value ? 'primary' : ''"
                :plain="selectedIdentity !== option.value"
                @click="changeIdentity(option.value)"
                :icon="option.icon"
                class="identity-tab"
            >
                {{ option.label }}
            </el-button>
        </div>

        <!-- 快捷切换 -->
        <div class="quick-switch">
            <el-divider content-position="center">
                <span style="font-size: 12px; color: #909399;">快捷切换</span>
            </el-divider>
            <el-radio-group v-model="selectedIdentity" size="small">
                <el-radio-button 
                    v-for="option in identityOptions" 
                    :key="option.value"
                    :label="option.value"
                >
                    {{ option.label }}
                </el-radio-button>
            </el-radio-group>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.identity-selector {
    .identity-tabs {
        display: flex;
        gap: 10px;
        justify-content: center;
        margin-bottom: 20px;
        padding: 15px;
        background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%);
        border-radius: 12px;

        .identity-tab {
            flex: 1;
            max-width: 140px;
            transition: all 0.3s ease;

            &:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
            }
        }
    }

    .quick-switch {
        margin-top: 10px;

        :deep(.el-divider__text) {
            font-weight: normal;
        }
    }
}
</style>
