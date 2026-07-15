import api from './client'

export const createCheckout = async (
  orderId: number,
  amountInCents: number,
  email: string
): Promise<string> => {
  const { data } = await api.post(`/payments/order/${orderId}`, null, {
    params: { amountInCents, email },
  })
  return data.url
}