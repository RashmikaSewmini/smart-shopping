
import axios from "axios";

const authApi = axios.create({
    baseURL: "http://localhost:8084/api/auth",
    headers: {
        "Content-Type": "application/json",
    },
});

export async function registerCustomer(customerData) {
    const response = await authApi.post("/register", customerData);
    return response.data;
}

export async function loginCustomer(credentials) {
    const response = await authApi.post("/login", credentials);

    // Support common JWT response field names.
    const token =
        response.data.token ??
        response.data.accessToken ??
        response.data.jwt;

    if (typeof token !== "string" || !token.trim()) {
        throw new Error(
            "Login succeeded but no JWT token was found in the response."
        );
    }

    localStorage.setItem("smartShoppingToken", token);
    return response.data;
}

export function getToken() {
    return localStorage.getItem("smartShoppingToken");
}

export function logoutCustomer() {
    localStorage.removeItem("smartShoppingToken");
}

export async function getCurrentCustomer() {
    const token = getToken();

    const response = await authApi.get("/me", {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });

    return response.data;
}

export default authApi;
