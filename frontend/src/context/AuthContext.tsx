import { useCallback, useEffect, useState } from "react";
import { isAxiosError } from "axios";
import * as authApi from "../api/auth";
import * as usersApi from "../api/user";
import { setUnauthorizedHandler } from "../api/client";
import type { UserResponse, LoginRequest, RegisterRequest } from "../types";
import { AuthContext } from "./auth-context";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const isAuthenticated = !!user;
  const isAdmin = user?.userRole === "ADMIN";
  const isCourier = user?.userRole === "COURIER";

  useEffect(() => {
    usersApi
      .getMe()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null));
    return () => setUnauthorizedHandler(null);
  }, []);

  const login = useCallback(async (credentials: LoginRequest) => {
    const response = await authApi.login(credentials);
    setUser(response.user);
  }, []);

  const register = useCallback(async (userData: RegisterRequest) => {
    await authApi.register(userData);
  }, []);

  const logout = useCallback(() => {
    authApi.logout().finally(() => {
      setUser(null);
      window.location.href = "/login";
    });
  }, []);

  const refreshUser = useCallback(async () => {
    try {
      const data = await usersApi.getMe();
      setUser(data);
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 401) {
        setUser(null);
      }
      throw error;
    }
  }, []);

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
