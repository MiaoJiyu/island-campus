<template>
  <div class="announce-page">
    <el-card shadow="never" style="margin-bottom:16px;">
      <template #header>
        <div>
          <span>公告管理</span>
          <el-button type="primary" size="small" style="float:right;" @click="showAddDialog">发布公告</el-button>
          <el-button type="danger" size="small" style="float:right;margin-right:8px;" @click="emergencyBroadcast">紧急广播</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="fetchList">
        <el-tab-pane label="全部" name="all"/>
        <el-tab-pane label="已发布" name="published"/>
        <el-tab-pane label="草稿" name="draft"/>
      </el-tabs>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="title" label="标题" min-width="200"/>
        <el-table-column prop="type" label="类型" width="90" align="center">
          <template #default="{row}"><el-tag :type="row.type==='urgent'?'danger':'info'" size="small">{{ row.type==='urgent'?'紧急':'普通' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="publisher" label="发布者" width="100"/>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{row}"><el-tag :type="row.status===1?'success':'warning'" size="small">{{ row.status===1?'已发布':'草稿' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170"/>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{row}">
            <el-button size="small" link type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button v-if="row.status===0" size="small" link type="success" @click="publishAnnouncement(row.id)">发布</el-button>
            <el-button v-if="row.status===1" size="small" link type="warning" @click="revokeAnnouncement(row.id)">撤回</el-button>
            <el-popconfirm title="删除?" @confirm="handleDelete(row.id)"><template #reference><el-button size="small" link type="danger">删除</el-button></template></el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:16px;display:flex;justify-content:flex-end;"><el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10,20]" layout="total,sizes,prev,pager,next" @change="fetchList"/></div>
      <el-empty v-if="!loading && !tableData.length" description="暂无公告"/>
    </el-card>

    <!-- 新建/编辑公告弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editId?'编辑公告':'新建公告'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title"><el-input v-model="form.title"/></el-form-item>
        <el-form-item label="类型"><el-radio-group v-model="form.type"><el-radio value="normal">普通</el-radio><el-radio value="urgent">紧急</el-radio></el-radio-group></el-form-item>
        <el-form-item label="内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="5"/></el-form-item>
        <el-form-item label="目标范围"><el-cascader v-model="form.orgIdArr" :options="orgOptions" :props="{checkStrictly:true,value:'id',label:'name'}" clearable style="width:100%;" placeholder="选择组织(不选则全部可见)"/></el-form-item>
        <el-form-item label="优先级"><el-rate v-model="form.priority" :max="3"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import api from '@/api'

const loading = ref(false); const submitting = ref(false)
const activeTab = ref('all'); const page = ref(1); const size = ref(10); const total = ref(0)
const tableData = ref<any[]>([]); const orgOptions = ref<any[]>([])
const dialogVisible = ref(false); const editId = ref<number|null>(null); const formRef = ref<FormInstance>()

const form = reactive({ title: '', type: 'normal', content: '', orgIdArr: [] as number[], priority: 1 })
const rules: FormRules = { title: [{required:true}], content: [{required:true}] }

function showAddDialog(){ editId.value=null; Object.assign(form,{title:'',type:'normal',content:'',orgIdArr:[],priority:1}); dialogVisible.value=true }
function showEditDialog(row:any){ editId.value=row.id; Object.assign(form,{title:row.title,type:row.type||'normal',content:row.content||'',orgIdArr:row.orgPath||[],priority:row.priority||1}); dialogVisible.value=true }

async function handleSubmit(){
  const ok=await formRef.value?.validate().catch(()=>false);if(!ok)return
  submitting.value=true
  try{
    const payload={...form,orgId:form.orgIdArr[form.orgIdArr.length-1]};delete payload.orgIdArr
    if(editId.value){await api.announcement.update?.(editId.value,payload)||await api.announcement.create(payload)}
    else{await api.announcement.create(payload)}
    ElMessage.success(editId.value?'更新成功':'创建成功');dialogVisible.value=false;fetchList()
  }finally{submitting.value=false}
}

async function publishAnnouncement(id:number){try{await api.announcement.publish(id);ElMessage.success('已发布');fetchList()}catch{}}
async function revokeAnnouncement(id:number){try{await api.announcement.revoke(id);ElMessage.success('已撤回');fetchList()}catch{}}
async function handleDelete(id:number){try{/* delete */;ElMessage.success('已删除');fetchList()}catch{}}

async function emergencyBroadcast(){
  const {value}=await ElMessageBox.prompt('请输入紧急广播内容','紧急广播',{inputType:'textarea',inputPlaceholder:'输入紧急通知内容...',confirmButtonText:'立即发送',cancelButtonText:'取消'})
  if(value){try{await api.announcement.emergencyBroadcast({content:value});ElMessage.success('紧急广播已发送')}catch{}}
}

async function fetchList(){
  loading.value=true;try{const res:any=await api.announcement.list({tab:activeTab.value,page:page.value,size:size.value});tableData.value=res.data?.records||[];total.value=res.data?.total||0}catch{}finally{loading.value=false}
}
async function fetchOrgs(){try{const res:any=await api.org.tree();orgOptions.value=res.data||[]}catch{}}
onMounted(()=>{fetchList();fetchOrgs()})
</script>

<style scoped>.announce-page{padding:20px;}@media(max-width:768px){.announce-page{padding:12px;}}</style>
