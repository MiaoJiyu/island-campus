import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'
import router from '@/router'

export interface UserInfo {
  userId: number; username: string; realName: string
  roleId: number; roleName: string; orgId: number | null; orgName: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const refreshToken = ref<string>(localStorage.getItem('refreshToken') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.roleId === 1)

  async function login(username: string, password: string) {
    const res = await api.auth.login({ username, password })
    token.value = res.data.token
    refreshToken.value = res.data.refreshToken
    userInfo.value = res.data.userInfo
    localStorage.setItem('token', token.value)
    localStorage.setItem('refreshToken', refreshToken.value)
    return res
  }

  function logout() {
    token.value = ''; refreshToken.value = ''; userInfo.value = null
    localStorage.removeItem('token'); localStorage.removeItem('refreshToken')
    router.push('/login')
  }

  async function fetchUserInfo() {
    try { const res = await api.auth.info(); userInfo.value = res.data }
    catch { /* token可能已过期 */ }
  }

  return { token, refreshToken, userInfo, isLoggedIn, isAdmin, login, logout, fetchUserInfo }
})
