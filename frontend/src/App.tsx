import { Routes, Route } from 'react-router-dom'
import MainLayout from './layout/MainLayout/MainLayout'
import Home from './pages/Home/Home'
import Login from './pages/auth/Login/Login'
import Register from './pages/auth/Register/Register'
import EmailVerification from './pages/auth/EmailVerification/EmailVerification'
import OAuth2Redirect from './pages/auth/OAuth2Redirect/OAuth2Redirect'
import ForgotPassword from './pages/auth/ForgotPassword/ForgotPassword'
import ResetPassword from './pages/auth/ResetPassword/ResetPassword'

function App() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
      </Route>
      <Route path="/verify-email" element={<EmailVerification />} />
      <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />

    </Routes>
  )
}

export default App