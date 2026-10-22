<script setup lang="js">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { Check, UserFilled, Avatar, Plus } from '@element-plus/icons-vue'
import { adminLogin } from '@/api/admin'
import { gmLogin } from '@/api/gridMember'
import { supervisorLogin, supervisorRegister } from '@/api/supervisor'
import { useUserStore } from '@/stores/user'
import FireflyBackground from '@/components/FireflyBackground.vue'
const userstore = useUserStore()



const router = useRouter()

// 身份标识 admin / grid / supervisor
const userType = ref('')
// 市民登录注册切换tab
const superTab = ref('login')

// ==================== 表单定义 ====================
// 管理员表单
const adminRef = ref()
const adminForm = ref({
  adminCode: '',
  password: ''
})

// 网格员表单
const gridRef = ref()
const gridForm = ref({
  gmCode: '',
  password: ''
})

// 市民登录表单
const superLoginRef = ref()
const superLoginForm = ref({
  telId: '',
  password: ''
})

// 市民注册表单(匹配supervisor数据表字段)
const superRegRef = ref()
const superRegForm = ref({
  telId: '',
  password: '',
  realName: '',
  sex: 1,
  birthday: '',
  remarks: ''
})

// ==================== 工具函数 ====================
// 切换身份清空所有表单数据
watch(userType, () => {
  adminForm.value = { adminCode: '', password: '' }
  gridForm.value = { gmCode: '', password: '' }
  superLoginForm.value = { telId: '', password: '' }
  superRegForm.value = { telId: '', password: '', realName: '', sex: 1, birthday: '', remarks: '' }
  superTab.value = 'login'
})

// ==================== 登录注册逻辑 ====================

// 管理员登录
const adminLoginHandler = async () => {
  const { adminCode, password } = adminForm.value
  if (!adminCode || !password) {
    return ElMessage.warning('账号密码不能为空')
  }
  
  try {
    console.log('[管理员登录] 开始请求，参数:', JSON.stringify(adminForm.value))
    const res = await adminLogin(adminForm.value)
    console.log('[管理员登录] 收到响应:', JSON.stringify(res, null, 2))
    
    // 严格的响应验证
    if (!res) {
      return ElMessage.error('登录失败：服务器响应异常')
    }
    if (res.code === 200) {
      ElMessage.success('管理员登录成功')
      console.log('[管理员登录] 完整响应数据:', JSON.stringify(res, null, 2))
      
      // 不使用 token，仅保存角色和后端返回的用户信息
      // 重要:必须在跳转前设置localStorage,否则路由守卫会拦截
      localStorage.setItem('role', 'admin')
      localStorage.setItem('token', res.data.token || '')
      // pinia 记录管理员信息
      userstore.setInfo({
        role: 'admin',
        ...res.data
      })
      console.log('[管理员登录] 角色已设置:', localStorage.getItem('role'))
      
      // 确保localStorage已设置后再跳转
      setTimeout(() => {
        router.push('/admin/home')
      }, 100)
    } else {
      // 账号或密码错误
      ElMessage.error(res.message || res.msg || '账号或密码错误，请检查后重试')
    }
  } catch (error) {
    console.error('[管理员登录] 错误详情:', error)
    console.error('[管理员登录] 错误响应:', error.response)
    const errorMsg = error.message || error?.response?.data?.msg || '登录失败，请检查网络连接或稍后重试'
    ElMessage.error(errorMsg)
  }
}

// 网格员登录
const gridLoginHandler = async () => {
  const { gmCode, password } = gridForm.value
  if (!gmCode || !password) {
    return ElMessage.warning('账号密码不能为空')
  }
  
  try {
    console.log('[网格员登录] 开始请求，参数:', JSON.stringify(gridForm.value))
    const res = await gmLogin(gridForm.value)
    console.log('[网格员登录] 收到响应:', JSON.stringify(res, null, 2))
    
    // 严格的响应验证
    if (!res) {
      return ElMessage.error('登录失败:服务器响应异常')
    }
    
    // 检查外层响应状态
    if (res.code !== 200) {
      return ElMessage.error(res.message || res.msg || '登录失败,请检查网络连接')
    }
    
    // 检查内层业务数据状态(后端返回嵌套结构)
    const businessData = res.data
    if (!businessData) {
      return ElMessage.error('登录失败:未获取到用户信息')
    }
    
    // 如果内层有code字段,需要检查业务状态码
    if (businessData.code && businessData.code !== 200) {
      return ElMessage.error(businessData.message || '账号或密码错误,请检查后重试')
    }
    
    // 登录成功
    ElMessage.success('网格员登录成功')
    console.log('[网格员登录] 完整响应数据:', JSON.stringify(res, null, 2))
    
    // 不使用 token,仅保存角色和后端返回的用户信息
    // 重要:必须在跳转前设置localStorage,否则路由守卫会拦截
    localStorage.setItem('role', 'grid')
    
    // 保存网格员ID到localStorage(供Dashboard.vue使用)
    // 注意:后端返回嵌套结构,res.data.data才是实际用户数据
    const userData = businessData.data || businessData
    const gmId = userData.gmId || userData.id
    if (gmId) {
      localStorage.setItem('gmId', gmId)
      console.log('[网格员登录] gmId已设置:', gmId)
    } else {
      console.warn('[网格员登录] 警告:后端返回数据中没有gmId字段!', userData)
    }
    if (userData.token) {
      localStorage.setItem('token', userData.token)
    }
    
    // pinia 记录网格员信息(保存实际用户数据)
    userstore.setInfo({
      role: 'grid',
      ...userData
    })
    console.log('[网格员登录] 角色已设置:', localStorage.getItem('role'))
    console.log('[网格员登录] Pinia store:', userstore.info)
    
    // 确保localStorage已设置后再跳转
    setTimeout(() => {
      router.push('/gridmember/home')
    }, 100)
  } catch (error) {
    console.error('网格员登录错误:', error)
    const errorMsg = error.message || error?.response?.data?.msg || '登录失败,请检查网络连接或稍后重试'
    ElMessage.error(errorMsg)
  }
}

// 市民登录
const superLoginHandler = async () => {
  const { telId, password } = superLoginForm.value
  if (!telId || !password) {
    return ElMessage.warning('手机号和密码不能为空')
  }
  
  try {
    console.log('[市民登录] 开始请求，参数:', JSON.stringify(superLoginForm.value))
    const res = await supervisorLogin(superLoginForm.value)
    console.log('[市民登录] 收到响应:', JSON.stringify(res, null, 2))
    
    // 严格的响应验证
    if (!res) {
      return ElMessage.error('登录失败：服务器响应异常')
    }
    
    // 检查外层响应状态
    if (res.code !== 200) {
      return ElMessage.error(res.message || res.msg || '登录失败,请检查网络连接')
    }
    
    // 检查内层业务数据状态(后端返回嵌套结构)
    const businessData = res.data
    if (!businessData) {
      return ElMessage.error('登录失败:未获取到用户信息')
    }
    
    // 如果内层有code字段,需要检查业务状态码(处理后端返回嵌套结构的问题)
    if (businessData.code && businessData.code !== 200) {
      // 内层code不为200,表示业务失败(如用户不存在、密码错误等)
      return ElMessage.error(businessData.message || '手机号或密码错误,请检查后重试')
    }
    
    // 登录成功
    ElMessage.success('市民登录成功')
    
    // 不使用 token，仅保存角色和后端返回的用户信息
    // 重要:必须在跳转前设置localStorage,否则路由守卫会拦截
    localStorage.setItem('role', 'supervisor')
    const userData = businessData.data || businessData
    if (userData.token) {
      localStorage.setItem('token', userData.token)
    }
    
    // pinia 记录市民监督员信息(保存实际用户数据)
    userstore.setInfo({
      role: 'supervisor',
      ...userData
    })
    console.log('[市民登录] 角色已设置:', localStorage.getItem('role'))
    console.log('[市民登录] Pinia store:', userstore.info)
    
    // 确保localStorage已设置后再跳转
    setTimeout(() => {
      router.push('/supervisorfeedback')
    }, 100)
  } catch (error) {
    console.error('市民登录错误:', error)
    let errorMsg = '登录失败，请稍后重试'
    
    // 判断错误类型
    if (error.response?.status === 404) {
      errorMsg = '登录接口不存在，请联系管理员确认后端服务配置'
    } else if (error.code === 'ERR_NETWORK' || !error.response) {
      errorMsg = '网络连接失败，请检查后端服务是否启动及CORS配置'
    } else if (error.message) {
      errorMsg = error.message
    }
    
    ElMessage.error(errorMsg)
  }
}

// 市民注册
const superRegisterHandler = async () => {
  const { telId, password, realName, birthday, remarks } = superRegForm.value
  
  // 表单验证
  if (!telId || !password || !realName || !birthday) {
    return ElMessage.warning('请填写完整注册信息')
  }
  
  // 手机号正则校验 (中国手机号为11位: 1 + [3-9] + [0-9]{9})
  if (!/^1[3-9][0-9]{9}$/.test(telId)) {
    return ElMessage.warning('手机号格式不正确')
  }
  
  try {
    // 构造注册数据(按照后端Supervisor实体字段)
    const registerData = {
      telId: telId,
      password: password,
      realName: realName,
      sex: superRegForm.value.sex,
      birthday: birthday,
      remarks: remarks || ''
    }
    
    const res = await supervisorRegister(registerData)
    console.log('[市民注册] 完整响应数据:', JSON.stringify(res, null, 2))
    
    // 严格的响应验证
    if (!res) {
      return ElMessage.error('注册失败：服务器响应异常')
    }
    
    // 检查外层响应状态
    if (res.code !== 200) {
      return ElMessage.error(res.msg || '注册失败,请检查网络连接')
    }
    
    // 检查内层业务数据状态(后端返回嵌套结构)
    const businessData = res.data
    if (!businessData) {
      return ElMessage.error('注册失败:未获取到响应数据')
    }
    
    // 如果内层有code字段,需要检查业务状态码
    if (businessData.code && businessData.code !== 200) {
      return ElMessage.error(businessData.message || '注册失败,请稍后重试')
    }
    
    // 注册成功
    ElMessage.success('注册成功,请登录')
    superTab.value = 'login'
    superLoginForm.value.telId = telId // 自动填充手机号
  } catch (error) {
    console.error('市民注册错误:', error)
    let errorMsg = '注册失败,请稍后重试'
    
    // 判断错误类型
    if (error.response?.status === 404) {
      errorMsg = '注册接口不存在，请联系管理员确认后端服务配置'
    } else if (error.code === 'ERR_NETWORK' || !error.response) {
      errorMsg = '网络连接失败，请检查后端服务是否启动及CORS配置'
    } else if (error.message) {
      errorMsg = error.message
    }
    
    ElMessage.error(errorMsg)
  }
}
</script>
<template>
  <div class="login-container">
    <!-- 萤火虫动态背景 -->
    <FireflyBackground :count="35" :leaf-count="8" />

    <!-- 环保装饰圆 -->
    <div class="eco-circle eco-circle--1"></div>
    <div class="eco-circle eco-circle--2"></div>
    <div class="eco-circle eco-circle--3"></div>

    <div class="login-card">
      <div class="logo-area">
        <span class="logo-icon">🌿</span>
        <h2 class="title">空气质量监督系统</h2>
        <p class="subtitle">守护蓝天白云 · 共建绿色家园</p>
      </div>

      <!-- 身份选择下拉 -->
      <el-select
        v-model="userType"
        placeholder="请选择登录身份"
        size="large"
        class="user-type-select"
      >
        <el-option label="系统管理员" value="admin"></el-option>
        <el-option label="网格员" value="grid"></el-option>
        <el-option label="市民监督员" value="supervisor"></el-option>
      </el-select>

      <!-- 1、管理员登录表单 -->
      <div v-if="userType === 'admin'" class="form-wrap">
        <el-form ref="adminRef" :model="adminForm" label-width="90px">
          <el-form-item label="管理员账号">
            <el-input v-model="adminForm.adminCode" placeholder="输入adminCode"></el-input>
          </el-form-item>
          <el-form-item label="登录密码">
            <el-input v-model="adminForm.password" type="password" placeholder="输入密码"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              class="submit-btn" 
              @click="adminLoginHandler"
              :icon="Check"
            >
              管理员登录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="tip">*管理员账号由后台创建，前端无注册功能</div>
      </div>

      <!-- 2、网格员登录表单 -->
      <div v-if="userType === 'grid'" class="form-wrap">
        <el-form ref="gridRef" :model="gridForm" label-width="90px">
          <el-form-item label="网格员账号">
            <el-input v-model="gridForm.gmCode" placeholder="输入gmCode"></el-input>
          </el-form-item>
          <el-form-item label="登录密码">
            <el-input v-model="gridForm.password" type="password" placeholder="输入密码"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              class="submit-btn" 
              @click="gridLoginHandler"
              :icon="UserFilled"
            >
              网格员登录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="tip">*网格员账号由后台统一录入，前端无注册</div>
      </div>

      <!-- 3、市民监督员：登录/注册切换 -->
      <div v-if="userType === 'supervisor'" class="form-wrap">
        <el-radio-group v-model="superTab" class="tab-radio">
          <el-radio label="login">市民登录</el-radio>
          <el-radio label="register">市民注册</el-radio>
        </el-radio-group>

        <!-- 市民登录 -->
        <el-form v-if="superTab === 'login'" ref="superLoginRef" :model="superLoginForm" label-width="90px">
          <el-form-item label="手机号">
            <el-input v-model="superLoginForm.telId" placeholder="手机号作为登录账号"></el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="superLoginForm.password" type="password"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              class="submit-btn" 
              @click="superLoginHandler"
              :icon="Avatar"
            >
              市民登录
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 市民注册 -->
        <el-form v-if="superTab === 'register'" ref="superRegRef" :model="superRegForm" label-width="90px">
          <el-form-item label="手机号">
            <el-input v-model="superRegForm.telId" placeholder="唯一登录账号"></el-input>
          </el-form-item>
          <el-form-item label="登录密码">
            <el-input v-model="superRegForm.password" type="password"></el-input>
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input v-model="superRegForm.realName"></el-input>
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="superRegForm.sex">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="0">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="生日">
            <el-date-picker
              v-model="superRegForm.birthday"
              type="date"
              placeholder="选择出生日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              v-model="superRegForm.remarks"
              type="textarea"
              :rows="3"
              placeholder="选填，可填写备注信息"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="success" 
              size="large" 
              class="submit-btn" 
              @click="superRegisterHandler"
              :icon="Plus"
            >
              完成注册
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>



<style lang="scss" scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #1b5e20 0%, #2e7d32 30%, #00897b 70%, #00695c 100%);
  background-size: 200% 200%;
  animation: gradient-shift 15s ease infinite;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;

  // 环保装饰圆 - 模糊光斑
  .eco-circle {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    opacity: 0.3;
    pointer-events: none;
    z-index: 0;

    &--1 {
      width: 400px;
      height: 400px;
      background: radial-gradient(circle, #81c784, transparent);
      top: -100px;
      left: -100px;
      animation: eco-breathe 8s ease-in-out infinite;
    }

    &--2 {
      width: 300px;
      height: 300px;
      background: radial-gradient(circle, #4caf50, transparent);
      bottom: -80px;
      right: -50px;
      animation: eco-breathe 10s ease-in-out infinite 2s;
    }

    &--3 {
      width: 250px;
      height: 250px;
      background: radial-gradient(circle, #a5d6a7, transparent);
      top: 40%;
      right: 15%;
      animation: eco-breathe 12s ease-in-out infinite 1s;
    }
  }

  .login-card {
    width: 440px;
    padding: 40px 36px;
    background: rgba(255, 255, 255, 0.92);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    box-shadow:
      0 8px 40px rgba(0, 0, 0, 0.2),
      0 0 0 1px rgba(255, 255, 255, 0.3);
    position: relative;
    z-index: 2;
    animation: card-fade-in 0.6s ease-out;

    .logo-area {
      text-align: center;
      margin-bottom: 28px;

      .logo-icon {
        font-size: 42px;
        display: block;
        margin-bottom: 8px;
        filter: drop-shadow(0 2px 8px rgba(76, 175, 80, 0.4));
        animation: leaf-sway 3s ease-in-out infinite;
      }
    }

    .title {
      text-align: center;
      margin: 0 0 6px 0;
      color: #1b5e20;
      font-size: 24px;
      font-weight: 700;
      letter-spacing: 1px;
    }

    .subtitle {
      text-align: center;
      margin: 0 0 24px 0;
      color: #4caf50;
      font-size: 13px;
      letter-spacing: 2px;
    }

    .user-type-select {
      width: 100%;
      margin-bottom: 24px;
    }

    .form-wrap {
      .tab-radio {
        margin-bottom: 16px;
      }

      .submit-btn {
        width: 100%;
        height: 46px;
        font-size: 16px;
        font-weight: 600;
        border-radius: 10px;
        border: none;
        background: linear-gradient(135deg, #2e7d32, #4caf50);
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(46, 125, 50, 0.4);
          background: linear-gradient(135deg, #1b5e20, #2e7d32);
        }

        &:active {
          transform: translateY(0);
        }
      }
    }

    .tip {
      margin-top: 12px;
      font-size: 13px;
      color: #81c784;
      text-align: center;
    }
  }
}

@keyframes card-fade-in {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
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