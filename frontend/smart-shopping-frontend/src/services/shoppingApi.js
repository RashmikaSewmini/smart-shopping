import axios from 'axios';

const shoppingApi = axios.create({
    baseURL: '/api/shopping-sessions',
});

export const createShoppingSession = async () => {
    const response = await shoppingApi.post('');
    return response.data;
};

export const getShoppingSession = async (sessionId) => {
    const response = await shoppingApi.get(`/${sessionId}`);
    return response.data;
};

export const addProductByBarcode = async (sessionId, barcode) => {
    const response = await shoppingApi.post(
        `/${sessionId}/items/barcode/${encodeURIComponent(barcode)}`
    );
    return response.data;
};


export const addManualItem = async (sessionId, item) => {
    const response = await shoppingApi.post(
        `/${sessionId}/items/manual`,
        {
            productName: item.productName,
            unitPrice: String(item.unitPrice),
            quantity: Number(item.quantity),
        }
    );

    return response.data;
};


export const updateItemQuantity = async (
    sessionId,
    itemId,
    quantity
) => {
    const response = await shoppingApi.patch(
        `/${sessionId}/items/${itemId}/quantity`,
        { quantity: Number(quantity) }
    );

    return response.data;
};

export const removeShoppingItem = async (sessionId, itemId) => {
    const response = await shoppingApi.delete(
        `/${sessionId}/items/${itemId}`
    );
    return response.data;
};