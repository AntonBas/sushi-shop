import { lazy, Suspense } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./context/useAuth";
import Loading from "./components/UI/Loading/Loading";
import ProtectedRoute from "./components/Auth/ProtectedRoute";
import MainLayout from "./layout/MainLayout/MainLayout";
import ProfileLayout from "./components/Profile/ProfileLayout/ProfileLayout";
import Home from "./pages/Home/Home";
import Login from "./pages/Auth/Login/Login";
import Register from "./pages/Auth/Register/Register";
import EmailVerification from "./pages/Auth/EmailVerification/EmailVerification";
import VerifyEmailChange from "./pages/Auth/VerifyEmailChange/VerifyEmailChange";
import OAuth2Redirect from "./pages/Auth/OAuth2Redirect/OAuth2Redirect";
import ForgotPassword from "./pages/Auth/ForgotPassword/ForgotPassword";
import ResetPassword from "./pages/Auth/ResetPassword/ResetPassword";
import CartPage from "./pages/Cart/CartPage";
import ProfilePage from "./pages/Profile/ProfilePage/ProfilePage";
import MyOrdersPage from "./pages/Profile/MyOrdersPage/MyOrdersPage";
import PromotionPage from "./pages/Promotion/PromotionPage";
import ProductPage from "./pages/Product/ProductPage";
import CheckoutPage from "./pages/Checkout/CheckoutPage";
import MenuPage from "./pages/Menu/MenuPage";
import OrderSuccessPage from "./pages/Order/Success/SuccessPage";
import OrderCancelPage from "./pages/Order/Cancel/CancelPage";
import NotFoundPage from "./pages/NotFound/NotFoundPage";

const AdminLayout = lazy(() => import("./components/Admin/AdminLayout/AdminLayout"));
const AdminProductsPage = lazy(() => import("./pages/Admin/Products/AdminProductsPage/AdminProductsPage"));
const AdminProductForm = lazy(() => import("./pages/Admin/Products/AdminProductForm/AdminProductForm"));
const AdminOrdersPage = lazy(() => import("./pages/Admin/Orders/AdminOrdersPage"));
const AdminPromotionsPage = lazy(() => import("./pages/Admin/Promotions/AdminPromotionsPage/AdminPromotionsPage"));
const AdminPromotionForm = lazy(() => import("./pages/Admin/Promotions/AdminPromotionForm/AdminPromotionForm"));
const AdminAuditPage = lazy(() => import("./pages/Admin/Audit/AdminAuditPage"));

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
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
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
        <Route path="audit" element={<ProtectedRoute adminOnly><AdminAuditPage /></ProtectedRoute>} />
      </Route>
    </Routes>
  );
}

export default App;
