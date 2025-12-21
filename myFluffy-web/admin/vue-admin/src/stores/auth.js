import { defineStore } from "pinia";
import { jwtDecode } from 'jwt-decode';

export const useAuthStore = defineStore("auth", {
  state: () => ({
    accessToken: null,
    userId: null,
    loginId: null,
    roleId: null
  }),
  actions: {
    setAccessToken(accessToken) {
      this.accessToken = accessToken;

      try {
        const decodedToken = jwtDecode(accessToken);
          this.userId = decodedToken.userId;
          this.loginId = decodedToken.sub;
          this.roleId = decodedToken.role;
          
      } catch {
        this.clearAccessToken();
      }
    },

    clearAccessToken() {
      this.accessToken = null
      this.userId = null;
      this.loginId = null;
      this.roleId = null;
    }
  },
});
