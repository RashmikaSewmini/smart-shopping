import axios from 'axios';

const productApi = axios.create({
    baseURL: '/api/products',
});

export const getProducts = async () => {
    const response = await productApi.get('');
    return response.data;
};

export const getProductByBarcode = async (barcode) => {
    const response = await productApi.get(
        `/barcode/${encodeURIComponent(barcode)}`
    );
    return response.data;
};

export const lookupPublicBarcode = async (barcode) => {
    const response = await axios.get(
        `/api/products/lookup/barcode/${encodeURIComponent(barcode)}`
    );

    return response.data;
};

