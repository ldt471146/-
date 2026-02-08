// token 本地管理
const TOKEN_KEY = 'token'
const REMEMBER_KEY = 'remember'

export const getToken = () => localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY)

export const setToken = (token, remember) => {
  if (remember) {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(REMEMBER_KEY, '1')
    sessionStorage.removeItem(TOKEN_KEY)
  } else {
    sessionStorage.setItem(TOKEN_KEY, token)
    localStorage.removeItem(TOKEN_KEY)
    localStorage.setItem(REMEMBER_KEY, '0')
  }
}

export const clearToken = () => {
  localStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REMEMBER_KEY)
}

export const getRemember = () => localStorage.getItem(REMEMBER_KEY) === '1'
