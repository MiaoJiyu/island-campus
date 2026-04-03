<template>
  <div class="org-page">
    <el-row :gutter="20">
      <!-- 左侧树 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="6">
        <el-card shadow="never" class="tree-card">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <span>组织架构</span>
              <el-button type="primary" size="small" link @click="handleAddRoot">新增根节点</el-button>
            </div>
          </template>
          <el-tree
            ref="treeRef"
            :data="treeData"
            :props="{label:'name',children:'children'}"
            node-key="id"
            default-expand-all
            highlight-current
            :expand-on-click-node="false"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <el-icon><component :is="getIcon(data.type)"/></el-icon>
                <span>{{ node.label }}</span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 右侧详情/编辑面板 -->
      <el-col :xs="24" :sm="24" :md="16" :lg="18">
        <el-card shadow="never">
          <template #header>
            <span>{{ currentNode ? `编辑: ${currentNode.name}` : '选择节点查看详情' }}</span>
          </template>

          <el-empty v-if="!currentNode" description="请在左侧选择一个组织节点"/>

          <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width:560px;">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" disabled>
                <el-option label="分区" value="zone" />
                <el-option label="学校" value="school" />
                <el-option label="年级" value="grade" />
                <el-option label="班级" value="class" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdate">保存修改</el-button>
              <el-button @click="handleAddChild">添加子节点</el-button>
              <el-popconfirm title="确定删除该节点?" @confirm="handleRemove">
                <template #reference><el-button type="danger">删除</el-button></template>
              </el-popconfirm>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { OfficeBuilding, School, Collection, UserFilled } from '@element-plus/icons-vue'
import api from '@/api'

const treeRef = ref()
const formRef = ref<FormInstance>()
const treeData = ref<any[]>([])
const currentNode = ref<any>(null)

const form = reactive({ id: 0, name: '', type: 'class', parentId: null as number | null, sortOrder: 0 })
const rules: FormRules = { name: [{ required: true, message: '请输入名称', trigger: 'blur' }] }

function getIcon(type: string) {
  switch (type) {
    case 'zone': return OfficeBuilding
    case 'school': return School
    case 'grade': return Collection
    case 'class': return UserFilled
    default: return OfficeBuilding
  }
}

function handleNodeClick(data: any) {
  currentNode.value = data
  Object.assign(form, { id: data.id, name: data.name, type: data.type, parentId: data.parentId, sortOrder: data.sortOrder || 0 })
}

async function handleAddRoot() {
  currentNode.value = null
  Object.assign(form, { id: 0, name: '', type: 'zone', parentId: null, sortOrder: 0 })
}

function handleAddChild() {
  Object.assign(form, { id: 0, name: '', type: getNextType(form.type), parentId: currentNode.value?.id || null, sortOrder: 0 })
}

function getNextType(current: string): string {
  const map: Record<string,string> = { zone: 'school', school: 'grade', grade: 'class', class: 'class' }
  return map[current] || 'class'
}

async function fetchTree() {
  try {
    const res: any = await api.org.tree()
    treeData.value = res.data || []
  } catch {}
}

async function handleUpdate() {
  if (!currentNode.value?.id) return await handleCreate()
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await api.org.update(form.id, form)
    ElMessage.success('更新成功')
    await fetchTree()
  } catch {}
}

async function handleCreate() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await api.org.create(form)
    ElMessage.success('创建成功')
    await fetchTree()
  } catch {}
}

async function handleRemove() {
  if (!currentNode.value?.id) return
  try {
    await org.remove(currentNode.value.id)
    ElMessage.success('删除成功')
    currentNode.value = null
    await fetchTree()
  } catch {}
}

onMounted(fetchTree)
</script>

<style scoped>
.org-page { padding: 20px; }
.tree-card { height: calc(100vh - 180px); overflow-y: auto; }
.tree-node { display: inline-flex; align-items: center; gap: 6px; flex: 1; }
@media (max-width: 768px) {
  .org-page { padding: 12px; }
}
</style>
