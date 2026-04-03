<template>
  <div class="health-dashboard">
    <div class="header">
      <h2>{{ $route.meta.title }}</h2>
      <div class="date-range">
        <span>统计时间：</span>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :shortcuts="dateShortcuts"
          @change="loadDashboardData"
        />
      </div>
    </div>

    <!-- 概览卡片 -->
    <div class="overview-cards">
      <el-card class="card">
        <template #header>
          <div class="card-header">设备健康概览</div>
        </template>
        <div class="card-content">
          <div class="health-status">
            <div class="status-item">
              <div class="status-label">健康设备</div>
              <div class="status-value status-green">{{ dashboardData.healthyCount || 0 }}</div>
              <div class="status-percent">{{ formatPercent(dashboardData.healthyPercent) }}</div>
            </div>
            <div class="status-divider"></div>
            <div class="status-item">
              <div class="status-label">预警设备</div>
              <div class="status-value status-yellow">{{ dashboardData.warningCount || 0 }}</div>
              <div class="status-percent">{{ formatPercent(dashboardData.warningPercent) }}</div>
            </div>
            <div class="status-divider"></div>
            <div class="status-item">
              <div class="status-label">异常设备</div>
              <div class="status-value status-red">{{ dashboardData.errorCount || 0 }}</div>
              <div class="status-percent">{{ formatPercent(dashboardData.errorPercent) }}</div>
            </div>
            <div class="status-divider"></div>
            <div class="status-item">
              <div class="status-label">离线设备</div>
              <div class="status-value status-gray">{{ dashboardData.offlineCount || 0 }}</div>
              <div class="status-percent">{{ formatPercent(dashboardData.offlinePercent) }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="chart-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">设备健康趋势</div>
            </template>
            <div class="chart-container">
              <div ref="trendChart" style="width: 100%; height: 320px"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">健康状态分布</div>
            </template>
            <div class="chart-container">
              <div ref="pieChart" style="width: 100%; height: 320px"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="24">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">异常类型统计</div>
              <div class="card-extra">
                <el-button type="text" @click="exportProblemReport">导出报告</el-button>
              </div>
            </template>
            <div class="chart-container">
              <el-table :data="problemStatList" style="width: 100%" v-loading="loading">
                <el-table-column prop="problemType" label="异常类型" width="180">
                  <template #default="{ row }">
                    {{ problemTypeMap[row.problemType] || row.problemType }}
                  </template>
                </el-table-column>
                <el-table-column prop="count" label="发生次数" width="120">
                  <template #default="{ row }">
                    <span :class="{
                      'text-warning': row.count > 10,
                      'text-success': row.count <= 3
                    }">
                      {{ row.count }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="affectedComputers" label="影响设备数" width="120">
                  <template #default="{ row }">
                    <span class="text-primary">{{ row.affectedComputers }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="autoFixedCount" label="自动修复次数" width="120">
                  <template #default="{ row }">
                    <span class="text-success">{{ row.autoFixedCount }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="manualFixedCount" label="手动修复次数" width="120">
                  <template #default="{ row }">
                    <span class="text-info">{{ row.manualFixedCount }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="修复成功率" width="120">
                  <template #default="{ row }">
                    <span :class="{
                      'text-success': row.fixSuccessRate >= 80,
                      'text-warning': row.fixSuccessRate >= 50 && row.fixSuccessRate < 80,
                      'text-danger': row.fixSuccessRate < 50
                    }">
                      {{ row.fixSuccessRate }}%
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="建议操作">
                  <template #default="{ row }">
                    <span v-if="row.problemType === 'cpu_high'">建议清理后台程序或降低系统负荷</span>
                    <span v-else-if="row.problemType === 'disk_full'">建议清理垃圾文件</span>
                    <span v-else-if="row.problemType === 'memory_high'">建议关闭不必要应用</span>
                    <span v-else-if="row.problemType === 'temperature_high'">建议改善散热条件</span>
                    <span v-else-if="row.problemType === 'virus'">建议全盘查杀病毒</span>
                    <span v-else-if="row.problemType === 'network_slow'">建议检查网络连接</span>
                    <span v-else>建议关注设备状态</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 设备详情 -->
    <div class="device-detail-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>设备健康详情</span>
            <div style="display: flex; gap: 8px;">
              <el-input v-model="deviceSearch" placeholder="搜索设备名称/MAC地址" style="width: 200px" />
              <el-select v-model="deviceFilterStatus" placeholder="状态筛选" style="width: 120px">
                <el-option label="全部状态" value="" />
                <el-option label="健康" value="green" />
                <el-option label="预警" value="yellow" />
                <el-option label="异常" value="red" />
                <el-option label="离线" value="offline" />
              </el-select>
              <el-button type="primary" @click="sendFixCommandAll" :disabled="loading">批量修复</el-button>
            </div>
          </div>
        </template>
        <div class="device-list">
          <div v-for="device in filteredDevices" :key="device.computerId" class="device-card">
            <div class="device-header">
              <div class="device-info">
                <div class="device-name">
                  <span>{{ device.computerName }}</span>
                  <el-tag size="small" :type="getStatusType(device.healthStatus)">
                    {{ getStatusText(device.healthStatus) }}
                  </el-tag>
                </div>
                <div class="device-meta">
                  <span>MAC: {{ device.macAddress }}</span>
                  <span>IP: {{ device.ipAddress }}</span>
                  <span>最后上报: {{ formatDateTime(device.lastReportTime) }}</span>
                </div>
              </div>
              <div class="device-actions">
                <el-button size="small" @click="viewDeviceDetail(device.computerId)">详情</el-button>
                <el-button
                  v-if="device.healthStatus === 2"
                  size="small"
                  type="success"
                  @click="sendFixCommand(device.computerId)"
                >
                  修复
                </el-button>
              </div>
            </div>
            <div class="device-problems" v-if="device.problemSummary && device.problemSummary.length > 0">
              <div class="problems-title">问题概览：</div>
              <div class="problems-content">
                {{ device.problemSummary.join('；') }}
              </div>
            </div>
          </div>
        </div>
        <div class="pagination">
          <el-pagination
            v-model:current-page="deviceQuery.current"
            v-model:page-size="deviceQuery.size"
            :total="deviceTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadDeviceList"
            @current-change="loadDeviceList"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'
import { formatDateTime } from '@/utils/format'

interface DashboardData {
  healthyCount: number
  warningCount: number
  errorCount: number
  offlineCount: number
  totalCount: number
  healthyPercent: number
  warningPercent: number
  errorPercent: number
  offlinePercent: number
}

interface ProblemStat {
  problemType: string
  count: number
  affectedComputers: number
  autoFixedCount: number
  manualFixedCount: number
  fixSuccessRate: number
}

interface DeviceHealthVO {
  computerId: number
  computerName: string
  macAddress: string
  ipAddress: string
  classId: number
  className: string
  healthStatus: number  // 0: 绿色，1: 黄色，2: 红色，3: 离线
  lastReportTime: string
  problemSummary?: string[]
}

const dateRange = ref<[Date, Date] | null>(null)
const loading = ref(false)
const dashboardData = ref<DashboardData>({
  healthyCount: 0,
  warningCount: 0,
  errorCount: 0,
  offlineCount: 0,
  totalCount: 0,
  healthyPercent: 0,
  warningPercent: 0,
  errorPercent: 0,
  offlinePercent: 0
})

const problemStatList = ref<ProblemStat[]>([])
const deviceList = ref<DeviceHealthVO[]>([])
const deviceTotal = ref(0)
const deviceSearch = ref('')
const deviceFilterStatus = ref('')
const deviceQuery = reactive({
  current: 1,
  size: 10
})

const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: ECharts | null = null
let pieChart: ECharts | null = null

const dateShortcuts = [
  { text: '最近7天', value: () => {
    const end = new Date()
    const start = new Date()
    start.setDate(start.getDate() - 7)
    return [start, end]
  }},
  { text: '最近30天', value: () => {
    const end = new Date()
    const start = new Date()
    start.setDate(start.getDate() - 30)
    return [start, end]
  }},
  { text: '最近90天', value: () => {
    const end = new Date()
    const start = new Date()
    start.setDate(start.getDate() - 90)
    return [start, end]
  }}
]

const problemTypeMap = {
  'cpu_high': 'CPU使用率过高',
  'disk_full': '磁盘空间不足',
  'memory_high': '内存占用过高',
  'temperature_high': 'CPU温度过高',
  'virus': '病毒或恶意程序',
  'network_slow': '网络延迟过高',
  'process_blacklist': '黑名单进程运行',
  'system_crash': '系统崩溃/蓝屏',
  'browser_crash': '浏览器崩溃',
  'service_down': '关键服务异常'
}

onMounted(() => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 7)
  dateRange.value = [start, end]
  
  loadDashboardData()
  initCharts()
})

// 计算属性：过滤设备列表
const filteredDevices = computed(() => {
  let filtered = deviceList.value
  
  if (deviceSearch.value) {
    const search = deviceSearch.value.toLowerCase()
    filtered = filtered.filter(d => 
      d.computerName.toLowerCase().includes(search) ||
      d.macAddress.toLowerCase().includes(search) ||
      d.ipAddress.includes(search)
    )
  }
  
  if (deviceFilterStatus.value) {
    const statusMap = { green: 0, yellow: 1, red: 2, offline: 3 }
    filtered = filtered.filter(d => {
      if (deviceFilterStatus.value === 'offline') {
        return d.healthStatus === 3
      }
      return d.healthStatus === statusMap[deviceFilterStatus.value as keyof typeof statusMap]
    })
  }
  
  return filtered
})

// 加载仪表盘数据
async function loadDashboardData() {
  loading.value = true
  try {
    // 加载仪表盘概要
    const res1: any = await api.health.dashboard()
    console.log(res1.data)
    dashboardData.value = res1.data
    
    // 加载问题统计
    const res2: any = await api.health.problemStats({
      startTime: dateRange.value?.[0]?.toISOString(),
      endTime: dateRange.value?.[1]?.toISOString()
    })
    problemStatList.value = res2.data
    
    // 加载设备列表
    await loadDeviceList()
    
    // 加载趋势图数据
    await loadTrendData()
    
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
    nextTick(() => {
      if (trendChart) trendChart.resize()
      if (pieChart) pieChart.resize()
    })
  }
}

// 加载设备列表
async function loadDeviceList() {
  try {
    const res: any = await api.health.deviceList(deviceQuery)
    deviceList.value = res.data.records
    deviceTotal.value = res.data.total
  } catch (e) {
    console.error(e)
  }
}

// 加载趋势数据
async function loadTrendData() {
  try {
    const res: any = await api.health.trend({
      startTime: dateRange.value?.[0]?.toISOString(),
      endTime: dateRange.value?.[1]?.toISOString(),
      type: 'daily'
    })
    updateTrendChart(res.data)
    updatePieChart()
  } catch (e) {
    console.error(e)
  }
}

// 初始化图表
function initCharts() {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
}

// 更新趋势图
function updateTrendChart(data: any) {
  if (!trendChart || !data) return
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['健康数', '预警数', '异常数', '离线数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.dates || []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '健康数',
        type: 'line',
        smooth: true,
        data: data.healthy || [],
        lineStyle: { color: '#67C23A' },
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '预警数',
        type: 'line',
        smooth: true,
        data: data.warning || [],
        lineStyle: { color: '#E6A23C' },
        itemStyle: { color: '#E6A23C' }
      },
      {
        name: '异常数',
        type: 'line',
        smooth: true,
        data: data.error || [],
        lineStyle: { color: '#F56C6C' },
        itemStyle: { color: '#F56C6C' }
      },
      {
        name: '离线数',
        type: 'line',
        smooth: true,
        data: data.offline || [],
        lineStyle: { color: '#909399' },
        itemStyle: { color: '#909399' }
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 更新饼图
function updatePieChart() {
  if (!pieChart || !dashboardData.value) return
  
  const data = [
    { value: dashboardData.value.healthyCount, name: '健康', itemStyle: { color: '#67C23A' } },
    { value: dashboardData.value.warningCount, name: '预警', itemStyle: { color: '#E6A23C' } },
    { value: dashboardData.value.errorCount, name: '异常', itemStyle: { color: '#F56C6C' } },
    { value: dashboardData.value.offlineCount, name: '离线', itemStyle: { color: '#909399' } }
  ].filter(item => item.value > 0)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        name: '健康状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}\n({d}%)'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold'
          }
        },
        data: data
      }
    ]
  }
  
  pieChart.setOption(option)
}

// 工具函数
function formatPercent(percent: number): string {
  return percent ? `${percent.toFixed(1)}%` : '0%'
}

function getStatusType(status: number): string {
  const types = ['success', 'warning', 'danger', 'info']
  return types[Math.min(status, 3)] || 'info'
}

function getStatusText(status: number): string {
  const texts = ['健康', '预警', '异常', '离线']
  return texts[Math.min(status, 3)] || '未知'
}

// 查看设备详情
function viewDeviceDetail(computerId: number) {
  ElMessage.info(`查看设备 ${computerId} 的详细健康报告`)
}

// 发送修复命令
async function sendFixCommand(computerId: number) {
  try {
    await ElMessageBox.confirm('确定要发送修复命令吗？该操作将由客户端执行自动修复', '提示', {
      type: 'warning'
    })
    await api.health.fix({ computerId })
    ElMessage.success('修复命令已发送')
    await loadDashboardData()
  } catch {
    // 用户取消
  }
}

// 批量修复
async function sendFixCommandAll() {
  try {
    await ElMessageBox.confirm('确定要批量修复所有选中的异常设备吗？', '提示', {
      type: 'warning'
    })
    const promises = deviceList.value
      .filter(d => d.healthStatus === 2)
      .map(d => api.health.fix({ computerId: d.computerId }))
    await Promise.all(promises)
    ElMessage.success('批量修复命令已发送')
    await loadDashboardData()
  } catch {
    // 用户取消
  }
}

// 导出报告
async function exportProblemReport() {
  try {
    loading.value = true
    await api.health.exportReport({
      startTime: dateRange.value?.[0]?.toISOString(),
      endTime: dateRange.value?.[1]?.toISOString()
    })
    ElMessage.success('报告导出成功')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.health-dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 40px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.overview-cards {
  margin-bottom: 20px;
}

.card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  font-weight: 500;
  font-size: 16px;
  color: #303133;
}

.health-status {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 20px 0;
}

.status-item {
  text-align: center;
  flex: 1;
}

.status-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.status-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 4px;
}

.status-green { color: #67C23A; }
.status-yellow { color: #E6A23C; }
.status-red { color: #F56C6C; }
.status-gray { color: #909399; }

.status-percent {
  font-size: 14px;
  color: #606266;
}

.status-divider {
  width: 1px;
  height: 60px;
  background: #dcdfe6;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.chart-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  padding: 4px;
}

.text-primary { color: #409EFF; }
.text-success { color: #67C23A; }
.text-warning { color: #E6A23C; }
.text-danger { color: #F56C6C; }
.text-info { color: #909399; }

.device-detail-section {
  margin-top: 20px;
}

.device-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.device-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  transition: box-shadow 0.3s;
}

.device-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.device-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.device-info {
  flex: 1;
}

.device-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.device-name span {
  font-weight: 500;
  font-size: 16px;
  color: #303133;
}

.device-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.device-actions {
  display: flex;
  gap: 8px;
}

.device-problems {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.problems-title {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}

.problems-content {
  font-size: 13px;
  color: #f56c6c;
  line-height: 1.4;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .health-status {
    flex-direction: column;
    gap: 20px;
  }
  
  .status-divider {
    width: 80%;
    height: 1px;
    margin: 10px 0;
  }
  
  .device-list {
    grid-template-columns: 1fr;
  }
}
</style>