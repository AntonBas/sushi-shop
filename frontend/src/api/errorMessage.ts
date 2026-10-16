import { AxiosError } from "axios";
import type { ApiErrorResponse } from "@/types/common";

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

export function getFieldErrors(err: unknown): Record<string, string> {
  if (!(err instanceof AxiosError)) return {};
  const data = err.response?.data as ApiErrorResponse | undefined;
  const fieldErrors: Record<string, string> = {};
  for (const subError of data?.subErrors ?? []) {
    if (subError.field && !(subError.field in fieldErrors)) {
      fieldErrors[subError.field] = subError.message;
    }
  }
  return fieldErrors;
}

export function getGlobalErrorMessage(err: unknown): string {
  if (!(err instanceof AxiosError)) return "";
  const data = err.response?.data as ApiErrorResponse | undefined;
  return (data?.subErrors ?? [])
    .filter((subError) => !subError.field)
    .map((subError) => subError.message)
    .join("; ");
}

export function getErrorCode(err: unknown): string | undefined {
  if (err instanceof AxiosError) {
    return (err.response?.data as ApiErrorResponse | undefined)?.code;
  }
  return undefined;
}
