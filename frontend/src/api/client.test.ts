import { describe, it, expect, vi, beforeEach } from "vitest";
import type { AxiosError } from "axios";
import { handleResponseError, setUnauthorizedHandler } from "./client";

function axiosErrorWithStatus(
  status: number,
  skipAuthRedirect?: boolean,
): AxiosError {
  return {
    isAxiosError: true,
    response: { status } as AxiosError["response"],
    config: { skipAuthRedirect } as AxiosError["config"],
  } as AxiosError;
}

describe("api/client handleResponseError", () => {
  beforeEach(() => {
    setUnauthorizedHandler(null);
  });

  it("calls the registered handler on a 401 without skipAuthRedirect", async () => {
    const onUnauthorized = vi.fn();
    setUnauthorizedHandler(onUnauthorized);

    await expect(
      handleResponseError(axiosErrorWithStatus(401)),
    ).rejects.toBeDefined();

    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });

  it("does not call the handler when skipAuthRedirect is set", async () => {
    const onUnauthorized = vi.fn();
    setUnauthorizedHandler(onUnauthorized);

    await expect(
      handleResponseError(axiosErrorWithStatus(401, true)),
    ).rejects.toBeDefined();

    expect(onUnauthorized).not.toHaveBeenCalled();
  });

  it("does not call the handler for non-401 errors", async () => {
    const onUnauthorized = vi.fn();
    setUnauthorizedHandler(onUnauthorized);

    await expect(
      handleResponseError(axiosErrorWithStatus(500)),
    ).rejects.toBeDefined();

    expect(onUnauthorized).not.toHaveBeenCalled();
  });

  it("does not throw when no handler is registered", async () => {
    await expect(
      handleResponseError(axiosErrorWithStatus(401)),
    ).rejects.toBeDefined();
  });
});
