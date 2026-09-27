import { Navigate } from "react-router-dom";
import { useAuth } from "../../context/useAuth";

interface Props {
  children: React.ReactNode;
}

export default function GuestRoute({ children }: Props) {
  const { isAuthenticated } = useAuth();

  if (isAuthenticated) return <Navigate to="/" replace />;

  return <>{children}</>;
}
