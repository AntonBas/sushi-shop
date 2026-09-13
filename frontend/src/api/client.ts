import axios from "axios";
import { API_BASE_URL } from "../config/env";
import { clearAuthToken, getAuthToken } from "./authToken";

const api = axios.create({
    baseURL: `${API_BASE_URL}/api`,
});

api.interceptors.request.use((config) => {
    const token = getAuthToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            clearAuthToken();
            window.location.href = "/login";
        }
        return Promise.reject(error);
    }
);

export default api;