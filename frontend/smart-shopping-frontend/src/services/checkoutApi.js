
import axios from "axios";
import { getToken } from "./authApi";

const checkoutApi = axios.create({
    baseURL: "/api/checkouts",
    headers: {
        "Content-Type": "application/json",
    },
});

checkoutApi.interceptors.request.use((config) => {
    const token = getToken();

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

export async function createCheckout(checkoutData) {
    const response = await checkoutApi.post("", checkoutData);
    return response.data;
}

export async function getCheckout(checkoutId) {
    const response = await checkoutApi.get(`/${checkoutId}`);
    return response.data;
}

export async function getCheckoutBySessionId(sessionId) {
    const response = await checkoutApi.get(`/session/${sessionId}`);
    return response.data;
}

export default checkoutApi;