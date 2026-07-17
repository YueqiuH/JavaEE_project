import { delStorage, getStorage, setStorage } from '@/utils/localStorage.js'

const CURRENT_USER_KEY = 'smart-campus.current-user'

export const getStoredCurrentUser = () => {
    const serializedUser = getStorage(CURRENT_USER_KEY)
    if (!serializedUser) {
        return null
    }
    try {
        return JSON.parse(serializedUser)
    } catch {
        delStorage(CURRENT_USER_KEY)
        return null
    }
}

export const setStoredCurrentUser = (currentUser) => {
    setStorage(CURRENT_USER_KEY, JSON.stringify(currentUser))
}

export const clearStoredCurrentUser = () => {
    delStorage(CURRENT_USER_KEY)
    delStorage('userInfo')
}
