import { useState, useEffect } from 'react'

type Theme = 'light' | 'dark'

export function getInitialTheme(): Theme {
  const saved = localStorage.getItem('theme')
  if (saved === 'light' || saved === 'dark') return saved
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function applyTheme(theme: Theme) {
  document.documentElement.classList.toggle('dark', theme === 'dark')
}

export function useTheme() {
  const [theme, setTheme] = useState<Theme>(getInitialTheme)

  useEffect(() => {
    applyTheme(theme)
  }, [theme])

  const toggleTheme = () => {
    const next: Theme = theme === 'light' ? 'dark' : 'light'
    localStorage.setItem('theme', next)
    setTheme(next)
  }

  return { theme, toggleTheme }
}
