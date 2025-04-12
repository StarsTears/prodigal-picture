/**
 * 格式化文件大小
 * @param size
 */

export const formatSize = (size?: number) => {
  if (!size) return '未知'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / (1024 * 1024)).toFixed(2) + ' MB'
}

/**
 * 下载图片
 * @param url 图片下载地址
 * @param fileName 要保存为的文件名
 */
import {saveAs} from "file-saver";
export function downloadImage(url?: string, fileName?: string) {
  if (!url) {
    return
  }
  saveAs(url, fileName)
}

/**
 * 将十六进制 转换为 RGB
 * @param input
 */
export function toHexColor(input) {
  // 去掉 0x 前缀
  const colorValue = input.startsWith('0x') ? input.slice(2) : input

  // 将剩余部分解析为十六进制数，再转成 6 位十六进制字符串
  const hexColor = parseInt(colorValue, 16).toString(16).padStart(6, '0')

  // 返回标准 #RRGGBB 格式
  return `#${hexColor}`
}


// region 拖拽图片
/**
 * 处理拖拽图片
 *
 * 需要用在 <div> 元素上，并设置 draggable="true"</div>
 * @param event
 */
export function handleDragStart(event) {
  event.preventDefault() // 阻止默认拖拽行为
}
// endregion 拖拽图片

// region 格式化数字为 k
// console.log(formatNumber(999));   // "999"
// console.log(formatNumber(1000));  // "1k"
// console.log(formatNumber(1020));  // "1.02k"
// console.log(formatNumber(1120));  // "1.12k"
// console.log(formatNumber(1500));  // "1.5k"
// console.log(formatNumber(1999));  // "2k"（四舍五入后为 2.00，去零后为 2）
export function formatNumber(value: number): string {
  if (value < 1000) {
    return value.toString()
  }

  const kValue = value / 1000
  // 保留两位小数并移除末尾的零
  let formatted = kValue.toFixed(2).replace(/\.?0+$/, '')
  // 处理如 "2." 变成 "2" 的情况
  if (formatted.endsWith('.')) {
    formatted = formatted.slice(0, -1)
  }

  return `${formatted}k`
}

// endregion 格式化数字为 k
