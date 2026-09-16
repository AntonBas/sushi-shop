import axios, { type AxiosError } from "axios";
import { API_BASE_URL } from "../config/env";

declare module "axios" {
    export interface AxiosRequestConfig {
        skipAuthRedirect?: boolean;
        _sessionRechecked?: boolean;
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

export const handleResponseError = async (error: AxiosError) => {
    const config = error.config;

    if (error.response?.status !== 401 || !config || config.skipAuthRedirect) {
        return Promise.reject(error);
    }

    if (!config._sessionRechecked) {
        config._sessionRechecked = true;
        try {
            await api.get("/users/me", { skipAuthRedirect: true });
            return api.request(config);
        } catch {
            // session really is gone, fall through to logout
        }
    }

    onUnauthorized?.();
    return Promise.reject(error);
};

api.interceptors.response.use((response) => response, handleResponseError);

export default api;
