import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { NotificationProvider } from './context/NotificationContext'
import NotificationContainer from './components/ui/Notification/NotificationContainer'
import { AuthProvider } from './context/AuthContext'
import App from './App'
import './index.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <NotificationProvider>
        <AuthProvider>
          <App />
          <NotificationContainer />
        </AuthProvider>
      </NotificationProvider>
    </BrowserRouter>
  </StrictMode>,
)