export default class PictureEditWebsocket {
  private pictureId: string
  private socket: WebSocket | null
  private eventHandlers: any
  private heartbeatTimer: number | null = null
  private heartbeatTimeoutTimer: number | null = null
  private lastPongTime: number = 0
  private manualClose: boolean = false
  private reconnectAttempts: number = 0

  private static readonly HEARTBEAT_INTERVAL = 15_000   // 心跳间隔 15s
  private static readonly HEARTBEAT_TIMEOUT = 30_000     // 超时 30s 判定断连
  private static readonly MAX_RECONNECT_ATTEMPTS = 3

  constructor(pictureId: string) {
    this.pictureId = pictureId // 当前编辑的图片 ID
    this.socket = null // WebSocket 实例
    this.eventHandlers = {} // 自定义事件处理器
  }

  /**
   * 初始化 WebSocket 连接
   */
  connect() {
    this.manualClose = false
    this.reconnectAttempts = 0
    const url = `ws://localhost:9999/api/ws/picture/edit?pictureId=${this.pictureId}`
    this.socket = new WebSocket(url)

    // 设置携带 cookie
    this.socket.binaryType = 'blob'

    // 监听连接成功事件
    this.socket.onopen = () => {
      console.log('WebSocket 连接已建立')
      this.startHeartbeat()
      this.triggerEvent('open')
    }

    // 监听消息事件
    this.socket.onmessage = (event) => {
      const message = JSON.parse(event.data)
      // 心跳消息不打印日志，减少控制台输出
      if (message.type !== 'HEARTBEAT') {
        console.log('收到消息:', message)
      }
      // 任何服务端消息都视为心跳回应，重置超时计时
      this.lastPongTime = Date.now()

      // 根据消息类型触发对应事件
      const type = message.type
      this.triggerEvent(type, message)
    }

    // 监听连接关闭事件
    this.socket.onclose = (event) => {
      console.log('WebSocket 连接已关闭:', event)
      this.stopHeartbeat()
      this.triggerEvent('close', event)

      // 非手动关闭时尝试自动重连
      if (!this.manualClose && this.reconnectAttempts < PictureEditWebsocket.MAX_RECONNECT_ATTEMPTS) {
        this.reconnectAttempts++
        const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 10_000)
        console.log(`WebSocket 将在 ${delay}ms 后重连 (第 ${this.reconnectAttempts} 次)`)
        setTimeout(() => this.connect(), delay)
      }
    }

    // 监听错误事件
    this.socket.onerror = (error) => {
      console.error('WebSocket 发生错误:', error)
      this.triggerEvent('error', error)
    }
  }

  /**
   * 启动心跳定时器
   */
  private startHeartbeat() {
    this.lastPongTime = Date.now()
    this.heartbeatTimer = window.setInterval(() => {
      this.sendMessage({ type: 'HEARTBEAT' })
    }, PictureEditWebsocket.HEARTBEAT_INTERVAL)

    // 超时检测：每 5s 检查一次是否断连
    this.heartbeatTimeoutTimer = window.setInterval(() => {
      if (Date.now() - this.lastPongTime > PictureEditWebsocket.HEARTBEAT_TIMEOUT) {
        console.warn('WebSocket 心跳超时，主动关闭连接')
        this.stopHeartbeat()
        if (this.socket) {
          this.socket.close()
        }
      }
    }, 5000)
  }

  /**
   * 停止心跳定时器
   */
  private stopHeartbeat() {
    if (this.heartbeatTimer !== null) {
      window.clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
    if (this.heartbeatTimeoutTimer !== null) {
      window.clearInterval(this.heartbeatTimeoutTimer)
      this.heartbeatTimeoutTimer = null
    }
  }

  /**
   * 关闭 WebSocket 连接
   */
  disconnect() {
    this.manualClose = true
    this.stopHeartbeat()
    if (this.socket) {
      this.socket.close()
      console.log('WebSocket 连接已手动关闭')
    }
  }

  /**
   * 发送消息到后端
   * @param {Object} message 消息对象
   */
  sendMessage(message: object) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(message))
    } else {
      console.error('WebSocket 未连接，无法发送消息:', message)
    }
  }

  /**
   * 添加自定义事件监听
   * @param {string} type 消息类型
   * @param {Function} handler 消息处理函数
   */
  on(type: string, handler: (data?: any) => void) {
    if (!this.eventHandlers[type]) {
      this.eventHandlers[type] = []
    }
    this.eventHandlers[type].push(handler)
  }

  /**
   * 触发事件
   * @param {string} type 消息类型
   * @param {Object} data 消息数据
   */
  triggerEvent(type: string, data?: any) {
    const handlers = this.eventHandlers[type]
    if (handlers) {
      handlers.forEach((handler: any) => handler(data))
    }
  }
}
