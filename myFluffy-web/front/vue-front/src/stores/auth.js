import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: null,
    userId: localStorage.getItem('userId') || null,
    loginId: localStorage.getItem('loginId') || '',
    userName: localStorage.getItem('userName') || '',
    userType: localStorage.getItem('userType') || '',
  }),
  actions: {
    setAccessToken(accessToken) {
      this.accessToken = accessToken
    },
    clearAccessToken() {
      this.accessToken = null;
    },
    setUserInfo({ userId, loginId, userName, userType }) {
      this.userId = userId
      this.loginId = loginId
      this.userName = userName
      this.userType = userType
      
      localStorage.setItem('userId', userId)
      localStorage.setItem('loginId', loginId)
      localStorage.setItem('userName', userName)
      localStorage.setItem('userType', userType)
    },
    clearUserInfo() {
      this.accessToken = null;
      this.userId = null;
      this.userName = null;
      this.loginId = null;
      this.userType = null;

      localStorage.removeItem('userId');
      localStorage.removeItem('loginId');
      localStorage.removeItem('userName');
      localStorage.removeItem('userType');
    },
  },
})