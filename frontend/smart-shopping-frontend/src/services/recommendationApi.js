
import axios from "axios";
import { getToken } from "./authApi";

const recommendationApi = axios.create({
    baseURL: "http://localhost:8085/api/recommendations",
    headers: {
        "Content-Type": "application/json",
    },
});

recommendationApi.interceptors.request.use((config) => {
    const token = getToken();

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

export async function getRecommendations() {
    const response = await recommendationApi.get("");
    return response.data;
}

export async function getPurchaseHistory() {
    const response = await recommendationApi.get("/history");
    return response.data;
}

export async function getPurchaseSummary() {
    const response = await recommendationApi.get("/summary");
    return response.data;
}

export async function recordPurchase(purchaseData) {
    const response = await recommendationApi.post(
        "/purchases",
        purchaseData
    );

    return response.data;
}

export default recommendationApi;
