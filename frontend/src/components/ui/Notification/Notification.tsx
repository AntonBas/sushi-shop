import { useEffect } from 'react'
import { createPortal } from 'react-dom'
import { CheckCircle2, XCircle, AlertTriangle, Info } from 'lucide-react'
import type { NotificationType } from '../../../context/NotificationContext'

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

  const colors = {
    success: 'bg-green-900 text-green-200',
    error: 'bg-red-900 text-red-200',
    warning: 'bg-yellow-900 text-yellow-200',
    info: 'bg-blue-900 text-blue-200'
  }

  const content = (
    <div
      className={`fixed top-4 right-4 min-w-[300px] max-w-[400px] rounded-lg p-3 shadow-lg transition-all duration-300 z-[10000]
        ${isVisible ? 'translate-x-0 opacity-100' : 'translate-x-full opacity-0'}
        ${colors[type]}`}
      style={{ top: `${20 + position * 80}px` }}
    >
      <div className="flex items-center gap-3">
        {icons[type]}
        <span className="flex-1 text-sm">{message}</span>
        <button onClick={() => onClose(id)} className="opacity-70 hover:opacity-100">✕</button>
      </div>
      <div className="h-1 bg-white/20 mt-2 rounded">
        <div className="h-full bg-white/40 rounded animate-[shrink_5s_linear]" />
      </div>
    </div>
  )

  return createPortal(content, document.body)
}