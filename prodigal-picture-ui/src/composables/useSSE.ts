import { ref, onUnmounted } from 'vue'
import { useLoginUserStore } from '@/stores/loginUserStore'
import { SSE_EMAIL_SENT, SSE_EMAIL_SEND_SUCCESS } from '@/constants/sse'

type SSECallback = (event: { type: string; message: string }) => void

const listeners = new Set<SSECallback>()
let eventSource: EventSource | null = null

function handleEvent(e: MessageEvent) {
  try {
    const data = JSON.parse(e.data)
    console.log('[SSE] 收到事件:', e.type, data)
    listeners.forEach((fn) => fn(data))
  } catch {
    console.warn('[SSE] 解析事件数据失败:', e.type, e.data)
  }
}

export function useSSE() {
  const connected = ref(false)

  const connect = () => {
    const loginUserStore = useLoginUserStore()
    if (!loginUserStore.loginUser?.id) {
      console.warn('[SSE] 未登录，跳过连接')
      return
    }
    if (eventSource && eventSource.readyState !== EventSource.CLOSED) {
      console.log('[SSE] 连接已存在，readyState=', eventSource.readyState)
      return
    }

    console.log('[SSE] 开始连接, userId=', loginUserStore.loginUser.id)
    eventSource = new EventSource('/api/notification/subscribe', { withCredentials: true })

    eventSource.onopen = () => {
      connected.value = true
      console.log('[SSE] 连接已建立')
    }

    eventSource.addEventListener(SSE_EMAIL_SENT, handleEvent)
    eventSource.addEventListener(SSE_EMAIL_SEND_SUCCESS, handleEvent)

    eventSource.onerror = () => {
      connected.value = false
      console.warn('[SSE] 连接错误, readyState=', eventSource?.readyState)
    }
  }

  const disconnect = () => {
    if (eventSource) {
      console.log('[SSE] 断开连接')
      eventSource.close()
      eventSource = null
      connected.value = false
    }
  }

  const onEmailSent = (fn: SSECallback) => {
    listeners.add(fn)
    return () => listeners.delete(fn)
  }

  onUnmounted(() => {
    // 组件卸载时不自动断开，SSE 应保持连接
  })

  return { connected, connect, disconnect, onEmailSent }
}
