import { delStorage, getStorage, setStorage } from '@/utils/localStorage.js'
import { ref } from 'vue'

const CURRENT_USER_KEY = 'smart-campus.current-user'

export const reactiveUser = ref(loadFromStorage())

function loadFromStorage() {
    const serialized = getStorage(CURRENT_USER_KEY)
    if (!serialized) return null
    try { return JSON.parse(serialized) } catch { delStorage(CURRENT_USER_KEY); return null }
}

export const getStoredCurrentUser = () => {
    const serializedUser = getStorage(CURRENT_USER_KEY)
    if (!serializedUser) return null
    try { return JSON.parse(serializedUser) } catch { delStorage(CURRENT_USER_KEY); return null }
}

export const setStoredCurrentUser = (currentUser) => {
    setStorage(CURRENT_USER_KEY, JSON.stringify(currentUser))
    reactiveUser.value = currentUser
}

export const clearStoredCurrentUser = () => {
    delStorage(CURRENT_USER_KEY)
    delStorage('userInfo')
    reactiveUser.value = null
}
