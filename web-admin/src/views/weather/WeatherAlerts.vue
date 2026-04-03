<template>
  <div class="weather-alerts">
    <el-card shadow="never" style="margin-bottom:16px;">
      <template #header><div><span>天气告警规则</span><el-button type="primary" size="small" style="float:right;" @click="showAddDialog">新建规则</el-button></div></template>
      <el-table :data="ruleList" v-loading="loading" stripe border>
        <el-table-column prop="name" label="规则名称" width="160"/>
        <el-table-column label="条件" min-width="240">
          <template #default="{row}"><el-tag size="small">{{ conditionLabel(row.condition) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="动作" width="160">
          <template #default="{row}"><el-tag type="warning" size="small">{{ actionLabel(row.actions) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="enabled" label="启用" width="80" align="center">
          <template #default="{row}"><el-switch v-model="row.enabled" @change="toggleRule(row)"/></template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button size="small" link type="primary" @click="testAlert(row.id)">测试</el-button>
            <el-button size="small" link type="info" @click="showEditDialog(row)">编辑</el-button>
            <el-popconfirm title="删除?" @confirm="handleDelete(row.id)"><template #reference><el-button size="small" link type="danger">删除</el-button></template></el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !ruleList.length" description="暂无告警规则"/>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editRuleId?'编辑规则':'新建规则'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="规则名称" prop="name"><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="条件类型" prop="condition.type">
          <el-select v-model="form.condition.type" style="width:100%;">
            <el-option label="1小时内有雨" value="rain_1h"/><el-option label="气温 > 阈值" value="temp_gt"/><el-option label="气温 < 阈值" value="temp_lt"/><el-option label="AQI > 阈值" value="aqi_gt"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="['temp_gt','temp_lt','aqi_gt'].includes(form.condition.type)" label="阈值">
          <el-input-number v-model="form.condition.threshold" :min="-50" :max="500"/>
          <span style="margin-left:8px;color:#909399;">{{ form.condition.type.includes('temp')?'℃':'' }}</span>
        </el-form-item>
        <el-form-item label="触发动作" prop="actions">
          <el-checkbox-group v-model="form.actions">
            <el-checkbox label="flash_icon">闪烁图标</el-checkbox>
            <el-checkbox label="announcement">发公告</el-checkbox>
            <el-checkbox label="remote_notify">远程通知</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import api from '@/api'

const loading = ref(false); const submitting = ref(false); const ruleList = ref<any[]>([])
const dialogVisible = ref(false); const editRuleId = ref<number|null>(null); const formRef = ref<FormInstance>()

const form = reactive({ name: '', condition: { type: 'rain_1h', threshold: 35 }, actions: [] as string[] })
const rules: FormRules = { name: [{required:true}] }

const condMap:{[k:string]:string}={'rain_1h':'1小时内有雨','temp_gt':'气温高于阈值','temp_lt':'气温低于阈值','aqi_gt':'AQI超标'}
function conditionLabel(c?:any){if(!c)return'-';return (condMap[c.type]||c.type)+(c.threshold?` >${c.threshold}`:'')}
const actionMap:{[k:string]:string}={'flash_icon':'闪烁图标','announcement':'发公告','remote_notify':'远程通知'}
function actionLabel(actions?:string[]){return (actions||[]).map(a=>actionMap[a]||a).join(', ')||'-'}

function showAddDialog(){ editRuleId.value=null; Object.assign(form,{name:'',condition:{type:'rain_1h',threshold:35},actions:[]}); dialogVisible.value=true }
function showEditDialog(row:any){ editRuleId.value=row.id; Object.assign(form,{name:row.name,condition:row.condition||{type:'rain_1h'},actions:row.actions||[]}); dialogVisible.value=true }

async function handleSubmit(){
  const ok=await formRef.value?.validate().catch(()=>false);if(!ok)return
  submitting.value=true
  try{await api.weather.createAlert({...form});ElMessage.success('保存成功');dialogVisible.value=false;fetchRules()}finally{submitting.value=false}
}

async function testAlert(id:number){try{await api.weather.testAlert(id);ElMessage.success('测试触发成功')}catch{}}
async function handleDelete(id:number){/* delete */}
async function toggleRule(row:any){/* update enabled */}

async function fetchRules(){
  loading.value=true
  try{const res:any=await api.weather.alertRuleList();ruleList.value=res.data||[
    {id:1,name:'高温预警',condition:{type:'temp_gt',threshold:35},actions:['flash_icon','announcement'],enabled:true},
    {id:2,name:'暴雨预警',condition:{type:'rain_1h'},actions:['flash_icon','remote_notify'],enabled:true},
  ]}catch{}finally{loading.value=false}
}

onMounted(fetchRules)
</script>

<style scoped>.weather-alerts{padding:20px;}@media(max-width:768px){.weather-alerts{padding:12px;}}</style>
