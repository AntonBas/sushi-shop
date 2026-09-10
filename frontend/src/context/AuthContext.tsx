import { useEffect, useRef, useState } from "react";
import * as authApi from "../api/auth";
import * as usersApi from "../api/user";
import { clearAuthToken, getAuthToken, setAuthToken } from "../api/authToken";
import type { UserResponse, LoginRequest, RegisterRequest } from "../types";
import { AuthContext } from "./auth-context";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [initialLoading, setInitialLoading] = useState(true);
  const fetchedRef = useRef(false);
  const loading = initialLoading;

  const token = getAuthToken();
  const isAuthenticated = !!user;
  const isAdmin = user?.userRole === "ADMIN";
  const isCourier = user?.userRole === "COURIER";

  useEffect(() => {
    if (token && !fetchedRef.current) {
      fetchedRef.current = true;
      usersApi
        .getMe()
        .then(setUser)
        .catch(() => {
          clearAuthToken();
          setUser(null);
        })
        .finally(() => setInitialLoading(false));
    } else {
      setInitialLoading(false);
    }
  }, []);

  const login = async (credentials: LoginRequest) => {
    const response = await authApi.login(credentials);
    setAuthToken(response.token);
    fetchedRef.current = true;
    setUser(response.user);
  };

  const register = async (userData: RegisterRequest) => {
    const response = await authApi.register(userData);
    setAuthToken(response.token);
    fetchedRef.current = true;
    setUser(response.user);
    return response.user;
  };

  const logout = () => {
    clearAuthToken();
    setUser(null);
    fetchedRef.current = false;
    window.location.href = "/login";
  };

  const refreshUser = async () => {
    try {
      const data = await usersApi.getMe();
      setUser(data);
    } catch {
      logout();
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAuthenticated,
        isAdmin,
        isCourier,
        login,
        register,
        logout,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
