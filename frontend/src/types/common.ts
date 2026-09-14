export interface Page<T> {
  content: T[]
  page: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}

export interface ApiSubError {
  object: string
  field?: string | null
  rejectedValue?: unknown
  message: string
}

export interface ApiErrorResponse {
  message?: string
  subErrors?: ApiSubError[]
}