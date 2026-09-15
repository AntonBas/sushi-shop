import axios, { type AxiosError } from "axios";
import { API_BASE_URL } from "../config/env";

declare module "axios" {
    export interface AxiosRequestConfig {
        skipAuthRedirect?: boolean;
    }
}

let onUnauthorized: (() => void) | null = null;

export const setUnauthorizedHandler = (handler: (() => void) | null) => {
    onUnauthorized = handler;
};

const api = axios.create({
    baseURL: `${API_BASE_URL}/api`,
    withCredentials: true,
    headers: {
        "X-Requested-With": "XMLHttpRequest",
    },
});

export const handleResponseError = (error: AxiosError) => {
    if (error.response?.status === 401 && !error.config?.skipAuthRedirect) {
        onUnauthorized?.();
    }
    return Promise.reject(error);
};

api.interceptors.response.use((response) => response, handleResponseError);

export default api;
