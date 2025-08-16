// 环境配置文件
export const getApiBaseUrl = () => {
  // 检查是否在Vite环境中
  if (typeof import.meta !== 'undefined' && import.meta.env) {
    // 优先使用Vite环境变量
    if (import.meta.env.VITE_API_BASE_URL) {
      return import.meta.env.VITE_API_BASE_URL
    }
    
    // 根据环境变量设置 API 基础 URL
    return import.meta.env.PROD 
      ? '/api' // 生产环境使用相对路径，nginx会代理到后端服务
      : 'http://localhost:8123/api' // 开发环境指向本地后端服务
  }
  
  // Node.js环境下的默认配置image.png
  const nodeEnv = process.env.NODE_ENV || 'development'
  return nodeEnv === 'production' 
    ? '/api' 
    : 'http://localhost:8123/api'
}

export const API_BASE_URL = getApiBaseUrl()

// 导出环境信息，方便调试
export const ENV_INFO = {
  isProduction: typeof import.meta !== 'undefined' && import.meta.env ? import.meta.env.PROD : (process.env.NODE_ENV === 'production'),
  isDevelopment: typeof import.meta !== 'undefined' && import.meta.env ? import.meta.env.DEV : (process.env.NODE_ENV === 'development'),
  apiBaseUrl: API_BASE_URL,
  mode: typeof import.meta !== 'undefined' && import.meta.env ? import.meta.env.MODE : (process.env.NODE_ENV || 'development')
}
