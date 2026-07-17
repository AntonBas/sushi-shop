import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import Loading from './components/ui/Loading/Loading'
import MainLayout from './layout/MainLayout/MainLayout'
import AdminLayout from './components/admin/AdminLayout/AdminLayout'
import Home from './pages/Home/Home'
import Login from './pages/auth/Login/Login'
import Register from './pages/auth/Register/Register'
import EmailVerification from './pages/auth/EmailVerification/EmailVerification'
import OAuth2Redirect from './pages/auth/OAuth2Redirect/OAuth2Redirect'
import ForgotPassword from './pages/auth/ForgotPassword/ForgotPassword'
import ResetPassword from './pages/auth/ResetPassword/ResetPassword'
import AdminProductsPage from './pages/admin/products/AdminProductsPage'
import AdminProductForm from './pages/admin/products/AdminProductForm'

function App() {
  const { loading } = useAuth()

  if (loading) return <Loading text="Loading Sushi Shop..." />

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
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<Navigate to="products" replace />} />
        <Route path="products" element={<AdminProductsPage />} />
        <Route path="products/new" element={<AdminProductForm />} />
        <Route path="products/:id/edit" element={<AdminProductForm />} />
      </Route>
    </Routes>
  )
}

export default App