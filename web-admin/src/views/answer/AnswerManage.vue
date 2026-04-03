<template>
  <div class="answer-manage">
    <div class="header">
      <h2>{{ $route.meta.title }}</h2>
      <el-button type="primary" @click="handleCreate">新建答案集</el-button>
    </div>

    <div class="content">
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="name" label="名称" width="180">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px">
              <span>{{ row.name }}</span>
              <el-tag v-if="row.isPublished" size="small" type="success">已公布</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="题目数" width="80" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="publishMethod" label="公布方式" width="120">
          <template #default="{ row }">
            {{ publishMethodMap[row.publishMethod] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="公布时间" width="180">
          <template #default="{ row }">
            <span v-if="row.publishTime">{{ formatDateTime(row.publishTime) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已启用' : '未启用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="actions">
              <el-button size="small" @click="viewDetail(row)">详情</el-button>
              <el-button size="small" type="primary" @click="editAnswer(row)">编辑</el-button>
              <el-button v-if="!row.isPublished" size="small" type="success" @click="publishDialog(row)">
                公布
              </el-button>
              <el-button v-else size="small" type="warning" @click="revokePublish(row)">
                撤回
              </el-button>
              <el-button size="small" type="danger" @click="deleteAnswer(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="答案集名称" prop="name">
          <el-input v-model="form.name" placeholder="如：2025-2026第二学期期中考试答案" />
        </el-form-item>
        <el-form-item label="关联班级" prop="targetClassIds">
          <el-select
            v-model="form.targetClassIds"
            multiple
            placeholder="请选择班级"
            style="width: 100%"
          >
            <el-option
              v-for="cls in classes"
              :key="cls.id"
              :label="cls.name"
              :value="cls.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="题目列表">
          <div style="margin-bottom: 12px">
            <el-button type="primary" size="small" @click="addQuestion">添加题目</el-button>
          </div>
          <div class="question-list">
            <div v-for="(q, index) in form.questions" :key="index" class="question-item">
              <div class="question-header">
                <span>题目 {{ index + 1 }}</span>
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="removeQuestion(index)"
                >
                  删除
                </el-button>
              </div>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 8px">
                <div>
                  <div style="font-size: 13px; color: #666; margin-bottom: 4px">题目描述</div>
                  <el-input v-model="q.question" type="textarea" rows="3" placeholder="输入题目描述" />
                </div>
                <div>
                  <div style="font-size: 13px; color: #666; margin-bottom: 4px">答案</div>
                  <el-input v-model="q.answer" type="textarea" rows="3" placeholder="输入标准答案" />
                </div>
                <div>
                  <div style="font-size: 13px; color: #666; margin-bottom: 4px">分值</div>
                  <el-input-number v-model="q.score" :min="1" :max="100" style="width: 100%" />
                </div>
                <div>
                  <div style="font-size: 13px; color: #666; margin-bottom: 4px">题目类型</div>
                  <el-select v-model="q.type" style="width: 100%">
                    <el-option label="文本" value="text" />
                    <el-option label="图片" value="image" />
                  </el-select>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 公布方式弹窗 -->
    <el-dialog v-model="publishDialogVisible" title="公布答案" width="400px">
      <el-form :model="publishForm" label-width="80px">
        <el-form-item label="公布方式">
          <el-radio-group v-model="publishForm.method">
            <el-radio label="timer">定时公布</el-radio>
            <el-radio label="decrypt">密码公布</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="publishForm.method === 'timer'" label="公布时间" required>
          <el-date-picker
            v-model="publishForm.publishTime"
            type="datetime"
            placeholder="选择公布时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="publishForm.method === 'decrypt'" label="密码" required>
          <el-input
            v-model="publishForm.password"
            placeholder="设置解密密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="confirmPublish">公布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import api from '@/api'
import { formatDateTime } from '@/utils/format'

interface AnswerSetVO {
  id: number
  name: string
  questionCount: number
  totalScore: number
  publishMethod: 'timer' | 'decrypt' | ''
  publishTime: string
  isPublished: boolean
  status: number
  creatorName: string
  createdAt: string
}

interface QuestionItem {
  question: string
  answer: string
  score: number
  type: 'text' | 'image'
}

const list = ref<AnswerSetVO[]>([])
const loading = ref(false)
const total = ref(0)
const query = reactive({
  current: 1,
  size: 10,
  name: '',
  status: ''
})

const classes = ref<Array<{ id: number; name: string }>>([])

// 新建/编辑弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEditing = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  targetClassIds: [] as number[],
  status: 1,
  questions: [] as QuestionItem[]
})
const rules: FormRules = {
  name: [{ required: true, message: '请输入答案集名称', trigger: 'blur' }],
  targetClassIds: [{ type: 'array', required: true, message: '请至少选择一个班级', trigger: 'blur' }]
}

// 公布弹窗
const publishDialogVisible = ref(false)
const publishing = ref(false)
const publishForm = reactive({
  method: 'timer' as 'timer' | 'decrypt',
  publishTime: '',
  password: ''
})
const currentAnswerId = ref(0)

const publishMethodMap = {
  timer: '定时公布',
  decrypt: '密码公布'
}

onMounted(() => {
  loadData()
  loadClasses()
})

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res: any = await api.answer.list(query)
    list.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 加载班级列表
async function loadClasses() {
  try {
    const res: any = await api.org.getAllClasses()
    classes.value = res.data
  } catch (e) {
    console.error(e)
  }
}

// 创建
function handleCreate() {
  dialogTitle.value = '新建答案集'
  isEditing.value = false
  Object.assign(form, {
    id: undefined,
    name: '',
    targetClassIds: [],
    status: 1,
    questions: []
  })
  dialogVisible.value = true
}

// 编辑
async function editAnswer(row: AnswerSetVO) {
  try {
    const res: any = await api.answer.detail(row.id)
    dialogTitle.value = '编辑答案集'
    isEditing.value = true
    Object.assign(form, res.data)
    dialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

// 查看详情
function viewDetail(row: AnswerSetVO) {
  ElMessageBox.alert(`查看答案集 "${row.name}" 详情`, '详情', {
    confirmButtonText: '确定'
  })
}

// 添加题目
function addQuestion() {
  form.questions.push({
    question: '',
    answer: '',
    score: 10,
    type: 'text'
  })
}

// 删除题目
function removeQuestion(index: number) {
  form.questions.splice(index, 1)
}

// 提交表单
async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (form.questions.length === 0) {
    ElMessage.warning('请至少添加一个题目')
    return
  }

  submitting.value = true
  try {
    if (isEditing.value) {
      await api.answer.update(form.id!, form)
      ElMessage.success('更新成功')
    } else {
      await api.answer.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

// 显示公布弹窗
function publishDialog(row: AnswerSetVO) {
  if (row.isPublished) {
    ElMessage.warning('该答案集已公布')
    return
  }
  currentAnswerId.value = row.id
  Object.assign(publishForm, {
    method: 'timer',
    publishTime: '',
    password: ''
  })
  publishDialogVisible.value = true
}

// 确认公布
async function confirmPublish() {
  if (publishForm.method === 'timer' && !publishForm.publishTime) {
    ElMessage.warning('请选择公布时间')
    return
  }
  if (publishForm.method === 'decrypt' && !publishForm.password) {
    ElMessage.warning('请输入解密密码')
    return
  }

  publishing.value = true
  try {
    if (publishForm.method === 'timer') {
      await api.answer.publishTimer(currentAnswerId.value, { publishTime: publishForm.publishTime })
    } else {
      await api.answer.publishDecrypt(currentAnswerId.value, { password: publishForm.password })
    }
    ElMessage.success('公布成功')
    publishDialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    publishing.value = false
  }
}

// 撤回公布
async function revokePublish(row: AnswerSetVO) {
  try {
    await ElMessageBox.confirm('确定要撤回公布吗？撤回后学生将无法查看答案', '提示', {
      type: 'warning'
    })
    await api.answer.revoke(row.id)
    ElMessage.success('撤回成功')
    loadData()
  } catch {
    // 用户取消
  }
}

// 删除
async function deleteAnswer(row: AnswerSetVO) {
  try {
    await ElMessageBox.confirm('确定要删除该答案集吗？', '提示', {
      type: 'warning'
    })
    await api.answer.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.answer-manage {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 40px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.content {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.question-list {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 12px;
}

.question-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 12px;
}

.question-item:last-child {
  margin-bottom: 0;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.question-header span {
  font-weight: 500;
  color: #333;
}

.actions {
  display: flex;
  gap: 4px;
}

.pagination {
  margin-top: 16px;
  text-align: center;
}

@media (max-width: 768px) {
  .answer-manage {
    padding: 12px;
  }
  
  .header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .actions {
    flex-wrap: wrap;
  }
}
</style>