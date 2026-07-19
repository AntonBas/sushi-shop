import { useEffect, useState } from 'react'
import { createPortal } from 'react-dom'
import { CheckCircle2, XCircle, AlertTriangle, Info } from 'lucide-react'
import clsx from 'clsx'
import type { NotificationType } from '../../../context/NotificationContext'
import styles from './Notification.module.css'

interface Props {
  id: string
  message: string
  type: NotificationType
  isVisible: boolean
  onClose: (id: string) => void
  duration?: number
  position?: number
}

export default function Notification({ id, message, type, isVisible, onClose, duration = 5000, position = 0 }: Props) {
  const [isHiding, setIsHiding] = useState(false)

  useEffect(() => {
    if (!isVisible && !isHiding) {
      setIsHiding(true)
    }
  }, [isVisible])

  useEffect(() => {
    if (isVisible && duration > 0) {
      const timer = setTimeout(() => onClose(id), duration)
      return () => clearTimeout(timer)
    }
  }, [isVisible, duration, onClose, id])

  const icons = {
    success: <CheckCircle2 size={20} />,
    error: <XCircle size={20} />,
    warning: <AlertTriangle size={20} />,
    info: <Info size={20} />
  }

  const content = (
    <div
      className={clsx(
        styles.notification,
        styles[type],
        (isVisible || isHiding) && styles.show,
        isHiding && styles.hide
      )}
      style={{ '--offset': `${position * 100}%` } as React.CSSProperties}
      onAnimationEnd={() => {
        if (isHiding) onClose(id)
      }}
    >
      <div className={styles.content}>
        <span className={styles.icon}>{icons[type]}</span>
        <span className={styles.message}>{message}</span>
        <button className={styles.close} onClick={() => onClose(id)}>×</button>
      </div>
      <div className={styles.progress}>
        <div className={styles.progressBar} style={{ animationDuration: `${duration}ms` }} />
      </div>
    </div>
  )

  return createPortal(content, document.body)
}