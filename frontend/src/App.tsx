import { lazy, Suspense } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./context/useAuth";
import Loading from "./components/UI/Loading/Loading";
import ProtectedRoute from "./features/auth/components/Auth/ProtectedRoute";
import GuestRoute from "./features/auth/components/Auth/GuestRoute";
import MainLayout from "./layout/MainLayout/MainLayout";
import ProfileLayout from "./features/user/components/ProfileLayout/ProfileLayout";
import Home from "./pages/Home/Home";
import Login from "./features/auth/pages/Auth/Login/Login";
import Register from "./features/auth/pages/Auth/Register/Register";
import EmailVerification from "./features/auth/pages/Auth/EmailVerification/EmailVerification";
import VerifyEmailChange from "./features/auth/pages/Auth/VerifyEmailChange/VerifyEmailChange";
import OAuth2Redirect from "./features/auth/pages/Auth/OAuth2Redirect/OAuth2Redirect";
import ForgotPassword from "./features/auth/pages/Auth/ForgotPassword/ForgotPassword";
import ResetPassword from "./features/auth/pages/Auth/ResetPassword/ResetPassword";
import CartPage from "./features/order/pages/Cart/CartPage";
import ProfilePage from "./features/user/pages/ProfilePage/ProfilePage";
import MyOrdersPage from "./features/order/pages/MyOrdersPage/MyOrdersPage";
import PromotionPage from "./features/promotion/pages/Promotion/PromotionPage";
import ProductPage from "./features/product/pages/Product/ProductPage";
import CheckoutPage from "./features/order/pages/Checkout/CheckoutPage";
import MenuPage from "./features/product/pages/Menu/MenuPage";
import OrderSuccessPage from "./features/order/pages/Order/Success/SuccessPage";
import OrderCancelPage from "./features/order/pages/Order/Cancel/CancelPage";
import NotFoundPage from "./pages/NotFound/NotFoundPage";
import PrivacyPolicyPage from "./pages/Privacy/PrivacyPolicyPage";

const AdminLayout = lazy(() => import("./components/Admin/AdminLayout/AdminLayout"));
const AdminProductsPage = lazy(() => import("./features/product/admin/Products/AdminProductsPage/AdminProductsPage"));
const AdminProductForm = lazy(() => import("./features/product/admin/Products/AdminProductForm/AdminProductForm"));
const AdminOrdersPage = lazy(() => import("./features/order/admin/Orders/AdminOrdersPage"));
const AdminPromotionsPage = lazy(() => import("./features/promotion/admin/Promotions/AdminPromotionsPage/AdminPromotionsPage"));
const AdminPromotionForm = lazy(() => import("./features/promotion/admin/Promotions/AdminPromotionForm/AdminPromotionForm"));
const AdminAuditPage = lazy(() => import("./features/audit/admin/Audit/AdminAuditPage"));
const AdminUsersPage = lazy(() => import("./features/user/admin/Users/AdminUsersPage"));

function App() {
  const { loading } = useAuth();

  if (loading) return <Loading text="Loading Sushi Shop..." />;

  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/menu" element={<MenuPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/products/:slug" element={<ProductPage />} />
        <Route path="/promotions/:slug" element={<PromotionPage />} />
        <Route path="/privacy" element={<PrivacyPolicyPage />} />
        <Route path="/login" element={<GuestRoute><Login /></GuestRoute>} />
        <Route path="/register" element={<GuestRoute><Register /></GuestRoute>} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route
          path="/checkout"
          element={
            <ProtectedRoute>
              <CheckoutPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/order-success"
          element={
            <ProtectedRoute>
              <OrderSuccessPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/order-cancel"
          element={
            <ProtectedRoute>
              <OrderCancelPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <ProfileLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<ProfilePage />} />
          <Route path="address" element={<ProfilePage />} />
          <Route path="security" element={<ProfilePage />} />
          <Route path="orders" element={<MyOrdersPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
      <Route path="/verify-email" element={<EmailVerification />} />
      <Route path="/verify-email-change" element={<VerifyEmailChange />} />
      <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
      <Route
        path="/admin"
        element={
          <ProtectedRoute staffOnly>
            <Suspense fallback={<Loading text="Loading admin panel..." />}>
              <AdminLayout />
            </Suspense>
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="orders" replace />} />
        <Route path="products" element={<ProtectedRoute adminOnly><AdminProductsPage /></ProtectedRoute>} />
        <Route path="products/new" element={<ProtectedRoute adminOnly><AdminProductForm /></ProtectedRoute>} />
        <Route path="products/:id/edit" element={<ProtectedRoute adminOnly><AdminProductForm /></ProtectedRoute>} />
        <Route path="orders" element={<AdminOrdersPage />} />
        <Route path="promotions" element={<ProtectedRoute adminOnly><AdminPromotionsPage /></ProtectedRoute>} />
        <Route path="promotions/new" element={<ProtectedRoute adminOnly><AdminPromotionForm /></ProtectedRoute>} />
        <Route path="promotions/:id/edit" element={<ProtectedRoute adminOnly><AdminPromotionForm /></ProtectedRoute>} />
        <Route path="users" element={<ProtectedRoute adminOnly><AdminUsersPage /></ProtectedRoute>} />
        <Route path="audit" element={<ProtectedRoute adminOnly><AdminAuditPage /></ProtectedRoute>} />
      </Route>
    </Routes>
  );
}

export default App;
