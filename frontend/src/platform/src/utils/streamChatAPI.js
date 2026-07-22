import { getAccessToken } from '@/utils/authToken.js'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888'

export const streamChatAPI = {
    async streamChat(url, body, onEvent, onError, onComplete) {
        const controller = new AbortController()
        try {
            const token = getAccessToken()
            const response = await fetch(`${API_BASE_URL}${url}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(token ? { Authorization: `Bearer ${token}` } : {}),
                },
                body: JSON.stringify(body),
                signal: controller.signal
            })
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)
            const reader = response.body.getReader()
            const decoder = new TextDecoder()
            let buffer = ''
            let currentEvent = 'message'
            while (true) {
                const { done, value } = await reader.read()
                if (done) { if (onComplete) onComplete(); break }
                buffer += decoder.decode(value, { stream: true })
                const lines = buffer.split('\n')
                buffer = lines.pop() || ''
                for (const line of lines) {
                    const trimmed = line.trim()
                    if (trimmed.startsWith('event:')) {
                        currentEvent = trimmed.substring(6).trim()
                    } else if (trimmed.startsWith('data:')) {
                        const dataStr = trimmed.substring(5).trim()
                        if (dataStr && dataStr !== '[DONE]') {
                            try {
                                const parsed = JSON.parse(dataStr)
                                onEvent(currentEvent, parsed)
                            } catch { onEvent(currentEvent, dataStr) }
                        }
                    }
                }
            }
        } catch (error) {
            if (error.name === 'AbortError') console.log('SSE aborted')
            else if (onError) onError(error)
        }
        return controller
    }
}
