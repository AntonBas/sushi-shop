import { useNotification } from '../../../context/NotificationContext'
import Notification from './Notification'

export default function NotificationContainer() {
  const { notifications, hideNotification } = useNotification()

  return (
    <>
      {notifications.map((n, i) => (
        <Notification key={n.id} {...n} position={i} onClose={hideNotification} />
      ))}
    </>
  )
}