const TOKEN_KEY = 'token'

export const getAuthToken = (): string | null => localStorage.getItem(TOKEN_KEY)

export const setAuthToken = (token: string): void => localStorage.setItem(TOKEN_KEY, token)

export const clearAuthToken = (): void => localStorage.removeItem(TOKEN_KEY)
