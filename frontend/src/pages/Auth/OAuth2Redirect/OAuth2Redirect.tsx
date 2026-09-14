import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../context/useAuth";
import { setAuthToken } from "../../../api/authToken";
import { exchangeOAuth2Code } from "../../../api/auth";
import Loading from "../../../components/UI/Loading/Loading";

export default function OAuth2Redirect() {
  const navigate = useNavigate();
  const { refreshUser } = useAuth();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const code = params.get("code");
    window.history.replaceState(null, "", window.location.pathname);

    if (code) {
      exchangeOAuth2Code(code)
        .then(({ token }) => {
          setAuthToken(token);
          return refreshUser();
        })
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
