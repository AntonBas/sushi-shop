import { useCallback, useEffect, useRef, useState } from 'react'
import { useSearchParams } from 'react-router-dom'

const PAGE_PARAM = 'page'
const SEARCH_DEBOUNCE_MS = 300

type ParamValue = string | number | boolean | null | undefined

interface UpdateOptions {
  replace?: boolean
  keepPage?: boolean
}

function parsePage(value: string | null): number {
  const page = Number(value)
  return Number.isInteger(page) && page > 1 ? page - 1 : 0
}

export function useListSearchParams() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = parsePage(searchParams.get(PAGE_PARAM))

  const getParam = useCallback((key: string) => searchParams.get(key) ?? '', [searchParams])

  const updateParams = useCallback((updates: Record<string, ParamValue>, options: UpdateOptions = {}) => {
    setSearchParams((previous) => {
      const next = new URLSearchParams(previous)
      Object.entries(updates).forEach(([key, value]) => {
        if (value === null || value === undefined || value === '') next.delete(key)
        else next.set(key, String(value))
      })
      if (!options.keepPage) next.delete(PAGE_PARAM)
      return next
    }, { replace: options.replace })
  }, [setSearchParams])

  const setPage = useCallback((nextPage: number) => {
    updateParams({ [PAGE_PARAM]: nextPage > 0 ? nextPage + 1 : null }, { keepPage: true })
  }, [updateParams])

  return { page, getParam, updateParams, setPage }
}

export function pickAllowed<T extends string>(value: string, allowed: readonly T[]): T | '' {
  return (allowed as readonly string[]).includes(value) ? value as T : ''
}

export function useDebouncedParamInput(
  urlValue: string,
  commit: (value: string) => void,
): [string, (value: string) => void] {
  const [input, setInput] = useState(urlValue)
  const [syncedUrlValue, setSyncedUrlValue] = useState(urlValue)
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  if (urlValue !== syncedUrlValue) {
    setSyncedUrlValue(urlValue)
    setInput(urlValue)
  }

  useEffect(() => () => {
    if (timerRef.current) clearTimeout(timerRef.current)
  }, [])

  const onChange = (value: string) => {
    setInput(value)
    if (timerRef.current) clearTimeout(timerRef.current)
    timerRef.current = setTimeout(() => commit(value), SEARCH_DEBOUNCE_MS)
  }

  return [input, onChange]
}
