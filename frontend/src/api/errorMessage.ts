import { AxiosError } from "axios";
import type { ApiErrorResponse } from "../types/common";

export function getErrorMessage(err: unknown, fallback: string): string {
  if (err instanceof AxiosError) {
    const data = err.response?.data as ApiErrorResponse | undefined;
    if (data?.subErrors?.length) {
      return data.subErrors.map((subError) => subError.message).join("; ");
    }
    if (data?.message) return data.message;
  }
  return fallback;
}
