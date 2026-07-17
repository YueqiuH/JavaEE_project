import { computed, ref } from 'vue'

const FAVORITES_KEY = 'smart-campus.favorite-services'
const RECENTS_KEY = 'smart-campus.recent-services'

const parseStoredArray = (key) => {
  try {
    const value = JSON.parse(localStorage.getItem(key) || '[]')
    return Array.isArray(value) ? value : []
  } catch {
    return []
  }
}

const favoriteKeys = ref(parseStoredArray(FAVORITES_KEY))
const recentKeys = ref(parseStoredArray(RECENTS_KEY))

const persist = (key, value) => localStorage.setItem(key, JSON.stringify(value))

export const useServicePreferences = () => {
  const toggleFavorite = (serviceKey) => {
    favoriteKeys.value = favoriteKeys.value.includes(serviceKey)
      ? favoriteKeys.value.filter((key) => key !== serviceKey)
      : [...favoriteKeys.value, serviceKey]
    persist(FAVORITES_KEY, favoriteKeys.value)
  }

  const recordRecent = (serviceKey) => {
    recentKeys.value = [serviceKey, ...recentKeys.value.filter((key) => key !== serviceKey)].slice(0, 8)
    persist(RECENTS_KEY, recentKeys.value)
  }

  return {
    favoriteKeys: computed(() => favoriteKeys.value),
    recentKeys: computed(() => recentKeys.value),
    isFavorite: (serviceKey) => favoriteKeys.value.includes(serviceKey),
    toggleFavorite,
    recordRecent,
  }
}
