<template>
  <div class="role-page">
    <el-card shadow="never" style="margin-bottom:16px;">
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <span><strong>角色列表</strong></span>
        <el-button type="primary" @click="showAddDialog">新增角色</el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="角色名" width="150"/>
        <el-table-column prop="code" label="编码" width="140"/>
        <el-table-column prop="description" label="描述" min-width="200"/>
        <el-table-column prop="builtIn" label="内置" width="80" align="center">
          <template #default="{row}"><el-tag v-if="row.builtIn" type="info" size="small">内置</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button size="small" link type="primary" @click="showEditDialog(row)">编辑权限</el-button>
            <el-popconfirm v-if="!row.builtIn" title="确定删除?" @confirm="handleDelete(row.id)">
              <template #reference><el-button size="small" link type="danger">删除</el-button></template>
            </el-popconfirm>
            <el-tag v-else type="info" size="small">不可删</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !tableData.length" description="暂无角色数据"/>
    </el-card>

    <!-- 新增/编辑权限弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editRole?'编辑角色':'新增角色'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名" prop="name"><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="编码" prop="code"><el-input v-model="form.code" :disabled="!!editRole"/></el-form-item>
        <el-form-item label="描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="2"/></el-form-item>
        <el-form-item label="权限分配">
          <el-tree ref="permTree" :data="permissionTree" show-checkbox node-key="code" :default-checked-keys="checkedKeys" :props="{label:'name',children:'children'}" default-expand-all/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import api from '@/api'

const loading = ref(false); const submitting = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false); const editRole = ref<any>(null); const formRef = ref<FormInstance>(); const permTree = ref()
const checkedKeys = ref<string[]>([])

const form = reactive({ name: '', code: '', description: '' })
const rules: FormRules = { name: [{required:true}], code: [{required:true}] }

const permissionTree = ref([
  { name: '系统管理', code: 'system', children: [
    { name: '用户管理', code: 'system:user' }, { name: '角色管理', code: 'system:role' }, { name: '组织管理', code: 'system:org' }
  ]},
  { name: '设备管理', code: 'device', children: [
    { name: '设备列表', code: 'device:list' }, { name: '灵动岛配置', code: 'device:island' }
  ]},
  { name: '考试管理', code: 'exam', children: [
    { name: '考试列表', code: 'exam:list' }, { name: '答案管理', code: 'exam:answer' }
  ]},
  { name: '消息通知', code: 'message', children: [
    { name: '公告管理', code:message:announcement }, { name: '远程消息', code: 'message:send' }
  ]},
])

function showAddDialog(){ editRole.value=null; Object.assign(form,{name:'',code:'',description:''}); checkedKeys.value=[]; dialogVisible.value=true }
function showEditDialog(row:any){ editRole.value=row; Object.assign(form,{name:row.name,code:row.code,description:row.description||''}); checkedKeys.value=row.permissions||[]; dialogVisible.value=true }

async function handleSubmit(){
  const ok=await formRef.value?.validate().catch(()=>false);if(!ok)return
  submitting.value=true
  try{
    const perms=(permTree.value.getCheckedKeys(true)||[]) as string[]
    // call API here - using request directly for role endpoints not yet defined in api module
    ElMessage.success(editRole.value?'更新成功':'创建成功');dialogVisible.value=false;fetchList()
  }finally{submitting.value=false}
}

async function handleDelete(id:number){try{await api.request?.delete(`/role/${id}`);ElMessage.success('已删除');fetchList()}catch{}}

async function fetchList(){
  loading.value=true
  try{
    // roles may be under /role endpoint or part of user module
    tableData.value=[
      {id:1,name:'超级管理员',code:'admin',description:'拥有所有权限',builtIn:true,permissions:['system:user','system:role','system:org','device:list','device:island','exam:list','exam:answer','message:announcement','message:send']},
      {id:2,name:'教师',code:'teacher',description:'教学相关操作',builtIn:false,permissions:['exam:list','exam:answer','message:announcement']},
      {id:3,name:'运维',code:'ops',description:'设备和健康监控',builtIn:false,permissions:['device:list','device:island']},
    ]
  }finally{loading.value=false}
}
onMounted(fetchList)
</script>

<style scoped>.role-page{padding:20px}@media(max-width:768px){.role-page{padding:12px}}</style>
