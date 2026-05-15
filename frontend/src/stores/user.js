import { defineStore } from 'pinia'

const STORAGE_KEY = 'xuance_user'
const BIRTH_PROFILE_KEY = 'zhexuan_birth_profile'
const APP_VERSION_KEY = 'zhexuan_app_version'
const APP_VERSION = '2026-05-14-permission-v2'

if (localStorage.getItem(APP_VERSION_KEY) !== APP_VERSION) {
  localStorage.removeItem(STORAGE_KEY)
  localStorage.setItem(APP_VERSION_KEY, APP_VERSION)
}

export const useUserStore = defineStore('user', {
  state: () => ({
    user: JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null')
  }),
  getters: {
    isLogin: state => Boolean(state.user),
    userId: state => state.user?.id,
    isAdmin: state => state.user?.role === 'ADMIN'
  },
  actions: {
    setUser(user) {
      this.user = user
      localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
    },
    clearUser() {
      this.user = null
      localStorage.removeItem(STORAGE_KEY)
    },
    getBirthProfile() {
      const localProfile = JSON.parse(localStorage.getItem(BIRTH_PROFILE_KEY) || 'null')
      if (localProfile) return localProfile
      if (!this.user) return null
      return {
        gender: this.user.gender || '',
        birthDate: this.user.birthDate || '',
        birthTime: this.user.birthTime || '',
        birthPlace: this.user.birthPlace || '',
        birthDayGanZhi: this.user.birthDayGanZhi || '',
        birthDayMaster: this.user.birthDayMaster || ''
      }
    },
    saveBirthProfile(profile) {
      const cleanProfile = {
        gender: profile.gender || '',
        birthDate: profile.birthDate || '',
        birthTime: profile.birthTime || '',
        birthProvince: profile.birthProvince || '',
        birthPlace: profile.birthPlace || '',
        useTrueSolarTime: Boolean(profile.useTrueSolarTime),
        yearPillar: profile.yearPillar || '',
        monthPillar: profile.monthPillar || '',
        dayPillar: profile.dayPillar || profile.birthDayGanZhi || '',
        hourPillar: profile.hourPillar || '',
        dayMaster: profile.dayMaster || profile.birthDayMaster || '',
        birthDayGanZhi: profile.birthDayGanZhi || profile.dayPillar || '',
        birthDayMaster: profile.birthDayMaster || profile.dayMaster || '',
        savedAt: new Date().toISOString()
      }
      localStorage.setItem(BIRTH_PROFILE_KEY, JSON.stringify(cleanProfile))
      if (this.user) {
        this.user = { ...this.user, ...cleanProfile }
        localStorage.setItem(STORAGE_KEY, JSON.stringify(this.user))
      }
      return cleanProfile
    }
  }
})
