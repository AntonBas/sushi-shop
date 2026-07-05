import api from './client'

export const createCheckout = async (orderId: number): Promise<string> => {
  const { data } = await api.post(`/payments/order/${orderId}`)
  return data.url
}