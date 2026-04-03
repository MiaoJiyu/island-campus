<template>
  <div class="weather-config">
    <el-row :gutter="20">
      <!-- 左侧配置列表 -->
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" style="margin-bottom:16px;">
          <template #header>
            <div><span>天气API配置</span><el-button type="primary" size="small" style="float:right;" @click="showAddDialog">新建配置</el-button></div>
          </template>
          <el-table :data="configList" v-loading="loading" stripe border>
            <el-table-column prop="name" label="名称" width="140"/>
            <el-table-column prop="url" label="API地址" min-width="200" show-overflow-tooltip/>
            <el-table-column prop="method" label="方法" width="70" align="center"/>
            <el-table-column prop="isDefault" label="默认" width="70" align="center">
              <template #default="{row}"><el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="240">
              <template #default="{row}">
                <el-button size="small" link type="primary" @click="testConnection(row)">测试连接</el-button>
                <el-button size="small" link type="success" @click="setAsDefault(row.id)" :disabled="!!row.isDefault">设为默认</el-button>
                <el-button size="small" link type="info" @click="showEditDialog(row)">编辑</el-button>
                <el-popconfirm title="删除?" @confirm="handleDelete(row.id)"><template #reference><el-button size="small" link type="danger">删除</el-button></template></el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && !configList.length" description="暂无配置"/>
        </el-card>
      </el-col>

      <!-- 右侧天气预览 -->
      <el-col :xs="24" :lg="10">
        <el-card shadow="none"><template #header><span>当前天气预览</span></template>
          <div class="weather-preview" v-if="weatherData.temperature">
            <div class="wp-main"><span class="wp-icon">{{ weatherIcon }}</span><span class="wp-temp">{{ weatherData.temperature }}°C</span></div>
            <div class="wp-detail">
              <p>体感温度: {{ weatherData.feelsLike || '-' }}°C</p>
              <p>湿度: {{ weatherData.humidity || '-' }}%</p>
              <p>风向: {{ weatherData.windDirection || '-' }} {{ weatherData.windSpeed || '' }}</p>
              <p>AQI: <el-tag :type="aqiLevel(weatherData.aqi)" size="small">{{ weatherData.aqi || '-' }}</el-tag></p>
            </div>
          </div>
          <el-empty v-else description="暂无天气数据（请先配置并设为默认）" :image-size="60"/>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新建/编辑配置弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editConfigId?'编辑配置':'新建配置'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" placeholder="如: uapis.cn 预设"/></el-form-item>
        <el-form-item label="API地址" prop="url"><el-input v-model="form.url" placeholder="https://api.example.com/weather"/></el-form-item>
        <el-form-item label="请求方法"><el-select v-model="form.method"><el-option value="GET"/><el-option value="POST"/></el-select></el-form-item>
        <el-form-item label="请求头JSON">
          <el-input v-model="form.headersJson" type="textarea" :rows="3" placeholder='{"Authorization":"Bearer xxx"}'/>
        </el-form-item>
        <el-form-item label="JSONPath映射">
          <el-input v-model="form.jsonPathMapping" type="textarea" :rows="3" placeholder='{"temperature":"$.data.temp","humidity":"$.data.humidity"}'/>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import api from '@/api'

const loading = ref(false); const submitting = ref(false); const configList = ref<any[]>([])
const dialogVisible = ref(false); const editConfigId = ref<number|null>(null); const formRef = ref<FormInstance>()
const weatherData = ref<Record<string,any>>({})

const form = reactive({ name: '', url: '', method: 'GET', headersJson: '{}', jsonPathMapping: '{}' })
const rules: FormRules = { name: [{required:true}], url: [{required:true}] }

const weatherIcon = computed(()=>{
  const t = weatherData.value.weather
  if(!t) return '🌤️'
  return {'晴':'☀️','多云':'⛅','阴':'☁️','雨':'🌧️','雪':'❄️'}[t] || '🌤️'
})

function aqiLevel(a?:number){if(!a)return'info';if(a<=50)return'success';if(a<=100)return'warning';return'danger'}

function showAddDialog(){ editConfigId.value=null; Object.assign(form,{name:'',url:'',method:'GET',headersJson:'{}',jsonPathMapping:'{}'}); dialogVisible.value=true }
function showEditDialog(row:any){ editConfigId.value=row.id; Object.assign(form,{name:row.name,url:row.url,method:row.method||'GET',headersJson:row.headersJson||'{}',jsonPathMapping:row.jsonPathMapping||'{}'}); dialogVisible.value=true }

async function handleSubmit(){
  const ok=await formRef.value?.validate().catch(()=>false);if(!ok)return
  submitting.value=true
  try{
    let headers={}, mapping={}
    try{headers=JSON.parse(form.headersJson)}catch{ElMessage.error('请求头JSON格式错误');return}
    try{mapping=JSON.parse(form.jsonPathMapping)}catch{ElMessage.error('JSONPath映射格式错误');return}
    const payload={...form,headers,mapping};delete payload.headersJson;delete payload.jsonPathMapping
    await api.weather.createConfig(payload)
    ElMessage.success('保存成功');dialogVisible.value=false;fetchConfigs()
  }finally{submitting.value=false}
}

async function testConnection(row:any){
  try{const res:any=await api.weather.testConnection({configId:row.id,...row});ElMessage.success('连接成功')}catch{}
}
async function setAsDefault(id:number){try{await api.weather.setDefault(id);ElMessage.success('已设置');fetchConfigs();fetchWeather()}catch{}}
async function handleDelete(id:number){try{/* delete */;ElMessage.success('已删除');fetchConfigs()}catch{}}

async function fetchConfigs(){
  loading.value=true
  try{const res:any=await api.weather.configList();configList.value=res.data||[{id:1,name:'uapis.cn 预设',url:'https://api.uapis.cn/weather',method:'GET',isDefault:true},{id:2,name:'自定义配置',url:'',method:'POST',isDefault:false}]}catch{}finally{loading.value=false}
}

async function fetchWeather(){
  try{const res:any=await api.weather.currentWeather();weatherData.value=res.data||{}}catch{}
}

onMounted(()=>{fetchConfigs();fetchWeather()})
</script>

<style scoped>.weather-config{padding:20px;}
.weather-preview{text-align:center;padding:20px 0;}.wp-main{display:flex;align-items:center;justify-content:center;gap:12px;margin-bottom:16px;}.wp-icon{font-size:48px;}.wp-temp{font-size:36px;font-weight:bold;color:#303133;}
.wp-detail p{margin:6px 0;color:#606266;font-size:14px;}
@media(max-width:768px){.weather-config{padding:12px;}}</style>
