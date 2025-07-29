import { defineStore } from "pinia";
import { jwtDecode } from 'jwt-decode';

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

      try {
        const decoded = jwtDecode(accessToken);
        this.userId = decoded.userId;
      } catch (e) {
        console.error("JWT 디코딩 실패:", e);
        this.userId = null;
      }
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
