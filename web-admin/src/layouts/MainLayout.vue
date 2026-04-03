<template>
  <el-container class="main-layout">
    <!-- PC端侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar mobile-hidden">
      <div class="logo-area" :class="{ collapsed: isCollapse }">
        <img src="/logo.svg" alt="" class="logo-img" v-if="!isCollapse">
        <span class="logo-text" v-show="!isCollapse">灵岛校园</span>
      </div>
      <el-menu :default-active="activeMenu" :collapse="isCollapse" router
               background-color="#001529" text-color="#ffffffa6"
               active-text-color="#409eff" class="sidebar-menu">
        <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon><template #title>仪表盘</template></el-menu-item>

        <el-sub-menu index="system">
          <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
          <el-menu-item index="/system/org">组织架构</el-menu-item>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色权限</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="core">
          <template #title><el-icon><Monitor /></el-icon><span>核心功能</span></template>
          <el-menu-item index="/island/config">灵动岛配置</el-menu-item>
          <el-menu-item index="/mode/list">情景模式</el-menu-item>
          <el-menu-item index="/exam/list">考试管理</el-menu-item>
          <el-menu-item index="/weather/config">天气服务</el-menu-item>
          <el-menu-item index="/weather/alerts">天气提醒</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="comm">
          <template #title><el-icon><Bell /></el-icon><span>通讯公告</span></template>
          <el-menu-item index="/announcement">公告广播</el-menu-item>
          <el-menu-item index="/message/send">远程消息</el-menu-item>
        </el-sub-menu>

        <el-sub-menu id="edu">
          <template #title><el-icon><Reading /></el-icon><span>教学辅助</span></template>
          <el-menu-item index="/answer/manage">答案公布</el-menu-item>
          <el-menu-item index="/health/dashboard">健康看板</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="device">
          <template #title><el-icon><Desktop /></el-icon><span>设备令牌</span></template>
          <el-menu-item index="/computer/list">设备管理</el-menu-item>
          <el-menu-item index="/token/manage">查询令牌</el-menu-item>
          <el-menu-item index="/plugin/manage">插件管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse"><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          <!-- 移动端菜单按钮 -->
          <el-button class="mobile-only" text @click="showMobileMenu = true">
            <el-icon size="20"><Menu /></el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRoute?.meta?.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
            <el-button text @click="$router.push('/message/send')"><el-icon><Bell /></el-icon> 消息
            </el-button>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="28">{{ userStore.userInfo?.realName?.charAt(0) || '?' }}</el-avatar>
              <span class="mobile-hidden username">{{ userStore.userInfo?.realName || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <!-- 移动端抽屉菜单 -->
    <el-drawer v-model="showMobileMenu" direction="ltr" size="220px" :with-header="false">
      <div style="padding: 16px; font-weight: bold; color: #409eff;">灵岛校园</div>
      <el-menu :default-active="activeMenu" router>
        <el-menu-item v-for="item in mobileMenuItems" :key="item.path" :index="item.path"
                      @click="showMobileMenu = false">{{ item.title }}</el-menu-item>
      </el-menu>
    </el-drawer>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const route = useRoute()
const userStore = useUserStore()
const isCollapse = ref(false)
const showMobileMenu = ref(false)
const unreadCount = ref(0)

const activeMenu = computed(() => '/' + (route.path.split('/')[1] + (route.path.split('/')[2] ? '/' + route.path.split('/')[2] : '')))
const currentRoute = computed(() => route)

const mobileMenuItems = [
  { path: '/dashboard', title: '仪表盘' },
  { path: '/system/org', title: '组织管理' },
  { path: '/island/config', title: '灵动岛配置' },
  { path: '/mode/list', title: '情景模式' },
  { path: '/exam/list', title: '考试管理' },
  { path: '/weather/config', title: '天气服务' },
  { path: '/announcement', title: '公告管理' },
  { path: '/health/dashboard', title: '健康看板' },
  { path: '/computer/list', title: '设备管理' },
]

// 获取未读消息数
api.message.unreadCount().then((res: any) => { unreadCount.value = res.data }).catch(() => {})

function handleCommand(cmd: string) {
  if (cmd === 'logout') userStore.logout()
}
</script>

<style scoped lang="scss">
.main-layout { height: 100vh; overflow: hidden; }
.sidebar { background-color: #001529; transition: width 0.3s; overflow-y: auto; z-index: 10; }
.logo-area { height: 60px; display: flex; align-items: center; padding: 0 16px; gap: 8px; border-bottom: 1px solid rgba(255,255,255,0.08); &.collapsed { justify-content: center; padding: 0; }}
.logo-img { width: 32px; height: 32px; }
.logo-text { font-size: 18px; font-weight: bold; color: #fff; white-space: nowrap; }
.sidebar-menu { border-right: none !important; }
.header { display: flex; align-items: center; justify-content: space-between; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.08); padding: 0 20px; height: var(--header-height); z-index: 9; }
.header-left { display: flex; align-items: center; gap: 12px; }
.collapse-btn { font-size: 20px; cursor: pointer; color: #606266; &:hover { color: var(--primary-color); } }
.header-right { display: flex; align-items: center; gap: 12px; }
.user-info { display: flex; align-items: center; gap: 6px; cursor: pointer; .username { font-size: 14px; color: #303133; }}
.main-content { background: var(--bg-color); overflow-y: auto; }

@media (max-width: 768px) {
  .mobile-only { display: block !important; }
  .header-right .username { display: none; }
}

.mobile-only { display: none; }

.collapse-btn:hover { opacity: 0.7; }
</style>
