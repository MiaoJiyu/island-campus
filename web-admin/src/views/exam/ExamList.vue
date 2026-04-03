<template>
  <div class="exam-page">
    <el-card shadow="never" style="margin-bottom:16px;">
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <div>
          <el-input v-model="keyword" placeholder="搜索考试名称" clearable style="width:220px;margin-right:10px;" @keyup.enter="fetchList"/>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </div>
        <el-button type="success" @click="showAddDialog">+ 新建考试</el-button>
      </div>
    </el-card>

    <el-tabs v-model="viewTab">
      <el-tab-pane label="列表视图" name="list">
        <el-card shadow="never">
          <el-table :data="tableData" v-loading="loading" stripe border>
            <el-table-column prop="name" label="名称" min-width="160"/>
            <el-table-column label="范围" width="180"><template #default="{row}">{{ row.orgName || '全校' }}</template></el-table-column>
            <el-table-column label="时间" width="280">
              <template #default="{row}">{{ row.startTime }} ~ {{ row.endTime }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{row}">
                <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300" fixed="right">
              <template #default="{row}">
                <el-button size="small" link type="primary" @click="showDetail(row)">详情</el-button>
                <el-button v-if="row.status===0" size="small" link type="success" @click="handleStart(row.id)">开始</el-button>
                <el-button v-if="row.status===1" size="small" link type="warning" @click="handleEnd(row.id)">结束</el-button>
                <el-button size="small" link type="info" @click="viewLogs(row.id)">日志</el-button>
                <el-popconfirm title="删除该考试?" @confirm="handleDelete(row.id)">
                  <template #reference><el-button size="small" link type="danger">删除</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top:16px;display:flex;justify-content:flex-end;"><el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total,prev,pager,next" @change="fetchList"/></div>
          <el-empty v-if="!loading && !tableData.length" description="暂无考试数据"/>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="日历视图" name="calendar">
        <el-card shadow="none"><p style="color:#909399;text-align:center;padding:40px 0;">日历视图 - 接入FullCalendar后展示</p></el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 新建/编辑考试弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editId?'编辑考试':'新建考试'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="适用范围" prop="orgIds">
          <el-cascader v-model="form.orgIdArr" :options="orgOptions" :props="{checkStrictly:true,value:'id',label:'name'}" clearable style="width:100%;" placeholder="选择组织"/>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime"><el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%;"/></el-form-item>
        <el-form-item label="结束时间" prop="endTime"><el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%;"/></el-form-item>
        <el-form-item label="解除密码" prop="unlockPassword"><el-input v-model="form.unlockPassword" placeholder="考试结束时解除锁屏的密码"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <!-- 日志弹窗 -->
    <el-dialog v-model="logDialogVisible" title="操作日志" width="600px"><el-timeline><el-timeline-item v-for="(log,i) in logs" :key="i" :timestamp="log.time">{{ log.action }} - {{ log.detail }}</el-timeline-item></el-timeline><el-empty v-if="!logs.length" description="暂无日志"/></el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import api from '@/api'

const loading = ref(false); const submitting = ref(false)
const keyword = ref(''); const page = ref(1); const size = ref(10); const total = ref(0); const tableData = ref<any[]>([])
const viewTab = ref('list'); const dialogVisible = ref(false); const editId = ref<number|null>(null); const formRef = ref<FormInstance>()
const orgOptions = ref<any[]>([])

const form = reactive({ name: '', orgIdArr: [] as number[], startTime: '', endTime: '', unlockPassword: '' })
const rules: FormRules = { name: [{ required: true, message: '请输入名称', trigger: 'blur' }] }

const logDialogVisible = ref(false); const logs = ref<any[]>([])

function statusType(s:number){return [,'warning','success','info','danger'][s]||'info'}
function statusLabel(s:number){return ['', '未开始', '进行中', '已结束', '已取消'][s]||'未知'}

function showAddDialog(){ editId.value=null; Object.assign(form,{name:'',orgIdArr:[],startTime:'',endTime:'',unlockPassword:''}); dialogVisible.value=true }
function showDetail(row:any){ ElMessage.info('查看详情: '+row.name) }

async function handleSubmit(){
  const ok=await formRef.value?.validate().catch(()=>false);if(!ok)return
  submitting.value=true
  try {
    const payload={...form,orgId:form.orgIdArr[form.orgIdArr.length-1]}
    delete payload.orgIdArr
    if(editId.value){await api.exam.update(editId.value,payload)}else{await api.exam.create(payload)}
    ElMessage.success(editId.value?'更新成功':'创建成功');dialogVisible.value=false;fetchList()
  }finally{submitting.value=false}
}

async function handleStart(id:number){try{await api.exam.startExam(id);ElMessage.success('已开始');fetchList()}catch{}}
async function handleEnd(id:number){try{await api.exam.endExam(id);ElMessage.success('已结束');fetchList()}catch{}}
async function handleDelete(id:number){try{await api.exam.remove(id);ElMessage.success('已删除');fetchList()}catch{}}
async function viewLogs(id:number){ logDialogVisible.value=true; try{const res:any=await api.exam.logs(id);logs.value=res.data||[]}catch{logs.value=[]}}

async function fetchOrgs(){try{const res:any=await api.org.tree();orgOptions.value=res.data||[]}catch{}}
async function fetchList(){ loading.value=true; try{const res:any=await api.exam.list({page:page.value,size:size.value,keyword:keyword.value});tableData.value=res.data?.records||[];total.value=res.data?.total||0}catch{}finally{loading.value=false} }
onMounted(()=>{fetchList();fetchOrgs()})
</script>

<style scoped>.exam-page{padding:20px;}@media(max-width:768px){.exam-page{padding:12px;}}</style>
