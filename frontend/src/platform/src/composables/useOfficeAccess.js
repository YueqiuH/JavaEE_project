import { computed, inject, ref } from 'vue'
import { getStoredCurrentUser } from '@/utils/authSession.js'

export const useOfficeAccess = () => {
  const currentUser = inject('currentUser', ref(getStoredCurrentUser()))
  const userId = computed(() => currentUser.value?.user?.userId ?? null)
  const permissions = computed(() => new Set(currentUser.value?.permissions || []))
  const hasPermission = (permission) => permissions.value.has(permission)

  return { currentUser, userId, permissions, hasPermission }
}
