<template>
  <div class="mode-page">
    <!-- 内置模式卡片 -->
    <el-row :gutter="16" style="margin-bottom:20px;">
      <el-col :xs="12" :sm="8" :md="6" v-for="m in modes" :key="m.id">
        <el-card shadow="hover" class="mode-card" :style="{borderTop:`3px solid ${m.color}`}">
          <div style="text-align:center;">
            <div class="mode-icon" :style="{background:m.color+'22',color:m.color}">{{ m.icon }}</div>
            <h4 style="margin:10px 0 4px;">{{ m.name }}</h4>
            <p style="margin:0;color:#909399;font-size:12px;">{{ m.description }}</p>
            <div style="margin-top:10px;">
              <el-tag size="small" :type="m.lockScreen?'warning':'info'">{{ m.lockScreen?'锁屏':'不锁屏' }}</el-tag>
              <el-tag size="small" :type="m.limitExternal?'danger':'info'" style="margin-left:4px;">{{ m.limitExternal?'限制外网':'不限' }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 切换计划 + 当前状态 -->
    <el-tabs v-model="activeTab">
      <el-tab-pane label="切换计划" name="schedule">
        <el-card shadow="never">
          <template #header><div><span>计划时间线</span><el-button type="primary" size="small" style="float:right;" @click="showScheduleDialog">新建计划</el-button></div></template>
          <el-table :data="schedules" border stripe>
            <el-table-column prop="name" label="名称" width="150"/>
            <el-table-column label="目标模式" width="120"><template #default="{row}">{{ row.modeName || '-'}}</template></el-table-column>
            <el-table-column label="时间" width="200"><template #default="{row}">每周{{ weekDayMap[row.weekDay]||'' }} {{ row.startTime }} - {{ row.endTime }}</template></el-table-column>
            <el-table-column prop="enabled" label="启用" width="80" align="center"><template #default="{row}"><el-switch v-model="row.enabled" @change="toggleSchedule(row)"/></template></el-table-column>
            <el-table-column label="操作" width="140"><template #default="{row}">
              <el-button size="small" link type="primary" @click="editSchedule(row)">编辑</el-button>
              <el-popconfirm title="删除?" @confirm="removeSchedule(row.id)"><template #reference><el-button size="small" link type="danger">删除</el-button></template></el-popconfirm>
            </template></el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="当前状态" name="status">
        <el-card shadow="none">
          <el-table :data="currentStatus" border stripe>
            <el-table-column prop="computerName" label="设备名" width="160"/>
            <el-table-column prop="ip" label="IP" width="130"/>
            <el-table-column label="当前模式" width="120"><template #default="{row}"><el-tag>{{ row.modeName || '正常' }}</el-tag></template></el-table-column>
            <el-table-column prop="since" label="切换时间" min-width="160"/>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 手动切换弹窗 -->
    <el-dialog v-model="switchDialogVisible" title="手动切换模式" width="500px" destroy-on-close>
      <el-form :model="switchForm" label-width="100px">
        <el-form-item label="目标设备">
          <el-select v-model="switchForm.computerIds" multiple placeholder="选择设备" style="width:100%;">
            <el-option v-for="c in computerOptions" :key="c.id" :label="c.name" :value="c.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="目标模式">
          <el-radio-group v-model="switchForm.modeId">
            <el-radio v-for="m in modes" :key="m.id" :value="m.id">{{ m.name }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="switchDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="doSwitchMode">执行切换</el-button>
      </template>
    </el-dialog>

    <!-- 新建/编辑计划弹窗 -->
    <el-dialog v-model="schedDialogVisible" :title="editSchedId?'编辑计划':'新建计划'" width="480px">
      <el-form ref="sFormRef" :model="schedForm" :rules="schedRules" label-width="90px">
        <el-form-item label="名称" prop="name"><el-input v-model="schedForm.name"/></el-form-item>
        <el-form-item label="星期" prop="weekDay"><el-select v-model="schedForm.weekDay" style="width:100%;"><el-option v-for="(d,i) in ['一','二','三','四','五','六','日']" :key="i+1" :label="'周'+d" :value="i+1"/></el-select></el-form-item>
        <el-form-item label="时间范围">
          <el-time-picker v-model="schedForm.timeRange" is-range range-separator="至" start-placeholder="开始" end-placeholder="结束" format="HH:mm" value-format="HH:mm" style="width:100%;"/>
        </el-form-item>
        <el-form-item label="目标模式"><el-select v-model="schedForm.modeId" style="width:100%;"><el-option v-for="m in modes" :key="m.id" :label="m.name" :value="m.id"/></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="schedDialogVisible=false">取消</el-button><el-button type="primary" @click="saveSchedule">确定</el-button></template>
    </el-dialog>

    <!-- 浮动按钮 -->
    <el-button type="primary" circle size="large" class="fab-btn" @click="openSwitchDialog">
      <span>⚡</span>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import api from '@/api'

const activeTab = ref('schedule')
const modes = ref([
  { id: 1, name: '正常模式', icon: '✅', color: '#67C23A', description: '正常使用', lockScreen: false, limitExternal: false },
  { id: 2, name: '考试模式', icon: '📝', color: '#409EFF', description: '考试专用', lockScreen: true, limitExternal: true },
  { id: 3, name: '课间模式', icon: '☕', color: '#E6A23C', description: '课间休息', lockScreen: false, limitExternal: false },
  { id: 4, name: '锁定模式', icon: '🔒', color: '#F56C6C', description: '完全锁定', lockScreen: true, limitExternal: true },
])
const weekDayMap:{[k:number]:string} = {1:'一',2:'二',3:'三',4:'四',5:'五',6:'六',7:'日'}

const schedules = ref<any[]>([])
const currentStatus = ref<any[]>([])
const computerOptions = ref<any[]>([])

const switchDialogVisible = ref(false); const switchForm = reactive({ computerIds: [] as number[], modeId: 1 })
function openSwitchDialog(){ switchDialogVisible.value=true }
async function doSwitchMode(){
  if(!switchForm.computerIds.length){ElMessage.warning('请选择设备');return}
  try{await api.mode.switchMode({targetComputerIds:switchForm.computerIds,modeId:switchForm.modeId});ElMessage.success('切换成功');switchDialogVisible.value=false}catch{}
}

// schedule
const schedDialogVisible=ref(false); const editSchedId=ref<number|null>(null); const sFormRef=ref<FormInstance>()
const schedForm=reactive({name:'',weekDay:1,time:['08:00','12:00'] as string[],modeId:1})
const schedRules: FormRules={name:[{required:true}],weekDay:[{required:true}]}

function showScheduleDialog(){editSchedId.value=null;Object.assign(schedForm,{name:'',weekDay:1,time:['08:00','12:00'],modeId:1});schedDialogVisible.value=true}
function editSchedule(row:any){editSchedId.value=row.id;Object.assign(schedForm,{name:row.name,weekDay:row.weekDay,time:[row.startTime,row.endTime],modeId:row.modeId||1});schedDialogVisible.value=true}
async function saveSchedule(){
  const ok=await sFormRef.value?.validate().catch(()=>false);if(!ok)return
  try{
    const payload={...name:schedForm.name,weekDay:schedForm.weekDay,startTime:schedForm.time[0],endTime:schedForm.time[1],modeId:schedForm.modeId}
    if(editSchedId.value){await api.mode.updateSchedule(editSchedId.value,payload)}else{await api.mode.createSchedule(payload)}
    ElMessage.success('保存成功');schedDialogVisible.value=false;fetchSchedules()
  }catch{}
}
async function toggleSchedule(row:any){
  try{await api.mode.updateSchedule(row.id,{enabled:row.enabled})}catch{}
}
async function removeSchedule(id:number){try{await api.mode.removeSchedule(id);ElMessage.success('已删除');fetchSchedules()}catch{}}

async function fetchSchedules(){
  try{const res:any=await api.mode.scheduleList();schedules.value=res.data||(Array.from({length:3},(_,i)=>({id:i+1,name:'第'+(i+1)+'节课',weekDay:i%7+1,startTime:'0'+(8+i*2)+':00',endTime:'0'+(10+i*2)+':00',modeId:(i%4)+1,enabled:true,modeName:modes.value[i%4]?.name})))}catch{}
}
async function fetchCurrentStatus(){
  try{const res:any=await api.mode.currentStatus();currentStatus.value=res.data||(Array.from({length:5},(_,i)=>({computerName:'PC-'+String(i+1).padStart(3,'0'),ip:'192.168.1.'+(i+10),modeName:modes.value[i%4]?.name,since:new Date().toLocaleTimeString()})))}catch{}
}
async function fetchComputers(){
  try{const res:any=await api.computer.list({size:50});computerOptions.value=res.data?.records||[]}catch{}
}
onMounted(()=>{fetchSchedules();fetchCurrentStatus();fetchComputers()})
</script>

<style scoped>.mode-page{padding:20px;position:relative;}
.mode-card{text-align:center;margin-bottom:12px;transition:transform .2s;}.mode-card:hover{transform:translateY(-2px);}
.mode-icon{width:56px;height:56px;border-radius:14px;display:flex;align-items:center;justify-content:center;font-size:24px;margin:0 auto 8px;}
.fab-btn{position:fixed;right:30px;bottom:30px;z-index:99;width:56px;height:56px;font-size:22px;}
@media(max-width:768px){.mode-page{padding:12px;}.fab-btn{right:16px;bottom:16px;}}
</style>
