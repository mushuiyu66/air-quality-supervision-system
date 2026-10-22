<template>
  <div class="page-container page-fade-in">
    <el-card shadow="never" class="eco-card card-animate">
      <template #header>
        <div class="card-header page-card-header">
          <span class="title">👤 市民监督员管理</span>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="eco-filter-area">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="手机号">
            <el-input
              v-model="filterForm.telId"
              placeholder="请输入手机号"
              clearable
              style="width: 180px"
              maxlength="11"
              @keyup.enter="handleSearch"
            />
          </el-form-item>

          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form>

        <div class="action-bar">
          <el-button type="primary" @click="handleAdd" class="eco-btn">
            <el-icon><Plus /></el-icon>
            新增监督员
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        class="eco-table"
      >
        <el-table-column prop="tel_id" label="手机号" width="130" fixed />
        <el-table-column prop="real_name" label="姓名" width="100" />
        <el-table-column label="性别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.sex === 1 ? 'success' : 'warning'" size="small" effect="light" round>
              {{ row.sex === 1 ? '男' : '女' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="birthday" label="出生日期" width="110" />
        <el-table-column prop="remarks" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{ row }">
            <div class="eco-icon-actions">
              <el-tooltip content="编辑监督员" placement="top" :show-after="300">
                <el-button
                  type="primary"
                  size="small"
                  class="eco-icon-btn edit"
                  @click="handleEdit(row)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="手机号" prop="tel_id" v-if="dialogTitle === '新增监督员'">
          <el-input
            v-model="form.tel_id"
            placeholder="请输入手机号(作为登录账号)"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password" v-if="dialogTitle === '新增监督员'">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword" v-if="dialogTitle === '新增监督员'">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password" v-else-if="dialogTitle === '编辑监督员'">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="不修改则留空"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword" v-else-if="dialogTitle === '编辑监督员'">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="不修改则留空"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="real_name">
          <el-input
            v-model="form.real_name"
            placeholder="请输入真实姓名"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            placeholder="请选择出生日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="form.sex">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="form.remarks"
            type="textarea"
            placeholder="请输入备注信息"
            maxlength="100"
            show-word-limit
            :rows="3"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="js">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus, Edit } from '@element-plus/icons-vue'
import { 
  getSupervisorList, 
  getSupervisorByTel,
  addSupervisor, 
  updateSupervisor 
} from '@/api/supervisor'

// 筛选表单
const filterForm = reactive({
  telId: ''
})

// 列表数据
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 弹窗控制
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

// 表单数据
const form = reactive({
  tel_id: '',
  password: '',
  confirmPassword: '',
  real_name: '',
  birthday: '',
  sex: 1,
  remarks: ''
})

// 验证规则
const rules = {
  tel_id: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  real_name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  birthday: [
    { required: true, message: '请选择出生日期', trigger: 'change' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ]
}

// 获取监督员列表
const fetchSupervisorList = async () => {
  loading.value = true
  try {
    if (filterForm.telId) {
      // 根据手机号查询
      const res = await getSupervisorByTel(filterForm.telId)
      if (res.code === 200) {
        tableData.value = res.data ? [res.data] : []
        total.value = res.data ? 1 : 0
      } else {
        ElMessage.error(res.msg || '查询失败')
        tableData.value = []
        total.value = 0
      }
    } else {
      // 获取所有监督员
      const res = await getSupervisorList()
      if (res.code === 200) {
        tableData.value = res.data || []
        total.value = tableData.value.length
      } else {
        ElMessage.error(res.msg || '获取监督员列表失败')
      }
    }
  } catch (error) {
    console.error('获取监督员列表失败:', error)
    ElMessage.error(error.message || '获取数据失败,请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchSupervisorList()
}

// 重置
const handleReset = () => {
  Object.assign(filterForm, {
    telId: ''
  })
  handleSearch()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchSupervisorList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchSupervisorList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增监督员'
  Object.assign(form, {
    tel_id: '',
    password: '',
    confirmPassword: '',
    real_name: '',
    birthday: '',
    sex: 1,
    remarks: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑监督员'
  Object.assign(form, {
    tel_id: row.tel_id,
    password: '',
    confirmPassword: '',
    real_name: row.real_name,
    birthday: row.birthday,
    sex: row.sex,
    remarks: row.remarks || ''
  })
  dialogVisible.value = true
}

// 关闭弹窗
const handleDialogClose = () => {
  dialogVisible.value = false
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    
    if (dialogTitle.value === '新增监督员') {
      // 新增 - 使用注册接口,需要密码
      const { confirmPassword, ...addData } = form
      const res = await addSupervisor(addData)
      if (res.code === 200) {
        ElMessage.success('新增成功')
        handleDialogClose()
        fetchSupervisorList()
      } else {
        ElMessage.error(res.msg || '新增失败')
      }
    } else {
      // 编辑 - 包含所有字段(包括密码,但密码可能为空)
      const { confirmPassword, ...editData } = form
      // 如果密码为空,则不传递密码字段
      if (!editData.password) {
        delete editData.password
      }
      const res = await updateSupervisor(editData)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        handleDialogClose()
        fetchSupervisorList()
      } else {
        ElMessage.error(res.msg || '更新失败')
      }
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchSupervisorList()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 0;
}

.filter-form {
  margin-bottom: 0;
}

.action-bar {
  margin-left: auto;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
