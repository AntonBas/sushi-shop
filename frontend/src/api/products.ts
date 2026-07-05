import api from './client'
import type { CreateProductRequest, UpdateProductRequest, ProductListResponse, ProductResponse, Category } from '../types'
import type { Page } from '../types/common'

export const getProducts = async (params: {
  page?: number
  size?: number
  search?: string
  category?: Category
  available?: boolean
}): Promise<Page<ProductListResponse>> => {
  const { data } = await api.get('/products', { params })
  return data
}

export const getProduct = async (id: number): Promise<ProductResponse> => {
  const { data } = await api.get(`/products/${id}`)
  return data
}

export const createProduct = async (product: CreateProductRequest, images?: File[]): Promise<ProductResponse> => {
  const formData = new FormData()
  formData.append('product', new Blob([JSON.stringify(product)], { type: 'application/json' }))
  if (images) images.forEach(img => formData.append('images', img))
  const { data } = await api.post('/products', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return data
}

export const updateProduct = async (id: number, data: UpdateProductRequest): Promise<ProductResponse> => {
  const { data: res } = await api.put(`/products/${id}`, data)
  return res
}

export const deleteProduct = async (id: number): Promise<void> => {
  await api.delete(`/products/${id}`)
}

export const toggleProduct = async (id: number): Promise<void> => {
  await api.patch(`/products/${id}/toggle`)
}

export const addProductImage = async (id: number, image: File): Promise<void> => {
  const formData = new FormData()
  formData.append('image', image)
  await api.post(`/products/${id}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const deleteProductImage = async (productId: number, imageId: number): Promise<void> => {
  await api.delete(`/products/${productId}/images/${imageId}`)
}