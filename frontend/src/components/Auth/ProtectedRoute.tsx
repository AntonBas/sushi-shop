import { Navigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import Loading from "../UI/Loading/Loading";

interface Props {
  children: React.ReactNode;
  adminOnly?: boolean;
  staffOnly?: boolean;
}

export default function ProtectedRoute({
  children,
  adminOnly = false,
  staffOnly = false,
}: Props) {
  const { isAuthenticated, isAdmin, isCourier, loading } = useAuth();

  if (loading) return <Loading text="Loading..." />;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (adminOnly && !isAdmin) return <Navigate to="/" replace />;
  if (staffOnly && !isAdmin && !isCourier) return <Navigate to="/" replace />;

  return <>{children}</>;
}
