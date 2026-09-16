import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import type { AxiosError } from "axios";
import client, { handleResponseError, setUnauthorizedHandler } from "./client";

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

  afterEach(() => {
    vi.restoreAllMocks();
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

  it("retries the original request after a successful silent session recheck", async () => {
    const onUnauthorized = vi.fn();
    setUnauthorizedHandler(onUnauthorized);

    const getSpy = vi
      .spyOn(client, "get")
      .mockResolvedValueOnce({ data: {} } as never);
    const requestSpy = vi
      .spyOn(client, "request")
      .mockResolvedValueOnce({ data: "ok" } as never);

    const config = { url: "/cart" } as AxiosError["config"];
    const error = {
      isAxiosError: true,
      response: { status: 401 } as AxiosError["response"],
      config,
    } as AxiosError;

    await expect(handleResponseError(error)).resolves.toEqual({
      data: "ok",
    });

    expect(getSpy).toHaveBeenCalledWith("/users/me", {
      skipAuthRedirect: true,
    });
    expect(requestSpy).toHaveBeenCalledWith(config);
    expect(onUnauthorized).not.toHaveBeenCalled();
    expect(config?.skipAuthRedirect).toBeUndefined();
  });

  it("logs out when the silent session recheck also fails", async () => {
    const onUnauthorized = vi.fn();
    setUnauthorizedHandler(onUnauthorized);

    vi.spyOn(client, "get").mockRejectedValueOnce(
      new Error("still unauthorized"),
    );

    const config = { url: "/cart" } as AxiosError["config"];
    const error = {
      isAxiosError: true,
      response: { status: 401 } as AxiosError["response"],
      config,
    } as AxiosError;

    await expect(handleResponseError(error)).rejects.toBe(error);
    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });

  it("does not recheck again if the retried request itself gets 401", async () => {
    const onUnauthorized = vi.fn();
    setUnauthorizedHandler(onUnauthorized);

    const getSpy = vi.spyOn(client, "get");

    const config = {
      url: "/cart",
      _sessionRechecked: true,
    } as AxiosError["config"];
    const error = {
      isAxiosError: true,
      response: { status: 401 } as AxiosError["response"],
      config,
    } as AxiosError;

    await expect(handleResponseError(error)).rejects.toBe(error);
    expect(getSpy).not.toHaveBeenCalled();
    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });
});
