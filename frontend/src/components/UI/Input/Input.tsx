import styles from './Input.module.css'
import clsx from 'clsx'

interface InputProps {
  value: string
  onChange: (v: string) => void
  label?: string
  error?: string
  placeholder?: string
  type?: string
}

export default function Input({ value, onChange, label, error, placeholder, type = 'text' }: InputProps) {
  return (
    <div className={styles.container}>
      {label && <label className={styles.label}>{label}</label>}
      <input
        type={type}
        value={value}
        onChange={e => onChange(e.target.value)}
        placeholder={placeholder}
        className={clsx(styles.input, error && styles.error)}
      />
      {error && <span className={styles.errorMessage}>{error}</span>}
    </div>
  )
}