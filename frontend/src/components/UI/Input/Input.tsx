import { useId } from 'react'
import styles from './Input.module.css'
import clsx from 'clsx'

interface InputProps {
  value: string
  onChange: (v: string) => void
  label?: string
  error?: string
  placeholder?: string
  type?: string
  name?: string
  autoComplete?: string
  required?: boolean
}

export default function Input({ value, onChange, label, error, placeholder, type = 'text', name, autoComplete, required }: InputProps) {
  const id = useId()
  const errorId = `${id}-error`

  return (
    <div className={styles.container}>
      {label && <label htmlFor={id} className={styles.label}>{label}</label>}
      <input
        id={id}
        type={type}
        name={name}
        value={value}
        onChange={e => onChange(e.target.value)}
        placeholder={placeholder}
        autoComplete={autoComplete}
        required={required}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? errorId : undefined}
        className={clsx(styles.input, error && styles.error)}
      />
      {error && <span id={errorId} className={styles.errorMessage}>{error}</span>}
    </div>
  )
}
