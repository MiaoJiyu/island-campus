<template>
  <div class="dashboard-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <el-statistic :title="item.title" :value="item.value">
            <template #prefix><el-icon :size="24" :style="{color: item.color}"><component :is="item.icon"/></el-icon></template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="never"><template #header><span>快捷操作</span></template>
          <div class="quick-actions">
            <el-button v-for="a in quickActions" :key="a.label" :type="a.type||'default'" @click="$router.push(a.path)">
              {{ a.label }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近动态 + 系统信息 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :md="14">
        <el-card shadow="never"><template #header><span>最近动态</span></template>
          <el-timeline>
            <el-timeline-item v-for="(log,i) in recentLogs" :key="i" :timestamp="log.time" placement="top">
              {{ log.content }}
            </el-timeline-item>
          </el-timeline>
          <el-empty v-if="!recentLogs.length" description="暂无动态" :image-size="60"/>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="never"><template #header><span>系统信息</span></template>
          <div class="sys-info"><p>系统版本: v1.0.0</p><p>在线设备: {{ stats[1].value }}</p><p>今日公告: {{ stats[2].value }}</p></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Monitor, Connection, Bell, Document } from '@element-plus/icons-vue'
import api from '@/api'

const stats = ref([
  { title: '总设备数', value: 0, icon: Monitor, color: '#409EFF' },
  { title: '在线设备', value: 0, icon: Connection, color: '#67C23A' },
  { title: '今日公告', value: 0, icon: Bell, color: '#E6A23C' },
  { title: '待处理考试', value: 0, icon: Document, color: '#F56C6C' },
])

const quickActions = [
  { label: '设备管理', path: '/computer/list', type: '' as const },
  { label: '灵动岛配置', path: '/island/config', type: 'primary' as const },
  { label: '考试管理', path: '/exam/list', type: '' as const },
  { label: '发布公告', path: '/announcement/list', type: 'success' as const },
  { label: '健康看板', path: '/health/dashboard', type: 'warning' as const },
]

const recentLogs = ref<Array<{ time: string; content: string }>>([])

onMounted(async () => {
  try {
    const [computerRes] = await Promise.all([api.computer.list({ page: 1, size: 1 })])
    // mock data for now
    stats.value[0].value = 128
    stats.value[1].value = 96
    stats.value[2].value = 5
    stats.value[3].value = 2
    recentLogs.value = [
      { time: '2024-01-15 10:30', content: '管理员 登录系统' },
      { time: '2024-01-15 10:25', content: '设备 PC-001 上报心跳' },
      { time: '2024-01-15 10:20', content: '发布公告 "考试安排通知"' },
      { time: '2024-01-15 10:15', content: '新建考试 "期中考试"' },
      { time: '2024-01-15 10:00', content: '用户 张三 修改密码' },
    ]
  } catch {}
})
</script>

<style scoped>
.dashboard-page { padding: 20px; }
.stat-card { text-align: center; margin-bottom: 12px; }
.quick-actions { display: flex; flex-wrap: wrap; gap: 10px; }
.sys-info p { margin: 8px 0; color: #606266; }
@media (max-width: 768px) {
  .dashboard-page { padding: 12px; }
  .stat-row .el-col { margin-bottom: 10px; }
}
</style>
