export interface Page<T> {
  content: T[]
  page: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}

interface ApiSubError {
  object: string
  field?: string | null
  rejectedValue?: unknown
  message: string
}

export interface ApiErrorResponse {
  code?: string
  message?: string
  subErrors?: ApiSubError[]
}