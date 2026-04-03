<template>
  <div class="island-config">
    <el-row :gutter="20">
      <!-- 左侧配置列表 -->
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="config-list-card">
          <template #header><span>配置列表</span></template>
          <el-tabs v-model="activeTab" @tab-change="loadConfigs">
            <el-tab-pane label="全局默认" name="global"/>
            <el-tab-pane label="年级覆盖" name="grade"/>
            <el-tab-pane label="班级覆盖" name="class"/>
          </el-tabs>
          <div class="config-list">
            <div v-for="cfg in configList" :key="cfg.id"
                 :class="['config-item',{active:selectedId===cfg.id}]"
                 @click="selectConfig(cfg)">
              <span class="config-name">{{ cfg.scopeName || '全局默认' }}</span>
              <el-tag size="small" :type="cfg.isDefault?'success':'info'">{{ cfg.isDefault?'默认':''}}</el-tag>
            </div>
            <el-empty v-if="!configList.length" description="暂无配置" :image-size="60"/>
          </div>
          <el-button style="width:100%;margin-top:12px;" type="primary" plain size="small" @click="createNewScope">+ 新增{{ tabLabel }}配置</el-button>
        </el-card>
      </el-col>

      <!-- 右侧编辑器 + 预览 -->
      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <span>配置编辑器</span>
              <el-button type="primary" :loading="saving" @click="saveConfig">保存配置</el-button>
            </div>
          </template>

          <el-form v-if="selectedId" label-width="110px" style="max-width:650px;">
            <el-form-item label="位置">
              <el-radio-group v-model="editor.position" @change="updatePreview">
                <el-radio-button value="top-left">左上</el-radio-button>
                <el-radio-button value="top-center">居中上</el-radio-button>
                <el-radio-button value="top-right">右上</el-radio-button>
                <el-radio-button value="bottom-center">底部居中</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="高度">
              <el-slider v-model="editor.height" :min="32" :max="80" show-input @input="updatePreview"/>
            </el-form-item>
            <el-form-item label="背景色"><el-color-picker v-model="editor.bgColor" @change="updatePreview"/></el-form-item>
            <el-form-item label="文字颜色"><el-color-picker v-model="editor.textColor" @change="updatePreview"/></el-form-item>
            <el-form-item label="圆角">
              <el-slider v-model="editor.borderRadius" :min="0" :max="24" show-input @input="updatePreview"/>
            </el-form-item>

            <el-divider>显示开关</el-divider>
            <el-form-item v-for="sw in switches" :key="sw.key" :label="sw.label">
              <el-switch v-model="editor[sw.key]" @change="updatePreview"/>
            </el-form-item>
            <el-form-item label="全屏自动隐藏">
              <el-switch v-model="editor.autoHideOnFullscreen" @change="updatePreview"/>
            </el-form-item>
          </el-form>

          <el-empty v-else description="请选择或新建一个配置"/>

          <!-- 实时预览区域 -->
          <el-divider>实时预览</el-divider>
          <div class="preview-area">
            <div class="preview-screen">
              <div class="preview-island" :style="previewStyle">
                <span v-if="editor.showLogo" class="pv-logo">🏫</span>
                <span v-if="editor.showDateTime" class="pv-time">10:30</span>
                <span v-if="editor.showCurrentCourse" class="pv-course">数学课</span>
                <span v-if="editor.showWeather" class="pv-weather">☀️26°</span>
                <span v-if="editor.showMarquee" class="pv-marquee">欢迎来到智慧课堂...</span>
                <span v-if="editor.showModeIcon" class="pv-mode">📖</span>
                <span v-if="editor.showHealthDot" class="pv-health dot-green"></span>
                <span v-if="editor.showMessageBadge" class="pv-msg-badge">3</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const activeTab = ref('global')
const configList = ref<any[]>([]); const selectedId = ref<number|null>(null); const saving = ref(false)

const switches = [
  { key: 'showLogo', label: '显示Logo' },
  { key: 'showDateTime', label: '显示时间' },
  { key: 'showCurrentCourse', label: '显示当前课程' },
  { key: 'showWeather', label: '显示天气' },
  { key: 'showMarquee', label: '显示跑马灯' },
  { key: 'showModeIcon', label: '显示模式图标' },
  { key: 'showHealthDot', label: '显示健康指示灯' },
  { key: 'showMessageBadge', label: '显示消息角标' },
]

const editor = reactive({
  position: 'top-center' as string,
  height: 44, bgColor: '#1a1a2e', textColor: '#ffffff',
  borderRadius: 16, autoHideOnFullscreen: false,
  showLogo: true, showDateTime: true, showCurrentCourse: true,
  showWeather: true, showMarquee: false, showModeIcon: true,
  showHealthDot: true, showMessageBadge: true,
})

const tabLabel = computed(()=> ({global:'全局',grade:'年级',class:'班级'})[activeTab.value] || '')

function updatePreview(){}

const previewStyle = computed(()=>{
  const posMap:{[k:string]:string}={
    'top-left':'top:10px;left:10px;',
    'top-center':'top:10px;left:50%;transform:translateX(-50%);',
    'top-right':'top:10px;right:10px;',
    'bottom-center':'bottom:10px;left:50%;transform:translateX(-50%);',
  }
  return `
    position:absolute;${posMap[editor.position]||''}
    height:${editor.height}px;background:${editor.bgColor};
    color:${editor.textColor};border-radius:${editor.borderRadius}px;
    padding:0 16px;display:flex;align-items:center;gap:8px;font-size:13px;
    box-shadow:0 2px 12px rgba(0,0,0,.3);
  `
})

function selectConfig(cfg:any){selectedId.value=cfg.id;Object.assign(editor,cfg.config||{})}
async function createNewScope(){/* create scope */}
async function saveConfig(){
  saving.value=true
  try{
    if(activeTab.value==='global'){await api.island.updateGlobal(editor)}
    else if(selectedId.value){await api.island.updateScope(selectedId.value,{...editor})}
    else{await api.island.createScope({...editor,scopeType:activeTab.value})}
    ElMessage.success('保存成功');await loadConfigs()
  }finally{saving.value=false}
}

async function loadConfigs(){
  try{const res:any=await api.island.getConfigList();configList.value=res.data||(activeTab.value==='global'?[{id:0,scopeName:'全局默认',config:{...editor},isDefault:true}]:[])}catch{}
}

onMounted(loadConfigs)
</script>

<style scoped>
.island-config{padding:20px}.config-list-card{height:calc(100vh - 160px);overflow-y:auto;}
.config-list{min-height:200px;}.config-item{padding:10px 12px;border-radius:6px;cursor:pointer;margin-bottom:6px;border:1px solid transparent;transition:all .2s;display:flex;justify-content:space-between;align-items:center;}
.config-item:hover{background:#f5f7fa;}.config-item.active{border-color:#409eff;background:#ecf5ff;}

.preview-area{background:#f0f0f0;border-radius:12px;padding:20px;overflow:hidden;}.preview-screen{position:relative;height:300px;background:#e8e8e8;border-radius:8px;overflow:hidden;}
.preview-island{z-index:10;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:90%;}
.pv-logo{font-size:16px;}.pv-time{font-weight:bold;}.pv-course{opacity:.85;}.pv-weather{}.pv-marquee{opacity:.75;font-style:italic;flex:1;overflow:hidden;}
.pv-mode{font-size:16px;}.pv-health{width:8px;height:8px;border-radius:50%;display:inline-block;}.dot-green{background:#67c23a;}
.pv-msg-badge{background:#f56c6c;color:#fff;border-radius:10px;padding:0 6px;font-size:11px;line-height:18px;}
@media(max-width:768px){.island-config{padding:12px;}}
</style>
