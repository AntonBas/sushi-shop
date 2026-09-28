import axios, { type AxiosError } from "axios";

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
    baseURL: "/api",
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
        const sessionAlive = await api
            .get("/users/me", { skipAuthRedirect: true })
            .then(() => true, () => false);
        if (sessionAlive) {
            return api.request(config);
        }
    }

    onUnauthorized?.();
    return Promise.reject(error);
};

api.interceptors.response.use((response) => response, handleResponseError);

export default api;
