import { ref, onUnmounted } from 'vue'
import { useLoginUserStore } from '@/stores/loginUserStore'

type SSECallback = (event: { type: string; message: string }) => void

const listeners = new Set<SSECallback>()
let eventSource: EventSource | null = null

export function useSSE() {
  const connected = ref(false)

  const connect = () => {
    const loginUserStore = useLoginUserStore()
    if (!loginUserStore.loginUser?.id) return
    if (eventSource && eventSource.readyState !== EventSource.CLOSED) return

    eventSource = new EventSource('/api/notification/subscribe', { withCredentials: true })

    eventSource.onopen = () => {
      connected.value = true
    }

    eventSource.addEventListener('email_sent', (e) => {
      try {
        const data = JSON.parse(e.data)
        listeners.forEach((fn) => fn(data))
      } catch {
        // ignore parse errors
      }
    })

    eventSource.onerror = () => {
      connected.value = false
      // EventSource 会自动重连，无需手动处理
    }
  }

  const disconnect = () => {
    if (eventSource) {
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
