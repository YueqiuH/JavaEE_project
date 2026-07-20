/**
 * JWT 客户端解码工具（仅解码 payload，不验证签名——签名由服务端校验）。
 * 用于前端路由守卫提前检查 token 是否过期，避免发出注定 401 的请求。
 */

/**
 * Base64url 解码为 UTF-8 字符串。
 * 兼容浏览器环境，处理 Unicode 字符。
 */
function base64UrlDecode(str) {
  // 补齐被省略的 padding
  let base64 = str.replace(/-/g, '+').replace(/_/g, '/')
  while (base64.length % 4 !== 0) {
    base64 += '='
  }
  try {
    const raw = atob(base64)
    // 处理含多字节 UTF-8 字符的 payload
    return decodeURIComponent(
      raw
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
  } catch {
    return null
  }
}

/**
 * 解码 JWT payload，返回解析后的对象；失败返回 null。
 * @param {string} token
 * @returns {object|null}
 */
export function decodeJwtPayload(token) {
  if (!token || typeof token !== 'string') return null
  const parts = token.split('.')
  if (parts.length !== 3) return null
  try {
    const decoded = base64UrlDecode(parts[1])
    if (!decoded) return null
    return JSON.parse(decoded)
  } catch {
    return null
  }
}

/**
 * 判断 token 是否已过期（根据 payload.exp，单位秒）。
 * 为容忍客户端与服务端时钟偏差，提前 30 秒视为过期。
 * 无 exp 字段时视为永不过期（非 JWT 或非标准 token，由服务端判断）。
 *
 * @param {string} token
 * @returns {boolean} true 表示已过期
 */
export function isTokenExpired(token) {
  const payload = decodeJwtPayload(token)
  if (!payload || !payload.exp) return false
  const nowSeconds = Math.floor(Date.now() / 1000)
  return payload.exp <= nowSeconds + 30
}
