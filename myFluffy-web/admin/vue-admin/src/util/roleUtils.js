import { computed } from 'vue';

// 관리자 권한 레벨 확인
export const hasAnyRole = (userRoleId, allowedRoles) => {
  return allowedRoles.includes(userRoleId);
};

// 권한 계층 정의
export const roleHierarchy = [
  { roleId: 'ROLE_SUPER_ADMIN', roleName: '최고 관리자', level: 3 },
  { roleId: 'ROLE_ADMIN', roleName: '일반 관리자', level: 2 },
  { roleId: 'ROLE_MANAGER', roleName: '운영자', level: 1 },
];

// ROLE_ID로 권한 레벨 가져오기
export const getRoleLevel = (roleId) => {
  const role = roleHierarchy.find(r => r.roleId === roleId);
  return role ? role.level : 0;
};

// 선택 가능한 권한 필터 (사용자의 권한 레벨 기준)
export const getAvailableRoles = (userRoleId) => {
  const userLevel = getRoleLevel(userRoleId);
  return roleHierarchy.filter(role => role.level <= userLevel);
};

// 반응형 computed - 권한 레벨 가져오기
export const useCurrentUserRoleLevel = (authStore) => {
  return computed(() => getRoleLevel(authStore.roleId));
};

// 반응형 computed - 선택 가능한 권한 필터 (현재 사용자의 권한 레벨 기준)
export const useAvailableRoles = (authStore) => {
  return computed(() => {
    const userLevel = getRoleLevel(authStore.roleId);
    return roleHierarchy.filter(role => role.level <= userLevel);
  });
};