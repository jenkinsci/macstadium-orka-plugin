/* eslint-disable jest/no-standalone-expect */

const handleResponse = (status, data, message) => {
    if (status < 200 || status >= 300) {
        console.log(`Status Code: ${status}, ${message}`);
    }

    if (data && data.errors && data.errors.length > 0) {
        data.errors.map((e) => console.log(e.message));
    }
};

const callApi = async (
    requestData,
    apiCall,
    { token, licenseKey, shouldCheckErrors = true, errorMessage = '' } = {},
) => {
    const { status, data } = await apiCall(...requestData, {
        token,
        licenseKey,
    });

    if (shouldCheckErrors) {
        expect(data.errors).toEqual([]);
    } else {
        handleResponse(status, data, errorMessage);
    }

    return data;
};

module.exports = {
    callApi,
};
