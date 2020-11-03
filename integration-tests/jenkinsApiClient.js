const axios = require('axios');
const { JENKINS_API_URL } = require('./config');

const apiClient = axios.create({
    baseURL: JENKINS_API_URL,
    validateStatus: null,
});

const executeRequest = async (url, method, data) => {
    const headers = {
        'Content-Type': 'application/json',
    };

    const response = await apiClient({
        method,
        url,
        headers,
        data,
    }).catch((err) => {
        if (err.response) {
            console.log(err.response.status, err.response.data);
        } else {
            console.log(err.message);
        }

        throw err;
    });

    return response;
};

const get = (url, data = {}) => executeRequest(url, 'GET', data);

module.exports = {
    get,
};
