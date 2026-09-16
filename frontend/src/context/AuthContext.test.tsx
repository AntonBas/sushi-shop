import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { AxiosError, AxiosHeaders } from "axios";
import type { ReactNode } from "react";
import { AuthProvider } from "./AuthContext";
import { useAuth } from "./useAuth";
import * as usersApi from "../api/user";
import * as authApi from "../api/auth";
import { handleResponseError } from "../api/client";
import type { UserResponse } from "../types";

vi.mock("../api/user");
vi.mock("../api/auth");

const wrapper = ({ children }: { children: ReactNode }) => (
  <AuthProvider>{children}</AuthProvider>
);

const user: UserResponse = {
  id: 1,
  email: "a@example.com",
  name: "A",
  phone: "",
  userRole: "CUSTOMER",
  googleSignInEnabled: true,
};

function axiosErrorWithStatus(status: number) {
  return new AxiosError(
    "Request failed",
    String(status),
    { headers: new AxiosHeaders() },
    {},
    {
      status,
      statusText: "",
      headers: {},
      config: { headers: new AxiosHeaders() },
      data: undefined,
    },
  );
}

describe("AuthContext refreshUser", () => {
  beforeEach(() => {
    vi.mocked(usersApi.getMe).mockReset();
    vi.mocked(authApi.logout).mockReset();
  });

  it("clears the user and rejects on a 401", async () => {
    vi.mocked(usersApi.getMe)
      .mockResolvedValueOnce(user)
      .mockRejectedValueOnce(axiosErrorWithStatus(401));

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.user).toEqual(user));

    await expect(result.current.refreshUser()).rejects.toBeInstanceOf(
      AxiosError,
    );
    await waitFor(() => expect(result.current.user).toBeNull());
  });

  it("keeps the current user and rejects on a transient network/server error", async () => {
    vi.mocked(usersApi.getMe)
      .mockResolvedValueOnce(user)
      .mockRejectedValueOnce(axiosErrorWithStatus(500));

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.user).toEqual(user));

    await expect(result.current.refreshUser()).rejects.toBeInstanceOf(
      AxiosError,
    );
    expect(result.current.user).toEqual(user);
  });
});

describe("AuthContext unauthorized handler registration", () => {
  beforeEach(() => {
    vi.mocked(usersApi.getMe).mockReset();
  });

  it("clears the user when any other request gets a 401", async () => {
    vi.mocked(usersApi.getMe).mockResolvedValueOnce(user);

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.user).toEqual(user));

    handleResponseError(axiosErrorWithStatus(401)).catch(() => {});

    await waitFor(() => expect(result.current.user).toBeNull());
  });
});
