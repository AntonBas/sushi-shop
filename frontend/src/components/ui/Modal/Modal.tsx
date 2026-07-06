import { useEffect } from 'react'
import { createPortal } from 'react-dom'
import styles from './Modal.module.css'

interface Props {
  isOpen: boolean
  onClose: () => void
  title?: string
  children: React.ReactNode
}

export default function Modal({ isOpen, onClose, title, children }: Props) {
  useEffect(() => {
    document.body.style.overflow = isOpen ? 'hidden' : ''
    return () => { document.body.style.overflow = '' }
  }, [isOpen])

  if (!isOpen) return null

  return createPortal(
    <div className={styles.overlay} onClick={e => e.target === e.currentTarget && onClose()}>
      <div className={styles.modal}>
        {title && (
          <div className={styles.header}>
            <h2>{title}</h2>
            <button onClick={onClose}>✕</button>
          </div>
        )}
        <div>{children}</div>
      </div>
    </div>,
    document.body
  )
}