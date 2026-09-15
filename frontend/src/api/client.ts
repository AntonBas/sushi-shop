import axios from "axios";
import { API_BASE_URL } from "../config/env";

declare module "axios" {
    export interface AxiosRequestConfig {
        skipAuthRedirect?: boolean;
    }
}

const api = axios.create({
    baseURL: `${API_BASE_URL}/api`,
    withCredentials: true,
    headers: {
        "X-Requested-With": "XMLHttpRequest",
    },
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401 && !error.config?.skipAuthRedirect) {
            window.location.href = "/login";
        }
        return Promise.reject(error);
    }
);

export default api;
