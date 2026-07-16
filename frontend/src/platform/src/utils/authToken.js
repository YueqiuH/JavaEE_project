import { delStorage, getStorage, setStorage } from '@/utils/localStorage.js'

const ACCESS_TOKEN_KEY = 'smart-campus.access-token'
const LEGACY_TOKEN_KEY = 'Token'

export const getAccessToken = () => {
    const token = getStorage(ACCESS_TOKEN_KEY) || getStorage(LEGACY_TOKEN_KEY)
    if (token && !getStorage(ACCESS_TOKEN_KEY)) {
        setStorage(ACCESS_TOKEN_KEY, token)
        delStorage(LEGACY_TOKEN_KEY)
    }
    return token
}

export const setAccessToken = (token) => {
    setStorage(ACCESS_TOKEN_KEY, token)
    delStorage(LEGACY_TOKEN_KEY)
}

export const clearAccessToken = () => {
    delStorage(ACCESS_TOKEN_KEY)
    delStorage(LEGACY_TOKEN_KEY)
}
