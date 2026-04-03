<template>
  <div class="user-manage">
    <!-- 搜索栏 -->
    <el-card shadow="never" style="margin-bottom: 16px;">
      <el-form :inline="true" :model="query">
        <el-form-item label="用户名"><el-input v-model="query.keyword" placeholder="搜索用户名/手机号" clearable @clear="fetchList" @keyup.enter="fetchList"/></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" @change="fetchList">
            <el-option label="启用" value="1"/><el-option label="禁用" value="0"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="showAddDialog">新增用户</el-button>
          <el-button type="warning" @click="triggerImport">批量导入</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe border style="width:100%">
        <el-table-column prop="username" label="账号" width="120"/>
        <el-table-column prop="realName" label="姓名" width="100"/>
        <el-table-column prop="phone" label="手机号" width="130"/>
        <el-table-column prop="roleName" label="角色" width="120"/>
        <el-table-column prop="orgName" label="所属组织" min-width="150"/>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{row}">
            <el-button size="small" link type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确定重置密码为123456?" @confirm="handleResetPwd(row.id)">
              <template #reference><el-button size="small" link type="warning">重置密码</el-button></template>
            </el-popconfirm>
            <el-popconfirm title="确定删除该用户?" @confirm="handleDelete(row.id)">
              <template #reference><el-button size="small" link type="danger">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:16px;display:flex;justify-content:flex-end;">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="fetchList"/>
      </div>
      <el-empty v-if="!loading && !tableData.length" description="暂无数据"/>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑用户':'新增用户'" width="520px" destroy-on-close>
      <el-form ref="dFormRef" :model="dForm" :rules="dRules" label-width="80px">
        <el-form-item label="账号" prop="username"><el-input v-model="dForm.username" :disabled="!!editId"/></el-form-item>
        <el-form-item label="姓名" prop="realName"><el-input v-model="dForm.realName"/></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="dForm.phone"/></el-form-item>
        <el-form-item label="密码" prop="password" v-if="!editId"><el-input v-model="dForm.password" type="password" show-password/></el-form-item>
        <el-form-item label="组织" prop="orgId"><el-cascader v-model="dForm.orgIdArr" :options="orgOptions" :props="{checkStrictly:true,value:'id',label:'name'}" clearable style="width:100%"/></el-form-item>
        <el-form-item label="状态" prop="status"><el-switch v-model="dForm.statusBool" active-value="1" inactive-value="0"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入弹窗 -->
    <el-dialog v-model="importDialog" title="批量导入Excel" width="400px">
      <el-upload drag action="" :auto-upload="false" accept=".xlsx,.xls" :limit="1" :on-change="handleFileChange" :file-list="fileList">
        <el-icon style="font-size:48px;color:#c0c4cc;"><UploadFilled/></el-icon>
        <div>将Excel文件拖到此处，或<em>点击上传</em></div>
      </el-upload>
      <template #footer><el-button @click="importDialog=false">取消</el-button><el-button type="primary" @click="doImport" :loading="importing">导入</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadFile, UploadFiles } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import api from '@/api'

const loading = ref(false); const submitting = ref(false); const importing = ref(false)
const tableData = ref<any[]>([]); const total = ref(0)
const query = reactive({ keyword: '', status: '' as string, page: 1, size: 20 })

const dialogVisible = ref(false); const editId = ref<number|null>(null); const dFormRef = ref<FormInstance>()
const dForm = reactive({ username: '', realName: '', phone: '', password: '', orgIdArr: [] as number[], statusBool: '1' })
const dRules: FormRules = {
  username: [{required:true,message:'请输入账号',trigger:'blur'}],
  realName: [{required:true,message:'请输入姓名',trigger:'blur'}],
  password: [{required:true,min:6,message:'密码至少6位',trigger:'blur'}],
}
const orgOptions = ref<any[]>([])

// import
const importDialog = ref(false); const fileList = ref<UploadFile[]>([]); let importFile: File | null = null

function showAddDialog() { editId.value=null; Object.assign(dForm,{username:'',realName:'',phone:'',password:'',orgIdArr:[],statusBool:'1'}); dialogVisible.value=true }
function showEditDialog(row:any) { editId.value=row.id; Object.assign(dForm,{username:row.username,realName:row.realName,phone:row.phone,password:'',orgIdArr:row.orgPath||[],statusBool:String(row.status)}); dialogVisible.value=true }

async function handleSubmit() {
  const ok=await dFormRef.value?.validate().catch(()=>false);if(!ok)return
  submitting.value=true
  try {
    const payload={...dForm,orgId:dForm.orgIdArr[dForm.orgIdArr.length-1],status:Number(dForm.statusBool)}
    delete payload.orgIdArr;if(!payload.password)delete payload.password
    if(editId.value){await api.user.update(editId.value,payload)}else{await api.user.create(payload)}
    ElMessage.success(editId.value?'更新成功':'创建成功');dialogVisible.value=false;fetchList()
  }finally{submitting.value=false}
}

async function handleResetPwd(id:number){
  try{await api.user.resetPassword(id,'123456');ElMessage.success('已重置为123456')}catch{}
}
async function handleDelete(id:number){try{await api.user.remove(id);ElMessage.success('已删除');fetchList()}catch{}}

function handleReset(){Object.assign(query,{keyword:'',status:'',page:1});fetchList()}
function triggerImport(){importDialog.value=true;fileList.value=[];importFile=null}
function handleFileChange(file:UploadFile,fileList:UploadFiles){importFile=file.raw as File}
async function doImport(){
  if(!importFile){ElMessage.warning('请先选择文件');return}
  importing.value=true
  try{await api.user.batchImport(importFile);ElMessage.success('导入成功');importDialog.value=false;fetchList()}finally{importing.value=false}
}

async function fetchOrgs(){try{const res:any=await api.org.tree();orgOptions.value=res.data||[]}catch{}}

async function fetchList(){
  loading.value=true;try{const res:any=await api.user.list(query);tableData.value=res.data?.records||[];total.value=res.data?.total||0}catch{}finally{loading.value=false}
}
onMounted(()=>{fetchList();fetchOrgs()})
</script>

<style scoped>.user-manage{padding:20px}@media(max-width:768px){.user-manage{padding:12px}}</style>
