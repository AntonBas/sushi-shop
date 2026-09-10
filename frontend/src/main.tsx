import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { NotificationProvider } from "./context/NotificationContext";
import NotificationContainer from "./components/UI/Notification/NotificationContainer";
import { AuthProvider } from "./context/AuthContext";
import { CartProvider } from "./context/CartContext";
import ErrorBoundary from "./components/ErrorBoundary/ErrorBoundary";
import App from "./App";
import "./styles/variables.css";
import "./index.css";

createRoot(document.getElementById("root")!).render(
  <ErrorBoundary>
    <BrowserRouter>
      <NotificationProvider>
        <AuthProvider>
          <CartProvider>
            <App />
            <NotificationContainer />
          </CartProvider>
        </AuthProvider>
      </NotificationProvider>
    </BrowserRouter>
  </ErrorBoundary>,
);
