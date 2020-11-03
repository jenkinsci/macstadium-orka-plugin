const axios = require('axios');
const { API_URL } = require('./config');

const apiClient = axios.create({
    baseURL: API_URL,
    validateStatus: null,
});

const executeRequest = async (url, method, data, token, licenseKey) => {
    let headers = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
    };

    if (licenseKey) {
        headers = {
            ...headers,
            'orka-licensekey': licenseKey,
        };
    }

    const response = await apiClient({
        method,
        url,
        headers,
        data,
    }).catch((err) => {
        if (err.response) {
            console.log(err.response.status, err.response.data);
        } else {
            console.log(err);
        }
        throw err;
    });

    return response;
};

const get = (
    url,
    data = {},
    { token: currentToken, licenseKey: currentKey } = {},
) => executeRequest(url, 'GET', data, currentToken, currentKey);

const post = (
    url,
    data,
    { token: currentToken, licenseKey: currentKey } = {},
) => executeRequest(url, 'POST', data, currentToken, currentKey);

const deleteAction = (
    url,
    data,
    { token: currentToken, licenseKey: currentKey } = {},
) => executeRequest(url, 'DELETE', data, currentToken, currentKey);

const patch = (
    url,
    data,
    { token: currentToken, licenseKey: currentKey } = {},
) => executeRequest(url, 'PATCH', data, currentToken, currentKey);

const put = (url, data, { token: currentToken, licenseKey: currentKey } = {}) =>
    executeRequest(url, 'PUT', data, currentToken, currentKey);

module.exports = {
    apiClient,
    executeRequest,
    get,
    post,
    deleteAction,
    patch,
    put,
};
