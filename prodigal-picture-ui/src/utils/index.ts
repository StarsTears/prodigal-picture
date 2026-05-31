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
export async function downloadImage(url?: string, fileName?: string) {
  if (!url) {
    return
  }
  //图片url 做了代理，且下载的文件地址是预签名
  // 故不使用 url 去 head 请求图片数据再保存
  // saveAs(url, fileName)
  try {
    // 1. 发起 GET 请求获取文件
    const response = await fetch(url);
    const contentType = response.headers.get('content-type');
    // 2. 检查请求是否成功
    if (!response.ok) {
      // 如果响应是 XML，说明是 COS 返回的错误信息
      if (contentType?.includes('xml')) {
        const errorText = await response.text();
        console.error('COS 错误响应:', errorText);
        // 可以解析 XML 获取具体错误码
        throw new Error(`COS 返回错误: ${response.status}`);
      }
      throw new Error(`下载失败: ${response.status}`);
    }

    // 检查是否真的是图片
    if (!contentType?.startsWith('image/')) {
      console.warn('返回的不是图片，Content-Type:', contentType);
      // 可能是 XML 或其他格式
      if (contentType?.includes('xml')) {
        const errorText = await response.text();
        console.error('实际返回内容:', errorText);
        throw new Error('预签名 URL 无效');
      }
    }
    // 3. 获取文件 Blob 数据
    const blob = await response.blob();

    // 4. 使用 saveAs 保存（传入 Blob 而非 URL）
    saveAs(blob, fileName || 'download');
  } catch (error) {
    console.error('下载失败:', error);
  }
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
