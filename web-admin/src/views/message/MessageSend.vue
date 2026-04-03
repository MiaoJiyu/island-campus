<template>
  <div class="msg-send">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <template #header><span>发送消息</span></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="标题" prop="title"><el-input v-model="form.title" placeholder="消息标题"/></el-form-item>
            <el-form-item label="内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="5" placeholder="消息内容"/></el-form-item>
            <el-form-item label="目标类型" prop="targetType">
              <el-radio-group v-model="form.targetType" @change="form.targetId=undefined">
                <el-radio value="org">按组织</el-radio><el-radio value="class">按班级</el-radio><el-radio value="computer">指定电脑</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="目标选择" prop="targetId">
              <el-cascader v-if="form.targetType!=='computer'" v-model="form.targetIdArr" :options="orgOptions" :props="{checkStrictly:true,value:'id',label:'name'}" clearable style="width:100%;" placeholder="选择目标组织/班级"/>
              <el-select v-else v-model="form.targetId" multiple filterable style="width:100%;" placeholder="选择电脑">
                <el-option v-for="c in computerOptions" :key="c.id" :label="c.name" :value="c.id"/>
              </el-select>
            </el-form-item>
            <el-form-item><el-button type="primary" :loading="sending" @click="handleSend">发送</el-button></el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card shadow="none">
          <template #header>
            <div><span>已发送消息</span><el-badge :value="unreadCount" :hidden="!unreadCount" style="margin-left:8px;"/><el-button size="small" link type="primary" @click="markAllRead" style="float:right;">全部已读</el-button></div>
          </template>
          <el-table :data="msgList" v-loading="loading" stripe border>
            <el-table-column prop="title" label="标题" min-width="160"/>
            <el-table-column prop="targetName" label="目标" width="140"/>
            <el-table-column prop="read" label="状态" width="80" align="center">
              <template #default="{row}"><el-tag :type="row.read?'success':'warning'" size="small">{{ row.read?'已读':'未读' }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="createdAt" label="发送时间" width="160"/>
            <el-table-column label="操作" width="100">
              <template #default="{row}">
                <el-button v-if="!row.read" size="small" link type="primary" @click="markRead(row.id)">标记已读</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top:12px;display:flex;justify-content:flex-end;"><el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @change="fetchMsgs"/></div>
          <el-empty v-if="!loading && !msgList.length" description="暂无消息记录"/>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import api from '@/api'

const formRef = ref<FormInstance>(); const sending = ref(false); const loading = ref(false)
const unreadCount = ref(0); const msgList = ref<any[]>([]); const total = ref(0); const page = ref(1)
const orgOptions = ref<any[]>([]); const computerOptions = ref<any[]>([])

const form = reactive({ title: '', content: '', targetType: 'org' as string, targetId: undefined as number|undefined, targetIdArr: [] as number[] })
const rules: FormRules = { title: [{required:true,message:'请输入标题',trigger:'blur'}], content: [{required:true,message:'请输入内容',trigger:'blur'}] }

async function handleSend(){
  const ok=await formRef.value?.validate().catch(()=>false);if(!ok)return
  sending.value=true
  try{
    const targetId=form.targetType==='computer'?undefined:(form.targetIdArr[form.targetIdArr.length-1])
    await api.message.send({title:form.title,content:form.content,targetType:form.targetType,targetId,computerIds:form.targetType==='computer'?form.targetId:undefined})
    ElMessage.success('发送成功');Object.assign(form,{title:'',content:'',targetId:undefined,targetIdArr:[]});fetchMsgs()
  }finally{sending.value=false}
}

async function markRead(id:number){try{await api.message.markRead(id);ElMessage.success('已标记');fetchMsgs()}catch{}}
async function markAllRead(){try{await api.message.markAllRead();ElMessage.success('已全部标记');fetchMsgs()}catch{}}

async function fetchMsgs(){
  loading.value=true;try{const res:any=await api.message.messages({page:page.value,size:10});msgList.value=res.data?.records||[];total.value=res.data?.total||0}catch{}finally{loading.value=false}
}
async function fetchUnread(){try{const res:any=await api.message.unreadCount();unreadCount.value=res.data||0}catch{}}
async function fetchTargets(){
  try{const [o,c]=await Promise.all([api.org.tree(),api.computer.list({size:50})]);orgOptions.value=o.data||[];computerOptions.value=c.data?.records||[]}catch{}
}
onMounted(()=>{fetchMsgs();fetchUnread();fetchTargets()})
</script>

<style scoped>.msg-send{padding:20px;}@media(max-width:768px){.msg-send{padding:12px;}}</style>
