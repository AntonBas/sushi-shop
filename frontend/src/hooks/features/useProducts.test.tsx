import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor, act } from "@testing-library/react";
import type { ReactNode } from "react";
import { useProducts } from "./useProducts";
import { NotificationProvider } from "../../context/NotificationContext";
import * as productsApi from "../../api/products";
import type { ProductListResponse } from "../../types";
import type { Page } from "../../types/common";

vi.mock("../../api/products");

const wrapper = ({ children }: { children: ReactNode }) => (
  <NotificationProvider>{children}</NotificationProvider>
);

function pageOf(names: string[]): Page<ProductListResponse> {
  return {
    content: names.map(
      (name, i) => ({ id: i, name }) as ProductListResponse,
    ),
    page: { totalPages: 1, totalElements: names.length, size: 12, number: 0 },
  } as Page<ProductListResponse>;
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((r) => (resolve = r));
  return { promise, resolve };
}

describe("useProducts stale-response guarding", () => {
  beforeEach(() => {
    vi.mocked(productsApi.getProducts).mockReset();
  });

  it("keeps the result of the latest request even if an earlier one resolves later", async () => {
    const sushiResult = deferred<Page<ProductListResponse>>();
    const rollsResult = deferred<Page<ProductListResponse>>();
    vi.mocked(productsApi.getProducts)
      .mockReturnValueOnce(sushiResult.promise)
      .mockReturnValueOnce(rollsResult.promise);

    const { result } = renderHook(() => useProducts(), { wrapper });

    let sushiCall!: Promise<void>;
    let rollsCall!: Promise<void>;
    act(() => {
      sushiCall = result.current.loadMoreProducts(0, { search: "sushi" });
    });
    act(() => {
      rollsCall = result.current.loadMoreProducts(0, { search: "rolls" });
    });

    rollsResult.resolve(pageOf(["Rolls product"]));
    await act(() => rollsCall);
    sushiResult.resolve(pageOf(["Sushi product"]));
    await act(() => sushiCall);

    await waitFor(() =>
      expect(result.current.products.map((p) => p.name)).toEqual([
        "Rolls product",
      ]),
    );
  });
});
