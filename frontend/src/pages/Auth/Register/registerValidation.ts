export interface RegisterFormValues {
  name: string
  email: string
  phone: string
  password: string
  confirmPassword: string
  city: string
  street: string
  house: string
  apartment: string
}

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const PHONE_PATTERN = /^\+?[0-9]{10,15}$/
const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).+$/

export function validateRegisterForm(values: RegisterFormValues): Record<string, string> {
  const errors: Record<string, string> = {}

  if (!values.name.trim()) errors.name = 'Name is required'
  else if (values.name.length > 50) errors.name = 'Name must be less than 50 characters'

  if (!values.email.trim()) errors.email = 'Email is required'
  else if (!EMAIL_PATTERN.test(values.email.trim())) errors.email = 'Invalid email format'

  if (!values.phone.trim()) errors.phone = 'Phone number is required'
  else if (!PHONE_PATTERN.test(values.phone.trim())) errors.phone = 'Phone must be 10-15 digits'

  if (!values.password) errors.password = 'Password is required'
  else if (values.password.length < 8 || values.password.length > 64) {
    errors.password = 'Password must be between 8 and 64 characters'
  } else if (!PASSWORD_PATTERN.test(values.password)) {
    errors.password = 'Password must contain at least one letter and one number'
  }

  if (!values.confirmPassword) errors.confirmPassword = 'Password confirmation is required'
  else if (values.confirmPassword !== values.password) errors.confirmPassword = "Passwords don't match"

  const hasAddress = [values.city, values.street, values.house, values.apartment].some((part) => part.trim())
  if (hasAddress) {
    if (!values.city.trim()) errors['address.city'] = 'City is required'
    if (!values.street.trim()) errors['address.street'] = 'Street is required'
    if (!values.house.trim()) errors['address.house'] = 'House number is required'
  }

  return errors
}
