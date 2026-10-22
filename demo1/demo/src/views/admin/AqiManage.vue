<template>
  <div class="page-container page-fade-in">
    <el-card shadow="never" class="eco-card card-animate">
      <template #header>
        <div class="card-header page-card-header">
          <span class="title">🌱 AQI标准管理</span>
          <el-button type="primary" @click="handleAdd" class="eco-btn">
            <el-icon><Plus /></el-icon>
            新增标准
          </el-button>
        </div>
      </template>

      <!-- 操作提示 -->
      <el-alert
        title="🌿 环保提示"
        description="AQI标准是空气质量评价的基础数据，请谨慎修改，确保与环保部门发布标准一致。"
        type="success"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      />

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        class="eco-table"
      >
        <el-table-column prop="aqi_id" label="编号" width="60" align="center" />
        <el-table-column prop="chinese_explain" label="级别" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :style="{ color: row.color, borderColor: row.color + '60' }">{{ row.chinese_explain }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aqi_explain" label="指数描述" min-width="110" />
        <el-table-column label="颜色标识" width="110" align="center">
          <template #default="{ row }">
            <div class="color-display">
              <span 
                class="color-block" 
                :style="{ backgroundColor: row.color }"
              ></span>
              <span class="color-value">{{ row.color }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="health_impact" label="健康影响" min-width="160" show-overflow-tooltip />
        <el-table-column prop="take_steps" label="建议措施" min-width="180" show-overflow-tooltip />
        
        <!-- 浓度范围列 -->
        <el-table-column label="SO₂" width="110" align="center">
          <template #default="{ row }">
            <span class="range-value">{{ row.so2_min }} - {{ row.so2_max }}</span>
          </template>
        </el-table-column>
        <el-table-column label="CO" width="100" align="center">
          <template #default="{ row }">
            <span class="range-value">{{ row.co_min }} - {{ row.co_max }}</span>
          </template>
        </el-table-column>
        <el-table-column label="PM2.5" width="110" align="center">
          <template #default="{ row }">
            <span class="range-value">{{ row.spm_min }} - {{ row.spm_max }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="remarks" label="备注" min-width="120" show-overflow-tooltip />
        
        <el-table-column label="操作" width="90" fixed="right" align="center">
          <template #default="{ row }">
            <div class="eco-icon-actions compact nowrap">
              <el-tooltip content="编辑标准" placement="top" :show-after="300">
                <el-button
                  type="primary"
                  size="small"
                  class="eco-icon-btn edit"
                  @click="handleEdit(row)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除标准" placement="top" :show-after="300">
                <el-button
                  type="danger"
                  size="small"
                  class="eco-icon-btn delete"
                  @click="handleDelete(row)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑AQI标准' : '新增AQI标准'"
      width="800px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="140px"
      >
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form-item label="级别汉字表述" prop="chinese_explain">
          <el-input
            v-model="form.chinese_explain"
            placeholder="例如:一、二、三"
            maxlength="10"
          />
        </el-form-item>

        <el-form-item label="指数级别描述" prop="aqi_explain">
          <el-input
            v-model="form.aqi_explain"
            placeholder="例如:优、良、轻度污染"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="颜色标识" prop="color">
          <el-color-picker
            v-model="form.color"
            show-alpha
          />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">
            当前值: {{ form.color }}
          </span>
        </el-form-item>

        <el-divider content-position="left">浓度限值配置</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="SO₂最小值(μg/m³)" prop="so2_min">
              <el-input-number
                v-model="form.so2_min"
                :min="0"
                :max="9999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="SO₂最大值(μg/m³)" prop="so2_max">
              <el-input-number
                v-model="form.so2_max"
                :min="0"
                :max="9999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="CO最小值(μg/m³)" prop="co_min">
              <el-input-number
                v-model="form.co_min"
                :min="0"
                :max="9999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="CO最大值(μg/m³)" prop="co_max">
              <el-input-number
                v-model="form.co_max"
                :min="0"
                :max="9999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="PM2.5最小值(μg/m³)" prop="spm_min">
              <el-input-number
                v-model="form.spm_min"
                :min="0"
                :max="9999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="PM2.5最大值(μg/m³)" prop="spm_max">
              <el-input-number
                v-model="form.spm_max"
                :min="0"
                :max="9999"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">健康影响与措施</el-divider>

        <el-form-item label="对健康影响情况" prop="health_impact">
          <el-input
            v-model="form.health_impact"
            type="textarea"
            placeholder="请输入对健康影响情况的描述"
            maxlength="500"
            show-word-limit
            :rows="3"
          />
        </el-form-item>

        <el-form-item label="建议采取的措施" prop="take_steps">
          <el-input
            v-model="form.take_steps"
            type="textarea"
            placeholder="请输入建议采取的措施"
            maxlength="500"
            show-word-limit
            :rows="3"
          />
        </el-form-item>

        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="form.remarks"
            type="textarea"
            placeholder="请输入备注信息"
            maxlength="100"
            show-word-limit
            :rows="2"
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
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { 
  getAqiList, 
  addAqi,
  updateAqi,
  deleteAqi
} from '@/api/aqi'

// 列表数据
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])

// 弹窗控制
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)

// 表单数据
const form = reactive({
  aqi_id: null,
  chinese_explain: '',
  aqi_explain: '',
  color: '#02E300',
  health_impact: '',
  take_steps: '',
  so2_min: 0,
  so2_max: 50,
  co_min: 0,
  co_max: 5,
  spm_min: 0,
  spm_max: 35,
  remarks: ''
})

// 验证规则
const rules = {
  chinese_explain: [
    { required: true, message: '请输入级别汉字表述', trigger: 'blur' }
  ],
  aqi_explain: [
    { required: true, message: '请输入指数级别描述', trigger: 'blur' }
  ],
  color: [
    { required: true, message: '请选择颜色标识', trigger: 'change' }
  ],
  health_impact: [
    { required: true, message: '请输入对健康影响情况', trigger: 'blur' }
  ],
  take_steps: [
    { required: true, message: '请输入建议采取的措施', trigger: 'blur' }
  ]
}

// 获取AQI标准列表
const fetchAqiList = async () => {
  loading.value = true
  try {
    const res = await getAqiList()
    if (res.code === 200) {
      tableData.value = res.data || []
    } else {
      ElMessage.error(res.msg || '获取AQI标准列表失败')
    }
  } catch (error) {
    console.error('获取AQI标准失败:', error)
    ElMessage.error(error.message || '获取数据失败,请稍后重试')
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    aqi_id: null,
    chinese_explain: '',
    aqi_explain: '',
    color: '#02E300',
    health_impact: '',
    take_steps: '',
    so2_min: 0,
    so2_max: 50,
    co_min: 0,
    co_max: 5,
    spm_min: 0,
    spm_max: 35,
    remarks: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    aqi_id: row.aqi_id,
    chinese_explain: row.chinese_explain,
    aqi_explain: row.aqi_explain,
    color: row.color,
    health_impact: row.health_impact,
    take_steps: row.take_steps,
    so2_min: row.so2_min,
    so2_max: row.so2_max,
    co_min: row.co_min,
    co_max: row.co_max,
    spm_min: row.spm_min,
    spm_max: row.spm_max,
    remarks: row.remarks || ''
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${row.chinese_explain} - ${row.aqi_explain}" 这条记录吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteAqi(row.aqi_id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchAqiList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
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
    
    let res
    if (isEdit.value) {
      // 更新
      res = await updateAqi(form)
    } else {
      // 新增
      res = await addAqi(form)
    }
    
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
      handleDialogClose()
      fetchAqiList()
    } else {
      ElMessage.error(res.msg || (isEdit.value ? '更新失败' : '新增失败'))
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchAqiList()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 0;
}

// 颜色展示块
.color-display {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;

  .color-block {
    width: 18px;
    height: 18px;
    border-radius: 4px;
    border: 1px solid rgba(129, 199, 132, 0.5);
    display: inline-block;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .color-value {
    font-size: 11px;
    color: #666;
    font-family: 'Courier New', monospace;
  }
}

.range-value {
  font-size: 12px;
  color: #555;
  font-weight: 500;
}

:deep(.el-divider__text) {
  font-weight: 600;
  color: #1b5e20;
  background: rgba(232, 245, 233, 0.8);
}

:deep(.el-alert) {
  background: rgba(232, 245, 233, 0.5) !important;
  border: 1px solid rgba(129, 199, 132, 0.3) !important;

  .el-alert__title {
    color: #2e7d32;
  }
}
</style>
