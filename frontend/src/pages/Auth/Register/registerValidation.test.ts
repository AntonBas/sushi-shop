import { describe, it, expect } from 'vitest'
import { validateRegisterForm, type RegisterFormValues } from './registerValidation'

const valid: RegisterFormValues = {
  name: 'Anton',
  email: 'anton@example.com',
  phone: '+380991234567',
  password: 'secret123',
  confirmPassword: 'secret123',
  city: '',
  street: '',
  house: '',
  apartment: '',
}

describe('validateRegisterForm', () => {
  it('accepts a valid form without address', () => {
    expect(validateRegisterForm(valid)).toEqual({})
  })

  it('reports every empty required field', () => {
    const errors = validateRegisterForm({ ...valid, name: ' ', email: '', phone: '', password: '', confirmPassword: '' })
    expect(Object.keys(errors).sort()).toEqual(['confirmPassword', 'email', 'name', 'password', 'phone'])
  })

  it('mirrors backend password rules', () => {
    expect(validateRegisterForm({ ...valid, password: 'abc123', confirmPassword: 'abc123' }).password)
      .toBe('Password must be between 8 and 64 characters')
    expect(validateRegisterForm({ ...valid, password: 'abcdefgh', confirmPassword: 'abcdefgh' }).password)
      .toBe('Password must contain at least one letter and one number')
    expect(validateRegisterForm({ ...valid, confirmPassword: 'secret124' }).confirmPassword)
      .toBe("Passwords don't match")
  })

  it('rejects invalid email and phone formats', () => {
    const errors = validateRegisterForm({ ...valid, email: 'not-an-email', phone: '+38099abc4567' })
    expect(errors.email).toBe('Invalid email format')
    expect(errors.phone).toBe('Phone must be 10-15 digits')
  })

  it('requires city, street and house once any address field is filled', () => {
    const errors = validateRegisterForm({ ...valid, apartment: '12' })
    expect(Object.keys(errors).sort()).toEqual(['address.city', 'address.house', 'address.street'])
  })
})
