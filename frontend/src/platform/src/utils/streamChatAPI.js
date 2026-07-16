/**
 * 使用Fetch API进行流式请求
 */
const URL = "http://localhost:8888";
export const streamChatAPI = {
    /**
     * 流式聊天
     * @param {string} url - API地址
     * @param {object} body - 请求体
     * @param {function} onMessage - 接收消息的回调
     * @param {function} onError - 错误回调
     * @param {function} onComplete - 完成回调
     * @returns {AbortController} - 用于取消请求的控制器
     */
    async streamChat(url, body, onMessage, onError, onComplete) {
        const controller = new AbortController()

        try {
            const response = await fetch(`${URL}${url}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(body),
                signal: controller.signal
            })

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`)
            }

            const reader = response.body.getReader()
            const decoder = new TextDecoder()
            let buffer = ''

            while (true) {
                const {
                    done,
                    value
                } = await reader.read()

                if (done) {
                    if (onComplete) onComplete()
                    break
                }

                // 解码数据
                buffer += decoder.decode(value, {
                    stream: true
                })

                // 处理SSE格式的数据
                const lines = buffer.split('\n')
                buffer = lines.pop() || ''

                for (const line of lines) {
                    if (line.trim() && line.startsWith('data:')) {
                        const data = line.substring(5).trim()
                        if (data && data !== '[DONE]') {
                            try {
                                const parsed = JSON.parse(data)
                                onMessage(typeof parsed === 'string' ? parsed : JSON.stringify(parsed))
                            } catch {
                                onMessage(data)
                            }
                        }
                    }
                }
            }
        } catch (error) {
            if (error.name === 'AbortError') {
                console.log('请求被取消')
            } else {
                console.error('流式请求错误:', error)
                if (onError) onError(error)
            }
        }

        return controller
    }
}
