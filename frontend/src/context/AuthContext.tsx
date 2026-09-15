import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import * as authApi from "../api/auth";
import * as usersApi from "../api/user";
import type { UserResponse, LoginRequest, RegisterRequest } from "../types";
import { AuthContext } from "./auth-context";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [initialLoading, setInitialLoading] = useState(true);
  const loading = initialLoading;

  const isAuthenticated = !!user;
  const isAdmin = user?.userRole === "ADMIN";
  const isCourier = user?.userRole === "COURIER";

  useEffect(() => {
    usersApi
      .getMe()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setInitialLoading(false));
  }, []);

  const login = async (credentials: LoginRequest) => {
    const response = await authApi.login(credentials);
    setUser(response.user);
  };

  const register = async (userData: RegisterRequest) => {
    const response = await authApi.register(userData);
    setUser(response.user);
    return response.user;
  };

  const logout = () => {
    authApi.logout().finally(() => {
      setUser(null);
      window.location.href = "/login";
    });
  };

  const refreshUser = async () => {
    try {
      const data = await usersApi.getMe();
      setUser(data);
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 401) {
        setUser(null);
      }
      throw error;
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
