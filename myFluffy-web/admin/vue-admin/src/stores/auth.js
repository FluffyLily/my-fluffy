import { defineStore } from "pinia";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    accessToken: localStorage.getItem("accessToken") || null,
    refreshToken: localStorage.getItem("refreshToken") || null,
    userId: null,
    loginId: null,
    roleId: null
  }),
  actions: {
    setAccessToken(accessToken) {
      this.accessToken = accessToken;
      localStorage.setItem("accessToken", accessToken);
    },
    clearAccessToken() {
      this.accessToken = null;
      localStorage.removeItem("accessToken");
    },
    setRefreshToken(refreshToken) {
      this.refreshToken = refreshToken;
    },
    clearRefreshToken() {
      this.refreshToken = null;
    },
    setUserId(userId) {
      this.userId = userId;
    },
    clearUserId() {
      this.userId = null;
    },
    setLoginId(loginId) {
      this.loginId = loginId;
    },
    clearLoginId() {
      this.loginId = null;
    },
    setRoleId(roleId) {
      this.roleId = roleId;
    },
    clearRoleId() {
      this.roleId = null;
    }
  },
});
