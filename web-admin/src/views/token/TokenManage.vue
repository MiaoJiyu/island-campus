<template>
  <div class="token-manage">
    <div class="header">
      <h2>{{ $route.meta.title }}</h2>
      <el-button type="primary" @click="handleCreateToken">生成新令牌</el-button>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="令牌名称">
          <el-input v-model="filterForm.name" placeholder="请输入令牌名称" clearable />
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="filterForm.classId" placeholder="请选择班级" clearable style="width: 200px">
            <el-option v-for="cls in classes" :key="cls.id" :label="cls.name" :value="cls.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="有效" :value="1" />
            <el-option label="已禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="失效时间">
          <el-date-picker
            v-model="filterForm.expireTime"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 令牌列表 -->
    <el-card>
      <div style="margin-bottom: 16px">
        <span>共 {{ total }} 个令牌</span>
        <span style="margin-left: 20px">有效：{{ validCount }}</span>
        <span style="margin-left: 10px">已使用：{{ usedCount }}</span>
      </div>

      <el-table :data="list" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="令牌名称" width="180">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px">
              <span>{{ row.name }}</span>
              <el-tag v-if="row.expired" size="small" type="info">已过期</el-tag>
              <el-tag v-else-if="row.revoked" size="small" type="danger">已禁用</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="tokenCode" label="令牌码" width="200">
          <template #default="{ row }">
            <span style="font-family: monospace; letter-spacing: 0.5px">{{ formatTokenCode(row.tokenCode) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" width="140" />
        <el-table-column prop="creatorName" label="创建人" width="120" />
        <el-table-column prop="expireTime" label="失效时间" width="180">
          <template #default="{ row }">
            <span v-if="row.expireTime" :class="{ 'text-warning': row.expired }">
              {{ formatDateTime(row.expireTime) }}
            </span>
            <span v-else>永久有效</span>
          </template>
        </el-table-column>
        <el-table-column prop="usedCount" label="使用次数" width="100">
          <template #default="{ row }">
            <span>{{ row.usedCount }}/{{ row.maxUseCount || '∞' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastUsedAt" label="上次使用" width="180">
          <template #default="{ row }">
            <span v-if="row.lastUsedAt">{{ formatDateTime(row.lastUsedAt) }}</span>
            <span v-else class="text-muted">从未使用</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="actions">
              <el-button size="small" @click="showTokenInfo(row)">详情</el-button>
              <el-button
                v-if="!row.revoked && !row.expired"
                size="small"
                type="danger"
                @click="revokeToken(row)"
              >
                禁用
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="deleteToken(row)"
              >
                删除
              </el-button>
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
    </el-card>

    <!-- 批量操作 -->
    <div v-if="selectedTokens.length > 0" class="batch-actions">
      <el-card>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>已选择 {{ selectedTokens.length }} 个令牌</span>
          <div>
            <el-button size="small" type="warning" @click="batchRevoke">批量禁用</el-button>
            <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
            <el-button size="small" @click="clearSelection">取消选择</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 生成令牌弹窗 -->
    <el-dialog
      v-model="createDialogVisible"
      title="生成新令牌"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="令牌名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入令牌名称" />
        </el-form-item>
        <el-form-item label="关联班级" prop="classId">
          <el-select v-model="form.classId" placeholder="请选择班级" style="width: 100%">
            <el-option v-for="cls in classes" :key="cls.id" :label="cls.name" :value="cls.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="使用次数限制" prop="maxUseCount">
          <el-input-number
            v-model="form.maxUseCount"
            :min="1"
            :max="10000"
            placeholder="不限制请留空"
            style="width: 100%"
          />
          <div style="font-size: 12px; color: #909399; margin-top: 4px">
            设置最大使用次数，为空表示不限制
          </div>
        </el-form-item>
        <el-form-item label="失效时间" prop="expireTime">
          <el-date-picker
            v-model="form.expireTime"
            type="datetime"
            placeholder="选择失效时间"
            style="width: 100%"
          />
          <div style="font-size: 12px; color: #909399; margin-top: 4px">
            留空表示永久有效
          </div>
        </el-form-item>
        <el-form-item label="可用权限" prop="permissions">
          <el-select
            v-model="form.permissions"
            multiple
            placeholder="选择可用权限"
            style="width: 100%"
          >
            <el-option label="查看课程表" value="timetable" />
            <el-option label="查看公告" value="announcement" />
            <el-option label="查看健康概览" value="health" />
            <el-option label="查看课堂快照" value="screenshot" />
            <el-option label="查看答案" value="answer" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreateToken">生成</el-button>
      </template>
    </el-dialog>

    <!-- 令牌详情弹窗 -->
    <el-dialog
      v-model="infoDialogVisible"
      :title="tokenInfoTitle"
      width="600px"
      append-to-body
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="令牌名称">{{ tokenInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="令牌代码">
          <span style="font-family: monospace; word-break: break-all">
            {{ tokenInfo.tokenCode }}
          </span>
          <el-button
            type="text"
            size="small"
            @click="copyTokenCode(tokenInfo.tokenCode)"
            style="margin-left: 8px"
          >
            复制
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="关联班级">{{ tokenInfo.className }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ tokenInfo.creatorName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(tokenInfo.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="使用次数">{{ tokenInfo.usedCount }}/{{ tokenInfo.maxUseCount || '∞' }}</el-descriptions-item>
        <el-descriptions-item label="上次使用时间">
          <span v-if="tokenInfo.lastUsedAt">{{ formatDateTime(tokenInfo.lastUsedAt) }}</span>
          <span v-else class="text-muted">从未使用</span>
        </el-descriptions-item>
        <el-descriptions-item label="失效时间">
          <span v-if="tokenInfo.expireTime">{{ formatDateTime(tokenInfo.expireTime) }}</span>
          <span v-else>永久有效</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag
            :type="tokenInfo.expired ? 'info' : tokenInfo.revoked ? 'danger' : 'success'"
            size="small"
          >
            {{ tokenInfo.expired ? '已过期' : tokenInfo.revoked ? '已禁用' : '有效' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="可用权限" :span="2">
          <el-tag
            v-for="perm in tokenInfo.permissions"
            :key="perm"
            size="small"
            style="margin-right: 8px; margin-bottom: 4px"
          >
            {{ permissionMap[perm] }}
          </el-tag>
          <span v-if="!tokenInfo.permissions || tokenInfo.permissions.length === 0">
            无限制
          </span>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="tokenInfo.tokenCode && !tokenInfo.expired && !tokenInfo.revoked" style="margin-top: 24px">
        <h4 style="margin-bottom: 12px">查询地址</h4>
        <div style="display: flex; gap: 8px; align-items: center">
          <el-input
            :value="getPublicUrl(tokenInfo.tokenCode)"
            readonly
            style="flex: 1"
          >
            <template #append>
              <el-button @click="copyPublicUrl(tokenInfo.tokenCode)" size="small">复制</el-button>
            </template>
          </el-input>
          <el-button type="primary" @click="openPublicUrl(tokenInfo.tokenCode)">打开</el-button>
        </div>
      </div>

      <template #footer>
        <el-button @click="infoDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import api from '@/api'
import { formatDateTime } from '@/utils/format'

interface TokenVO {
  id: number
  name: string
  tokenCode: string
  classId: number
  className: string
  creatorId: number
  creatorName: string
  expireTime: string
  maxUseCount: number | null
  usedCount: number
  lastUsedAt: string | null
  createdAt: string
  revoked: boolean
  expired: boolean
  status: number
  permissions: string[]
}

const list = ref<TokenVO[]>([])
const loading = ref(false)
const total = ref(0)
const validCount = ref(0)
const usedCount = ref(0)
const classes = ref<Array<{ id: number; name: string }>>([])

// 查询条件
const query = reactive({
  current: 1,
  size: 10
})
const filterForm = reactive({
  name: '',
  classId: null as number | null,
  status: null as number | null,
  expireTime: null as [Date, Date] | null
})

// 多选
const selectedTokens = ref<TokenVO[]>([])

// 权限映射字典
const permissionMap = {
  'timetable': '查看课程表',
  'announcement': '查看公告',
  'health': '查看健康概览',
  'screenshot': '查看课堂快照',
  'answer': '查看答案'
}

// 创建弹窗
const createDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({
  name: '',
  classId: null as number | null,
  maxUseCount: null as number | null,
  expireTime: null as string | null,
  permissions: [] as string[]
})
const rules: FormRules = {
  name: [{ required: true, message: '请输入令牌名称', trigger: 'blur' }],
  classId: [{ required: true, message: '请选择关联班级', trigger: 'blur' }]
}

// 详情弹窗
const infoDialogVisible = ref(false)
const tokenInfoTitle = ref('')
const tokenInfo = ref<any>({})

onMounted(() => {
  loadData()
  loadClasses()
})

// 加载数据
async function loadData() {
  loading.value = true
  
  const params: any = {
    ...query,
    name: filterForm.name || undefined,
    classId: filterForm.classId || undefined,
    status: filterForm.status || undefined,
  }
  
  if (filterForm.expireTime) {
    params.expireTimeStart = filterForm.expireTime[0].toISOString()
    params.expireTimeEnd = filterForm.expireTime[1].toISOString()
  }
  
  try {
    const res: any = await api.token.list(params)
    list.value = res.data.records || []
    total.value = res.data.total || 0
    
    // 统计信息
    validCount.value = list.value.filter(t => t.status === 1 && !t.expired).length
    usedCount.value = list.value.filter(t => t.usedCount > 0).length
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

// 重置筛选条件
function resetFilter() {
  Object.assign(filterForm, {
    name: '',
    classId: null,
    status: null,
    expireTime: null
  })
  loadData()
}

// 多选处理
function handleSelectionChange(selection: TokenVO[]) {
  selectedTokens.value = selection
}

function clearSelection() {
  selectedTokens.value = []
}

// 显示创建令牌弹窗
function handleCreateToken() {
  createDialogVisible.value = true
  Object.assign(form, {
    name: '',
    classId: null,
    maxUseCount: null,
    expireTime: null,
    permissions: []
  })
}

// 提交创建令牌
async function submitCreateToken() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      ...form,
      maxUseCount: form.maxUseCount || undefined,
      expireTime: form.expireTime || undefined
    }
    
    await api.token.create(payload)
    ElMessage.success('令牌创建成功')
    createDialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

// 显示令牌详情
async function showTokenInfo(row: TokenVO) {
  try {
    const res: any = await api.token.detail(row.id)
    tokenInfo.value = res.data
    tokenInfoTitle.value = `令牌详情 - ${row.name}`
    infoDialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

// 撤销/禁用令牌
async function revokeToken(row: TokenVO) {
  try {
    await ElMessageBox.confirm(`确定要禁用令牌 "${row.name}" 吗？禁用后无法继续使用`, '提示', {
      type: 'warning'
    })
    await api.token.revoke(row.id)
    ElMessage.success('令牌已禁用')
    loadData()
  } catch {
    // 用户取消
  }
}

// 删除令牌
async function deleteToken(row: TokenVO) {
  try {
    await ElMessageBox.confirm(`确定要删除令牌 "${row.name}" 吗？删除后不可恢复`, '提示', {
      type: 'warning'
    })
    await api.token.delete(row.id)
    ElMessage.success('令牌已删除')
    loadData()
  } catch {
    // 用户取消
  }
}

// 批量禁用
async function batchRevoke() {
  try {
    await ElMessageBox.confirm(`确定要禁用选中的 ${selectedTokens.value.length} 个令牌吗？`, '提示', {
      type: 'warning'
    })
    for (const token of selectedTokens.value) {
      if (!token.revoked && !token.expired) {
        await api.token.revoke(token.id)
      }
    }
    ElMessage.success('批量禁用完成')
    selectedTokens.value = []
    loadData()
  } catch {
    // 用户取消
  }
}

// 批量删除
async function batchDelete() {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedTokens.value.length} 个令牌吗？删除后不可恢复`, '提示', {
      type: 'warning'
    })
    for (const token of selectedTokens.value) {
      await api.token.delete(token.id)
    }
    ElMessage.success('批量删除完成')
    selectedTokens.value = []
    loadData()
  } catch {
    // 用户取消
  }
}

// 工具函数
function formatTokenCode(tokenCode: string): string {
  if (tokenCode.length <= 16) return tokenCode
  return `${tokenCode.slice(0, 8)}...${tokenCode.slice(-8)}`
}

function copyTokenCode(tokenCode: string) {
  navigator.clipboard.writeText(tokenCode).then(() => {
    ElMessage.success('令牌代码已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

function copyPublicUrl(tokenCode: string) {
  const url = getPublicUrl(tokenCode)
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('查询地址已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

function getPublicUrl(tokenCode: string): string {
  const baseUrl = window.location.origin
  return `${baseUrl}/token/public/info?code=${tokenCode}`
}

function openPublicUrl(tokenCode: string) {
  window.open(getPublicUrl(tokenCode), '_blank')
}
</script>

<style scoped>
.token-manage {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 40px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.actions {
  display: flex;
  gap: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.batch-actions {
  margin-top: 20px;
}

.text-muted {
  color: #909399;
}

.text-warning {
  color: #e6a23c;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>