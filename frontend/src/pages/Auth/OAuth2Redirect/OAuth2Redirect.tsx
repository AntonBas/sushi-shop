import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../context/AuthContext";
import { setAuthToken } from "../../../api/authToken";
import Loading from "../../../components/UI/Loading/Loading";

export default function OAuth2Redirect() {
  const navigate = useNavigate();
  const { refreshUser } = useAuth();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get("token");

    if (token) {
      setAuthToken(token);
      refreshUser()
        .then(() => {
          window.location.href = "/";
        })
        .catch(() => navigate("/login?error=oauth2_failed", { replace: true }));
    } else {
      navigate("/login?error=oauth2_failed", { replace: true });
    }
  }, [navigate, refreshUser]);

  return <Loading text="Completing login..." />;
}
