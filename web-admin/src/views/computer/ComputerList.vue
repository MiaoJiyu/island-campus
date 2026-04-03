<template>
  <div class="computer-list">
    <div class="header">
      <h2>{{ $route.meta.title }}</h2>
      <div class="actions">
        <el-button type="primary" @click="importDialogVisible = true">批量导入</el-button>
        <el-button type="success" @click="exportComputers">导出列表</el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="设备名称">
          <el-input v-model="filterForm.name" placeholder="请输入设备名称" clearable />
        </el-form-item>
        <el-form-item label="MAC地址">
          <el-input v-model="filterForm.macAddress" placeholder="请输入MAC地址" clearable />
        </el-form-item>
        <el-form-item label="所属班级">
          <el-select v-model="filterForm.classId" placeholder="请选择班级" clearable style="width: 200px">
            <el-option v-for="cls in classes" :key="cls.id" :label="cls.name" :value="cls.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="在线状态">
          <el-select v-model="filterForm.isOnline" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="健康状态">
          <el-select v-model="filterForm.healthStatus" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="健康" :value="0" />
            <el-option label="预警" :value="1" />
            <el-option label="异常" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 设备列表 -->
    <el-card>
      <div style="margin-bottom: 16px">
        <span>共 {{ total }} 台设备</span>
        <span style="margin-left: 20px">在线：{{ onlineCount }}</span>
        <span style="margin-left: 10px">离线：{{ offlineCount }}</span>
      </div>

      <el-table :data="list" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="设备名称" width="160">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-tag size="small" :type="row.isOnline ? 'success' : 'info'">
                {{ row.isOnline ? '在线' : '离线' }}
              </el-tag>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="macAddress" label="MAC地址" width="160" />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="classInfo" label="所属班级" width="140">
          <template #default="{ row }">
            {{ row.className || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="osInfo" label="系统信息" width="160">
          <template #default="{ row }">
            <el-popover
              placement="top-start"
              width="200"
              trigger="hover"
              :content="row.osInfo || '未上报'"
            >
              <template #reference>
                <span class="ellipsis">{{ row.osInfo || '未上报' }}</span>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="clientVersion" label="客户端版本" width="120" />
        <el-table-column prop="healthStatus" label="健康状态" width="100">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 4px">
              <span :class="`status-dot status-${getHealthStatusClass(row.healthStatus)}`"></span>
              <span>{{ getHealthStatusText(row.healthStatus) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="lastHeartbeat" label="最后心跳" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.lastHeartbeat) }}
            <span v-if="row.lastHeartbeat" style="font-size: 12px; color: #909399; margin-left: 4px">
              {{ formatTimeAgo(row.lastHeartbeat) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="启停状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="toggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="actions">
              <el-button size="small" @click="viewDetail(row)">详情</el-button>
              <el-button size="small" type="primary" @click="editDevice(row)">编辑</el-button>
              <el-button size="small" type="warning" @click="sendCommand(row)">指令</el-button>
              <el-button size="small" type="danger" @click="deleteDevice(row)">删除</el-button>
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
    <div v-if="selectedDevices.length > 0" class="batch-actions">
      <el-card>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>已选择 {{ selectedDevices.length }} 台设备</span>
          <div>
            <el-button size="small" @click="sendBatchCommand">发送批量指令</el-button>
            <el-button size="small" type="warning" @click="batchMoveClass">移动到班级</el-button>
            <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
            <el-button size="small" @click="clearSelection">取消选择</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 设备详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="detailTitle"
      width="600px"
      append-to-body
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备名称">{{ detailInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="MAC地址">{{ detailInfo.macAddress }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailInfo.ipAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属班级">{{ detailInfo.className || '-' }}</el-descriptions-item>
        <el-descriptions-item label="系统信息" :span="2">{{ detailInfo.osInfo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户端版本">{{ detailInfo.clientVersion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="屏幕分辨率">{{ detailInfo.resolution || '-' }}</el-descriptions-item>
        <el-descriptions-item label="在线状态">
          <el-tag :type="detailInfo.isOnline ? 'success' : 'info'">
            {{ detailInfo.isOnline ? '在线' : '离线' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="健康状态">
          <div style="display: flex; align-items: center; gap: 4px">
            <span :class="`status-dot status-${getHealthStatusClass(detailInfo.healthStatus)}`"></span>
            <span>{{ getHealthStatusText(detailInfo.healthStatus) }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="最后心跳时间" :span="2">
          {{ formatDateTime(detailInfo.lastHeartbeat) }}
        </el-descriptions-item>
        <el-descriptions-item label="灵动岛配置" :span="2">
          <pre v-if="detailInfo.islandConfigJson" style="background: #f5f7fa; padding: 8px; border-radius: 4px; max-height: 200px; overflow: auto">
{{ JSON.stringify(JSON.parse(detailInfo.islandConfigJson), null, 2) }}
          </pre>
          <span v-else>无自定义配置</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailInfo.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEditing ? '编辑设备' : '新增设备'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="MAC地址" prop="macAddress">
          <el-input v-model="form.macAddress" placeholder="格式：xx:xx:xx:xx:xx:xx" />
        </el-form-item>
        <el-form-item label="所属班级" prop="classId">
          <el-select v-model="form.classId" placeholder="请选择班级" style="width: 100%">
            <el-option v-for="cls in classes" :key="cls.id" :label="cls.name" :value="cls.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="屏幕分辨率">
          <el-input v-model="form.resolution" placeholder="如：1920x1080" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入弹窗 -->
    <el-dialog v-model="importDialogVisible" title="批量导入设备" width="700px">
      <div style="margin-bottom: 16px">
        <p>1. 下载模板文件：<el-button type="text" @click="downloadTemplate">设备导入模板.xlsx</el-button></p>
        <p>2. 填写设备信息（设备名称、MAC地址、所属班级）</p>
        <p>3. 上传填写好的Excel文件</p>
      </div>
      
      <el-upload
        class="upload-demo"
        drag
        action=""
        :before-upload="handleTemplateUpload"
        :show-file-list="false"
        accept=".xlsx,.xls"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">点击或拖拽Excel文件到此处上传</div>
        <template #tip>
          <div class="el-upload__tip" style="color: #606266">
            仅支持 .xlsx, .xls 格式文件，文件大小不超过5MB
          </div>
        </template>
      </el-upload>

      <div v-if="importPreview.length > 0" style="margin-top: 24px">
        <h4>导入预览（{{ importPreview.length }} 条）</h4>
        <el-table :data="importPreview" style="width: 100%; margin-top: 12px" height="250">
          <el-table-column prop="name" label="设备名称" width="120" />
          <el-table-column prop="macAddress" label="MAC地址" width="160" />
          <el-table-column prop="className" label="班级名称" width="120" />
          <el-table-column prop="classId" label="班级ID" width="80">
            <template #default="{ row }">
              <span :class="row.classId ? 'text-success' : 'text-danger'">
                {{ row.classId || '未匹配' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <span v-if="row.error" class="text-danger">错误</span>
              <span v-else-if="row.duplicate" class="text-warning">重复</span>
              <span v-else class="text-success">正常</span>
            </template>
          </el-table-column>
          <el-table-column label="验证信息">
            <template #default="{ row }">
              <span v-if="row.error">{{ row.error }}</span>
              <span v-else-if="row.duplicate">设备已存在</span>
              <span v-else>√</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="importPreview.length === 0 || hasImportErrors"
          :loading="importing"
          @click="confirmImport"
        >
          确认导入
        </el-button>
      </template>
    </el-dialog>

    <!-- 发送指令弹窗 -->
    <el-dialog v-model="commandDialogVisible" title="发送指令" width="400px">
      <el-form :model="commandForm" label-width="80px">
        <el-form-item label="指令类型" required>
          <el-select v-model="commandForm.type" style="width: 100%">
            <el-option label="重启客户端" value="restart_client" />
            <el-option label="更新灵动岛配置" value="update_island_config" />
            <el-option label="立即上报健康数据" value="report_health" />
            <el-option label="立即截图" value="take_screenshot" />
            <el-option label="进入考试模式" value="enter_exam_mode" />
            <el-option label="退出考试模式" value="exit_exam_mode" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="commandForm.type === 'update_island_config'" label="配置内容">
          <el-input v-model="commandForm.configJson" type="textarea" :rows="4" placeholder="请输入JSON配置" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="commandDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="sendingCommand" @click="doSendCommand">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import api from '@/api'
import { formatDateTime, formatTimeAgo } from '@/utils/format'

interface ComputerVO {
  id: number
  name: string
  macAddress: string
  ipAddress: string
  classId: number
  className: string
  osInfo: string
  clientVersion: string
  isOnline: boolean
  healthStatus: number  // 0: 绿色，1: 黄色，2: 红色
  lastHeartbeat: string
  resolution: string
  islandConfigJson: string
  remark: string
  status: number
}

interface ImportPreviewItem {
  name: string
  macAddress: string
  className: string
  classId?: number
  error?: string
  duplicate?: boolean
}

const list = ref<ComputerVO[]>([])
const loading = ref(false)
const total = ref(0)
const onlineCount = ref(0)
const offlineCount = ref(0)
const classes = ref<Array<{ id: number; name: string }>>([])

// 查询条件
const query = reactive({
  current: 1,
  size: 10
})
const filterForm = reactive({
  name: '',
  macAddress: '',
  classId: null as number | null,
  isOnline: null as number | null,
  healthStatus: null as number | null
})

// 多选
const selectedDevices = ref<ComputerVO[]>([])

// 详情弹窗
const detailDialogVisible = ref(false)
const detailTitle = ref('')
const detailInfo = ref<any>({})

// 编辑弹窗
const editDialogVisible = ref(false)
const isEditing = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  macAddress: '',
  classId: null as number | null,
  resolution: '',
  remark: ''
})
const rules: FormRules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  macAddress: [
    { required: true, message: '请输入MAC地址', trigger: 'blur' },
    { pattern: /^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$/, message: 'MAC地址格式不正确', trigger: 'blur' }
  ],
  classId: [{ required: true, message: '请选择所属班级', trigger: 'blur' }]
}

// 导入相关
const importDialogVisible = ref(false)
const importPreview = ref<ImportPreviewItem[]>([])
const importing = ref(false)

// 指令相关
const commandDialogVisible = ref(false)
const sendingCommand = ref(false)
const commandForm = reactive({
  type: 'restart_client',
  configJson: ''
})

onMounted(() => {
  loadData()
  loadClasses()
})

// 加载数据
async function loadData() {
  loading.value = true
  const params = {
    ...query,
    ...Object.fromEntries(
      Object.entries(filterForm).filter(([_, v]) => v !== null && v !== '')
    )
  }
  
  try {
    const res: any = await api.computer.list(params)
    list.value = res.data.records || []
    total.value = res.data.total || 0
    
    // 统计在线/离线数量
    onlineCount.value = list.value.filter(d => d.isOnline).length
    offlineCount.value = list.value.filter(d => !d.isOnline).length
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
    macAddress: '',
    classId: null,
    isOnline: null,
    healthStatus: null
  })
  loadData()
}

// 多选处理
function handleSelectionChange(selection: ComputerVO[]) {
  selectedDevices.value = selection
}

function clearSelection() {
  selectedDevices.value = []
}

// 查看详情
async function viewDetail(row: ComputerVO) {
  try {
    const res: any = await api.computer.detail(row.id)
    detailInfo.value = res.data
    detailTitle.value = `设备详情 - ${row.name}`
    detailDialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

// 新增设备
function addDevice() {
  isEditing.value = false
  Object.assign(form, {
    id: undefined,
    name: '',
    macAddress: '',
    classId: null,
    resolution: '',
    remark: ''
  })
  editDialogVisible.value = true
}

// 编辑设备
async function editDevice(row: ComputerVO) {
  try {
    const res: any = await api.computer.detail(row.id)
    isEditing.value = true
    Object.assign(form, {
      id: row.id,
      name: res.data.name,
      macAddress: res.data.macAddress,
      classId: res.data.classId,
      resolution: res.data.resolution,
      remark: res.data.remark
    })
    editDialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

// 提交表单
async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEditing.value) {
      await api.computer.update(form.id!, form)
      ElMessage.success('更新成功')
    } else {
      await api.computer.create(form)
      ElMessage.success('创建成功')
    }
    editDialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

// 删除设备
async function deleteDevice(row: ComputerVO) {
  try {
    await ElMessageBox.confirm(`确定要删除设备 "${row.name}" 吗？`, '提示', {
      type: 'warning'
    })
    await api.computer.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 用户取消
  }
}

// 批量删除
async function batchDelete() {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedDevices.value.length} 台设备吗？`, '提示', {
      type: 'warning'
    })
    for (const device of selectedDevices.value) {
      await api.computer.delete(device.id)
    }
    ElMessage.success('批量删除完成')
    selectedDevices.value = []
    loadData()
  } catch {
    // 用户取消
  }
}

// 启停状态切换
async function toggleStatus(row: ComputerVO) {
  try {
    await api.computer.updateStatus(row.id, { status: row.status })
    ElMessage.success('状态更新成功')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1  // 回滚状态
    console.error(e)
  }
}

// 发送指令
function sendCommand(row: ComputerVO) {
  commandForm.type = 'restart_client'
  commandForm.configJson = ''
  commandDialogVisible.value = true
  currentCommandDeviceId = row.id
}

// 批量发送指令
async function sendBatchCommand() {
  ElMessage.warning('批量指令功能正在开发中...')
}

// 移动到班级
async function batchMoveClass() {
  ElMessage.warning('批量移动到班级功能正在开发中...')
}

// 执行发送指令
let currentCommandDeviceId = 0
async function doSendCommand() {
  sendingCommand.value = true
  try {
    // 这里应该调用发送指令的API
    await api.computer.sendCommand(currentCommandDeviceId, commandForm)
    ElMessage.success('指令发送成功')
    commandDialogVisible.value = false
  } catch (e) {
    console.error(e)
  } finally {
    sendingCommand.value = false
  }
}

// 导出设备列表
async function exportComputers() {
  try {
    const res: any = await api.computer.export(filterForm)
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `设备列表_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    console.error(e)
  }
}

// 下载模板
function downloadTemplate() {
  // 创建模拟模板
  const templateData = [
    ['设备名称', 'MAC地址', '班级名称', '备注'],
    ['计算机-001', '00:1A:2B:3C:4D:5E', '三年二班', '备用设备'],
    ['计算机-002', '00:1A:2B:3C:4D:5F', '三年二班', '']
  ]
  
  const ws = XLSX.utils.aoa_to_sheet(templateData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '设备导入模板')
  XLSX.writeFile(wb, '设备导入模板.xlsx')
}

// 处理上传
async function handleTemplateUpload(file: File) {
  try {
    const reader = new FileReader()
    reader.onload = async (e) => {
      const data = e.target?.result
      if (data) {
        // 解析Excel
        const workbook = XLSX.read(data, { type: 'binary' })
        const sheet = workbook.Sheets[workbook.SheetNames[0]]
        const rows = XLSX.utils.sheet_to_json(sheet)
        
        // 构建预览
        const previewData = rows.map((row: any) => {
          const className = row['班级名称']
          const classMatch = classes.value.find(c => c.name === className)
          
          return {
            name: row['设备名称'],
            macAddress: row['MAC地址'],
            className: className,
            classId: classMatch?.id,
            error: !row['设备名称'] ? '设备名称不能为空' : 
                   !row['MAC地址'] ? 'MAC地址不能为空' :
                   !classMatch ? '班级名称不存在' : undefined
          }
        })
        
        importPreview.value = previewData
      }
    }
    reader.readAsBinaryString(file)
  } catch (e) {
    console.error(e)
  }
  return false  // 阻止自动上传
}

// 确认导入
async function confirmImport() {
  importing.value = true
  try {
    const validRows = importPreview.value.filter(item => !item.error && !item.duplicate)
    if (validRows.length === 0) {
      ElMessage.warning('没有有效数据可以导入')
      return
    }
    
    const importData = validRows.map(item => ({
      name: item.name,
      macAddress: item.macAddress,
      classId: item.classId
    }))
    
    await api.computer.batchImport(importData)
    ElMessage.success(`成功导入 ${importData.length} 条设备数据`)
    importDialogVisible.value = false
    importPreview.value = []
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    importing.value = false
  }
}

// 计算是否包含导入错误
const hasImportErrors = computed(() => {
  return importPreview.value.some(item => item.error)
})

// 工具函数
function getHealthStatusClass(status: number): string {
  const classes = ['green', 'yellow', 'red']
  return classes[Math.min(status, 2)] || 'gray'
}

function getHealthStatusText(status: number): string {
  const texts = ['健康', '预警', '异常']
  return texts[Math.min(status, 2)] || '未知'
}
</script>

<style scoped>
.computer-list {
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

.ellipsis {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-green { background: #67C23A; }
.status-yellow { background: #E6A23C; }
.status-red { background: #F56C6C; }

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

.text-success { color: #67C23A; }
.text-danger { color: #F56C6C; }
.text-warning { color: #E6A23C; }

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>