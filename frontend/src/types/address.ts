export interface AddressRequest {
  city: string
  street: string
  house: string
  apartment?: string
  comment?: string
}

export interface AddressResponse {
  city: string
  street: string
  house: string
  apartment?: string
  comment?: string
}