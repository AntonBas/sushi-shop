import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./context/AuthContext";
import Loading from "./components/UI/Loading/Loading";
import MainLayout from "./layout/MainLayout/MainLayout";
import AdminLayout from "./components/Admin/AdminLayout/AdminLayout";
import ProfileLayout from "./components/Profile/ProfileLayout/ProfileLayout";
import Home from "./pages/Home/Home";
import Login from "./pages/Auth/Login/Login";
import Register from "./pages/Auth/Register/Register";
import EmailVerification from "./pages/Auth/EmailVerification/EmailVerification";
import OAuth2Redirect from "./pages/Auth/OAuth2Redirect/OAuth2Redirect";
import ForgotPassword from "./pages/Auth/ForgotPassword/ForgotPassword";
import ResetPassword from "./pages/Auth/ResetPassword/ResetPassword";
import CartPage from "./pages/Cart/CartPage";
import ProfilePage from "./pages/Profile/ProfilePage/ProfilePage";
import MyOrdersPage from "./pages/Profile/MyOrdersPage/MyOrdersPage";
import PromotionPage from "./pages/Promotion/PromotionPage";
import ProductPage from "./pages/Product/ProductPage";
import AdminProductsPage from "./pages/Admin/Products/AdminProductsPage/AdminProductsPage";
import AdminProductForm from "./pages/Admin/Products/AdminProductForm/AdminProductForm";
import AdminOrdersPage from "./pages/Admin/Orders/AdminOrdersPage";
import AdminPromotionsPage from "./pages/Admin/Promotions/AdminPromotionsPage/AdminPromotionsPage";
import AdminPromotionForm from "./pages/Admin/Promotions/AdminPromotionForm/AdminPromotionForm";
import AdminAuditPage from "./pages/Admin/Audit/AdminAuditPage";
import CheckoutPage from "./pages/Checkout/CheckoutPage";
import MenuPage from "./pages/Menu/MenuPage";
import OrderSuccessPage from "./pages/OrderSuccess/OrderSuccessPage";
import OrderCancelPage from './pages/OrderCancel/OrderCancelPage'

function App() {
  const { loading } = useAuth();

  if (loading) return <Loading text="Loading Sushi Shop..." />;

  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/menu" element={<MenuPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/products/:id" element={<ProductPage />} />
        <Route path="/promotions/:id" element={<PromotionPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/order-success" element={<OrderSuccessPage />} />
        <Route path="/order-cancel" element={<OrderCancelPage />} />
        <Route path="/profile" element={<ProfileLayout />}>
          <Route index element={<ProfilePage />} />
          <Route path="orders" element={<MyOrdersPage />} />
        </Route>
      </Route>
      <Route path="/verify-email" element={<EmailVerification />} />
      <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<Navigate to="products" replace />} />
        <Route path="products" element={<AdminProductsPage />} />
        <Route path="products/new" element={<AdminProductForm />} />
        <Route path="products/:id/edit" element={<AdminProductForm />} />
        <Route path="orders" element={<AdminOrdersPage />} />
        <Route path="promotions" element={<AdminPromotionsPage />} />
        <Route path="promotions/new" element={<AdminPromotionForm />} />
        <Route path="audit" element={<AdminAuditPage />} />
      </Route>
    </Routes>
  );
}

export default App;
