import axios from "axios";
import { clearAuthToken, getAuthToken } from "./authToken";

const api = axios.create({
    baseURL: "/api",
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