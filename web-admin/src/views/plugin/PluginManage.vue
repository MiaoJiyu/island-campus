<template>
  <div class="plugin-manage">
    <div class="header">
      <h2>{{ $route.meta.title }}</h2>
      <div>
        <el-button type="primary" @click="uploadDialogVisible = true">上传插件</el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="插件名称">
          <el-input v-model="filterForm.name" placeholder="请输入插件名称" clearable />
        </el-form-item>
        <el-form-item label="插件类型">
          <el-select v-model="filterForm.type" placeholder="请选择类型" clearable style="width: 120px">
            <el-option label="前端插件" value="frontend" />
            <el-option label="后端脚本" value="backend" />
            <el-option label="JAR包" value="jar" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-select v-model="filterForm.enabled" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="已启用" :value="true" />
            <el-option label="已禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 插件列表 -->
    <el-card>
      <div style="margin-bottom: 16px">
        <span>共 {{ total }} 个插件</span>
        <span style="margin-left: 20px">前端插件：{{ frontendCount }}</span>
        <span style="margin-left: 10px">后端插件：{{ backendCount }}</span>
        <span style="margin-left: 10px">已启用：{{ enabledCount }}</span>
      </div>

      <el-table :data="list" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="插件名称" width="180">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-icon v-if="row.type === 'frontend'" color="#409EFF"><Platform /></el-icon>
              <el-icon v-else-if="row.type === 'backend'" color="#67C23A"><Cpu /></el-icon>
              <el-icon v-else><Element /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="插件代码" width="120" />
        <el-table-column prop="version" label="版本" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag
              size="small"
              :type="row.type === 'frontend' ? '' : row.type === 'backend' ? 'success' : 'warning'"
            >
              {{ pluginTypeMap[row.type] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="description" label="描述">
          <template #default="{ row }">
            <span class="ellipsis">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="启用状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.enabled" type="success" size="small">已启用</el-tag>
            <el-tag v-else type="info" size="small">已禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="installedAt" label="安装时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.installedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="actions">
              <el-button size="small" @click="viewPluginDetail(row)">详情</el-button>
              <el-button
                v-if="row.enabled"
                size="small"
                type="warning"
                @click="togglePluginStatus(row)"
              >
                禁用
              </el-button>
              <el-button
                v-else
                size="small"
                type="success"
                @click="togglePluginStatus(row)"
              >
                启用
              </el-button>
              <el-button
                v-if="row.type === 'backend'"
                size="small"
                type="primary"
                @click="executePlugin(row)"
              >
                执行
              </el-button>
              <el-button size="small" type="danger" @click="deletePlugin(row)">删除</el-button>
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
    <div v-if="selectedPlugins.length > 0" class="batch-actions">
      <el-card>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>已选择 {{ selectedPlugins.length }} 个插件</span>
          <div>
            <el-button size="small" type="success" @click="batchEnable">批量启用</el-button>
            <el-button size="small" type="warning" @click="batchDisable">批量禁用</el-button>
            <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
            <el-button size="small" @click="clearSelection">取消选择</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 上传插件弹窗 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传插件"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-steps :active="uploadStep" align-center style="margin-bottom: 24px">
        <el-step title="选择文件" />
        <el-step title="填写信息" />
        <el-step title="确认上传" />
      </el-steps>

      <div v-if="uploadStep === 1" class="upload-step">
        <el-upload
          class="upload-box"
          drag
          action=""
          :before-upload="handleFileSelect"
          :show-file-list="false"
          accept=".groovy,.jar,.zip,.js,.vue"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">点击或拖拽文件到此处上传</div>
        </el-upload>
        
        <div style="margin-top: 20px">
          <h4>支持的文件类型：</h4>
          <ul style="color: #606266; font-size: 14px; padding-left: 20px">
            <li><strong>前端插件</strong>: .vue, .js 文件（Vue组件或JS模块）</li>
            <li><strong>后端脚本</strong>: .groovy 文件（Groovy脚本）</li>
            <li><strong>JAR包</strong>: .jar 文件（Java扩展包）</li>
          </ul>
        </div>
      </div>

      <div v-if="uploadStep === 2">
        <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="100px">
          <el-form-item label="插件名称" prop="name">
            <el-input v-model="uploadForm.name" placeholder="请输入插件名称" />
          </el-form-item>
          <el-form-item label="插件代码" prop="code" required>
            <el-input v-model="uploadForm.code" placeholder="请输入插件代码（英文唯一标识）" />
          </el-form-item>
          <el-form-item label="插件类型" prop="type" required>
            <el-radio-group v-model="uploadForm.type">
              <el-radio label="frontend">前端插件</el-radio>
              <el-radio label="backend">后端脚本</el-radio>
              <el-radio label="jar">JAR包</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="入口点" prop="entryPoint">
            <el-input v-model="uploadForm.entryPoint" placeholder="请输入入口点（如：main.groovy）" />
            <div style="font-size: 12px; color: #909399; margin-top: 4px">
              前端插件：Vue组件名称（如：MyPluginComponent）<br>
              后端脚本：Groovy脚本类名（如：com.example.MyScript）<br>
              JAR包：主类全限定名
            </div>
          </el-form-item>
          <el-form-item label="版本号" prop="version" required>
            <el-input v-model="uploadForm.version" placeholder="格式：x.x.x" />
          </el-form-item>
          <el-form-item label="作者">
            <el-input v-model="uploadForm.author" placeholder="请输入作者" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="uploadForm.description" type="textarea" :rows="3" placeholder="请输入插件描述" />
          </el-form-item>
        </el-form>
      </div>

      <div v-if="uploadStep === 3">
        <div style="padding: 20px; background: #f5f7fa; border-radius: 8px">
          <h4>插件信息确认</h4>
          <div style="margin-top: 12px">
            <p><strong>文件名：</strong>{{ uploadFile?.name }}</p>
            <p><strong>文件大小：</strong>{{ formatFileSize(uploadFile?.size) }}</p>
            <p><strong>插件名称：</strong>{{ uploadForm.name }}</p>
            <p><strong>插件代码：</strong>{{ uploadForm.code }}</p>
            <p><strong>插件类型：</strong>{{ pluginTypeMap[uploadForm.type] }}</p>
            <p><strong>入口点：</strong>{{ uploadForm.entryPoint || '-' }}</p>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button v-if="uploadStep > 1" @click="uploadStep--">上一步</el-button>
        <el-button
          v-if="uploadStep < 3"
          type="primary"
          :disabled="uploadStep === 1 && !uploadFile"
          @click="handleUploadNextStep"
        >
          下一步
        </el-button>
        <el-button v-if="uploadStep === 3" type="primary" :loading="uploading" @click="confirmUpload">
          确认上传
        </el-button>
        <el-button @click="closeUpload">取消</el-button>
      </template>
    </el-dialog>

    <!-- 插件详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="detailTitle"
      width="700px"
      append-to-body
    >
      <el-tabs v-model="detailTab">
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="插件名称">{{ pluginDetail.name }}</el-descriptions-item>
            <el-descriptions-item label="插件代码">{{ pluginDetail.code }}</el-descriptions-item>
            <el-descriptions-item label="插件类型">
              <el-tag
                :type="pluginDetail.type === 'frontend' ? '' : pluginDetail.type === 'backend' ? 'success' : 'warning'"
              >
                {{ pluginTypeMap[pluginDetail.type] }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="版本">{{ pluginDetail.version }}</el-descriptions-item>
            <el-descriptions-item label="作者">{{ pluginDetail.author || '-' }}</el-descriptions-item>
            <el-descriptions-item label="启用状态">
              <el-tag v-if="pluginDetail.enabled" type="success" size="small">已启用</el-tag>
              <el-tag v-else type="info" size="small">已禁用</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="安装时间">{{ formatDateTime(pluginDetail.installedAt) }}</el-descriptions-item>
            <el-descriptions-item label="入口点">{{ pluginDetail.entryPoint || '-' }}</el-descriptions-item>
            <el-descriptions-item label="文件路径">{{ pluginDetail.filePath || '-' }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ pluginDetail.description || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane v-if="pluginDetail.permissionsDeclared" label="权限声明" name="permissions">
          <div style="margin-top: 12px">
            <h4>插件声明的权限：</h4>
            <div style="margin-top: 8px">
              <el-tag
                v-for="(perm, idx) in pluginDetail.permissionsDeclared"
                :key="idx"
                size="small"
                style="margin-right: 8px; margin-bottom: 4px"
              >
                {{ perm }}
              </el-tag>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="pluginDetail.configSchema" label="配置管理" name="config">
          <div style="margin-top: 12px">
            <div v-html="renderConfigSchema(pluginDetail.configSchema)"></div>
            
            <div style="margin-top: 24px">
              <h4>配置管理</h4>
              <el-button type="primary" size="small" @click="savePluginConfig(pluginDetail.id)">
                保存配置
              </el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button v-if="pluginDetail.configSchema" type="primary" @click="savePluginConfig(pluginDetail.id)">
          保存配置
        </el-button>
      </template>
    </el-dialog>

    <!-- 执行插件弹窗 -->
    <el-dialog
      v-model="executeDialogVisible"
      title="执行插件"
      width="800px"
      append-to-body
    >
      <el-form :model="executeForm" label-width="80px">
        <el-form-item label="插件代码">
          <el-input :value="selectedPluginCode" disabled />
        </el-form-item>
        <el-form-item label="参数配置">
          <div style="margin-bottom: 8px">
            <el-button size="small" @click="addExecuteParam">添加参数</el-button>
          </div>
          <div v-for="(param, index) in executeForm.params" :key="index" style="margin-bottom: 8px; display: flex; gap: 8px">
            <el-input v-model="param.key" placeholder="参数名" style="width: 150px" />
            <el-input v-model="param.value" placeholder="参数值" style="flex: 1" />
            <el-button size="small" type="danger" @click="removeExecuteParam(index)">删除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="脚本内容">
          <el-input v-model="executeForm.script" type="textarea" :rows="8" placeholder="输入Groovy脚本内容" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="executeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="executing" @click="doExecutePlugin">执行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { UploadFilled, Platform, Cpu, Element } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import api from '@/api'
import { formatDateTime } from '@/utils/format'

interface PluginVO {
  id: number
  name: string
  code: string
  type: 'frontend' | 'backend' | 'jar'
  version: string
  description: string
  author: string
  filePath: string
  entryPoint: string
  enabled: boolean
  installedAt: string
  permissionsDeclared: string[]
  configSchema: string
  configValues: string | null
}

const list = ref<PluginVO[]>([])
const loading = ref(false)
const total = ref(0)
const frontendCount = ref(0)
const backendCount = ref(0)
const enabledCount = ref(0)

// 查询条件
const query = reactive({
  current: 1,
  size: 10
})
const filterForm = reactive({
  name: '',
  type: null as string | null,
  enabled: null as boolean | null
})

// 多选
const selectedPlugins = ref<PluginVO[]>([])

// 映射字典
const pluginTypeMap = {
  'frontend': '前端插件',
  'backend': '后端脚本', 
  'jar': 'JAR包'
}

// 上传相关
const uploadDialogVisible = ref(false)
const uploadStep = ref(1)
const uploadFile = ref<File | null>(null)
const uploadFormRef = ref<FormInstance>()
const uploading = ref(false)
const uploadForm = reactive({
  name: '',
  code: '',
  type: 'frontend' as 'frontend' | 'backend' | 'jar',
  entryPoint: '',
  version: '1.0.0',
  author: '',
  description: ''
})
const uploadRules: FormRules = {
  name: [{ required: true, message: '请输入插件名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入插件代码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择插件类型', trigger: 'change' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }]
}

// 详情相关
const detailDialogVisible = ref(false)
const detailTitle = ref('')
const detailTab = ref('info')
const pluginDetail = ref<any>({})

// 执行相关
const executeDialogVisible = ref(false)
const executing = ref(false)
const selectedPluginCode = ref('')
const executeForm = reactive({
  params: [] as Array<{ key: string; value: string }>,
  script: 'println "Hello from Groovy Sandbox!"'
})

onMounted(() => {
  loadData()
})

// 加载数据
async function loadData() {
  loading.value = true
  
  const params: any = {
    ...query,
    name: filterForm.name || undefined,
    type: filterForm.type || undefined,
    enabled: filterForm.enabled ?? undefined
  }
  
  try {
    const res: any = await api.plugin.list(params)
    list.value = res.data.records || []
    total.value = res.data.total || 0
    
    // 统计信息
    frontendCount.value = list.value.filter(p => p.type === 'frontend').length
    backendCount.value = list.value.filter(p => p.type === 'backend').length
    enabledCount.value = list.value.filter(p => p.enabled).length
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 重置筛选条件
function resetFilter() {
  Object.assign(filterForm, {
    name: '',
    type: null,
    enabled: null
  })
  loadData()
}

// 多选处理
function handleSelectionChange(selection: PluginVO[]) {
  selectedPlugins.value = selection
}

function clearSelection() {
  selectedPlugins.value = []
}

// 选择文件
function handleFileSelect(file: File) {
  uploadFile.value = file
  autoFillUploadInfo(file)
  return false  // 阻止自动上传
}

// 自动填充上传信息
function autoFillUploadInfo(file: File) {
  const fileName = file.name
  const baseName = fileName.replace(/\.[^/.]+$/, '')
  
  // 根据文件类型设置插件类型
  if (fileName.endsWith('.groovy')) {
    uploadForm.type = 'backend'
    uploadForm.entryPoint = baseName
  } else if (fileName.endsWith('.jar')) {
    uploadForm.type = 'jar'
    uploadForm.entryPoint = baseName
  } else if (fileName.endsWith('.vue') || fileName.endsWith('.js')) {
    uploadForm.type = 'frontend'
    uploadForm.entryPoint = baseName
  }
  
  // 设置插件代码（用文件名转换）
  uploadForm.code = baseName.replace(/[^a-zA-Z0-9_]/g, '_').toLowerCase()
  if (!uploadForm.name) {
    uploadForm.name = baseName
  }
  if (!uploadForm.version) {
    uploadForm.version = '1.0.0'
  }
}

// 上传下一步
async function handleUploadNextStep() {
  if (uploadStep.value === 2) {
    const valid = await uploadFormRef.value?.validate().catch(() => false)
    if (!valid) return
  }
  uploadStep.value++
}

// 关闭上传
function closeUpload() {
  uploadDialogVisible.value = false
  uploadStep.value = 1
  uploadFile.value = null
  Object.assign(uploadForm, {
    name: '',
    code: '',
    type: 'frontend',
    entryPoint: '',
    version: '1.0.0',
    author: '',
    description: ''
  })
}

// 确认上传
async function confirmUpload() {
  if (!uploadFile.value) {
    ElMessage.warning('请选择插件文件')
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value)
    formData.append('type', uploadForm.type)
    formData.append('code', uploadForm.code)
    formData.append('entryPoint', uploadForm.entryPoint || '')
    formData.append('description', uploadForm.description)
    formData.append('version', uploadForm.version)
    if (uploadForm.author) {
      formData.append('author', uploadForm.author)
    }
    if (uploadForm.name) {
      formData.append('name', uploadForm.name)
    }

    await api.plugin.upload(formData)
    ElMessage.success('插件上传成功')
    closeUpload()
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    uploading.value = false
  }
}

// 查看插件详情
async function viewPluginDetail(row: PluginVO) {
  try {
    const res: any = await api.plugin.detail(row.id)
    pluginDetail.value = res.data
    detailTitle.value = `插件详情 - ${row.name}`
    detailDialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

// 切换插件状态
async function togglePluginStatus(row: PluginVO) {
  try {
    const action = row.enabled ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}插件 "${row.name}" 吗？`, '提示', {
      type: 'warning'
    })
    if (row.enabled) {
      await api.plugin.disable(row.id)
    } else {
      await api.plugin.enable(row.id)
    }
    ElMessage.success(`插件已${action}`)
    loadData()
  } catch {
    // 用户取消
  }
}

// 执行插件
async function executePlugin(row: PluginVO) {
  selectedPluginCode.value = row.code
  executeForm.params = []
  executeForm.script = ''
  executeDialogVisible.value = true
}

// 添加执行参数
function addExecuteParam() {
  executeForm.params.push({ key: '', value: '' })
}

// 移除执行参数
function removeExecuteParam(index: number) {
  executeForm.params.splice(index, 1)
}

// 执行插件
async function doExecutePlugin() {
  if (!selectedPluginCode.value) {
    ElMessage.warning('请选择插件')
    return
  }

  executing.value = true
  try {
    const script = executeForm.script.trim()
    if (!script) {
      ElMessage.warning('请输入脚本内容')
      return
    }

    const params: Record<string, any> = {}
    executeForm.params.forEach(p => {
      if (p.key && p.value) {
        params[p.key] = p.value
      }
    })

    const res: any = await api.plugin.execute(selectedPluginCode.value, {
      script: script,
      parameters: params
    })

    ElMessage.success('插件执行成功')
    // 显示执行结果
    console.log('插件执行结果:', res.data)
    executeDialogVisible.value = false
  } catch (e) {
    console.error(e)
  } finally {
    executing.value = false
  }
}

// 删除插件
async function deletePlugin(row: PluginVO) {
  try {
    await ElMessageBox.confirm(`确定要删除插件 "${row.name}" 吗？删除后不可恢复`, '提示', {
      type: 'warning'
    })
    await api.plugin.delete(row.id)
    ElMessage.success('插件已删除')
    loadData()
  } catch {
    // 用户取消
  }
}

// 批量启用
async function batchEnable() {
  try {
    await ElMessageBox.confirm(`确定要启用选中的 ${selectedPlugins.value.length} 个插件吗？`, '提示', {
      type: 'warning'
    })
    for (const plugin of selectedPlugins.value) {
      if (!plugin.enabled) {
        await api.plugin.enable(plugin.id)
      }
    }
    ElMessage.success('批量启用完成')
    selectedPlugins.value = []
    loadData()
  } catch {
    // 用户取消
  }
}

// 批量禁用
async function batchDisable() {
  try {
    await ElMessageBox.confirm(`确定要禁用选中的 ${selectedPlugins.value.length} 个插件吗？`, '提示', {
      type: 'warning'
    })
    for (const plugin of selectedPlugins.value) {
      if (plugin.enabled) {
        await api.plugin.disable(plugin.id)
      }
    }
    ElMessage.success('批量禁用完成')
    selectedPlugins.value = []
    loadData()
  } catch {
    // 用户取消
  }
}

// 批量删除
async function batchDelete() {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedPlugins.value.length} 个插件吗？删除后不可恢复`, '提示', {
      type: 'warning'
    })
    for (const plugin of selectedPlugins.value) {
      await api.plugin.delete(plugin.id)
    }
    ElMessage.success('批量删除完成')
    selectedPlugins.value = []
    loadData()
  } catch {
    // 用户取消
  }
}

// 渲染配置Schema
function renderConfigSchema(schema: string) {
  try {
    const json = JSON.parse(schema)
    return JSON.stringify(json, null, 2)
  } catch {
    return schema
  }
}

// 保存插件配置
async function savePluginConfig(pluginId: number) {
  try {
    await api.plugin.updateConfig(pluginId, {})  // 需要传入实际配置值
    ElMessage.success('配置保存成功')
  } catch (e) {
    console.error(e)
  }
}

// 工具函数
function formatFileSize(bytes?: number): string {
  if (!bytes) return '0 B'
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(2) + ' ' + sizes[i]
}
</script>

<style scoped>
.plugin-manage {
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
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.upload-step {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-box {
  width: 100%;
  max-width: 400px;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>